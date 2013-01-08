/*
 *   Copyright 2012-2013 Hauser Olsson GmbH
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
 */
package ch.agent.crnickl.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.T2DBMsg;
import ch.agent.crnickl.T2DBMsg.D;
import ch.agent.crnickl.api.Attribute;
import ch.agent.crnickl.api.AttributeDefinition;
import ch.agent.crnickl.api.Chronicle;
import ch.agent.crnickl.api.Series;
import ch.agent.crnickl.api.SeriesDefinition;
import ch.agent.crnickl.api.Surrogate;
import ch.agent.crnickl.api.UpdatableSeries;
import ch.agent.crnickl.api.ValueType;
import ch.agent.t2.T2Exception;
import ch.agent.t2.time.Range;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.time.TimeIndex;
import ch.agent.t2.timeseries.Observation;
import ch.agent.t2.timeseries.TimeAddressable;
import ch.agent.t2.timeseries.TimeSeriesFactory;

/**
 * Default implementation of {@link Series}.
 * 
 * @author Jean-Paul Vetterli
 * @param <T> the class of the elements of the underlying time series
 */
public class SeriesImpl<T> extends DBObjectImpl implements Series<T> {

	private Chronicle chronicle;
	private String name; // lazy
	private int number;
	private SeriesDefinition schema; // lazy
	private TimeDomain timeDomain; // lazy
	private ValueType<T> type; // lazy
	
	/**
	 * Construct a {@link Series}.
	 * 
	 * @param chronicle a chronicle
	 * @param name a string or null
	 * @param number a positive number 
	 * @param surrogate a surrogate
	 */
	public SeriesImpl(Chronicle chronicle, String name, int number, Surrogate surrogate) {
		super(surrogate);
		this.chronicle = chronicle;
		this.name = name;
		this.number = number;
	}
	
	/**
	 * Refresh state. More accurately, pretend to refresh, because there is no
	 * state to refresh in this implementation.
	 */
	protected void update() {
		// nothing which can be updated is kept in the object
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The new and old objects share internal data.
	 */
	@Override
	public UpdatableSeries<T> edit() {
		return new UpdatableSeriesImpl<T>(chronicle, name, number, getSurrogate());
	}

	@Override
	public Chronicle getChronicle() {
		return chronicle;
	}
	
	@Override
	public SeriesDefinition getDefinition() throws T2DBException {
		if (schema == null)
			schema = getChronicle().getSchema(true).getSeriesDefinition(number, true);
		return schema;
	}

	@Override
	public String getName(boolean full) throws T2DBException {
		if (full)
			return getDatabase().getNamingPolicy().fullName(getNames());
		else {
			if (name == null)
				name = getDefinition().getName();
			return name;
		}
	}

	@Override
	public int getNumber() {
		return number;
	}

	@Override
	public List<String> getNames() throws T2DBException {
		List<String> names = getChronicle().getNames();
		names.add(getName(false));
		return names;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ValueType<T> getValueType() throws T2DBException {
		if (type == null)
			type = (ValueType<T>) getDefinition().getValueType();
		return type;
	}

	@Override
	public TimeDomain getTimeDomain() throws T2DBException {
		if (timeDomain == null)
			timeDomain = getDefinition().getTimeDomain();
		return timeDomain;
	}

	@Override
	public boolean isSparse() throws T2DBException {
		return getDefinition().isSparse();
	}

	@Override
	public String getDescription(boolean full) throws T2DBException {
		if (full)
			return getDatabase().getNamingPolicy().fullDescription(getDescriptions());
		else
			return getDefinition().getDescription();
	}

	@Override
	public List<String> getDescriptions() throws T2DBException {
		List<String> descriptions = getChronicle().getDescriptions();
		descriptions.add(getDescription(false));
		return descriptions;
	}

	@Override
	public Attribute<?> getAttribute(String name, boolean mustExist) throws T2DBException {
		AttributeDefinition<?> def = getDefinition().getAttributeDefinition(name, false);
		if (def != null)
			return def.getAttribute();
		else
			return getChronicle().getAttribute(name, mustExist);
	}
	
	@Override
	public Collection<Attribute<?>> getAttributes() throws T2DBException {
		Collection<Attribute<?>> result = new ArrayList<Attribute<?>>();
		SeriesDefinition schema = getDefinition();
		if (schema != null) {
			Collection<AttributeDefinition<?>> defs = schema.getAttributeDefinitions();
			for (AttributeDefinition<?> def : defs) {
				if (!def.isComplete())
					throw T2DBMsg.exception(D.D50115, getName(true), def.getNumber());
				result.add(getAttribute(def.getName(), true));
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The usage of a sparse time series for the result depends on the series
	 * schema ({@link SeriesDefinition#isSparse()} or on heuristics. 
	 */
	@Override
	public TimeAddressable<T> getValues(Range range) throws T2Exception, T2DBException {
		return getValues(range, false);
	}
	
	@Override
	public TimeAddressable<T> getValues(Range range, boolean forceSparse) throws T2Exception, T2DBException {
		if (range != null)
			range.getTimeDomain().requireEquality(getTimeDomain());
		if (forceSparse == false)
			forceSparse = isSparse();
		TimeAddressable<T> ts = TimeSeriesFactory.make(getTimeDomain(), getValueType().getType(), forceSparse);
		if (!this.getSurrogate().inConstruction())
			getDatabase().getValues(this, range, ts);
		return ts;
	}

	private void checkTime(TimeIndex time) throws T2Exception, T2DBException {
		if (time == null)
			throw new IllegalArgumentException("time null");
		time.getTimeDomain().requireEquality(getTimeDomain());
	}
	
	@Override
	public Observation<T> getLastObservation(TimeIndex time) throws T2Exception, T2DBException {
		if (time != null)
			checkTime(time);
		if (this.getSurrogate().inConstruction())
			return null;
		else
			return getDatabase().getLastObservation(this, time);
	}

	@Override
	public Observation<T> getFirstObservation(TimeIndex time) throws T2Exception, T2DBException {
		if (time != null)
			checkTime(time);
		if (this.getSurrogate().inConstruction())
			return null;
		else
			return getDatabase().getFirstObservation(this, time);
	}

	@Override
	public T getValue(TimeIndex time) throws T2Exception, T2DBException {
		checkTime(time);
		return getValues(new Range(time, time)).get(time);
	}

	@Override
	public Range getRange() throws T2Exception, T2DBException {
		return getDatabase().getRange(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <S> Series<S> typeCheck(Class<S> type) throws T2DBException {
		try {
			if (type.isAssignableFrom(getValueType().getType()))
				return (Series<S>) this;
		} catch (Exception e) {
		}
		throw T2DBMsg.exception(D.D50101, getName(true), type.getName(), getValueType().getType().getName());
	}

	@Override
	public String toString() {
		try {
			return getName(true);
		} catch (T2DBException e) {
//			throw new RuntimeException("problem", e);
			return getSurrogate().toString();
		}
	}
	
}
