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

import java.util.List;

import ch.agent.crnickl.T2DBException;


/**
 * A Property defines an {@link Attribute}. The value
 * of an attribute must agree with the property's {@link ValueType}. 
 * 
 * @author Jean-Paul Vetterli
 * @param <T> the underlying data type of attributes having this property
 */
public interface Property<T> extends DBObject {
	
	/**
	 * Return an {@link UpdatableProperty} corresponding to this property. 
	 * Successfully getting an {@link Updatable} object does not imply
	 * that updates can be successfully applied.
	 * 
	 * @return an updatable property corresponding to this property
	 */
	UpdatableProperty<T> edit();

	/**
	 * Return the name of the property.
	 * 
	 * @return the name of the property
	 */
	String getName();

	/**
	 * Return true if the property has been defined as indexed.
	 * When a property is defined as <q>indexed</q>, it is meaningful to 
	 * search for chronicles using {@link #getChronicles(Object, int)}. To give an example,
	 * imagine a very large database where chronicles represent persons, with two attributes:
	 * country of residence and email address. You would not search for persons residing in the 
	 * USA, would you? The size of the result would be enormous. On the other hand finding persons with a given email 
	 * address would be fast.
	 *  
	 * @return true if this is an indexed property
	 */
	boolean isIndexed();
	
	/**
	 * Return the value type of the property.
	 * 
	 * @return the value type
	 */
	ValueType<T> getValueType();
	
	/**
	 * Check the value for conformity with the {@link ValueType}.
	 * An exception is thrown if the value does not conform.
	 * A typical cause of failure is that the value is not in the list of values
	 * allowed by a restricted value type. 
	 * 
	 * @param value a value to check
	 * @throws T2DBException
	 */
	void check(T value) throws T2DBException;

	/**
	 * Return an object of the underlying data type for this property
	 * converted from a textual value.
	 *  
	 * @param value a textual representation of the value
	 * @return the value of the underlying data type
	 * @throws T2DBException
	 */
	T scan(String value) throws T2DBException;
	
	/**
	 * Return a list of chronicles which have the given attribute value. A
	 * chronicle is included in the result only if the value is explicitly
	 * specified; default attribute values do not count. It is possible to limit
	 * the size of the result.
	 * 
	 * @param value
	 *            the value to match
	 * @param maxSize
	 *            the maximum size of the result, 0 for no limit
	 * @return a list of chronicles
	 * @throws T2DBException
	 */
	List<Chronicle> getChronicles(T value, int maxSize) throws T2DBException;

	/**
	 * Cast the property to the type specified. The method performs type
	 * checking at run time and allows to catch type errors in controlled
	 * fashion.
	 * 
	 * @param type the underlying type required 
	 * @return the property cast to the type specified
	 * @throws T2DBException
	 */
	<S>Property<S> typeCheck(Class<S> type) throws T2DBException;

}
