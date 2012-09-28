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
 * Type: SchemaImpl
 * Version: 1.0.0
 */
package ch.agent.crnickl.impl;

import java.util.Collection;
import java.util.List;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.T2DBMsg;
import ch.agent.crnickl.T2DBMsg.D;
import ch.agent.crnickl.api.AttributeDefinition;
import ch.agent.crnickl.api.Schema;
import ch.agent.crnickl.api.SeriesDefinition;
import ch.agent.crnickl.api.Surrogate;
import ch.agent.crnickl.api.UpdatableSchema;

/**
 * Default implementation of {@link Schema}.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public class SchemaImpl extends DBObjectImpl implements Schema {
	
	private boolean updatable;
	private String name;
	private SchemaComponents<SeriesDefinition> seriesDefinitions;
	private SchemaComponents<AttributeDefinition<?>> attributes;
	private List<Surrogate> dependencyList;
	
	/**
	 * Construct a {@link Schema}.
	 * 
	 * @param name a string
	 * @param attributeDefs a collection of attribute definitions
	 * @param seriesDefinitions a collection of series definitions
	 * @param surrogate a surrogate
	 * @param dependencyList a list of surrogates
	 */
	public SchemaImpl(String name, Collection<AttributeDefinition<?>> attributeDefs, Collection<SeriesDefinition> seriesDefinitions, Surrogate surrogate, List<Surrogate> dependencyList) {
		this(false, name, attributeDefs, seriesDefinitions, surrogate, dependencyList);
	}
	
	/**
	 * Construct a {@link Schema}.
	 * 
	 * @param updatable set to true when invoked by an {@link UpdatableSchemaImpl} constructor, set to
	 * false when invoked by a {@link SchemaImpl} constructor
	 * @param name a string
	 * @param attributeDefs a collection of attribute definitions
	 * @param seriesDefinitions a collection of series definitions
	 * @param surrogate a surrogate
	 * @param dependencyList a list of surrogates
	 */
	protected SchemaImpl(boolean updatable, String name, Collection<AttributeDefinition<?>> attributeDefs, Collection<SeriesDefinition> seriesDefinitions, Surrogate surrogate, List<Surrogate> dependencyList) {
		super(surrogate);
		this.updatable = updatable;
		if (name == null)
			throw new IllegalArgumentException("name null");
		this.name = name;
		DatabaseBackend database = ((SurrogateImpl) surrogate).getDatabase();
		this.attributes = new SchemaComponents<AttributeDefinition<?>>(attributeDefs, 
				database.getNameIndexThreshold());
			
		this.seriesDefinitions = new SchemaComponents<SeriesDefinition>(seriesDefinitions, 
				database.getNameIndexThreshold());
		this.dependencyList = dependencyList;
	}
	
	/**
	 * Refresh state.
	 */
	protected void update() throws T2DBException {
		// load the whole stuff again, it's expected to be a rare operation 
		SchemaImpl schema = null;
		if (updatable)
			schema = (SchemaImpl) getDatabase().getUpdatableSchema(getSurrogate());
		else
			schema = (SchemaImpl) getDatabase().getSchema(getSurrogate());
		this.name = schema.name;
		this.seriesDefinitions = schema.seriesDefinitions;
		this.attributes = schema.attributes;
	}
	
	@Override
	public boolean isComplete() {
		return attributes.isComplete() && seriesDefinitions.getComponents().size() > 0 && seriesDefinitions.isComplete();
	}

	@Override
	public UpdatableSchema edit() {
		try {
			return getDatabase().getUpdatableSchema(this);
		} catch (T2DBException e) {
			throw new RuntimeException("bug", e);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Collection<SeriesDefinition> getSeriesDefinitions() {
		return seriesDefinitions.getComponents();
	}

	@Override
	public SeriesDefinition getSeriesDefinition(String name, boolean mustExist) throws T2DBException {
		SeriesDefinition schema = seriesDefinitions.getComponent(name);
		if (schema == null && mustExist)
			throw T2DBMsg.exception(D.D30121, name, getName());
		return schema;
	}
	
	@Override
	public SeriesDefinition getSeriesDefinition(int number, boolean mustExist) throws T2DBException {
		SeriesDefinition schema = seriesDefinitions.getComponent(number);
		if (schema == null && mustExist)
			throw T2DBMsg.exception(D.D30125, number, getName());
		return schema;
	}

	@Override
	public Collection<AttributeDefinition<?>> getAttributeDefinitions() {
		return attributes.getComponents();
	}
	
	@Override
	public AttributeDefinition<?> getAttributeDefinition(String name, boolean mustExist) throws T2DBException {
		AttributeDefinition<?> def = attributes.getComponent(name);
		if (def == null && mustExist)
			throw T2DBMsg.exception(D.D30122, name, getName());
		return def;
	}

	@Override
	public AttributeDefinition<?> getAttributeDefinition(int number, boolean mustExist) throws T2DBException {
		AttributeDefinition<?> def = attributes.getComponent(number);
		if (def == null && mustExist)
			throw T2DBMsg.exception(D.D30123, number, getName());
		return def;
	}

	/**
	 * Return the object managing attribute definitions.
	 * @return the object managing attribute definitions
	 */
	protected SchemaComponents<AttributeDefinition<?>> getAttributeDefinitionsObject() {
		return attributes;
	}
	
	/**
	 * Return the object managing series definitions.
	 * @return the object managing series definitions
	 */
	protected SchemaComponents<SeriesDefinition> getSeriesDefinitionsObject() {
		return seriesDefinitions;
	}
	
	/**
	 * Return the list of schema surrogates which this schema extends.
	 * @return a list of schema surrogates
	 */
	public List<Surrogate> getDependencyList() {
		return dependencyList;
	}

	@Override
	public boolean dependsOnSchema(Schema schema) {
		Surrogate surrogate = schema.getSurrogate();
		if (dependencyList != null) {
			for (Surrogate k : dependencyList) {
				if (k.equals(surrogate))
					return true;
			}
			return false;
		} else
			return getSurrogate().equals(surrogate);
	}

	@Override
	public String toString() {
		return getName();
	}
	
//	/**
//	 * Debugging method to print the state of the schema on a stream.
//	 * 
//	 * @param out a print stream
//	 * @throws T2DBException
//	 */
//	public void dump(PrintStream out) throws T2DBException {
//		out.println(String.format("%s %s (ready: %b)", this.getName(), getSurrogate().toString(), isComplete()));
//		for (AttributeDefinition<?> def : this.getAttributeDefinitions()) {
//			out.println(String.format("  A #%d %s=%s", def.getNumber(), def.getName(), def.getValue()));
//		}
//		for (SeriesDefinition ss : this.getSeriesDefinitions()) {
//			TimeDomain td = ss.getTimeDomain();
//			out.println(String.format("  S #%d Name=%s Description=%s Type=%s TimeDomain=%s", 
//					ss.getNumber(), ss.getName(), ss.getDescription(), ss.getValueType(), 
//					td == null ? null : td.getLabel()));
//			for (AttributeDefinition<?> def : ss.getAttributeDefinitions()) {
//				out.println(String.format("    SA #%d %s=%s", def.getNumber(), def.getName(), def.getValue()));
//			}
//		}
//	}
	
}
