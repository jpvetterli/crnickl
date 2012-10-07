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
 * Type: UpdatableChronicleImpl
 * Version: 1.1.0
 */
package ch.agent.crnickl.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.T2DBMsg;
import ch.agent.crnickl.T2DBMsg.D;
import ch.agent.crnickl.api.Attribute;
import ch.agent.crnickl.api.AttributeDefinition;
import ch.agent.crnickl.api.Chronicle;
import ch.agent.crnickl.api.DBObjectType;
import ch.agent.crnickl.api.Property;
import ch.agent.crnickl.api.Schema;
import ch.agent.crnickl.api.Series;
import ch.agent.crnickl.api.SeriesDefinition;
import ch.agent.crnickl.api.Surrogate;
import ch.agent.crnickl.api.UpdatableChronicle;
import ch.agent.crnickl.api.UpdatableSchema;
import ch.agent.crnickl.api.UpdatableSeries;
import ch.agent.crnickl.api.ValueType;

/**
 * Default implementation of {@link UpdatableChronicle}.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.1.0
 */
public class UpdatableChronicleImpl extends ChronicleImpl implements UpdatableChronicle {

	private boolean delete;
	private Schema schema;
	private Chronicle collection;
	private String name;
	private String description;
	private Map<String, Attribute<?>> attributesUpdate;
	private Map<String, UpdatableSeries<?>> seriesUpdate;
	
	/**
	 * @param handle may not be null
	 */
	/**
	 * Construct an {@link UpdatableChronicle}.
	 * 
	 * @param surrogate a surrogate
	 */
	public UpdatableChronicleImpl(Surrogate surrogate) {
		super(surrogate);
		this.seriesUpdate = new LinkedHashMap<String, UpdatableSeries<?>>();
	}
	
	@Override
	public UpdatableChronicle edit() {
		return this;
	}

	@Override
	public String getName(boolean full) throws T2DBException {
		String result = null;
		if (full)
			result = super.getName(full);
		else {
			if (name != null)
				result = name;
			else if (!getSurrogate().inConstruction())
				result = super.getName(full);
		}
		return result;
	}
	
	@Override
	public String getDescription(boolean full) throws T2DBException {
		String result = null;
		if (full)
			result = super.getDescription(full);
		else {
			if (description != null)
				result = description;
			else if (!getSurrogate().inConstruction())
				result = super.getDescription(full);
		}
		return result;
	}

	@Override
	public Attribute<?> getAttribute(String attrName, boolean mustExist) throws T2DBException {
		Attribute<?> result = attributesUpdate == null ? null : attributesUpdate.get(attrName);
		if (result == null) {
			if (getSurrogate().inConstruction()) {
				Schema esch = getSchema(true);
				if (esch == null) {
					if (mustExist)
						throw T2DBMsg.exception(D.D40114, getName(true));
				} else {
					AttributeDefinition<?> def = getSchema(true).getAttributeDefinition(attrName, mustExist);
					if (def != null)
						result = def.getAttribute();
				}
			} else	
				result = super.getAttribute(attrName, mustExist);
		}
		return result;
	}

	@Override
	public Chronicle getCollection() throws T2DBException {
		Chronicle result = null;
		if (collection != null)
			result = collection;
		else if (!getSurrogate().inConstruction())
			result = super.getCollection();
		return result;
	}

	@Override
	public Schema getSchema(boolean effective) throws T2DBException {
		Schema result = null;
		if (schema != null)
			result = schema;
		else {
			if (getSurrogate().inConstruction()) {
				if (collection != null)
					result = collection.getSchema(effective);
			} else {
				result = super.getSchema(effective);
			}
		}
		return result;
	}
	
	@Override
	public void setName(String name) throws T2DBException {
		if (delete)
			throw T2DBMsg.exception(D.D40109, getName(true));
		getDatabase().getNamingPolicy().checkSimpleName(name, false);
		this.name = name;
	}

	@Override
	public void setDescription(String description) throws T2DBException {
		if (delete)
			throw T2DBMsg.exception(D.D40109, getName(true));
		this.description = description;		
	}

	@Override
	public void setAttribute(Attribute<?> value) throws T2DBException {
		if (delete)
			throw T2DBMsg.exception(D.D40109, getName(true));
		if (attributesUpdate == null)
			attributesUpdate = new HashMap<String, Attribute<?>>();
		attributesUpdate.put(value.getProperty().getName(), value);
	}
	
	@Override
	public void setCollection(Chronicle collection) throws T2DBException {
		if (delete)
			throw T2DBMsg.exception(D.D40109, getName(true));
		this.collection = collection;		
	}
	
	@Override
	public void setSchema(Schema schema) throws T2DBException {
		if (delete)
			throw T2DBMsg.exception(D.D40109, getName(true));
		this.schema = schema;		
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Series<T> getSeries(String seriesName) throws T2DBException {
		Series<T> s = null;
		if (seriesUpdate != null) {
			UpdatableSeries<?> u = seriesUpdate.get(seriesName);
			if (u != null)
				s = (Series<T>) u;
		}
		if (s == null && !getSurrogate().inConstruction())
			s = super.getSeries(seriesName);
		return s;
	}
	
	@Override
	public <T> UpdatableSeries<T> createSeries(String seriesName) throws T2DBException {
		Series<T> s = getSeries(seriesName);
		if (s != null)
			throw T2DBMsg.exception(D.D50108, s.getName(true));
		SeriesDefinition schema = getSchema(true).getSeriesDefinition(seriesName, true);
		UpdatableSeries<T> u = new UpdatableSeriesImpl<T>(this, seriesName, schema.getNumber(), 
				new SurrogateImpl(getDatabase(), DBObjectType.SERIES, null));
		seriesUpdate.put(seriesName, u);
		return u;
	}

	@Override
	public <T> UpdatableSeries<T> updateSeries(String seriesName) throws T2DBException {
		Series<T> s = getSeries(seriesName);
		UpdatableSeries<T> u = s == null ? null : s.edit();
		if (u != null)
			seriesUpdate.put(seriesName, u);
		return u;
	}

	@Override
	public UpdatableChronicle createChronicle(String orig, boolean tweakable,
			String description, Collection<Attribute<?>> attributes,
			Schema schema) throws T2DBException {
		
		if (getSurrogate().inConstruction() && !isTopChronicle())
			throw T2DBMsg.exception(D.D40108, getName(true));
		
		if (schema instanceof UpdatableSchema)
			throw T2DBMsg.exception(D.D40104, getName(true), schema.getName());
		
		String name = getDatabase().getNamingPolicy().checkSimpleName(orig,	tweakable);
		Chronicle current = getChronicle(name, false);
		if (current != null) {
			if (tweakable) {
				name = findNextAvailableName(name, 2, 9);
				if (name == null)
					throw T2DBMsg.exception(D.D40127, name, getName(true));
			} else
				throw T2DBMsg.exception(D.D40126, name, getName(true));
		}
		UpdatableChronicleImpl ent = new UpdatableChronicleImpl(
				new SurrogateImpl(getDatabase(), DBObjectType.CHRONICLE, null));
		ent.collection = this;
		ent.name = name;
		ent.description = description;
		ent.schema = schema;
		if (attributes != null) {
			for (Attribute<?> attr : attributes) {
				ent.setAttribute(attr);
			}
		}
		return ent;
	}
	
	private String findNextAvailableName(String orig, int start, int end) throws T2DBException {
		String name = null;
		for (int i = start; i <= end; i++) {
			name = orig + i;
			if (getChronicle(name, false) == null)
				break;
			else
				name = null;
		}
		return name;
	}

	@Override
	public void destroy() throws T2DBException {
		if (seriesUpdate.size() != 0 || name != null || description != null || collection != null
				|| schema != null || attributesUpdate != null)
			throw T2DBMsg.exception(D.D40107, getName(true));
		delete = true;
	}

	@Override
	public void applyUpdates() throws T2DBException {
		boolean done = false;
		getDatabase().getCache().clear(this); // a bit of overkill
		if (delete) {
			getDatabase().deleteChronicle(this);
			delete = false;
			done = true;
		} else {
			if (getSurrogate().inConstruction()) {
				getDatabase().create(this);
				name = null;
				description = null;
				collection = null;
				schema = null;
				done = true;
			} else
			if (updateIfModified()) {
				name = null;
				description = null;
				collection = null;
				schema = null;
				done = true;
			}
			if (attributesUpdate != null) {
				updateAttributes(attributesUpdate.values());
				done = true;
				attributesUpdate = null;
			}
			if (seriesUpdate.size() > 0) {
				for (UpdatableSeries<?> s : seriesUpdate.values()) {
					s.applyUpdates();
				}
				done = true;
				seriesUpdate.clear();
			}
		}
		if (done)
			update();
	}

	@Override
	protected void update() {
		super.update();
		// other things already taken care of in applyUpdates
	}

	
	/**
	 * Update attribute values.
	 * 
	 * @param attributes a collection of attributes
	 * @throws T2DBException
	 */
	public void updateAttributes(Collection<Attribute<?>> attributes) throws T2DBException {
		Schema schema = getSchema(true);
		if (schema == null)
			throw T2DBMsg.exception(D.D30115);
		for (Attribute<?> attribute : attributes) {
			Property<?> property = attribute.getProperty();
			ValueType<?> type = property.getValueType();
			AttributeDefinition<?> def = schema.getAttributeDefinition(property.getName(), true);
			if (def == null)
				throw T2DBMsg.exception(D.D30114, property.getName());
			String value = type.toString(attribute.get());
			if (value == null || value.length() == 0) {
				getDatabase().deleteAttributeValue(this, def);
			} else {
				String description = attribute.getDescription(false);
				if (description == null)
					description = "";
				else {
					// ignore description if identical to the value type list description
					if (type.isRestricted()) {
						if (description.equals(type.getValueDescriptions().get(attribute.get())))
							description = "";
					}
				}
				getDatabase().update(this, def, value, description);
			}
		}
	}
	
	private boolean updateIfModified() throws T2DBException {
		boolean anything = false;
		String name = getName(false);
		String description = getDescription(false);
		Chronicle current = getDatabase().getChronicle(this);
		Chronicle collection = getCollection();
		Schema schema = getSchema(false);
		if (!equals(name, current.getName(false))) {
			getDatabase().getNamingPolicy().checkSimpleName(name, false);
			anything = true;
		}
		if (!equals(description, current.getDescription(false))) {
			anything = true;
		}
		if (!equals(collection, current.getCollection()))
			throw T2DBMsg.exception(D.D40110, getName(true)); // not supported (yet)
		if (!equals(schema, current.getSchema(false)))
			throw T2DBMsg.exception(D.D40111, getName(true)); // not supported (yet)
		if (anything)
			getDatabase().update(this);
		return anything;
	}

	/**
	 * Return true if two objects are not null and equal.
	 * 
	 * @param object1
	 *            first object or null allowed
	 * @param object2
	 *            second object or null allowed
	 * @return
	 */
	private boolean equals(Object object1, Object object2) {
		if (object1 == object2)
			return true;
		if ((object1 == null) || (object2 == null)) {
			return false;
		}
	    return object1.equals(object2);
	}

}
