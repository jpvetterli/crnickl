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
 */
package ch.agent.crnickl.impl;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.api.AttributeDefinition;
import ch.agent.crnickl.api.SeriesDefinition;
import ch.agent.crnickl.api.UpdatableSchema;

/**
 * UpdatableSchemaVisitor defines a visitor pattern for schema updates.
 * Callback methods are invoked for each chronicle attribute definition,
 * series definition, and series attribute definition, which
 * was deleted or updated.
 * 
 * @author Jean-Paul Vetterli
 */
public interface UpdatableSchemaVisitor {

	/**
	 * Visit a series definition. The new and old definitions are passed as
	 * arguments. The old definition is null when creating and the new
	 * definition is null when deleting. All other cases are updates. The method
	 * is never called with two null arguments.
	 * 
	 * @param schema
	 *            the schema to which the definitions belong
	 * @param def
	 *            the new definition, or null if deleted
	 * @param original
	 *            the old definition, or null if a new definition is created
	 * @throws T2DBException
	 */
	void visit(UpdatableSchema schema, SeriesDefinition def, SeriesDefinition original)	throws T2DBException;

	/**
	 * Visit an attribute definition of a chronicle or a series. The new and old
	 * definitions are passed as arguments. The old definition is null when
	 * creating and the new definition is null when deleting. All other cases
	 * are updates. The method is never called with two null arguments.
	 * 
	 * @param schema
	 *            the schema to which the definitions belong
	 * @param seriesDef
	 *            a series definition, or null for a chronicle attribute
	 * @param attrDef
	 *            the new attribute definition, or null if deleted
	 * @param origAttrDef
	 *            the old attribute, or null if a new definition is created
	 * @throws T2DBException
	 */
	void visit(UpdatableSchema schema, SeriesDefinition seriesDef, AttributeDefinition<?> attrDef, AttributeDefinition<?> origAttrDef) throws T2DBException;
	
}
