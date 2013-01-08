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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.T2DBMsg;
import ch.agent.crnickl.T2DBMsg.D;

/**
 * The NamingPolicy defines the syntax for naming chronicles and series. 
 * It also provides methods and constants useful for presenting various 
 * textual elements with a unified appearance.
 * 
 * @author Jean-Paul Vetterli
 */
public class NamingPolicy {

	/**
	 * The format used when combining a value and its description.
	 */
	private static final String VALUE_DESCRIPTION_FORMAT = "%s - %s";
	private static final String VALUE_DESCRIPTION_SPLITTER = " - ";
	
	/**
	 * The maximum length of a simple name.
	 */
	private static final int NAME_MAX_LENGTH = 25;
	
	/**
	 * The string used as separator when joining simple names to form a full name.
	 */
	public static final String NAME_SEPARATOR = ".";
	private static final String NAME_SPLITTER = "\\.";
	
	/**
	 * The string used as separator when joining simple descriptions to form a full descritpion.
	 */
	public static final String DESCRIPTION_SEPARATOR = "; ";
	
	/**
	 * The string used as separator when joining elements of a list.
	 */
	public static final String LIST_SEPARATOR = ", ";
	
	private static final String invalidCharPattern = "[^A-Za-z0-9_\\-]";
	
	private Matcher invalidCharMatcher;

	/**
	 * Construct a naming policy.
	 */
	public NamingPolicy() {
	}

	/**
	 * Return an array of length 2 with the parent name and the simple name of the
	 * input argument. When the argument is already a simple name, 
	 * the first element of the result is null.
	 *  
	 * @param name a full name
	 * @return an array containing a full name, or null, and the last simple name
	 * @throws T2DBException
	 */
	public String[] split(String name) throws T2DBException {
		if (name == null || name.length() == 0)
			throw T2DBMsg.exception(D.D01102);
		int i = name.lastIndexOf(NAME_SEPARATOR);
		if (i < 0)
			return new String[]{null, name};
		else
			return new String[]{name.substring(0, i), name.substring(i + 1)};
	}
	
	/**
	 * Return an array of simple names obtained by splitting the input parameter.
	 * The elements are split assuming they are separated with {@link #NAME_SEPARATOR}s.
	 * 
	 * @param name a full name 
	 * @return an array of simple names
	 * @throws T2DBException
	 */
	public String[] asStringArray(String name) throws T2DBException {
		return name.split(NAME_SPLITTER, -1);
	}
	
	/**
	 * Check the length of a simple name, optionally replace 
	 * all illegal characters with underscores, 
	 * and convert to lower case.
	 * Legal characters are the 48 letters in the range A-Z and a-z, decimal digits, 
	 * the underscore (_), and the hyphen (-).
	 * 
	 * @param name a simple name
	 * @param canModify if true replace every illegal character with an underscore
	 * @return name with all illegal characters replaced with underscores and converted to lower case
	 * @throws T2DBException
	 */
	public String checkSimpleName(String name, boolean canModify) throws T2DBException {
		if (name == null || name.length() == 0 || name.length() > NAME_MAX_LENGTH)
			throw T2DBMsg.exception(D.D01103, name, NAME_MAX_LENGTH);
		if (invalidCharMatcher == null)
			invalidCharMatcher = Pattern.compile(invalidCharPattern).matcher("");
		invalidCharMatcher.reset(name);
		if(invalidCharMatcher.find()) {
			if (canModify)
				name = invalidCharMatcher.replaceAll("_");
			else
				throw T2DBMsg.exception(D.D01104, name);
		}
		return name.toLowerCase();
	}
	
	/**
	 * Return the full name obtained by joining the arguments 
	 * with {@link #NAME_SEPARATOR}.
	 * @param names an array of simple names
	 * @return a full name
	 */
	public String fullName(String... names) {
		return join(NAME_SEPARATOR, names);
	}
	
	/**
	 * Return the full name obtained by joining the elements of the argument 
	 * with {@link #NAME_SEPARATOR}.
	 * @param names a list of simple names
	 * @return a full name
	 */
	public String fullName(List<String> names) {
		return join(NAME_SEPARATOR, names.toArray(new String[names.size()]));
	}

	/**
	 * Return the full description obtained by joining the arguments 
	 * with {@link #DESCRIPTION_SEPARATOR}.
	 * @param descriptions an array of simple descriptions
	 * @return a full description
	 */
	public String fullDescription(String... descriptions) {
		return join(DESCRIPTION_SEPARATOR, descriptions);
	}

	/**
	 * Return the full description obtained by joining the elements of the argument 
	 * with {@link #DESCRIPTION_SEPARATOR}.
	 * @param descriptions a list of simple descriptions
	 * @return a full description
	 */
	public String fullDescription(List<String> descriptions) {
		return join(DESCRIPTION_SEPARATOR, descriptions.toArray(new String[descriptions.size()]));
	}
	
	/**
	 * Return a string with the value and the description joined
	 * with {@link #DESCRIPTION_SEPARATOR}.
	 * 
	 * @param value a value 
	 * @param description a description
	 * @return the value and description joined using the official format
	 */
	public String joinValueAndDescription(String value, String description) {
		if (description == null || description.length() == 0)
			return value;
		else
			return String.format(VALUE_DESCRIPTION_FORMAT, value, description);
	}
	
	/**
	 * Return an array with a value and a description obtained by splitting the input
	 * on {@link #DESCRIPTION_SEPARATOR}.
	 * If there is no description, the second element of the result is an empty string.
	 * 
	 * @param valueAndDescription a string with a value and a description
	 * @return and array with the value and the description
	 */
	public String[] splitValueAndDescription(String valueAndDescription) {
		if (valueAndDescription == null)
			return new String[]{null, null};
		String[] result = valueAndDescription.split(VALUE_DESCRIPTION_SPLITTER, 2);
		// no description: empty string
		return result.length == 1 ? new String[]{result[0], ""} : result;
	}
	
	/**
	 * Return a string with all the strings starting with the second parameter
	 * joined using the first parameter as the separator.
	 * 
	 * @param separator a string
	 * @param strings an array of strings
	 * @return a string
	 */
	protected String join(String separator, String... strings) {
		StringBuilder b = new StringBuilder();
		int size = strings.length;
		int i = 0;
		for (String string : strings) {
			if (string != null)
				b.append(string);
			if (++i < size)
				b.append(separator);
		}
		return b.toString();
	}
	
}
