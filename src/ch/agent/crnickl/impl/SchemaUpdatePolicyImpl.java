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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.T2DBMsg;
import ch.agent.crnickl.T2DBMsg.D;
import ch.agent.crnickl.api.AttributeDefinition;
import ch.agent.crnickl.api.Schema;
import ch.agent.crnickl.api.SeriesDefinition;
import ch.agent.crnickl.api.Surrogate;
import ch.agent.crnickl.api.UpdatableSchema;

/**
 * Default implementation of {@link SchemaUpdatePolicy}.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public class SchemaUpdatePolicyImpl implements SchemaUpdatePolicy {

	private DatabaseBackend database;

	/**
	 * Construct a {@link SchemaUpdatePolicy}.
	 * 
	 * @param database a database
	 */
	public SchemaUpdatePolicyImpl(DatabaseBackend database) {
		this.database = database;
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
		schemas.clear();
		schemas.add(schema);
		Collection<Surrogate> entities = database.findChronicles(schemas);
		if (entities.size() > 0)
			throw T2DBMsg.exception(D.D30141, schema.getName(), entities.size());
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
	public void willUpdate(UpdatableSchema s) throws T2DBException {
		UpdatableSchemaImpl schema = (UpdatableSchemaImpl) s;
		UpdatableSchema base = schema.getBase();
		UpdatableSchema previousBase = schema.getPreviousBase();
		boolean baseEdited = false;
		if (base != null)
			baseEdited = !base.equals(previousBase);
		else if (previousBase != null)
			baseEdited = true;
		if (baseEdited) {
			boolean otherEdits = schema.getDeletedAttributeDefinitions().size()
					+ schema.getEditedAttributeDefinitions().size()
					+ schema.getDeletedSeriesDefinitions().size()
					+ schema.getEditedSeriesDefinitions().size() > 0;
			if (otherEdits)
				throw T2DBMsg.exception(D.D30139, schema.getName());
			willUpdateBase(schema);
		}
	}

	/**
	 * Invoke only when base and edited base differ.
	 */
	private void willUpdateBase(UpdatableSchema s) throws T2DBException {
		UpdatableSchemaImpl schema = (UpdatableSchemaImpl) s;
		String baseSchemaName = schema.getBase() == null ? null : schema.getBase().getName();
		UpdatableSchema currentUES = new UpdatableSchemaImpl(schema.getName(), 
				schema.getPreviousBase(), schema.getAttributeDefinitions(), schema.getSeriesDefinitions(), 
				schema.getSurrogate());
		UpdatableSchema editedUES = new UpdatableSchemaImpl(schema.getName(), 
				schema.getBase(), schema.getAttributeDefinitions(), schema.getSeriesDefinitions(), 
				schema.getSurrogate());
		Schema current = database.resolve(currentUES);
		Schema edited = database.resolve(editedUES);
		if (current.getAttributeDefinitions().size() > edited.getAttributeDefinitions().size())
			throw T2DBMsg.exception(D.D30142, baseSchemaName);
		if (current.getSeriesDefinitions().size() > edited.getSeriesDefinitions().size())
			throw T2DBMsg.exception(D.D30143, baseSchemaName);
		for (AttributeDefinition<?> def : current.getAttributeDefinitions()) {
			if (!def.equals(edited.getAttributeDefinition(def.getNumber(), false)))
				throw T2DBMsg.exception(D.D30144, baseSchemaName, def.getNumber());
		}
		for (SeriesDefinition ss : current.getSeriesDefinitions()) {
			for (AttributeDefinition<?> def : current.getAttributeDefinitions()) {
				if (!def.equals(edited.getAttributeDefinition(def.getNumber(), false)))
					throw T2DBMsg.exception(D.D30145, baseSchemaName, def.getNumber(), ss.getNumber());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * An attribute definition can be deleted if it is not in use by any chronicle.
	 * It is in use if there is at least one chronicle with a non-default value for the
	 * attribute. Such values are in an attribute value table keyed by chronicle and property.
	 * But the property corresponding to the attribute definition is not unique to the schema
	 * being inspected here. So the check must only take into account chronicles having this schema
	 * or having a schema directly or indirectly extending this schema.
	 */
	@Override
	public void willDelete(UpdatableSchema schema, AttributeDefinition<?> def) throws T2DBException {
		
		Set<UpdatableSchema> schemas = findDependentSchemas(schema);
		// remove those schemas where the attribute was "erased"
		Iterator<UpdatableSchema> it = schemas.iterator();
		while(it.hasNext()) {
			UpdatableSchema s = it.next();
			if (s.getAttributeDefinition(def.getNumber(), false) == null)
				it.remove();
		}
		
		Collection<Surrogate> entities = database.findChronicles(def.getProperty(), schemas);
		if (entities.size() > 0)
			throw T2DBMsg.exception(D.D30146, def.getNumber(), schema.getName(), entities.size());
	}

	private Set<UpdatableSchema> findDependentSchemas(UpdatableSchema schema) throws T2DBException {
		// beware of cycles
		Collection<UpdatableSchema> schemas = database.getUpdatableSchemas("*");
		Map<String, UpdatableSchema> bases = new HashMap<String, UpdatableSchema>(schemas.size());
		for (UpdatableSchema s : schemas) {
			bases.put(s.getName(), s.getBase());
		}
		String name = schema.getName();
		Set<UpdatableSchema> result = new HashSet<UpdatableSchema>();
		result.add(schema);
		Set<String> cycleDetector = new LinkedHashSet<String>();
		for (UpdatableSchema s : schemas) {
			UpdatableSchema base = s.getBase();
			cycleDetector.clear();
			cycleDetector.add(s.getName());
			while (base != null) {
				if (base.getName().equals(name)) {
					if (!cycleDetector.add(base.getName()))
						throw T2DBMsg.exception(D.D30147, schema.getName(), cycleDetector.toString());
					result.add(s);
					break;
				} else
					base = base.getBase();
			}
		}
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Updating the default value is always allowed. No other update allowed. 
	 * This policy is implemented in the lower layer.
	 */
	@Override
	public void willUpdate(UpdatableSchema schema, AttributeDefinition<?> def) throws T2DBException {
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * A series definition can be deleted only if there are no series using it.
	 */
	@Override
	public void willDelete(UpdatableSchema schema, SeriesDefinition ss) throws T2DBException {
		Set<UpdatableSchema> schemas = findDependentSchemas(schema);
		// remove those schemas where the series was "erased"
		Iterator<UpdatableSchema> it = schemas.iterator();
		while(it.hasNext()) {
			UpdatableSchema s = it.next();
			if (s.getSeriesDefinition(ss.getNumber(), false) == null)
				it.remove();
		}
		
		Collection<Surrogate> entities = database.findChronicles(ss, schemas);
		if (entities.size() > 0)
			throw T2DBMsg.exception(D.D30150, ss.getNumber(), schema.getName(), entities.size());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Reject the deletion of a built-in series attribute.
	 */
	@Override
	public void willDelete(UpdatableSchema schema, SeriesDefinition ss, AttributeDefinition<?> def) throws T2DBException {
		if (database.isBuiltIn(def))
			throw T2DBMsg.exception(D.D30148, def.getNumber(), ss.getNumber(), schema.getName()); 
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Reject the update of a built-in series attribute, unless it's the series name. 
	 * 
	 */
	@Override
	public void willUpdate(UpdatableSchema schema, SeriesDefinition ss, AttributeDefinition<?> def) throws T2DBException {
		if (database.isBuiltIn(def) && def.getNumber() != DatabaseBackend.MAGIC_NAME_NR)
			throw T2DBMsg.exception(D.D30149, def.getNumber(), ss.getNumber(), schema.getName()); 
	}

}
