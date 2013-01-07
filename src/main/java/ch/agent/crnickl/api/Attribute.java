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
 * Type: Attribute
 * Version: 1.0.0
 */
package ch.agent.crnickl.api;

import ch.agent.crnickl.T2DBException;

/**
 * An Attribute is a constant characteristic of a {@link Chronicle} or of a {@link Series}. 
 * Its value does not vary over time. It is defined by a {@link Property} 
 * and has a value.
 * <p>
 * An attribute is defined in an {@link AttributeDefinition}.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 * @param <T> the underlying data type of the attribute
 *
 */
public interface Attribute<T> {

	/**
	 * Return the attribute's {@link Property}.
	 * 
	 * @return the property of the attribute
	 */
	Property<T> getProperty();
	
	/**
	 * Return the attribute's value. This value is either the default value,
	 * taken from the {@link AttributeDefinition}, or the value set with
	 * {@link #set} or {@link #scan}.
	 * 
	 * @return the value of the attribute
	 */
	T get();
	
	/**
	 * Return the description of the attribute's value. When the <code>effective</code>
	 * parameter is set, and there is no description, and the attribute value is from a
	 * value list, return the description from the value list, if any.
	 * 
	 * @param effective if true fall back to description from definition 
	 * @return a description of the current attribute value
	 */
	String getDescription(boolean effective);
	
	/**
	 * Set the description of the current attribute value.
	 * 
	 * @param description a description for the current attribute value
	 */
	void setDescription(String description);
	
	/**
	 * Reset the default value of the attribute. 
	 * This removes the current value of the attribute with the consequence
	 * that the current default value taken from the {@link AttributeDefinition} 
	 * will apply.
	 * <p>
	 * <b>Note</b>
	 * <p>
	 * When an attribute value happens to be equal to the default
	 * value but should not change when the default is updated, 
	 * it must be set explicitly.
	 */
	void reset();
	
	/**
	 * Set the value of the attribute. The value must agree with the
	 * {@link AttributeDefinition}, as enforced by
	 * {@link Property#check(Object)}. Setting the value to null has the same
	 * effect as calling {@link #reset}.
	 * 
	 * @param value
	 *            a value for the attribute
	 * @throws T2DBException
	 */
	void set(T value) throws T2DBException;
	
	/**
	 * Set the value of the attribute from an <code>Object</code>. 
	 * A null value is passed directly to {@link #set(Object)}.
	 * When not null, the value either must be assignment
	 * compatible with the underlying type of the attribute or it must be a
	 * string. In the first case it is cast and passed to
	 * {@link #set(Object)}. In the second case it is scanned 
	 * by {@link ValueType#scan(String)} before being passed to {@link #set(Object)}.
	 * 
	 * @param value an object representing the new value of the attribute
	 * @throws T2DBException
	 */
	void scan(Object value) throws T2DBException;
	
	/**
	 * Cast the attribute to the type specified. The method performs type
	 * checking at run time and allows to catch type errors in controlled
	 * fashion.
	 * 
	 * @param type the underlying type required 
	 * @return the attribute cast to the type specified
	 * @throws T2DBException
	 */
	<S>Attribute<S> typeCheck(Class<S> type) throws T2DBException;

}
