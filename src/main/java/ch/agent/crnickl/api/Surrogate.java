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
 * Type: Surrogate
 * Version: 1.0.0
 */
package ch.agent.crnickl.api;


/**
 * A Surrogate represents a database object.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public interface Surrogate {

	/**
	 * Applications can rely on this method when needing to display the surrogate in a
	 * user interface or in a report.
	 * 
	 * @return a readable and useful representation of the surrogate
	 */
	String toString();
	
	/**
	 * Test whether the data object is in the database or is a new object being
	 * constructed.
	 * 
	 * @return true if the database object is in construction
	 */
	boolean inConstruction();
	
	/**
	 * Leave the <em>in construction</em> state and take the identity of the
	 * parameter. This is the only modification which can be made to a
	 * surrogate. It is illegal to use this method on a surrogate not in
	 * construction and it is illegal to pass an incompatible surrogate or a
	 * surrogate in construction.
	 * 
	 * @param surrogate a surrogate which will transmit its identity to this surrogate
	 */
	void upgrade(Surrogate surrogate);
	
	/**
	 * Returns the database object corresponding to the surrogate. If the object
	 * cannot be found (for example because it was deleted), the method return
	 * an invalid database object. If the surrogate is in construction, the
	 * method returns null.
	 * 
	 * @return the database object or null
	 */
	DBObject getObject();
	
	/**
	 * Return the database of the database object.
	 * 
	 * @return a database
	 */
	Database getDatabase();
	
	/**
	 * Return a keyword identifying the type of database object of this surrogate.
	 * 
	 * @return a constant value identifying the database object type
	 */
	DBObjectType getDBObjectType();
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Clients can rely on this method to return true when the underlying database
	 * objects are the same. The method returns false when {@link #inConstruction} is true 
	 * for one of the objects involved.
	 * 
	 * @return true if this surrogate corresponds to the same database object as the argument's
	 */
	boolean equals(Object o);
	
	/**
	 * Return the hashCode of this surrogate unless it is in construction. 
	 * When {@link #inConstruction} is true throw an {@link IllegalStateException}.
	 * 
	 * @return the hashCode of this surrogate
	 */
	int hashCode();
}
