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
package ch.agent.crnickl.impl;

import ch.agent.crnickl.api.DBObject;
import ch.agent.crnickl.api.DBObjectId;
import ch.agent.crnickl.api.DBObjectType;
import ch.agent.crnickl.api.Database;
import ch.agent.crnickl.api.Surrogate;

/**
 * DatabaseMethods provides low level methods useful for implementing CrNiCKL
 * on an actual database system.
 * 
 * @author Jean-Paul Vetterli
 */
public interface DatabaseMethods {
	
	/**
	 * Create a surrogate for a database object.
	 * 
	 * @param db the database of the object
	 * @param dot the type of the object
	 * @param id the internal ID of the database object
	 * @return a surrogate
	 */
	Surrogate makeSurrogate(Database db, DBObjectType dot, DBObjectId id);

	/**
	 * Create a surrogate for a database object.
	 * 
	 * @param dBObject a database object
	 * @param id the internal ID of the database object
	 * @return a surrogate
	 */
	Surrogate makeSurrogate(DBObject dBObject, DBObjectId id);
	
}
