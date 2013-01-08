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

import java.util.ArrayList;
import java.util.Collection;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.T2DBMsg;
import ch.agent.crnickl.T2DBMsg.D;
import ch.agent.crnickl.api.AttributeDefinition;
import ch.agent.crnickl.api.SchemaComponent;
import ch.agent.crnickl.api.SeriesDefinition;
import ch.agent.crnickl.api.ValueType;
import ch.agent.t2.time.TimeDomain;

/**
 * Default implementation of {@link SeriesDefinition}.
 * 
 * @author Jean-Paul Vetterli
 */
public class SeriesDefinitionImpl extends SchemaComponentImpl implements SeriesDefinition {

	private boolean erasing;
	private int number;
	private String description;
	private SchemaComponents<AttributeDefinition<?>> attributes;
	
	/**
	 * Construct a {@link SeriesDefinition}.
	 * 
	 * @param number a positive number
	 * @param description a string 
	 * @param attributeDefs a collection of attribute definitions
	 * @throws T2DBException
	 */
	public SeriesDefinitionImpl(int number,	String description, Collection<AttributeDefinition<?>> attributeDefs) throws T2DBException {
		super();
		if (number < 1)
			throw T2DBMsg.exception(D.D30117);
		this.number = number;
		this.description = description;
		this.attributes = new SchemaComponents<AttributeDefinition<?>>(attributeDefs);
	}
	
	@Override
	public void setContainer(SchemaComponentContainer container) {
		this.attributes.setContainer(container);
	}

	@Override
	public boolean isComplete() {
		return !isErasing() 
			&& getName() != null && getValueType() != null && getTimeDomain() != null 
			&& attributes.isComplete();
	}

	@Override
	public boolean isErasing() {
		// make sure it is still erasing
		if (erasing)
			erasing = description == null && attributes.getMap().size() == 0;
		return erasing;
	}

	@Override
	public int getNumber() {
		return number;
	}

	@Override
	public Collection<AttributeDefinition<?>> getAttributeDefinitions() {
		return attributes.getComponents();
	}

	@Override
	public Collection<AttributeDefinition<?>> getCustomAttributeDefinitions() {
		Collection<AttributeDefinition<?>> defs = getAttributeDefinitions();
		Collection<AttributeDefinition<?>> customDefs = new ArrayList<AttributeDefinition<?>>(defs.size() - 3);
		for (AttributeDefinition<?> def : defs) {
			if (def.getNumber() > DatabaseBackend.MAX_MAGIC_NR)
				customDefs.add(def);
		}
		return customDefs;
	}
	
	@Override
	public AttributeDefinition<?> getAttributeDefinition(String name, boolean mustExist) throws T2DBException {
		AttributeDefinition<?> def =  attributes.getComponent(name);
		if (def == null && mustExist)
			throw T2DBMsg.exception(D.D30119, name, getNumber());
		return def;
	}

	@Override
	public AttributeDefinition<?> getAttributeDefinition(int attrNr, boolean mustExist) throws T2DBException {
		AttributeDefinition<?> def = attributes.getComponent(attrNr);
		if (def == null && mustExist)
			throw T2DBMsg.exception(D.D30120, attrNr, getNumber());
		return def;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getName() {
		@SuppressWarnings("unchecked")
		AttributeDefinition<String> def = (AttributeDefinition<String>) attributes.getComponent(DatabaseBackend.MAGIC_NAME_NR);
		return def == null ? null : def.getValue();
	}

	@Override
	public TimeDomain getTimeDomain() {
		@SuppressWarnings("unchecked")
		AttributeDefinition<TimeDomain> def = (AttributeDefinition<TimeDomain>)attributes.getComponent(DatabaseBackend.MAGIC_TIMEDOMAIN_NR);
		return def == null ? null : def.getValue();
	}

	@Override
	public ValueType<?> getValueType() {
		@SuppressWarnings("unchecked")
		AttributeDefinition<ValueType<?>> def = (AttributeDefinition<ValueType<?>>)attributes.getComponent(DatabaseBackend.MAGIC_TYPE_NR);
		return def == null ? null : def.getValue();
	}

	@Override
	public boolean isSparse() {
		@SuppressWarnings("unchecked")
		AttributeDefinition<Boolean> def = (AttributeDefinition<Boolean>)attributes.getComponent(DatabaseBackend.MAGIC_SPARSITY_NR);
		return def == null ? false : def.getValue();
	}

	/**
	 * Set the erasing mode.
	 * Setting erasing mode resets the description and all attributes.
	 * 
	 * @param erasing true if the definition should erase an inherited definition
	 */
	public void setErasing(boolean erasing) {
		checkEdit();
		doSetErasing(erasing);
	}
	
	private void doSetErasing(boolean erasing) {
		this.erasing = erasing;
		if (erasing) {
			this.description = null;
			try {
				this.attributes = new SchemaComponents<AttributeDefinition<?>>(
						null);
			} catch (T2DBException e) {
				// never happens
			}
		}
	}

	/**
	 * Set the description.
	 * 
	 * @param description a string
	 */
	public void setDescription(String description) {
		checkEdit();
		this.description = description;
	}

	/**
	 * Return the object managing the series' attribute definitions.
	 * 
	 * @return a collection of managed attribute definitions
	 */
	protected SchemaComponents<AttributeDefinition<?>> getAttributeDefinitionsObject() {
		return attributes;
	}
	@Override
	public void edit(SchemaComponent component) throws T2DBException {
		if (!(component instanceof SeriesDefinition))
			throw new IllegalArgumentException(component == null ? null : component.getClass().getName());
		SeriesDefinition arg = (SeriesDefinition) component;
		if (arg.isErasing())
			doSetErasing(true);
		else {
			if (arg.getDescription() != null)
				setDescription(arg.getDescription());
			attributes.consolidate();
		}
	}

	@Override
	public SchemaComponent copy() {
		try {
			SeriesDefinitionImpl sd = null;
			if (isErasing()) {
				sd = new SeriesDefinitionImpl(this.number, null, null);	
				sd.setErasing(true);
			} else {
				Collection<AttributeDefinition<?>> components = new ArrayList<AttributeDefinition<?>>();
				// deep copy
				for (AttributeDefinition<?> compo : this.attributes.getComponents()) {
					components.add((AttributeDefinition<?>) compo.copy());
				}
				sd = new SeriesDefinitionImpl(this.number, this.description, components);
			}
			// don't copy editMode
			return sd;
		} catch (T2DBException e) {
			throw new RuntimeException("bug", e);
		}
	}

	@Override
	public int compareTo(SeriesDefinition o) {
		if (getNumber() > o.getNumber())
			return 1;
		else if (getNumber() < o.getNumber())
			return -1;
		else
			return 0;
	}
	
	@Override
	public String toString() {
		String name = getName();
		return name == null ? "" + number : name;
	}

}
