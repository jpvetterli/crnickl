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
 * Type: SeriesDefinitionImpl
 * Version: 1.0.0
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
 * @version 1.0.0
 */
public class SeriesDefinitionImpl implements SeriesDefinition {

	private boolean editMode;
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
		if (number < 1)
			throw T2DBMsg.exception(D.D30117);
		this.number = number;
		this.description = description;
		this.attributes = new SchemaComponents<AttributeDefinition<?>>(attributeDefs, 3, 30, new AttributeDefinition<?>[0]);
	}
	
	@Override
	public boolean isComplete() {
		return !erasing 
			&& getName() != null && getValueType() != null && getTimeDomain() != null 
			&& attributes.isComplete();
	}

	@Override
	public boolean isErasing() {
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
	 * 
	 * @param erasing true if the definition should erase an inherited definition
	 */
	public void setErasing(boolean erasing) {
		checkEdit();
		this.erasing = erasing;
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
	public void edit() {
		this.editMode = true;
	}
	
	private void checkEdit() {
		if (!editMode)
			throw new IllegalStateException();
	}
	
	@Override
	public void edit(SchemaComponent component) throws T2DBException {
		if (!(component instanceof SeriesDefinition))
			throw new IllegalArgumentException(component == null ? null : component.getClass().getName());
		SeriesDefinition arg = (SeriesDefinition) component;
		if (arg.isErasing())
			erasing = true;
		else {
			if (arg.getDescription() != null)
				setDescription(arg.getDescription());
			try {
				attributes.consolidate();
			} catch (T2DBException e) {
				throw T2DBMsg.exception(e, D.D30131, getName(), component.getName());
			}
		}
	}

	@Override
	public void consolidate() throws T2DBException {
		attributes.consolidate();
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
