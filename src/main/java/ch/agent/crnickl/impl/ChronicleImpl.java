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
import java.util.List;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.T2DBMsg;
import ch.agent.crnickl.T2DBMsg.D;
import ch.agent.crnickl.api.Attribute;
import ch.agent.crnickl.api.AttributeDefinition;
import ch.agent.crnickl.api.Chronicle;
import ch.agent.crnickl.api.Schema;
import ch.agent.crnickl.api.Series;
import ch.agent.crnickl.api.SeriesDefinition;
import ch.agent.crnickl.api.Surrogate;
import ch.agent.crnickl.api.UpdatableChronicle;

/**
 * Default implementation of {@link Chronicle}.
 * 
 * @author Jean-Paul Vetterli
 */
public class ChronicleImpl extends DBObjectImpl implements Chronicle {

	/**
	 * RawData encapsulates the low level members of a chronicle.
	 */
	public static class RawData {
		private Surrogate surrogate;
		private String name;
		private String description;
		private Chronicle collection;
		private Surrogate schema;
		
		/**
		 * Construct a RawData object.
		 */
		public RawData() {
		}
		
		/**
		 * Set the name.
		 * 
		 * @param name a string
		 */
		public void setName(String name) {
			this.name = name;
		}
		
		/**
		 * Set the description.
		 * 
		 * @param description a string
		 */
		public void setDescription(String description) {
			this.description = description;
		}
		
		/**
		 * Set the surrogate.
		 * 
		 * @param surrogate a surrogate
		 */
		public void setSurrogate(Surrogate surrogate) {
			this.surrogate = surrogate;
		}
		
		/**
		 * Set the parent chronicle.
		 * 
		 * @param collection a chronicle
		 */
		public void setCollection(Chronicle collection) {
			this.collection = collection;
		}
		
		/**
		 * Set the schema.
		 * 
		 * @param schema a schema
		 */
		public void setSchema(Surrogate schema) {
			this.schema = schema;
		}
		
		/**
		 * Return the surrogate.
		 * 
		 * @return a chronicle surrogate
		 */
		public Surrogate getSurrogate() {
			return surrogate;
		}
		
		/**
		 * Return the parent chronicle.
		 * 
		 * @return the parent chronicle or null
		 */
		public Chronicle getCollection() {
			return collection;
		}
		
		/**
		 * Return the name.
		 * 
		 * @return the name
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * Return the description.
		 * @return the description
		 */
		public String getDescription() {
			return description;
		}
		
		/**
		 * Return the schema.
		 * 
		 * @return return a schema surrogate or null
		 */
		public Surrogate getSchema() {
			return schema;
		}
	}

	private class Data {
		private String name;
		private String description;
		private Chronicle collection;
		private Schema schema;
	}
	
	private Data data;
	
	/**
	 * Construct a {@link Chronicle}.
	 * 
	 * @param surrogate a chronicle surrogate
	 */
	public ChronicleImpl(Surrogate surrogate) {
		super(surrogate);
	}
	
	/**
	 * Construct a {@link Chronicle}.
	 * 
	 * @param name a string 
	 * @param description a string
	 * @param parent a chronicle or null
	 * @param schema a schema or null
	 * @param surrogate a surrogate
	 */
	protected ChronicleImpl(String name, String description, Chronicle parent, Schema schema, Surrogate surrogate) {
		this(surrogate);
		data = new Data();
		data.name = name;
		data.description = description;
		data.collection = parent;
		data.schema = schema;
	}
	
	/**
	 * Construct a {@link Chronicle}.
	 * 
	 * @param rawData a raw data object
	 * @throws T2DBException
	 */
	public ChronicleImpl(RawData rawData) throws T2DBException {
		super(rawData.getSurrogate());
		Schema schema = null;
		if (rawData.getSchema() != null)
			schema = getDatabase().getSchema(rawData.getSchema());
		data = new Data();
		data.name = rawData.getName();
		data.description = rawData.getDescription();
		data.collection = rawData.getCollection();
		data.schema = schema;
	}

	private void getData() throws T2DBException {
		if (data == null) {
			DatabaseBackend d = getDatabase();
			DatabaseCache c = d.getCache();
			ChronicleImpl e = c == null ? null : c.lookUpChronicle(getSurrogate());
			if (e != null) {
				data = e.data;
//				int log4j; d.getMessageListener().log(Level.INFO, String.format("*** CACHE HIT: %s %d", toString(), c.size()));
			} else {
				Chronicle chronicle = d.getChronicle(this);
				data = ((ChronicleImpl) chronicle).data;
				// cache the "parent"
				if (c != null && data.collection != null)
					c.store((ChronicleImpl) data.collection);
//				int log4j; d.getMessageListener().log(Level.INFO, String.format("*** CACHE MISS: %s %d", toString(),	c.size()));
			}
		}
	}
	
	@Override
	public Chronicle getChronicle(String simpleName, boolean mustExist) throws T2DBException {
		Chronicle chronicle = getDatabase().getChronicleOrNull(this, simpleName);
		if (chronicle == null && mustExist)
			throw T2DBMsg.exception(D.D40102, getName(true), simpleName);
		else
			return chronicle;
	}
	
	@Override
	public Chronicle findChronicle(String fullName, boolean mustExist) throws T2DBException {
		DatabaseCache cache = getDatabase().getCache();
		Chronicle chronicle = cache.lookUpChronicle(fullName);
		if (chronicle == null) {
			String[] parts = getDatabase().getNamingPolicy().split(fullName);
			if (parts[0] == null) {
				chronicle = getDatabase().getTopChronicle();
				boolean isNameSpace = chronicle.getName(true).equals(fullName);
				if (getDatabase().isStrictNameSpaceMode()) {
					// it must be the name space itself
					if (!isNameSpace)
						throw T2DBMsg.exception(D.D40103, chronicle.getName(true), fullName);
				} else {
					// tolerate name space
					if (!isNameSpace) {
						chronicle = chronicle.getChronicle(parts[1], mustExist);
						// cache top level entities
						if (chronicle != null)
							cache.store((ChronicleImpl)chronicle);
					}
				}
			} else {
				chronicle = findChronicle(parts[0], true);
				// cache entities with children
				cache.store((ChronicleImpl)chronicle);
				chronicle = chronicle.getChronicle(parts[1], mustExist);
				// cache the chronicle if it has a schema ?
			}
		}
		return chronicle;
	}

	@Override
	public boolean isTopChronicle() {
		return false;
	}
	
	@Override
	public UpdatableChronicle edit() {
		UpdatableChronicleImpl u = new UpdatableChronicleImpl(getSurrogate());
		if (data != null) {
			((ChronicleImpl)u).data = data;
			((ChronicleImpl)u).data.name = data.name;
			((ChronicleImpl)u).data.description = data.description;
			((ChronicleImpl)u).data.collection = data.collection;
			((ChronicleImpl)u).data.schema = data.schema;
		}
		return u;
	}

	/**
	 * Force refresh of state on next access.
	 */
	protected void update() {
		data = null;
	}

	@Override
	public Chronicle getCollection() throws T2DBException {
		getData();
		return data.collection;
	}

	@Override
	public Collection<Chronicle> getMembers() throws T2DBException {
		return getDatabase().getChroniclesByParent(this);
	}

	@Override
	public Schema getSchema(boolean effective) throws T2DBException {
		getData();
		Schema es = data.schema;
		if (effective && es == null) {
			Chronicle c = getCollection();
			if (c != null)
				es = c.getSchema(effective);
		}
		return es;
	}
	
	@Override
	public String getName(boolean full) throws T2DBException {
		if (full)
			return getDatabase().getNamingPolicy().fullName(getNames());
		else {
			getData();
			return data.name;
		}
	}
	
	@Override
	public List<String> getNames() throws T2DBException {
		List<String> names = null;
		Chronicle collection = getCollection();
		if (collection != null)
			names = collection.getNames();
		else {
			names = new ArrayList<String>();
			names.add(getDatabase().getTopChronicle().getName(false));
		}
		names.add(getName(false));
		return names;
	}

	@Override
	public String getDescription(boolean full) throws T2DBException {
		if (full)
			return getDatabase().getNamingPolicy().fullDescription(getDescriptions());
		else {
			getData();
			return data.description;
		}
	}

	@Override
	public List<String> getDescriptions() throws T2DBException {
		List<String> descriptions = null;
		Chronicle collection = getCollection();
		if (collection != null)
			descriptions = collection.getDescriptions();
		else {
			descriptions = new ArrayList<String>();
			descriptions.add(getDatabase().getTopChronicle().getDescription(false));
		}
		descriptions.add(getDescription(false));
		return descriptions;
	}

	@Override
	public Attribute<?> getAttribute(String attrName, boolean mustExist) throws T2DBException {
		Attribute<?> attribute = null;
		Schema esch = getSchema(true);
		if (esch == null) {
			if (mustExist)
				throw T2DBMsg.exception(D.D40114, getName(true));
		} else {
			AttributeDefinition<?> def = esch.getAttributeDefinition(attrName, mustExist);
			if (def != null) {
				attribute = def.getAttribute();
				List<Chronicle> chronicles = new ArrayList<Chronicle>();
				Chronicle chronicle = this;
				int lastChronicleWithSchema = 0;
				while (chronicle != null && !chronicle.isTopChronicle()) {
					// if chronicle has schema and schema does not have the attribute then break
					Schema sch = chronicle.getSchema(false);
					if (sch != null) {
						if (sch.getAttributeDefinition(def.getNumber(), false) == null)
							break;
						lastChronicleWithSchema = chronicles.size();
					}
					chronicles.add(chronicle);
					chronicle = chronicle.getCollection();
				}
				chronicles = chronicles.subList(0, lastChronicleWithSchema + 1);
				getDatabase().getAttributeValue(chronicles, attribute);
				
			// if not found, the attribute has the default value already set, so do nothing 
		
//			// add description from value type list 
//			if (attribute.getDescription() == null) {
//				ValueType<?> type = attribute.getProperty().getValueType();
//				if (type.isRestricted()) {
//					String description = type.getValueDescriptions().get(attribute.get());
//					if (description != null) {
//						attribute.setDescription(description);
//					}
//				}
//			}
			}
		}
		return attribute;
	}

	@Override
	public <T> Series<T> getSeries(String name) throws T2DBException {
		Series<T>[] s = getSeries(new String[]{name}, null, true);
		return s[0];
	}
	
	@Override
	public <T> Series<T>[] getSeries(String[] names, Class<T> type, boolean mustBeDefined) throws T2DBException {
		int[] seriesNr = new int[names.length];
		Schema es = getSchema(true);
		if (es == null)
			throw T2DBMsg.exception(D.D40114, getName(true));
		for (int i = 0; i < names.length; i++) {
			SeriesDefinition ss = es.getSeriesDefinition(names[i], mustBeDefined);
			seriesNr[i] = ss == null ? 0 : ss.getNumber();
		}
		Series<T>[] series = getDatabase().getSeries(this, names, seriesNr);
		if (type != null) {
			for (Series<T> s : series) {
				if (s !=null)
					s.typeCheck(type);
			}
		}
		return series;
	}

	@Override
	public Collection<Attribute<?>> getAttributes() throws T2DBException {
		Collection<Attribute<?>> result = new ArrayList<Attribute<?>>();
		Schema schema = getSchema(true);
		if (schema != null) {
			Collection<AttributeDefinition<?>> defs = schema.getAttributeDefinitions();
			for (AttributeDefinition<?> def : defs) {
				if (!def.isComplete())
					throw T2DBMsg.exception(D.D40115, getName(true), def.getNumber());
				result.add(getAttribute(def.getName(), true));
			}
		}
		return result;
	}

	@Override
	public Collection<Series<?>> getSeries() throws T2DBException {
		Collection<Series<?>> result = new ArrayList<Series<?>>();
		Schema schema = getSchema(true);
		if (schema != null) {
			Collection<SeriesDefinition> sss = schema.getSeriesDefinitions();
			String[] names = new String[sss.size()];
			int i = 0;
			for (SeriesDefinition ss : sss) {
				names[i++] = ss.getName();
			}
			Series<?>[] allSeries = getSeries(names, null, true);
			for (Series<?> s : allSeries) {
				if (s != null)
					result.add(s);
			}
		}
		return result;
	}
	
	@Override
	public boolean isMemberOf(Chronicle collection) throws T2DBException {
		if (this.equals(collection))
			return true;
		else {
			if (isTopChronicle())
				return false;
			else
				return ((ChronicleImpl) getCollection()).isMemberOf(collection);
		}
	}

	@Override
	public String toString() {
		try {
			return getName(true);
		} catch (T2DBException e) {
			throw new RuntimeException(e);
		}
	}

}
