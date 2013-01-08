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
import ch.agent.crnickl.api.Property;
import ch.agent.crnickl.api.UpdatableSchema;
import ch.agent.crnickl.api.ValueType;

/**
 * A SchemaUpdatePolicy supports the delegation of various decisions and
 * actions when updating schemas.
 * 
 * @author Jean-Paul Vetterli
 */
public interface SchemaUpdatePolicy extends UpdatePolicy {
	
	/**
	 * Throw an exception if the update is rejected.
	 * 
	 * @param schema a schema
	 * @throws T2DBException
	 */
	void willUpdate(UpdatableSchema schema) throws T2DBException;

	/**
	 * Throw an exception if the schema cannot be deleted.
	 * 
	 * @param schema a schema
	 * @throws T2DBException
	 */
	void willDelete(UpdatableSchema schema) throws T2DBException;
	
	/**
	 * Throw an exception if the property cannot be rejected.
	 * 
	 * @param property a property
	 * @throws T2DBException
	 */
	<T>void willDelete(Property<T> property) throws T2DBException;
	
	/**
	 * Throw an exception if the the value type cannot be deleted.
	 * 
	 * @param valueType a value type
	 * @throws T2DBException
	 */
	<T>void willDelete(ValueType<T> valueType) throws T2DBException;

	/**
	 * Throw an exception if the value type value cannot be deleted.
	 * 
	 * @param valueType  a value type
	 * @param value a value
	 * @throws T2DBException
	 */
	<T>void willDelete(ValueType<T> valueType, T value) throws T2DBException;

}
