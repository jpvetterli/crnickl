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
 * Type: UpdatableChronicle
 * Version: 1.0.0
 */
package ch.agent.crnickl.api;

import java.util.Collection;

import ch.agent.crnickl.T2DBException;

/**
 * An UpdatableChronicle is a chronicle which can be modified.
 *
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public interface UpdatableChronicle extends Chronicle, Updatable {

	/**
	 * Set the name of the chronicle.
	 * 
	 * @param name a string
	 * @throws T2DBException
	 */
	void setName(String name) throws T2DBException;

	/**
	 * Set the description of the chronicle.
	 * 
	 * @param description a string
	 * @throws T2DBException
	 */
	void setDescription(String description) throws T2DBException;
	
	/**
	 * Set an attribute. The attribute must be defined in the schema. 
	 * 
	 * @param value an attribute
	 * @throws T2DBException
	 */
	void setAttribute(Attribute<?> value) throws T2DBException;
	
	/**
	 * Set the collection of the chronicle. It is only allowed to specify a
	 * chronicle with a schema compatible with the current state of the chronicle.
	 * 
	 * @param collection a chronicle
	 * @throws T2DBException
	 */
	void setCollection(Chronicle collection) throws T2DBException;
	
	/**
	 * Set the schema of the chronicle. It is only allowed to specify 
	 * a schema compatible with the current state of the chronicle.
	 * 
	 * @param schema a schema
	 * @throws T2DBException
	 */
	void setSchema(Schema schema) throws T2DBException;
	
	/**
	 * Delete the chronicle. The operation fails if the chronicle is used as
	 * collection or if it has series. After applying updates {@link #isValid()}
	 * will return false.
	 * 
	 * @throws T2DBException
	 */
	void delete() throws T2DBException;
	
	/**
	 * Create an empty series with the given name. The series must be defined in
	 * the schema.
	 * 
	 * @param name a series name defined in the schema
	 * @return an updatable series
	 * @throws T2DBException
	 */
	<T>UpdatableSeries<T> createSeries(String name) throws T2DBException;
	
	/**
	 * Return the updatable series with the given name if it exists.
	 * The series must be defined in the schema.
	 * 
	 * @param name a series name defined in the schema
	 * @return an updatable series or null
	 * @throws T2DBException
	 */
	<T>UpdatableSeries<T> updateSeries(String name) throws T2DBException;

	/**
	 * Return a new chronicle to be created in this chronicle. The name must not
	 * be in use. The name may optionally be tweaked to satisfy this requirement
	 * (and to obey rules of the prevailing name syntax). The attributes must be
	 * defined in the schema. If no schema is specified, the schema of this
	 * chronicle will apply. If this chronicle has no schema, then the first
	 * schema along the chronicle chain will apply. On the other hand, if a
	 * schema is specified, it completely overrides any current schema.
	 * The client must execute {@link Updatable#applyUpdates()} on the result.
	 * 
	 * @param name
	 *            a name
	 * @param tweakName
	 *            if true, the name can be tweaked if required
	 * @param description
	 *            a description
	 * @param attributes
	 *            a collection of attributes
	 * @param schema
	 *            a schema or null
	 * @return an updatable chronicle
	 * @throws T2DBException
	 */
	UpdatableChronicle createChronicle(String name, boolean tweakName, String description, Collection<Attribute<?>> attributes, Schema schema) throws T2DBException;

}
