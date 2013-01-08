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

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.api.UpdatableChronicle;
import ch.agent.crnickl.api.UpdatableSeries;
import ch.agent.t2.time.Range;
import ch.agent.t2.time.TimeIndex;

/**
 * A ChronicleUpdatePolicy supports the delegation of various decisions and
 * actions when updating chronicles and series. There is a related interface 
 * {@link ChronicleUpdatePolicyExtension} which declares the same methods.
 * Implementations of this interface take care of invoking corresponding methods
 * in the extension interface.
 * 
 * @author Jean-Paul Vetterli
 */
public interface ChronicleUpdatePolicy extends UpdatePolicy {

	/**
	 * Throw an exception if the intended update is rejected.
	 * 
	 * @param chronicle a chronicle
	 * @throws T2DBException
	 */
	void willUpdate(UpdatableChronicle chronicle) throws T2DBException;
	
	/**
	 * Throw an exception if the intended update is rejected.
	 * 
	 * @param series a series 
	 * @param range the new range
	 * @throws T2DBException
	 */
	void willUpdate(UpdatableSeries<?> series, Range range) throws T2DBException;
	
	/**
	 * Throw an exception if the chronicle cannot be deleted. Typically, it is not
	 * possible to delete something when important dependent objects exist.
	 * 
	 * @param chronicle a chronicle
	 * @throws T2DBException
	 */
	void willDelete(UpdatableChronicle chronicle) throws T2DBException;
	
	/**
	 * Throw an exception if the series cannot be deleted. Typically, it is not
	 * possible to delete something when important dependent objects exist.
	 * 
	 * @param series a series
	 * @throws T2DBException
	 */
	void willDelete(UpdatableSeries<?> series) throws T2DBException;
	
	/**
	 * Throw an exception if the value cannot be deleted. Typically, it is not
	 * possible to delete something when important dependent objects exist.
	 * 
	 * @param series a series
	 * @param index a time index 
	 * @throws T2DBException
	 */
	void willDelete(UpdatableSeries<?> series, TimeIndex index) throws T2DBException;
	
	/**
	 * Perform actions required as a consequence of deleting a chronicle. Return
	 * true if something was done.
	 * <p>
	 * The method does not manage relationships or dependencies in the core
	 * system. Its responsibility is to call the relevant extension methods.
	 * 
	 * @param chronicle a chronicle
	 * @return true is anything was done
	 * @throws T2DBException
	 */
	boolean deleteChronicle(UpdatableChronicle chronicle) throws T2DBException;
	
	/**
	 * Perform actions required as a consequence of deleting a series. Return
	 * true if something was done.
	 * <p>
	 * The method does not manage relationships or dependencies in the core
	 * system. Its responsibility is to call the relevant extension methods.
	 * 
	 * @param series a series
	 * @return true is anything was done
	 * @throws T2DBException
	 */
	boolean deleteSeries(UpdatableSeries<?> series) throws T2DBException;
	
	/**
	 * Perform actions required as a consequence of deleting a value. Return
	 * true if something was done.
	 * <p>
	 * The method does not manage relationships or dependencies in the core
	 * system. Its responsibility is to call the relevant extension methods.
	 * 
	 * @param series a series
	 * @param index a time index
	 * @return true is anything was done
	 * @throws T2DBException
	 */
	boolean deleteValue(UpdatableSeries<?> series, TimeIndex index) throws T2DBException;
	
	/**
	 * Perform actions required as a consequence of updating the range of a series. Return
	 * true if something was done.
	 * <p>
	 * The method does not manage relationships or dependencies in the core
	 * system. Its responsibility is to call the relevant extension methods.
	 * 
	 * @param series a series
	 * @param range the new range
	 * @return true is anything was done
	 * @throws T2DBException
	 */
	boolean update(UpdatableSeries<?> series, Range range) throws T2DBException;
	
}
