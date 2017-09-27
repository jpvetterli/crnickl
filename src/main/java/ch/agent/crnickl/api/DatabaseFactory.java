/*
 *   Copyright 2012-2017 Hauser Olsson GmbH
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

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.T2DBMsg;
import ch.agent.crnickl.T2DBMsg.D;
import ch.agent.t2.time.TimeDomainCatalog;

/**
 * 
 * DatabaseFactory is the object which manages one or more {@link Database}s.
 * It creates a database using parameters specified in a {@link DatabaseConfiguration}.
 * Databases are accessed by name from the factory.
 *
 * @author Jean-Paul Vetterli
 */
public class DatabaseFactory {
	
	private static class Singleton {
		private static DatabaseFactory factory;
		static {
			factory = new DatabaseFactory();
		};
	}

	/**
	 * Return the DatabaseFactory instance.
	 * @return the DatabaseFactory instance
	 */
	public static DatabaseFactory getInstance() {
		return Singleton.factory;
	}
	
	private Map<String, DatabaseConfiguration> databases;
	
	/**
	 * Construct a DatabaseFactory.
	 */
	private DatabaseFactory() {
		databases = new HashMap<String, DatabaseConfiguration>();
	}

	/**
	 * Add a database to the factory.
	 * 
	 * @param configuration the configuration of the database
	 * @throws T2DBException
	 */
	public void addDatabase(DatabaseConfiguration configuration) throws T2DBException {
		String name = configuration.getName();
		if (databases.put(name, configuration) != null)
			throw T2DBMsg.exception(D.D00103, name);
	}
	
	/**
	 * Return the default database. This is simply <em>the</em> database
	 * available from the factory when there is one and only one database.  
	 * 
	 * @return the default database
	 * @throws T2DBException
	 */
	public Database getDefaultDatabase() throws T2DBException {
		if (databases.size() == 1)
			return getDatabase(databases.keySet().iterator().next());
		else
			throw T2DBMsg.exception(D.D00102);
	}

	/**
	 * Return the names of all databases.
	 * 
	 * @return the names of all databases in the factory
	 */
	public Collection<String> getDatabaseNames() {
		return databases.keySet();
	}
	
	/**
	 * Return the database with the given name.
	 * 
	 * @param name a string
	 * @return a database
	 * @throws T2DBException
	 */
	public Database getDatabase(String name) throws T2DBException {
		if (name == null)
			return getDefaultDatabase();
		DatabaseConfiguration configuration = databases.get(name);
		if (configuration == null)
			throw T2DBMsg.exception(D.D00101, name);
		try {
			Class<? extends Database> dbClass = configuration.getDatabaseClass();
			Constructor<? extends Database> constructor = dbClass.getConstructor(String.class, TimeDomainCatalog.class);
			Database d = constructor.newInstance(name, configuration.getTimeDomainCatalog());
			d.configure(configuration);
			return d;
		} catch (Exception e) {
			throw T2DBMsg.exception(e, D.D00105, name);
		}
	}
}
