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
 * Type: SchemaUpdatePolicyImpl
 * Version: 1.0.0
 */
package ch.agent.crnickl.impl;

import java.util.Collection;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.T2DBMsg;
import ch.agent.crnickl.T2DBMsg.D;
import ch.agent.crnickl.api.AttributeDefinition;
import ch.agent.crnickl.api.Chronicle;
import ch.agent.crnickl.api.Property;
import ch.agent.crnickl.api.Schema;
import ch.agent.crnickl.api.SeriesDefinition;
import ch.agent.crnickl.api.Surrogate;
import ch.agent.crnickl.api.UpdatableSchema;
import ch.agent.crnickl.api.ValueType;

/**
 * Default implementation of {@link SchemaUpdatePolicy}.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public class SchemaUpdatePolicyImpl implements SchemaUpdatePolicy {

	private static class Visitor implements UpdatableSchemaVisitor {

		private SchemaUpdatePolicyImpl policy;

		private Visitor(SchemaUpdatePolicyImpl policy) {
			this.policy = policy;
		}

		@Override
		public void visit(UpdatableSchema schema, SeriesDefinition def,	SeriesDefinition original) throws T2DBException {
			if (def == null) {
				if (!original.isErasing())
					policy.willDeleteOrErase(schema, original);
				// deleting an erasing series is like adding the series
			} else {
				if (def.isErasing()) {
					if (original == null || !original.isErasing())
						policy.willDeleteOrErase(schema, def);
				} else {
					// schema should have been resolved so don't check for completeness
				}
			}
		}

		@Override
		public void visit(UpdatableSchema schema, SeriesDefinition seriesDef,
			AttributeDefinition<?> attrDef, AttributeDefinition<?> origAttrDef)
			throws T2DBException {
			if (attrDef == null) {
				if (!origAttrDef.isErasing())
					policy.willDeleteOrErase(schema, seriesDef, origAttrDef);
				// deleting an erasing attribute is like adding the series
			} else {
				if (attrDef.isErasing()) {
					Schema resolved = schema.resolve();
					AttributeDefinition<?> def = resolved.getAttributeDefinition(attrDef.getNumber(), false);
					if (def != null)
						policy.willDeleteOrErase(schema, seriesDef, def);
				} else {
					// schema should have been resolved so don't check for completeness
					if (origAttrDef != null)
						policy.willUpdate(schema, seriesDef, attrDef);
				}
			}
		}
	}

	private DatabaseBackend database;
	private UpdatableSchemaVisitor visitor;

	/**
	 * Construct a {@link SchemaUpdatePolicy}.
	 * 
	 * @param database a database
	 */
	public SchemaUpdatePolicyImpl(DatabaseBackend database) {
		this.database = database;
		this.visitor = new Visitor(this);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * A schema cannot be deleted if it serves as the base of another schema
	 * or if it is used by a chronicle. 
	 */
	@Override
	public void willDelete(UpdatableSchema schema) throws T2DBException {
		// (1) cannot delete if schema used as a base for another schema
		Collection<UpdatableSchema> schemas = database.getUpdatableSchemas("*");
		int count = 0;
		for (UpdatableSchema s : schemas) {
			if (schema.equals(s.getBase()))
				count++;
		}
		if (count > 0)
			throw T2DBMsg.exception(D.D30140, schema.getName(), count);
		
		// (2) cannot delete if schema explicitly referenced by an entity
		Surrogate entity = database.findChronicle(schema);
		if (entity != null) {
			throw T2DBMsg.exception(D.D30141, schema.getName(), chronicleName(entity));
		}
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * It is not allowed to update the base schema of a schema and its
	 * attributes or series at the same time. It is allowed to modify the base
	 * of a schema if the new resulting schema has all the attributes and all
	 * the series of the old schema. It can have additional attributes,
	 * additional series, and additional series attributes.
	 */
	@Override
	public void willUpdate(UpdatableSchema schema) throws T2DBException {
		try {
			((UpdatableSchemaImpl) schema).traverse(true, visitor);
		} catch (T2DBException e) {
			throw T2DBMsg.exception(e, D.D30105, schema.getName());
		}
	}

	/**
	 * A series definition can be deleted only if there are no series using it.
	 */
	private void willDeleteOrErase(UpdatableSchema schema, SeriesDefinition ss) throws T2DBException {
		Surrogate entity = database.findChronicle(ss, schema);
		if (entity != null)
			throw T2DBMsg.exception(D.D30150, ss.getNumber(), schema.getName(), chronicleName(entity));
	}
	
	/**
	 * Reject the deletion of a built-in series attribute. Other series
	 * attributes can always be deleted because they only have a default value,
	 * in the schema.
	 * <p>
	 * A chronicle attribute definition can be deleted if it is not in use by
	 * any chronicle. It is in use if there is at least one chronicle with a
	 * non-default value for the attribute. Such values are in an attribute
	 * value table keyed by chronicle and property. But the property
	 * corresponding to the attribute definition is not unique to the schema
	 * being inspected here. So the check must only take into account chronicles
	 * having this schema or having a schema directly or indirectly extending
	 * this schema.
	 */
	private void willDeleteOrErase(UpdatableSchema schema, SeriesDefinition ss, AttributeDefinition<?> def) throws T2DBException {
		if (ss != null) { 
			if (database.isBuiltIn(def))
				throw T2DBMsg.exception(D.D30148, def.getNumber(), ss.getNumber(), schema.getName()); 
		} else {
			Surrogate entity = database.findChronicle(def.getProperty(), schema);
			if (entity != null)
				throw T2DBMsg.exception(D.D30146, def.getNumber(), schema.getName(), chronicleName(entity));
		}
	}

	/**
	 * Reject the update of a built-in series attribute, unless it's the series name. 
	 * For chronicle attributes, there is no restriction.
	 * 
	 */
	private void willUpdate(UpdatableSchema schema, SeriesDefinition ss, AttributeDefinition<?> def) throws T2DBException {
		if (ss != null) { 
			if (database.isBuiltIn(def) && def.getNumber() != DatabaseBackend.MAGIC_NAME_NR)
				throw T2DBMsg.exception(D.D30149, def.getNumber(), ss.getNumber(), schema.getName()); 
		}
	}

	@Override
	public <T> void willDelete(Property<T> property) throws T2DBException {
	}

	@Override
	public <T> void willDelete(ValueType<T> valueType) throws T2DBException {
	}

	@Override
	public <T> void willDelete(ValueType<T> valueType, T value)	throws T2DBException {
	}
	
	private String chronicleName(Surrogate entity) {
		try {
			Chronicle c = entity.getDatabase().getChronicle(entity);
			return c.getName(true);
		} catch (T2DBException e) {
			return entity.toString();
		}
	}

}
