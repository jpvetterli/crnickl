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
 * Type: AttributeDefinition
 * Version: 1.0.0
 */
package ch.agent.crnickl.api;

import ch.agent.crnickl.T2DBException;

/**
 * An AttributeDefinition is a component part of a {@link Schema} and defines an
 * {@link Attribute}. 
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 * @param <T>
 *            the underlying data type of the attribute
 */
public interface AttributeDefinition<T> extends SchemaComponent,
		Comparable<AttributeDefinition<T>> {

	/**
	 * Return true if the definition removes an inherited definition.
	 * This method is only relevant in edit mode.
	 * 
	 * @return true if the definition removes an inherited definition
	 */
	boolean isErasing();

	/**
	 * Return the attribute's property.
	 * 
	 * @return the property of the attribute
	 */
	public Property<T> getProperty();

	/**
	 * Return the attribute's default value.
	 * 
	 * @return the default value of the attribute
	 */
	public T getValue();

	/**
	 * Return the attribute. It is forbidden to use this method
	 * while the definition is not complete. Using the method when
	 * {@link #isComplete isComplete()} returns false throws an {@link IllegalStateException}
	 * 
	 * @return the attribute
	 * @throws T2DBException
	 */
	public Attribute<T> getAttribute();

	/**
	 * Cast the attribute definition to the type specified. The method performs
	 * type checking at run time and allows to catch type errors in controlled
	 * fashion.
	 * 
	 * @param type the underlying type required 
	 * @return the attribute definition cast to the type specified
	 * @throws T2DBException
	 */
	<S> AttributeDefinition<S> typeCheck(Class<S> type) throws T2DBException;

}
