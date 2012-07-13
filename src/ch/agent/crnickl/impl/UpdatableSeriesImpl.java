/*
 *   Copyright 2012 Hauser Olsson GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Package: ch.agent.crnickl.impl
 * Type: UpdatableSeriesImpl
 * Version: 1.0.0
 */
package ch.agent.crnickl.impl;

import java.util.HashSet;
import java.util.Set;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.T2DBMsg;
import ch.agent.crnickl.T2DBMsg.D;
import ch.agent.crnickl.api.Chronicle;
import ch.agent.crnickl.api.Surrogate;
import ch.agent.crnickl.api.UpdatableSeries;
import ch.agent.crnickl.api.ValueType;
import ch.agent.t2.T2Exception;
import ch.agent.t2.time.Range;
import ch.agent.t2.time.TimeIndex;
import ch.agent.t2.timeseries.Observation;
import ch.agent.t2.timeseries.TimeAddressable;
import ch.agent.t2.timeseries.TimeSeriesFactory;

/**
 * Default implementation of {@link UpdatableSeries}.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 * @param <T> the class of the elements of the underlying time series
 */
public class UpdatableSeriesImpl<T> extends SeriesImpl<T> implements UpdatableSeries<T> {

	private boolean delete;
	private Range range;
	private TimeAddressable<T> updates;
	private Set<TimeIndex> deletes;

	/**
	 * Construct an {@link UpdatableSeries}.
	 * 
	 * @param chronicle a chronicle
	 * @param name a string
	 * @param number a positive number
	 * @param surrogate a surrogate
	 */
	public UpdatableSeriesImpl(Chronicle chronicle, String name, int number, Surrogate surrogate) {
		super(chronicle, name, number, surrogate);
	}
	
	@Override
	public UpdatableSeries<T> edit() {
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <S> UpdatableSeries<S> typeCheck(Class<S> type) throws T2DBException {
		try {
			if (type.isAssignableFrom(getValueType().getType()))
				return (UpdatableSeries<S>) this;
		} catch (Exception e) {
		}
		throw T2DBMsg.exception(D.D50101, getName(true), type.getName(), getValueType().getType().getName());
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * An updatable series always uses a sparse time series to ensure that arbitrary large
	 * gaps can be accommodated.   
	 */
	@Override
	public TimeAddressable<T> getValues(Range reqRange) throws T2Exception,	T2DBException {
		if (reqRange != null)
			reqRange.getTimeDomain().requireEquality(getTimeDomain());
		/* Use a sparse array because it works even when there are large gaps: */
		TimeAddressable<T> result = TimeSeriesFactory.make(getTimeDomain(),	getValueType().getType(), true);
		if (!this.getSurrogate().inConstruction())
			getDatabase().getValues(this, reqRange, result);
		if (range != null)
			result.setRange(range);
		else {
			if (updates != null) {
				for (Observation<T> obs : updates) {
					long t = obs.getIndex();
					if (isInRange(t, reqRange))
						result.put(t, obs.getValue());
				}
			}
			if (deletes != null) {
				for (TimeIndex t : deletes) {
					if (isInRange(t.asLong(), reqRange))
						result.remove(t);
				}
			}
		}
		return result;
	}

	/**
	 * Return true if r is null or if t is in range r.
	 * 
	 * @param t
	 * @param r
	 * @return
	 */
	private boolean isInRange(long t, Range r) {
		return r == null || r.isInRange(t);
	}
	
	@Override
	public Range getRange() throws T2Exception, T2DBException {
		Range result = super.getRange();
		if (range != null)
			result = result.intersection(range);
		else {
			if (updates != null) {
				result = result.union(updates.getRange());
			}
			if (deletes != null) {
				boolean tryHarder = false;
				for (TimeIndex t : deletes) {
					if (t.asLong() == result.getFirstIndex() || t.asLong() == result.getLastIndex()) {
						tryHarder = true;
						break;
					}
				}
				if (tryHarder) {
					/* modifying range boundary can expose missing values so ... */
					/* ... just get all values (which is an expensive operation) */
					result = getValues(null).getRange();
				}
			}
		}
		return result;
	}

	@Override
	public Observation<T> getLastObservation(TimeIndex time) throws T2Exception, T2DBException {
		Observation<T> result = super.getLastObservation(time);
		if (range != null) {
			if (result != null && range.getLastIndex() < result.getIndex())
				result = super.getLastObservation(range.getLast());
		} else {
			if (updates != null) {
				Observation<T> obs = updates.getLast(time);
				if (result == null || obs.getIndex() > result.getIndex())
					result = obs;
			}
			if (result != null && deletes != null) {
				boolean tryHarder = false;
				for (TimeIndex t : deletes) {
					if (t.asLong() == result.getIndex()) {
						tryHarder = true;
						break;
					}
				}
				if (tryHarder)
					result = getValues(null).getLast(time); // see comment in getRange
			}
		}
		return result;
	}

	@Override
	public Observation<T> getFirstObservation(TimeIndex time) throws T2Exception, T2DBException {
		Observation<T> result = super.getFirstObservation(time);
		if (range != null) {
			if (result != null && range.getFirstIndex() > result.getIndex())
				result = super.getFirstObservation(range.getFirst());
		} else {
			if (updates != null) {
				Observation<T> obs = updates.getFirst(time);
				if (result == null || obs.getIndex() < result.getIndex())
					result = obs;
			}
			if (result != null && deletes != null) {
				boolean tryHarder = false;
				for (TimeIndex t : deletes) {
					if (t.asLong() == result.getIndex()) {
						tryHarder = true;
						break;
					}
				}
				if (tryHarder)
					result = getValues(null).getFirst(time); // see comment in getRange
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void scanValue(TimeIndex t, Object value) throws T2Exception, T2DBException {
		ValueType<T> vt = getValueType();
		if (value == null || vt.isCompatible(value))
			setValue(t, (T) value);
		else {
			if (value instanceof String)
				setValue(t, vt.scan((String) value));
			else
				throw T2DBMsg.exception(D.D10114, value, vt.getType().getName());
		}
	}

	@Override
	public void setValue(TimeIndex t, T value) throws T2Exception, T2DBException {
		if (delete || range != null)
			throw T2DBMsg.exception(D.D50109, getName(true));
		if (value == null) {
			t.getTimeDomain().requireEquality(getTimeDomain());
			if (deletes == null)
				deletes = new HashSet<TimeIndex>();
			deletes.add(t);
			if (updates != null)
				updates.remove(t);
		} else {
			if (updates == null) {
				updates = TimeSeriesFactory.make(getTimeDomain(), (Class<T>) getValueType().getType(), true);
				updates.put(t, value);
			} else
				updates.put(t, value);
			if (deletes != null)
				deletes.remove(t);
		}
	}

	@Override
	public void setValues(TimeAddressable<T> values) throws T2Exception, T2DBException {
		if (delete || range != null)
			throw T2DBMsg.exception(D.D50109, getName(true));
		/* Note to maintainer: don't optimize (beware of gaps, beware of "missing values") */
		/* if (updates == null)
			updates = values.copy();
		else
			updates.put(values, null); */
		for (Observation<T> obs : values) {
			T v = obs.getValue();
			if (values.isMissing(v))
				setValue(obs.getTime(), null);
			else
				setValue(obs.getTime(), v);
		}
	}
	
	@Override
	public boolean setRange(Range range) throws T2Exception, T2DBException {
		if (getSurrogate().inConstruction())
			throw T2DBMsg.exception(D.D50111, getName(true));
		if (delete || updates != null || deletes != null)
			throw T2DBMsg.exception(D.D50110, getName(true));
		if (range == null)
			range = new Range(getTimeDomain()); // (= set empty range)
		boolean inRange = range.isEmpty() || getRange().isInRange(range);
		if (inRange)
			this.range = range;
		return inRange;
	}
	
	@Override
	public void delete() throws T2DBException {
		if (getSurrogate().inConstruction())
			throw T2DBMsg.exception(D.D50111, getName(true));
		if (updates != null || deletes != null || range != null)
			throw T2DBMsg.exception(D.D50107, getName(true));
		delete = true;
	}
	
	@Override
	public void applyUpdates() throws T2DBException {
		if (delete) {
			getDatabase().delete(this);
			delete = false;
		} else {
			if (getSurrogate().inConstruction())
				getDatabase().create(this);
			if (updates != null)
				getDatabase().update(this, updates);
			if (deletes != null) {
				for (TimeIndex t : deletes) {
					getDatabase().delete(this, t);
				}
			}
			if (range != null)
				getDatabase().update(this, range);
			update();
		}
	}

	@Override
	protected void update() {
		super.update();
		updates = null;
		deletes = null;
		range = null;
	}

}
