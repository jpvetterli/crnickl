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
 * Type: DBObject
 * Version: 1.0.0
 */
package ch.agent.crnickl.api;


/**
 * Objects managed in a {@link Database} are instances of DBOBject
 * and are represented by a {@link Surrogate}.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public interface DBObject {

	/**
	 * Return the surrogate for this database object.
	 * 
	 * @return the surrogate
	 */
	Surrogate getSurrogate();
	
	/**
	 * Return true if the database object was not deleted.
	 * 
	 * @return true if the database object was not deleted
	 */
	boolean isValid();
	
}
