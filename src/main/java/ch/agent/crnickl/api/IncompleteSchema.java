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
 * Type: IncompleteSchema
 * Version: 1.0.0
 */
package ch.agent.crnickl.api;

import java.util.Collection;

import ch.agent.crnickl.T2DBException;

/**
 * An IncompleteSchema provides common behavior to {@link Schema} and
 * {@link SeriesDefinition}. Both can have a number of {@link Attribute}s defined.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public interface IncompleteSchema {
	
	/**
	 * Return the name of the schema or series definition.
	 * 
	 * @return the name of the schema or the series definition
	 */
	String getName();

	/**
	 * Return the collection of all attribute definitions. The collection is sorted by 
	 * attribute number. For a schema, the result does not include any series attribute
	 * definition. 
	 * 
	 * @return the collection of all attribute definitions
	 */
	Collection<AttributeDefinition<?>> getAttributeDefinitions();

	/**
	 * Return the attribute definition with the given attribute name. The
	 * <code>mustExist</code> parameter determines behavior when nothing is
	 * found: exception or null result.
	 * 
	 * @param name
	 *            the name of the attribute
	 * @param mustExist
	 *            if true throw an exception instead of returning null
	 * @return an attribute definition or null (only when mustExist is false)
	 * @throws T2DBException
	 */
	AttributeDefinition<?> getAttributeDefinition(String name, boolean mustExist) throws T2DBException;
	
	/**
	 * Return the attribute definition with the given attribute number. The
	 * <code>mustExist</code> parameter determines behavior when nothing is
	 * found: exception or null result.
	 * 
	 * @param number
	 *            the number of the attribute
	 * @param mustExist
	 *            if true throw an exception instead of returning null
	 * @return an attribute definition or null (only when mustExist is false)
	 * @throws T2DBException
	 */
	AttributeDefinition<?> getAttributeDefinition(int number, boolean mustExist) throws T2DBException;
}
