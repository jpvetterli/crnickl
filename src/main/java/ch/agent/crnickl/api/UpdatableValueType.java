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

/**
 * An UpdatableValueType is a value type which can be modified.
 * 
 * @author Jean-Paul Vetterli
 * @param <T> the underlying data type of the values
 */
public interface UpdatableValueType<T> extends ValueType<T>, Updatable {

	/**
	 * Set the name of the value type.
	 * 
	 * @param name a string
	 * @throws T2DBException
	 */
	void setName(String name) throws T2DBException;
	
	/**
	 * Add a value to list of allowed values. 
	 * This method can only be used if the value type is restricted.
	 * 
	 * @param value a value 
	 * @param description the value's description
	 * @throws T2DBException
	 */
	void addValue(T value, String description) throws T2DBException;
	
	/**
	 * Update the description of the given allowed value. 
	 * This method can only be used if the value type is restricted.

	 * @param value an allowed value
	 * @param description a string
	 * @throws T2DBException
	 */
	void updateValue(T value, String description) throws T2DBException;
	
	/**
	 * Delete a value from the list of allowed values. This method can only be
	 * used if the value type is restricted. The operation fails if the value is
	 * in use, either as the default value of an attribute or as an actual
	 * attribute value.
	 * 
	 * @param value
	 *            the value to delete
	 * @throws T2DBException
	 */
	void deleteValue(T value) throws T2DBException;
	
	/**
	 * Destroy the value type. The operation fails if the value type is in use.
	 * 
	 * @throws T2DBException
	 */
	void destroy() throws T2DBException;
	
}
