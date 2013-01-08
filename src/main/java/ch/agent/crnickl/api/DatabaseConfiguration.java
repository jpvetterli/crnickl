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

import java.util.LinkedHashMap;
import java.util.Map;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.T2DBMsg;
import ch.agent.crnickl.T2DBMsg.D;

/**
 * A DatabaseConfiguration is a group of parameters defining a {@link Database}.
 * Two parameters are essential: the name of the database and the class of the database.
 * All other parameters are arbitrary key-value pairs.
 * 
 * @author Jean-Paul Vetterli
 */
public class DatabaseConfiguration {
	
	private String databaseName;
	private Class<? extends Database> databaseClass;
	private Map<String, String> parameters;

	/**
	 * Construct a database configuration. If there is a problem with the
	 * database class, an exception is thrown.
	 * 
	 * @param databaseName
	 *            the name of the database
	 * @param databaseClassName
	 *            the name of a subclass of {@link Database}
	 * @throws T2DBException
	 */
	@SuppressWarnings("unchecked")
	public DatabaseConfiguration(String databaseName, String databaseClassName) throws T2DBException {
		this.databaseName = databaseName;
		try {
			databaseClass = (Class<? extends Database>) Class.forName(databaseClassName);
		} catch (Exception e) {
			throw T2DBMsg.exception(e, D.D00104, databaseName);
		}
		parameters = new LinkedHashMap<String, String>(); // keeps entry order
	}
	
	/**
	 * Set a parameter.
	 * 
	 * @param key a string
	 * @param value a string
	 */
	public void setParameter(String key, String value) {
		parameters.put(key, value);
	}

	/**
	 * Return the name of the database.
	 * 
	 * @return the name of the database
	 */
	public String getName() {
		return databaseName;
	}

	/**
	 * Return the class of the database.
	 * 
	 * @return a subclass of {@link Database}
	 */
	public Class<? extends Database> getDatabaseClass() {
		return databaseClass;
	}

	/**
	 * Return a parameter value given its key. If
	 * there is no such parameter, return null or throw an exception,
	 * depending on the value of <code>mustExist</code>.
	 * 
	 * @param key a string
	 * @param mustExist if true throw an exception when there is no such parameter
	 * @return the value of the parameter with the given key
	 * @throws T2DBException
	 */
	public String getParameter(String key, boolean mustExist) throws T2DBException {
		String value = parameters.get(key);
		if (value == null && mustExist)
			throw T2DBMsg.exception(D.D00109, key);
		return value;
	}
}
