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
 * Type: SchemaUpdatePolicy
 * Version: 1.0.0
 */
package ch.agent.crnickl.impl;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.api.AttributeDefinition;
import ch.agent.crnickl.api.SeriesDefinition;
import ch.agent.crnickl.api.UpdatableSchema;

/**
 * A SchemaUpdatePolicy supports the delegation of various decisions and
 * actions when updating schemas.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public interface SchemaUpdatePolicy {
	
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
	 * Throw an exception if the attribute in the schema cannot be deleted.
	 * 
	 * @param schema a schema
	 * @param def an attribute definition
	 * @throws T2DBException
	 */
	void willDelete(UpdatableSchema schema, AttributeDefinition<?> def) throws T2DBException;
	
	/**
	 * Throw an exception if the update of the attribute in the schema is rejected.
	 * 
	 * @param schema a schema
	 * @param def an attribute definition
	 * @throws T2DBException
	 */
	void willUpdate(UpdatableSchema schema, AttributeDefinition<?> def) throws T2DBException;
	
	/**
	 * Throw an exception if the series in the schema cannot be deleted.
	 * 
	 * @param schema a schema
	 * @param ss a series definition
	 * @throws T2DBException
	 */
	void willDelete(UpdatableSchema schema, SeriesDefinition ss) throws T2DBException;
	
	/**
	 * Throw an exception if the series attribute in the schema cannot be deleted.
	 * 
	 * @param schema a schema
	 * @param ss a series definition
	 * @param def an attribute definition
	 * @throws T2DBException
	 */
	void willDelete(UpdatableSchema schema, SeriesDefinition ss, AttributeDefinition<?> def) throws T2DBException;
	
	/**
	 * Throw an exception if the update of the series attribute in the schema is rejected.
	 * 
	 * @param schema a schema
	 * @param ss a series definition
	 * @param def an attribute definition
	 * @throws T2DBException
	 */
	void willUpdate(UpdatableSchema schema, SeriesDefinition ss, AttributeDefinition<?> def) throws T2DBException;
	
}
