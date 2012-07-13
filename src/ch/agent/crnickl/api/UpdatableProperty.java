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
 * Package: ch.agent.crnickl.api
 * Type: UpdatableProperty
 * Version: 1.0.0
 */
package ch.agent.crnickl.api;

import ch.agent.crnickl.T2DBException;

/**
 * An UpdatableProperty is a property which can be modified.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 * @param <T> the underlying data type of attributes having this property

 */
public interface UpdatableProperty<T> extends Property<T>, Updatable {

	/**
	 * Set the name of the property.
	 * @param name a string
	 * @throws T2DBException
	 */
	void setName(String name) throws T2DBException;
	
	/**
	 * Delete the property. The operation fails if the property is used in
	 * a schema.
	 * 
	 * @throws T2DBException
	 */
	void delete() throws T2DBException;
}
