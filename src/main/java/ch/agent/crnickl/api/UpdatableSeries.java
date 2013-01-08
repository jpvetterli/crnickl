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

import ch.agent.crnickl.T2DBException;
import ch.agent.t2.T2Exception;
import ch.agent.t2.time.Range;
import ch.agent.t2.time.TimeIndex;
import ch.agent.t2.timeseries.TimeAddressable;

/**
 * An UpdatableSeries is a series which can be modified.
 * 
 * @author Jean-Paul Vetterli
 * @param <T> the class of the elements of the underlying time series
 */
public interface UpdatableSeries<T> extends Series<T>, Updatable {

	/**
	 * Destroy the series. The operation fails if the range of the series
	 * is not empty.
	 * 
	 * @throws T2DBException
	 */
	void destroy() throws T2DBException;

	/**
	 * Add a value to the list of pending updates. If there is already an update
	 * pending for the same time, it is replaced. A null value is interpreted as
	 * a request to delete the value at the given time, possibly reducing the
	 * range of the time series. The Object is converted using the value type of
	 * the series. If the value is converted to a null, the effect will be to
	 * delete the observation.
	 * 
	 * @param t a time index
	 * @param value a value
	 * @throws T2DBException
	 */
	void scanValue(TimeIndex t, Object value) throws T2Exception, T2DBException;
	
	/**
	 * Add a value to the list of pending updates. If there is already
	 * an update pending for the same time, it is replaced. A null value
	 * is interpreted as a request to delete the value at the given time,
	 * possibly reducing the range of the time series.
	 * 
	 * @param t a time index
	 * @param value a value
	 * @throws T2DBException
	 */
	void setValue(TimeIndex t, T value) throws T2Exception, T2DBException;
	
	/**
	 * Set the time series to the list of pending updates. If there are already
	 * updates pending for the same time, they are replaced.
	 * 
	 * @param values a time series of values
	 * @throws T2DBException
	 */
	void setValues(TimeAddressable<T> values) throws T2Exception, T2DBException;

	/**
	 * Set the range of the series. If the range specified
	 * is not within the current range, nothing is done. If it is null
	 * all values are cleared.
	 * 
	 * @param range a range or null to clear the range
	 * @return true if the range parameter is within the current range
	 * @throws T2Exception
	 * @throws T2DBException
	 */
	boolean setRange(Range range) throws T2Exception, T2DBException;
	
	/**
	 * Cast the updatable series to the type specified. The method performs type
	 * checking at run time and allows to catch type errors in controlled
	 * fashion.
	 * 
	 * @param type the underlying type required 
	 * @return the updatable series cast to the type specified
	 * @throws T2DBException
	 */
	<S>UpdatableSeries<S> typeCheck(Class<S> type) throws T2DBException;
	
}
