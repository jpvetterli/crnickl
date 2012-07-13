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
 * Type: ValueScanner
 * Version: 1.0.0
 */
package ch.agent.crnickl.api;

import ch.agent.crnickl.T2DBException;

/**
 * A ValueScanner provides a standard textual interface to types.
 * Implementations can provide a constructor taking a {@link ValueType}. If such
 * a constructor is available it is used. Else value scanners are created using
 * a parameterless constructor.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 * @param <T>
 *            the underlying data type of the values
 */
public interface ValueScanner<T> {

	/**
	 * Return the class of underlying data type of the value scanner.
	 * 
	 * @return a class
	 */
	Class<T> getType();
	
	/**
	 * Scan a string and convert it to an object of the scanner's actual type.
	 * Throw an exception when the conversion fails. Whether the input can be
	 * null depends on the actual implementation. 
	 * The method also performs a {@link #check(Object)}. 
	 * 
	 * @param value a string
	 * @return the value converted to the underlying data type
	 * @throws T2DBException
	 */
	T scan(String value) throws T2DBException;

	/**
	 * Throw an exception if the value does not conform to 
	 * special restrictions enforced by the value scanner.
	 * For example, a value scanner for positive 
	 * numbers would check that the value is greater than zero.
	 * 
	 * @param value a value
	 * @throws T2DBException
	 */
	void check(T value) throws T2DBException;
	
	/**
	 * Check and convert the parameter to a string. Return null if the value is
	 * null.
	 * 
	 * @param value a value
	 * @return a string or null
	 * @throws T2DBException
	 */
	String toString(T value) throws T2DBException;
	
}
