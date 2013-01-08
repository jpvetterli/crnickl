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
package ch.agent.crnickl.api;

import java.util.Collection;
import java.util.List;

import ch.agent.crnickl.T2DBException;
import ch.agent.t2.T2Exception;
import ch.agent.t2.time.Range;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.time.TimeIndex;
import ch.agent.t2.timeseries.Observation;
import ch.agent.t2.timeseries.TimeAddressable;

/**
 * A Series provides access to a time series of values of some type. A series belongs to a
 * {@link Chronicle} and has three required {@link Attribute}s: a name, a value
 * type, and a time domain. These attributes are specified in the {@link Schema}
 * defining a collection of chronicles. There can be additional attributes.
 * <p>
 * The type of a series is not the same as its {@link ValueType} but corresponds
 * to the value of the type parameter of its value type.
 * <p>
 * @see TimeAddressable
 * @author Jean-Paul Vetterli
 * @param <T> the class of the elements of the underlying time series
 */
public interface Series<T> extends DBObject {
	
	/**
	 * Return an {@link UpdatableSeries} corresponding to this series. 
	 * Successfully getting an {@link Updatable} object does not imply
	 * that updates can be successfully applied.
	 * 
	 * @return an updatable series corresponding to this series
	 */
	UpdatableSeries<T> edit();
	
	/**
	 * Return the chronicle to which this series belongs.
	 * 
	 * @return the chronicle
	 */
	Chronicle getChronicle();
	
	/**
	 * Return the series number within the chronicle.
	 * 
	 * @return the series number
	 */
	int getNumber();
	
	/**
	 * Return the series' definition.
	 * 
	 * @return the definition of the series
	 * @throws T2DBException
	 */
	SeriesDefinition getDefinition() throws T2DBException;
	
	/**
	 * Return the simple name or the full name of the series.
	 * The simple name is specified in the schema, so it is 
	 * the same across a collection of chronicles with the same schema.
	 * The full name of a series is the full name of its chronicle plus
	 * its simple name.
	 * 
	 * @param full if true return the full name else the simple name
	 * @return the full name or the simple name
	 * @throws T2DBException
	 */
	String getName(boolean full) throws T2DBException;
	
	/**
	 * Return the list of all names along the chronicle chain and the series.
	 * The last element is the simple name of the series. The previous element
	 * is the name of the chronicle. The element before that is the name 
	 * of the chronicle's collection, and so on until the top chronicle.
	 * 
	 * @return the list of names along the chronicle chain and the series
	 * 
	 * @throws T2DBException
	 */
	List<String> getNames() throws T2DBException;

	/**
	 * Return the description of the series.
	 * The full description includes the description of the chronicle.
	 * Like the simple name, the simple description is specified in the schema.
	 * 
	 * @param full
	 *            if true return the full description, else the simple
	 *            description
	 * @return the full or the simple description of the chronicle
	 * @throws T2DBException
	 */
	String getDescription(boolean full) throws T2DBException;
	
	/**
	 * Return the list of all descriptions along the chronicle chain and the
	 * series. The last element is this series description. The previous element
	 * is the description of the chronicle. The element before that is the
	 * description of the chronicle collection, and so on until the top
	 * chronicle.
	 * 
	 * @return the list of descriptions along the chronicle chain and the series
	 * 
	 * @throws T2DBException
	 */
	List<String> getDescriptions() throws T2DBException;
	
	/**
	 * Return the value type of the series. For a given series name and schema,
	 * the type is always the same.
	 * 
	 * @return the value type of the series
	 * @throws T2DBException
	 */
	ValueType<T> getValueType() throws T2DBException;

	/**
	 * Return the time domain of the series. For a given series name and schema,
	 * the time domain is always the same.
	 * 
	 * @return the time domain of the series
	 * @throws T2DBException
	 */
	TimeDomain getTimeDomain() throws T2DBException;
	
	/**
	 * Return the attribute with the given name. When the parameter mustExist is
	 * set, the result is never null. The value of the attribute can always be
	 * null. When no attribute with the given name is defined, an exception is
	 * thrown.
	 * 
	 * @param name
	 *            the name of the attribute
	 * @return an attribute
	 * @throws T2DBException
	 */
	Attribute<?> getAttribute(String name, boolean mustExist) throws T2DBException;
	
	/**
	 * Return all the series' attributes.
	 * 
	 * @return all the attributes of the series
	 * @throws T2DBException
	 */
	Collection<Attribute<?>> getAttributes() throws T2DBException;
	
	/**
	 * Return the time series of values in the range specified. Any pending update
	 * is merged into the result. When there is a backing store for the series,
	 * values are always fetched from the backing store (they are not cached).
	 * The result is never null. When there are no values, the result is a time series
	 * with an empty range.
	 * 
	 * @param range
	 *            the range of values wanted or null for the full range
	 * @return a time series
	 * @throws T2DBException
	 */
	TimeAddressable<T> getValues(Range range) throws T2Exception, T2DBException;

	/**
	 * Return the time series of values in the range specified. Any pending
	 * update is merged into the result. When there is a backing store for the
	 * series, values are always fetched from the backing store (they are not
	 * cached). The result is never null. When there are no values, the result
	 * is a time series with an empty range. If the <code>forceSparse</code>
	 * parameter is true, a sparse time series will be used. If the code is
	 * false but the <em>sparsity</em> setting of the schema for the series
	 * requires a sparse time series, then one will be used. In all other cases
	 * the software will decide whether to use a standard or a sparse time
	 * series using heuristics.
	 * 
	 * @param range
	 *            the range of values wanted or null to get the full range
	 * @param forceSparse
	 *            if true use a sparse time series
	 * @return a time series
	 * @throws T2DBException
	 */
	TimeAddressable<T> getValues(Range range, boolean forceSparse) throws T2Exception, T2DBException;
	
	/**
	 * Return the value of the time series at the time index specified.
	 * When there is no value, the result is a missing value object.
	 * 
	 * @param time
	 *            a time index
	 * @return a value
	 * @throws T2DBException
	 * @see TimeAddressable#getMissingValue
	 */
	T getValue(TimeIndex time) throws T2Exception, T2DBException;
	
	/**
	 * Return the observation at the give time index or the last preceding
	 * observation. If the time parameter is null return the last observation. Return null if
	 * there is nothing.
	 * 
	 * @param time
	 *            a time index or null
	 * @return an observation or null
	 * @throws T2DBException
	 */
	Observation<T> getLastObservation(TimeIndex time) throws T2Exception, T2DBException;
	
	/**
	 * Return the observation at the given time index or the first following
	 * observation. If the time parameter is null return the first observation. Return
	 * null if there is nothing.
	 * 
	 * @param time
	 *            a time index or null
	 * @return an observation or null
	 * @throws T2DBException
	 */
	Observation<T> getFirstObservation(TimeIndex time) throws T2Exception, T2DBException;

	/**
	 * Return the range of values. The result is never null. When there are no
	 * values, the range is empty.
	 * 
	 * @return the range of existing values
	 * @throws T2Exception
	 * @throws T2DBException
	 */
	Range getRange() throws T2Exception, T2DBException;
	
	/**
	 * Return true if the series is forced to use sparse time series. If true
	 * the method {@link #getValues(Range)} must return a sparse time series,
	 * else it can return a sparse or a regular time series.
	 * 
	 * @return true if the series is forced to use sparse time series
	 * @throws T2DBException
	 */
	boolean isSparse() throws T2DBException;
	
	/**
	 * Cast the series to the type specified. The method performs type
	 * checking at run time and allows to catch type errors in controlled
	 * fashion.
	 * 
	 * @param type the underlying type required 
	 * @return the series cast to the type specified
	 * @throws T2DBException
	 */
	<S>Series<S> typeCheck(Class<S> type) throws T2DBException;
	
}
