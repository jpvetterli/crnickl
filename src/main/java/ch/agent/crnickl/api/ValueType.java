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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import ch.agent.crnickl.T2DBException;

/**
 * A ValueType defines value domains for {@link Attribute}s and
 * {@link Series}. Value types can be provided by the system or can be defined
 * procedurally with a {@link ValueScanner}.
 * 
 * @author Jean-Paul Vetterli
 * @param <T> the underlying data type of the values
 */
public interface ValueType<T> extends DBObject {
	
	/**
	 * StandardValueType provides an easy way to identify built-in value types.
	 */
	enum StandardValueType {
		/**
		 * A value type for booleans.
		 */
		BOOLEAN,
		/**
		 * A value type for dates. 
		 */
		DATE, 
		/**
		 * A value type for time stamps, with second precision.
		 */
		DATETIME, 
		/**
		 * A value type for texts agreeing with the CrNiCKL simple name syntax.
		 */
		NAME, 
		/**
		 * A value type for numeric values.
		 */
		NUMBER, 
		/**
		 * A value type for textual values. 
		 */
		TEXT, 
		/**
		 * A value type for time domain values.
		 */
		TIMEDOMAIN, 
		/**
		 * A value type for value type values (sic). 
		 */
		TYPE
	}
	
	/**
	 * Return the name of the value type.
	 * 
	 * @return the name of the value type
	 */
	String getName();
	
	/**
	 * Return true if the value type maintains an exhaustive list of allowed values.
	 * 
	 * @return true if the value type has a list of allowed values
	 */
	boolean isRestricted();
	
	/**
	 * This method is obsolete and always return false.
	 * 
	 * @return false
	 */
	@Deprecated
	boolean isBuiltIn();
	
	/**
	 * Return the class of the underlying data type.
	 * 
	 * @return the class of the underlying data type
	 */
	Class<T> getType();
	
	/**
	 * Return the value scanner.
	 * 
	 * @return the value scanner
	 */
	ValueScanner<T> getScanner();
	
	/**
	 * Return the constant representing the value type when it is a built-in value type.
	 * Return null if the value type is not built-in.
	 * 
	 * @return the constant representing the value type or null
	 */
	StandardValueType getStandardValueType();
	
	/**
	 * Return a string providing an external representation of the value type.
	 * This is either the string value of the standard value type or the name 
	 * of the value scanner class.
	 * 
	 * @return the external representation of the value type
	 */
	String getExternalRepresentation();

	/**
	 * Return the set of allowed values. Returns an empty set when there is no
	 * restriction.
	 * 
	 * @return the set of allowed values
	 * @throws T2DBException
	 */
	Set<T> getValues();
	
	/**
	 * Return a map with allowed values as keys and a description for each
	 * value. Descriptions can be null. Returns an empty map when values are not
	 * restricted.
	 * 
	 * @return a map with values and their descriptions
	 * @throws T2DBException
	 */
	Map<T, String> getValueDescriptions();
	
	/**
	 * Return values and descriptions as a collection of formatted strings. If a
	 * value has no description, simply convert it to a string. If a value has a
	 * description, use the format parameter, which is expected to include two %s fields.
	 * If the format is null, use a built-in format.
	 * 
	 * @param format a format
	 * @return a collection of formatter values and descriptions
	 */
	Collection<String> getValues(String format);
	
	/**
	 * Convert the argument to an object of the underlying data type. Throw an
	 * exception if the argument cannot be converted. A null is converted to a
	 * null. An empty value is converted to a null, unless the value type is
	 * restricted. When the value type is restricted, it must be capable of
	 * scanning and converting the empty string, and the result of the
	 * conversion must be present in the set of allowed values.
	 * 
	 * @param value a string
	 * @return the value converted to the value type's underlying data type
	 * @throws T2DBException
	 */
	T scan(String value) throws T2DBException;
	
	/**
	 * Returns true if the object can be assigned to a variable of the
	 * underlying data type. The method is provided because the instanceof
	 * operator cannot be used with generics. The method does not check if the
	 * value is allowed. Use {@link #check} for that.
	 * 
	 * @param obj
	 *            an object
	 * @return true if the object can be assigned to a variable of the
	 *         underlying data type
	 */
	boolean isCompatible(Object obj);

	/**
	 * Check that the value is allowed. Throw an exception if a set of allowed values
	 * is defined and the value is not in the set.
	 * 
	 * @param value a value
	 * @throws T2DBException
	 */
	void check(T value) throws T2DBException;

	/**
	 * Convert the argument to a string. An exception is thrown if the
	 * value is illegal for the value type. For example if the scanner actual
	 * type is Integer, and the scanner requires positive numbers, a negative or
	 * zero value will result in an exception. With most scanners, a null value
	 * is converted to a null result.
	 * 
	 * @param value a value
	 * @return the value converted to a string
	 * @throws T2DBException
	 */
	String toString(Object value) throws T2DBException;

	/**
	 * Return an {@link UpdatableValueType} corresponding to this value type. 
	 * Successfully getting an {@link Updatable} object does not imply
	 * that updates can be successfully applied.
	 * 
	 * @return an updatable value type corresponding to this value type
	 */
	UpdatableValueType<T> edit();
	
	/**
	 * Cast the value type to the type specified. The method performs type
	 * checking at run time and allows to catch type errors in controlled
	 * fashion.
	 * 
	 * @param type the underlying type required 
	 * @return the value type cast to the type specified
	 * @throws T2DBException
	 */
	<S>ValueType<S> typeCheck(Class<S> type) throws T2DBException;

}
