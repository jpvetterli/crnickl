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
 * Type: UpdatableSchemaImpl
 * Version: 1.0.0
 */
package ch.agent.crnickl.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.T2DBMsg;
import ch.agent.crnickl.T2DBMsg.D;
import ch.agent.crnickl.api.AttributeDefinition;
import ch.agent.crnickl.api.DBObjectType;
import ch.agent.crnickl.api.Property;
import ch.agent.crnickl.api.Schema;
import ch.agent.crnickl.api.SeriesDefinition;
import ch.agent.crnickl.api.Surrogate;
import ch.agent.crnickl.api.UpdatableSchema;
import ch.agent.crnickl.api.ValueType;
import ch.agent.t2.time.TimeDomain;

/**
 * Default implementation of {@link UpdatableSchema}.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public class UpdatableSchemaImpl extends SchemaImpl implements UpdatableSchema {

	private boolean delete;
	private UpdatableSchema base;
	private String editedName;
	private UpdatableSchema editedBase;
	private boolean editedBaseSet;
	
	/**
	 * Construct an {@link UpdatableSchema}.
	 * 
	 * @param name a string
	 * @param base a base schema
	 * @param attributeDefs a collection of attribute definitions
	 * @param seriesDefinitions a collection of series definitions
	 * @param surrogate a surrogate
	 */
	public UpdatableSchemaImpl(String name, UpdatableSchema base, 
			Collection<AttributeDefinition<?>> attributeDefs,
			Collection<SeriesDefinition> seriesDefinitions, Surrogate surrogate) {
		super(true, name, attributeDefs, seriesDefinitions, surrogate, null);
		this.base = base;
	}

	@Override
	public void applyUpdates() throws T2DBException {
		if (delete) {
			// cache already cleared
			getDatabase().delete(this);
			delete = false;
		} else {
			if (getSurrogate().inConstruction()) {
				getDatabase().create(this);
				if (editedBaseSet)
					base = editedBase;
				editedBaseSet = false;
				editedName = null;
				editedBase = null;
			} else
				getDatabase().getCache().clear(this);
			getDatabase().update(this);
			update();
		}
	}
	
	@Override
	protected void update() throws T2DBException {
		super.update();
		if (editedBaseSet)
			base = editedBase;
		editedBaseSet = false;
		editedBase = null;
		editedName = null;
		getAttributeDefinitionsObject().consolidate();
		getSeriesDefinitionsObject().consolidate();
	}

	@Override
	public UpdatableSchema edit() {
		return this;
	}

	@Override
	public void delete() throws T2DBException {
		delete = true;
	}
	
	@Override
	public String getName() {
		if (editedName != null)
			return editedName;
		else
			return super.getName();
	}
	
	@Override
	public void setName(String name) throws T2DBException {
		if (name != null && name.length() == 0)
			name = null;
		this.editedName = name;
	}

	@Override
	public UpdatableSchema getBase() {
		if (editedBase != null)
			return editedBase;
		else
			return base;
	}

	/**
	 * Return the base schema before it was modified with 
	 * {@link #setBase(UpdatableSchema)}. 
	 * Return the same result as {@link #getBase} if the base schema was not modified.
	 * 
	 * @return a schema
	 */
	public UpdatableSchema getPreviousBase() {
		return base;
	}
	
	@Override
	public void setBase(UpdatableSchema base) throws T2DBException {
		this.editedBase = base;
	}
	
	@Override
	public SeriesDefinition addSeries(int seriesNr) throws T2DBException {
		return addSeries(seriesNr, false);
	}
	
	private SeriesDefinition addSeries(int seriesNr, boolean merging) throws T2DBException {
		SeriesDefinition ss = new SeriesDefinitionImpl(seriesNr, null, null);
		boolean added = getSeriesDefinitionsObject().addComponent(ss);
		if (!(added || merging))
			throw T2DBMsg.exception(D.D30124, seriesNr, this);
		return ss;
	}

	@Override
	public void deleteSeries(int seriesNr) throws T2DBException {
		getSeriesDefinitionsObject().deleteComponent(seriesNr);
	}

	@Override
	public void eraseSeries(int seriesNr) throws T2DBException {
		startEditingSeriesSchema(seriesNr).setErasing(true);
	}

	@Override
	public void setSeriesDescription(int seriesNr, String description) throws T2DBException {
		editSeriesAttributeDefinition(seriesNr, DatabaseBackend.MAGIC_NAME_NR, true);
		startEditingSeriesSchema(seriesNr).setDescription(description);	
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setSeriesName(int seriesNr, String name) throws T2DBException {
		AttributeDefinitionImpl<String> def = (AttributeDefinitionImpl<String>) 
			editSeriesAttributeDefinition(seriesNr, DatabaseBackend.MAGIC_NAME_NR, false);
		if (def == null) {
			Property<String> property = (Property<String>) getBuiltInProperty(DatabaseBackend.MAGIC_NAME_NR);
			def = (AttributeDefinitionImpl<String>) addAttribute(seriesNr, DatabaseBackend.MAGIC_NAME_NR);
			def.setProperty(property);
		}
		def.setValue(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setSeriesType(int seriesNr, ValueType<?> type) throws T2DBException {
		AttributeDefinitionImpl<ValueType<?>> def = (AttributeDefinitionImpl<ValueType<?>>) 
			editSeriesAttributeDefinition(seriesNr, DatabaseBackend.MAGIC_TYPE_NR, false);
		if (def == null) {
			Property<ValueType<?>> property = (Property<ValueType<?>>) getBuiltInProperty(DatabaseBackend.MAGIC_TYPE_NR);
			def = (AttributeDefinitionImpl<ValueType<?>>) addAttribute(seriesNr, DatabaseBackend.MAGIC_TYPE_NR);
			def.setProperty(property);
		}
		def.setValue(type);
	}

	@Override
	public void setSeriesType(int seriesNr, String type) throws T2DBException {
		DatabaseBackend db = ((SurrogateImpl)getSurrogate()).getDatabase();
		Surrogate surrogate = new SurrogateImpl(db, DBObjectType.VALUE_TYPE, DatabaseBackend.MAGIC_TYPE_NR);
		@SuppressWarnings({ "rawtypes" })
		ValueType<ValueType> vt = db.getValueType(surrogate);
		setSeriesType(seriesNr, vt.scan(type));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setSeriesTimeDomain(int seriesNr, TimeDomain timeDomain) throws T2DBException {
		AttributeDefinitionImpl<TimeDomain> def = (AttributeDefinitionImpl<TimeDomain>) 
			editSeriesAttributeDefinition(seriesNr, DatabaseBackend.MAGIC_TIMEDOMAIN_NR, false);
		if (def == null) {
			Property<TimeDomain> property = (Property<TimeDomain>) getBuiltInProperty(DatabaseBackend.MAGIC_TIMEDOMAIN_NR);
			def = (AttributeDefinitionImpl<TimeDomain>) addAttribute(seriesNr, DatabaseBackend.MAGIC_TIMEDOMAIN_NR);
			def.setProperty(property);
		}
		def.setValue(timeDomain);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setSeriesSparsity(int seriesNr, boolean sparse)	throws T2DBException {
		AttributeDefinitionImpl<Boolean> def = (AttributeDefinitionImpl<Boolean>) 
				editSeriesAttributeDefinition(seriesNr, DatabaseBackend.MAGIC_SPARSITY_NR, false);
			if (def == null) {
				Property<Boolean> property = (Property<Boolean>) getBuiltInProperty(DatabaseBackend.MAGIC_SPARSITY_NR);
				def = (AttributeDefinitionImpl<Boolean>) addAttribute(seriesNr, DatabaseBackend.MAGIC_SPARSITY_NR);
				def.setProperty(property);
			}
			def.setValue(sparse);
	}

	/**
	 * Return the collection of edited series definitions.
	 * 
	 * @return the collection of edited series definitions
	 */
	public Collection<SeriesDefinition> getEditedSeriesDefinitions() {
		return getSeriesDefinitionsObject().getEditedComponents();
	}

	/**
	 * Return the collection of deleted series definitions.
	 * 
	 * @return the collection of deleted series definitions
	 */
	public Set<Integer> getDeletedSeriesDefinitions() {
		return getSeriesDefinitionsObject().getDeletedComponents();
	}

	@Override
	public AttributeDefinition<?> addAttribute(int attrNr) throws T2DBException {
		return addAttribute(attrNr, false);
	}
	
	private AttributeDefinition<?> addAttribute(int attrNr, boolean merging) throws T2DBException {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		AttributeDefinition<?> def = new AttributeDefinitionImpl(attrNr, null, null);
		boolean added = getAttributeDefinitionsObject().addComponent(def);
		if (!(added || merging))
			throw T2DBMsg.exception(D.D30127, attrNr, this);
		return def;
	}

	@Override
	public void deleteAttribute(int attrNr) throws T2DBException {
		getAttributeDefinitionsObject().deleteComponent(attrNr);
	}

	@Override
	public void eraseAttribute(int attrNr) throws T2DBException {
		editAttributeDefinition(attrNr).setErasing(true);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T>void setAttributeProperty(int attrNr, Property<T> property) throws T2DBException {
		editAttributeDefinition(attrNr).setProperty((Property)property);
	}

	@Override
	public <T>void setAttributeDefault(int attrNr, T defaultValue) throws T2DBException {
		editAttributeDefinition(attrNr).setValue(defaultValue);
	}
	
	@Override
	public AttributeDefinition<?> addAttribute(int seriesNr, int attrNr) throws T2DBException {
		return addAttribute(seriesNr, attrNr, false);
	}
	
	private AttributeDefinition<?> addAttribute(int seriesNr, int attrNr, boolean merging) throws T2DBException {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		AttributeDefinition<?> def = new AttributeDefinitionImpl(attrNr, null, null);
		boolean added = startEditingSeriesSchema(seriesNr).getAttributeDefinitionsObject().addComponent(def);
		if (!(added || merging))
			throw T2DBMsg.exception(D.D30118, attrNr, seriesNr, this);
		return def;
	}

	@Override
	public void deleteAttribute(int seriesNr, int attrNr) throws T2DBException {
		startEditingSeriesSchema(seriesNr).getAttributeDefinitionsObject().deleteComponent(attrNr);
	}

	@Override
	public void eraseAttribute(int seriesNr, int attrNr) throws T2DBException {
		editSeriesAttributeDefinition(seriesNr, attrNr, true).setErasing(true);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void setAttributeProperty(int seriesNr, int attrNr,	Property<?> property) throws T2DBException {
		editSeriesAttributeDefinition(seriesNr, attrNr, true).setProperty((Property) property);
	}

	@Override
	public void setAttributeDefault(int seriesNr, int attrNr, Object defaultValue) throws T2DBException {
		editSeriesAttributeDefinition(seriesNr, attrNr, true).setValue(defaultValue);
	}

	/**
	 * Return the collection of edited series attribute definitions.
	 * 
	 * @param seriesNr a series number
	 * @return the collection of edited series attribute definitions
	 * @throws T2DBException
	 */
	public Collection<AttributeDefinition<?>> getEditedAttributeDefinitions(int seriesNr) throws T2DBException {
		// note: only edited schemas have edited attribute definitions
		SeriesDefinitionImpl schema = (SeriesDefinitionImpl) findEditedSeriesSchema(seriesNr);
		return schema == null ?	new ArrayList<AttributeDefinition<?>>() : 
			schema.getAttributeDefinitionsObject().getEditedComponents();
	}

	/**
	 * Return the collection of deleted series attribute definitions.
	 * 
	 * @param seriesNr a series number
	 * @return the collection of deleted series attribute definitions
	 * @throws T2DBException
	 */
	public Set<Integer> getDeletedAttributeDefinitions(int seriesNr) throws T2DBException {
		// note: only edited schemas have deleted attribute definitions
		SeriesDefinitionImpl schema = (SeriesDefinitionImpl) findEditedSeriesSchema(seriesNr);
		return schema == null ? new HashSet<Integer>() : 
			schema.getAttributeDefinitionsObject().getDeletedComponents();
	}

	/**
	 * Return the collection of edited attribute definitions.
	 * 
	 * @return the collection of edited attribute definitions
	 * @throws T2DBException
	 */
	public Collection<AttributeDefinition<?>> getEditedAttributeDefinitions() {
		return getAttributeDefinitionsObject().getEditedComponents();
	}

	/**
	 * Return the collection of deleted attribute definitions.
	 * 
	 * @return the collection of deleted attribute definitions
	 * @throws T2DBException
	 */
	public Set<Integer> getDeletedAttributeDefinitions() {
		return getAttributeDefinitionsObject().getDeletedComponents();
	}

	/**
	 * Find an edited series definition.
	 * 
	 * @param seriesNr a series number
	 * @return an edited series definition 
	 * @throws T2DBException
	 */
	protected SeriesDefinition findEditedSeriesSchema(int seriesNr) throws T2DBException {
		SeriesDefinition schema = null;
		for (SeriesDefinition ss : this.getEditedSeriesDefinitions()) {
			if (ss.getNumber() == seriesNr) {
				schema = ss;
			}
		}
		return schema;
	}
	
	/**
	 * Start editing a series definition.
	 * 
	 * @param seriesNr a series number
	 * @return a series definition 
	 * @throws T2DBException
	 */
	protected SeriesDefinitionImpl startEditingSeriesSchema(int seriesNr) throws T2DBException {
		SeriesDefinitionImpl sch = (SeriesDefinitionImpl) getSeriesDefinitionsObject().edit(seriesNr);
		if (sch == null)
			throw T2DBMsg.exception(D.D30125, seriesNr, getName());
		return sch;
	}
	
	/**
	 * Start editing an attribute definition.
	 * 
	 * @param attrNr an attribute number
	 * @return an attribute definition
	 * @throws T2DBException
	 */
	protected AttributeDefinitionImpl<?> editAttributeDefinition(int attrNr) throws T2DBException {
		AttributeDefinitionImpl<?> def = (AttributeDefinitionImpl<?>) getAttributeDefinitionsObject().edit(attrNr);
		if (def == null)
			throw T2DBMsg.exception(D.D30123, attrNr, getName());
		return def;
	}

	/**
	 * Start editing a series attribute definition.
	 * @param seriesNr a series number
	 * @param attrNr an attribute number
	 * @param mustExist if true throw an exception instead of returning null
	 * @return an attribute definition or null if there is no such attribute definition
	 * @throws T2DBException
	 */
	protected AttributeDefinitionImpl<?> editSeriesAttributeDefinition(int seriesNr, int attrNr, boolean mustExist) throws T2DBException {
		AttributeDefinitionImpl<?> def = (AttributeDefinitionImpl<?>) startEditingSeriesSchema(seriesNr).getAttributeDefinitionsObject().edit(attrNr);
		if (def == null && mustExist)
			throw T2DBMsg.exception(D.D30126, attrNr, seriesNr, getName());
		return def;
	}
	
//	private void dump(String ASUD, Integer number, PrintStream out) throws T2DBException {
//		out.println(String.format("  %s #%d", ASUD, number));
//	}
//	
//	private void dump(String ASUD, AttributeDefinition<?> def, PrintStream out) throws T2DBException {
//		if (def.isErasing())
//			out.println(String.format("  %s #%d *ERASING*", ASUD, def.getNumber()));
//		else
//			out.println(String.format("  %s #%d %s=%s", ASUD, def.getNumber(), def.getName(), def.getValue()));
//	}
//	
//	private void dump(String ASUD, SeriesDefinitionImpl ss, PrintStream out) throws T2DBException {
//		if (ss.isErasing())
//			out.println(String.format("  %s #%d *ERASING*", ASUD, ss.getNumber()));
//		else {
//			out.println(String.format("  %s #%d", ASUD, ss.getNumber()));
//			if (ss.getName() != null)
//				out.println(String.format("    Name=%s", ss.getName())); 
//			if (ss.getDescription() != null)
//				out.println(String.format("    Description=%s", ss.getDescription())); 
//			if (ss.getValueType() != null)
//				out.println(String.format("    Type=%s", ss.getValueType())); 
//			if (ss.getTimeDomain() != null)
//				out.println(String.format("    TimeDomain=%s", ss.getTimeDomain().getLabel())); 
//			
//			for (AttributeDefinition<?> def : ss.getAttributeDefinitions()) {
//				dump("  A ", def, out);
//			}
//			for (AttributeDefinition<?> def : this.getEditedAttributeDefinitions(ss.getNumber())) {
//				dump("  AE", def, out);
//			}
//			for (Integer def : this.getDeletedAttributeDefinitions(ss.getNumber())) {
//				dump("  AD", def, out);
//			}
//		}
//	}
//	
//	@Override
//	public void dump(PrintStream out) throws T2DBException {
//		out.println(String.format("%s %s (ready: %b)", this.getName(), 
//				getSurrogate().inConstruction() ? "*IN CONSTRUCTION*" : getSurrogate().toString(), 
//				isComplete()));
//		out.println("  Extends: " + (this.getBase() == null ? " - " : this.getBase()));
//		for (AttributeDefinition<?> def : this.getAttributeDefinitions()) {
//			dump("A ", def, out);
//		}
//		for (AttributeDefinition<?> def : this.getEditedAttributeDefinitions()) {
//			dump("AE", def, out);
//		}
//		for (Integer def : this.getDeletedAttributeDefinitions()) {
//			dump("AD", def, out);
//		}
//
//		for (SeriesDefinition ss : this.getSeriesDefinitions()) {
//			dump("S ", (SeriesDefinitionImpl) ss, out);
//		}
//		for (SeriesDefinition ss : this.getEditedSeriesDefinitions()) {
//			dump("SE", (SeriesDefinitionImpl) ss, out);
//		}
//		for (Integer number : this.getDeletedSeriesDefinitions()) {
//			dump("SD", number, out);
//		}
//
//	}

	private Property<?> getBuiltInProperty(int magicNumber) throws T2DBException {
		SurrogateImpl thisKey = (SurrogateImpl) getSurrogate();
		Surrogate propKey = new SurrogateImpl(thisKey.getDatabase(), DBObjectType.PROPERTY, magicNumber);
		return thisKey.getDatabase().getProperty(propKey);
	}
	
	/**
	 * Merge a schema into this schema.
	 * 
	 * @param schema a schema
	 * 
	 * @throws T2DBException
	 */
	public void merge(UpdatableSchema schema) throws T2DBException {
		for (AttributeDefinition<?> def : schema.getAttributeDefinitions()) {
			int dn = def.getNumber();
			if (def.isErasing())
				deleteAttribute(dn);
			else {
				addAttribute(dn, true);
				if (def.getProperty() != null)
					setAttributeProperty(dn, def.getProperty());
				if (def.getValue() != null)
					setAttributeDefault(dn, def.getValue());
			}
		}
		
		for (SeriesDefinition ss : schema.getSeriesDefinitions()) {
			int sn = ss.getNumber();
			if (ss.isErasing())
				deleteSeries(sn);
			else {
				addSeries(sn, true);
				for (AttributeDefinition<?> def : ss.getAttributeDefinitions()) {
					int dn = def.getNumber();
					if (def.isErasing())
						deleteAttribute(sn, dn);
					else {
						addAttribute(sn, dn, true);
						if (def.getProperty() != null)
							setAttributeProperty(sn, dn, def.getProperty());
						if (def.getValue() != null)
							setAttributeDefault(sn, dn, def.getValue());
					}
				}
				if (ss.getDescription() != null)
					setSeriesDescription(sn, ss.getDescription());
			}
		}
	}
	
	/**
	 * Apply all changes and return the schema as a standard read-only schema.
	 * 
	 * @return a schema
	 */
	public Schema consolidate(String name, Surrogate surrogate, List<Surrogate> dependencyList) throws T2DBException {
		try {
			getAttributeDefinitionsObject().consolidate();
			getSeriesDefinitionsObject().consolidate();
		} catch (T2DBException e) {
			throw T2DBMsg.exception(e, D.D30129, name);
		}
		return new SchemaImpl(name, getAttributeDefinitions(), getSeriesDefinitions(), surrogate, dependencyList);
	}
	
	
}
