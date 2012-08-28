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
 * Type: ValueAccessMethods
 * Version: 1.0.0
 */
package ch.agent.crnickl.impl;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.api.Series;
import ch.agent.crnickl.api.UpdatableSeries;
import ch.agent.t2.time.Range;
import ch.agent.t2.time.TimeIndex;
import ch.agent.t2.timeseries.Observation;
import ch.agent.t2.timeseries.TimeAddressable;

/**
 * ValueAccessMethods is a generic interface for accessing and modifying series values.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 * @param <T> the data type of values
 */
public interface ValueAccessMethods<T> extends DatabaseMethods {

	/**
	 * Return the range of values of a series.
	 * 
	 * @param series a series 
	 * @return a range, never null, but possibly empty
	 * @throws T2DBException
	 */
	Range getRange(Series<T> series) throws T2DBException;
	
	/**
	 * Load values into the time series in the range specified. To load all
	 * values, specify a null range. Return the number of values loaded.
	 * Existing values outside the range are not touched. Missing values inside 
	 * the range do not delete existing values.
	 * 
	 * @param series
	 *            a series
	 * @param range
	 *            a range or null
	 * @param ts
	 *            a time series
	 * @return the number of values loaded
	 * @throws T2DBException
	 */
	long getValues(Series<T> series, Range range, TimeAddressable<T> ts) throws T2DBException;
	
	/**
	 * Return the observation at the time index or the first following one or null.
	 * If the time index is null return the first observation.
	 * 
	 * @param series a series
	 * @param time a time index
	 * @return an observation
	 * @throws T2DBException
	 */
	Observation<T> getFirst(Series<T> series, TimeIndex time) throws T2DBException;

	/**
	 * Return the observation at the time index or the last preceding one or null.
	 * If the time index is null return the last observation.
	 * 
	 * @param series a series
	 * @param time a time index
	 * @return an observation
	 * @throws T2DBException
	 */
	Observation<T> getLast(Series<T> series, TimeIndex time) throws T2DBException;
	
	/**
	 * Delete the value at the time index, if the policy allows.
	 * 
	 * @param series a series
	 * @param t a time index
	 * @param policy a policy
	 * @return true if a value was deleted
	 * @throws T2DBException
	 */
	boolean deleteValue(UpdatableSeries<T> series, TimeIndex t, ChronicleUpdatePolicy policy) throws T2DBException;
	
	/**
	 * Reduce the range of the series, if the policy allows.
	 * 
	 * @param series a series
	 * @param range a range
	 * @param policy a policy
	 * @return true if anything was done
	 * @throws T2DBException
	 */
	boolean updateSeries(UpdatableSeries<T> series, Range range, ChronicleUpdatePolicy policy) throws T2DBException;
	
	/**
	 * Update the series with the values in the time series, if the policy allows. 
	 * Missing values in the time series delete values in the database.
	 *  
	 * @param series a series
	 * @param values a time series of values
	 * @param policy a policy
	 * @return the number of values updated
	 * @throws T2DBException
	 */
	long updateValues(UpdatableSeries<T> series, TimeAddressable<T> values, ChronicleUpdatePolicy policy) throws T2DBException;

}
