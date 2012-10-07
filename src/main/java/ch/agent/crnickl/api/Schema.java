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
 * Type: Schema
 * Version: 1.0.0
 */
package ch.agent.crnickl.api;

import java.util.Collection;

import ch.agent.crnickl.T2DBException;

/**
 * A Schema defines the structure of a {@link Chronicle}. 
 * It could have been named "ChronicleDefinition" but because of its importance,
 * a simpler name was preferred.
 * A schema defines a number of {@link Attribute}s and a number
 * of {@link Series}. Series can themselves have attributes, as defined
 * in their {@link SeriesDefinition}.
 * <p>
 * A schema can extend, override, or erase elements of another schema. Because of this,
 * schemas form a tree structure (more precisely a forest). The base 
 * schema, if any, is only accessible from the {@link UpdatableSchema},
 * which can be obtained with the {@link #edit()} method.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public interface Schema extends IncompleteSchema, DBObject {

	/**
	 * Return true if the schema is complete. A schema is complete if all its
	 * components are complete. When a schema is complete it can be used in a
	 * chronicle. See also {@link SchemaComponent#isComplete()}.
	 * 
	 * @return true if the schema is complete
	 */
	boolean isComplete();
	
	/**
	 * Return an {@link UpdatableSchema} corresponding to this schema. 
	 * Successfully getting an {@link Updatable} object does not imply
	 * that any update can be successfully applied.
	 * 
	 * @return an updatable schema corresponding to this schema
	 */
	UpdatableSchema edit();
	
	/**
	 * Return all series definitions of this schema.
	 * 
	 * @return the collection of series definitions
	 */
	Collection<SeriesDefinition> getSeriesDefinitions();
	
	/**
	 * Return the series definition with the given series name. If there is no
	 * such definition, return null or throw an exception, depending on the
	 * value of the <code>mustExist</code> parameter.
	 * 
	 * @param name
	 *            the series defined
	 * @param mustExist
	 *            if true throw an exception instead of returning null
	 * @return a series definition or null (only when mustExist is false)
	 * @throws T2DBException
	 */
	SeriesDefinition getSeriesDefinition(String name, boolean mustExist)
			throws T2DBException;
	
	/**
	 * Return the series definition with the given series number. If there is no
	 * such definition, return null or throw an exception, depending on the
	 * value of the <code>mustExist</code> parameter.
	 * 
	 * @param number
	 *            the series defined
	 * @param mustExist
	 *            if true throw an exception instead of returning null
	 * @return a series definition or null (only when mustExist is false)
	 * @throws T2DBException
	 */
	SeriesDefinition getSeriesDefinition(int number, boolean mustExist) throws T2DBException;
	
	/**
	 * Return true if the schema depends on the schema specified.
	 * 
	 * @param schema a schema
	 * @return true if the schema inherits from a given schema
	 */
	boolean dependsOnSchema(Schema schema);

}
