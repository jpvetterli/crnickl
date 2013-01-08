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

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.api.Chronicle;
import ch.agent.crnickl.api.Property;
import ch.agent.crnickl.api.Schema;
import ch.agent.crnickl.api.Surrogate;
import ch.agent.crnickl.api.ValueType;

/**
 * A DatabaseCache is used to provide fast access to frequently used chronicles
 * and their schemas. Schema objects are expensive to load from the database.
 * Using this cache greatly improves performance under two easy assumptions:
 * <ol>
 * <li>a large database (millions of chronicles, hundreds of millions of values)
 * requires only a small number of schemas (hundreds), and
 * <li>schemas a very rarely modified.
 * </ol>
 * When a chronicle is stored in the cache, its parent chronicle, its parent's
 * parent, and so on, are also stored. Its schema and all the properties used in
 * the schema are stored. Naturally, all equal objects are stored only once.
 * 
 * @author Jean-Paul Vetterli
 */
public interface DatabaseCache {

	/**
	 * Look up a chronicle in the cache. Return null if not found.
	 * 
	 * @param surrogate a chronicle surrogate
	 * @return a chronicle or null
	 */
	ChronicleImpl lookUpChronicle(Surrogate surrogate);
	
	/**
	 * Look up a chronicle in the cache by name. Return null if not found.
	 * 
	 * @param name a string
	 * @return a chronicle or null
	 */
	ChronicleImpl lookUpChronicle(String name);
	
	/**
	 * Look up a property in the cache by name. If not in the cache
	 * get it from the database. Throw an exception if not found.
	 * This method is meant for accessing "built-in" properties
	 * 
	 * @param name a string
	 * @return a property
	 * @throws T2DBException
	 */
	Property<?> lookUpProperty(String name) throws T2DBException;
	
	/**
	 * Store a chronicle in the cache and return a copy. 
	 * Return null if the chronicle was not kept in the cache for some reason.
	 * 
	 * @param chronicle a chronicle
	 * @return a chronicle or null
	 * @throws T2DBException
	 */
	ChronicleImpl store(ChronicleImpl chronicle) throws T2DBException;
	
	/**
	 * Return the number of chronicles currently in the cache.
	 * 
	 * @return the number of chronicles in the cache
	 */
	int size();
	
	/**
	 * Remove a chronicle from the cache if present. This method must be called
	 * before renaming or deleting a chronicle.
	 * 
	 * @param chronicle
	 *            a chronicle
	 */
	void clear(Chronicle chronicle);
	
	/**
	 * Remove a schema from the cache if present. This method must be called
	 * before renaming or deleting a schema.
	 * 
	 * @param schema
	 *            a schema
	 */
	void clear(Schema schema);
	
	/**
	 * Remove a property from the cache if present. This method must be called
	 * before renaming or deleting a property.
	 * 
	 * @param property
	 *            a property
	 */
	void clear(Property<?> property);
	
	/**
	 * Remove a value type from the cache if present. This method must be called
	 * before modifying or deleting a value type.
	 * 
	 * @param valueType
	 *            a valueType
	 */
	void clear(ValueType<?> valueType);
	
	/**
	 * Clear the cache.
	 */
	void clear();
	
}
