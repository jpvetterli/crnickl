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
 * Type: DatabaseMethods
 * Version: 1.0.0
 */
package ch.agent.crnickl.impl;

import ch.agent.crnickl.api.DBObject;
import ch.agent.crnickl.api.DBObjectType;
import ch.agent.crnickl.api.Database;
import ch.agent.crnickl.api.Surrogate;

/**
 * DatabaseMethods provides low level methods useful for implementing CrNiCKL
 * on an actual database system.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public interface DatabaseMethods {
	
	/**
	 * Return the internal ID of a database object.
	 * The internal ID is not exposed to clients.
	 * 
	 * @param dBObject a database object
	 * @return a positive integer
	 */
	int getId(DBObject dBObject);

	/**
	 * Return the internal ID of a database object or 0 if the object is null.
	 * The internal ID is not exposed to clients.
	 * 
	 * @param dBObject a database object or null
	 * @return a non negative integer
	 */
	int getIdOrZero(DBObject dBObject);

	/**
	 * Extract the internal ID of a database object from its surrogate.
	 * The internal ID is not exposed to clients.
	 * 
	 * @param surrogate the surrogate of a database object
	 * @return a positive integer
	 */
	int getId(Surrogate surrogate);

	/**
	 * Create a surrogate for a database object.
	 * 
	 * @param db the database of the object
	 * @param dot the type of the object
	 * @param id the internal ID of the database object
	 * @return a surrogate
	 */
	Surrogate makeSurrogate(Database db, DBObjectType dot, int id);

	/**
	 * Create a surrogate for a database object.
	 * 
	 * @param dBObject a database object
	 * @param id the internal ID of the database object
	 * @return a surrogate
	 */
	Surrogate makeSurrogate(DBObject dBObject, int id);
	
}
