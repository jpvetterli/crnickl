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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.T2DBMsg;
import ch.agent.crnickl.T2DBMsg.D;
import ch.agent.crnickl.api.AttributeDefinition;
import ch.agent.crnickl.api.Chronicle;
import ch.agent.crnickl.api.DBObjectId;
import ch.agent.crnickl.api.Database;
import ch.agent.crnickl.api.MessageListener;
import ch.agent.crnickl.api.Property;
import ch.agent.crnickl.api.Schema;
import ch.agent.crnickl.api.SeriesDefinition;
import ch.agent.crnickl.api.Surrogate;
import ch.agent.crnickl.api.ValueType;

/**
 * Default implementation of {@link DatabaseCache}. There are actually three
 * separate caches:
 * <ol>
 * <li>for chronicles with a schema and for their parent chronicles
 * <li>for the schemas referenced in the chronicles
 * <li>for the properties referenced in the schemas
 * </ol>
 * The implementation is thread-safe. The chronicle cache is an LRU cache. The
 * assumption is that clients do not have a large number of chronicles with a
 * non-null schema and that the maximum capacity is specified to be large enough to
 * store all relevant chronicles. The schema and property caches always store
 * all relevant objects, and are automatically maintained if the LRU chronicle
 * cache requests the removal of a chronicle.
 * 
 * @author Jean-Paul Vetterli
 */
public class DatabaseCacheImpl implements DatabaseCache {

	private abstract class RefCounter {
		
		private int refCount;
		
		public RefCounter() {
			this.refCount = 1;
		}
		
		public void incr() {
			refCount++;
		}
		public int decr() {
			return --refCount;
		}
	}
	
	private class SchemaRefCounter extends RefCounter {
		
		private SchemaImpl schema;
		
		public SchemaRefCounter(SchemaImpl schema) {
			super();
			this.schema = schema;
		}
	}
	
	private class PropertyRefCounter extends RefCounter {
		
		private Property<?> property;
		
		public PropertyRefCounter(Property<?> property) {
			super();
			this.property = property;
		}
	}
	
	/**
	 * ChronicleCache is an LRU cache which requests removal of the least
	 * recently accessed value when the capacity is exceeded. A callback is
	 * provided to act on LRU removal. The initial capacity is computed from the
	 * requested capacity and load factor to avoid any rehash taking place.
	 */
	@SuppressWarnings("serial")
	protected class ChronicleCache extends LinkedHashMap<Object,ChronicleImpl> {

		private int capacity;
		private DatabaseCacheImpl callback;

		/**
		 * Construct a chronicle cache.
		 * The actual capacity will usually be larger than what is specified.
		 * The details depend on the underlying library. For example, for a load
		 * factor of 3/4 and the capacity requests 95, 96, and 97, a typical
		 * library implementation gives the actual capacities 96, 96 and 192.
		 * Even if the actual capacity is larger than necessary for the given
		 * load factor, the used capacity will never exceed the capacity
		 * specified.
		 * 
		 * @param capacity a positive number
		 * @param loadFactor a number between 0 and 1
		 * @param callback the cache object on which to invoked {@link DatabaseCacheImpl#removed(ChronicleImpl)}
		 */
		public ChronicleCache(int capacity,float loadFactor, DatabaseCacheImpl callback) {
			super((int) Math.ceil(capacity/ loadFactor), loadFactor, true);
			this.capacity = capacity;
			this.callback = callback;
		}

		@Override
		protected boolean removeEldestEntry(Entry<Object, ChronicleImpl> eldest) {
			if (size() > capacity) {
				callback.removed(eldest.getValue());
				return true;
			} else
				return false;
		}
	}
	
	private Database db;
	private int capacity;
	private int removedTotal;
	private int removedTotalThreshold = 1;
	private Map<Object, ChronicleImpl> byIdCache;
	private Map<String, ChronicleImpl> byNameCache;
	private Map<Object, SchemaRefCounter> schemaCache;
	private Map<Object, PropertyRefCounter> propCache;
	private Map<String, Property<?>> propByNameCache;
	private MessageListener messageListener;
	
	/**
	 * Construct a {@link DatabaseCache}.
	 * 
	 * @param db the database being cached
	 * @param capacity a positive number
	 * @param loadFactor a number between 0 and 1
	 */
	public DatabaseCacheImpl(Database db, int capacity, float loadFactor) {
		this.db = db;
		this.capacity = capacity;
		byIdCache = Collections.synchronizedMap(new ChronicleCache(capacity, loadFactor, this));
		byNameCache = Collections.synchronizedMap(new HashMap<String, ChronicleImpl>());
		schemaCache = Collections.synchronizedMap(new HashMap<Object, SchemaRefCounter>());
		propCache = Collections.synchronizedMap(new HashMap<Object, PropertyRefCounter>());
		propByNameCache = Collections.synchronizedMap(new HashMap<String, Property<?>>());
	}

	/**
	 * Construct a {@link DatabaseCache} with a 0.75 load factor.
	 * 
	 * @param db the database being cached
	 * @param capacity a positive number
	 */
	public DatabaseCacheImpl(Database db, int capacity) {
		this(db, capacity, 0.75f);
	}
	
	/**
	 * Set a message listener. When a message listener is defined, the cache can
	 * issue messages notify of cache removals. Monitoring such messages is
	 * useful for tuning cache parameters.
	 * 
	 * @param messageListener a message listener
	 */
	public void setMessageListener(MessageListener messageListener) {
		this.messageListener = messageListener;
	}

	// Chronicle
	
	@Override
	public ChronicleImpl lookUpChronicle(Surrogate surrogate) {
//		int log4j; 
//		ChronicleImpl ent = cache.get(((SurrogateImpl) key).getId());
//		if (ent == null)
//			message(Level.INFO, String.format("*** CACHE MISS: %s %d", key.toString(), size()));
//		else
//			message(Level.INFO, String.format("*** CACHE HIT: %s %s %d", ent.toString(), ent.getKey().toString(), size()));
//		return ent;
		return byIdCache.get(((SurrogateImpl) surrogate).getId());
	}
	
	@Override
	public ChronicleImpl lookUpChronicle(String name) {
//		int log4j; 
//		ChronicleImpl ent = ncache.get(name);
//		if (ent == null)
//			message(Level.INFO, String.format("*** CACHE MISS: %s %d", name, size()));
//		else
//			message(Level.INFO, String.format("*** CACHE HIT: %s %s %d", ent.toString(), ent.getKey().toString(), size()));
//		return ent;
		return byNameCache.get(name);
	}
	
	@Override
	public Property<?> lookUpProperty(String name) throws T2DBException {
		Property<?> p = propByNameCache.get(name);
		if (p == null) {
			p = db.getProperty(name, true);
			ref(p);
		}
		return p;
	}

	@Override
	public ChronicleImpl store(ChronicleImpl entity) throws T2DBException {
		ChronicleImpl copy = null;
		if (!entity.isTopChronicle() && lookUpChronicle(entity.getSurrogate()) == null) {
			Schema schema = entity.getSchema(false);
			schema = ref((SchemaImpl) schema);
			copy = new ChronicleImpl(entity.getName(false), entity.getDescription(false), 
					entity.getCollection(), schema, entity.getSurrogate());
			put(copy);
//			int log4j; message(Level.INFO, String.format("*** CACHE ADD: %s %s %d", entity.toString(), entity.getKey().toString(), size()));
		}
		return copy;
	}

	private void remove(ChronicleImpl entity) {
		try {
			Schema schema = entity.getSchema(false);
			if (schema != null)
				unRef((SchemaImpl) schema);
			byNameCache.remove(entity.getName(true));
		} catch (T2DBException e) {
			// never happens since entity in cache has full info 
			throw new RuntimeException("bug", e);
		}
	}
	
	private void put(ChronicleImpl entity) {
		try {
			byIdCache.put(((SurrogateImpl) entity.getSurrogate()).getId(), entity);
			byNameCache.put(entity.getName(true), entity);
		} catch (T2DBException e) {
			// never happens since entity in cache has full info 
			throw new RuntimeException("bug", e);
		}
	}
	
	@Override
	public int size() {
		return byIdCache.size(); 
	}
	
	/**
	 * Take action on cache removal.
	 * This method is called by the LRU logic.
	 * See {@link ChronicleCache#removeEldestEntry(Entry)}.
	 * @param chronicle a chronicle
	 */
	private void removed(ChronicleImpl chronicle) {
		remove(chronicle);
//		int log4j; message(Level.INFO, "*** CACHE REMOVE: " + entity.toString());
		removedTotal++;
//		if (removedTotal == 1 && messageListener.isListened(Level.FINER)) {
//			message(Level.FINER, "*** cache listing ***");
//			message(Level.FINER, chronicle.toString());
//			for (Chronicle c : byNameCache.values()) {
//				message(Level.FINER, c.toString());
//			}
//			message(Level.FINER, "***");
//		}
		if (removedTotal % removedTotalThreshold == 0) {
			message(Level.WARNING, new T2DBMsg(D.D00121, capacity, removedTotal, chronicle.toString()).toString());
			if (removedTotalThreshold < 100000 && removedTotal >= removedTotalThreshold * 10)
				removedTotalThreshold *= 10;
		}
		// chronicle itself removed from "by id" cache by LinkedHashMap 
	}
	
	/****** Schema ******/
	
	/**
	 * @param schema may be null
	 * @return the single copy of schema in cache
	 */
	private SchemaImpl ref(SchemaImpl schema) {
		SchemaImpl esh = null;
		if (schema != null) {
			DBObjectId id = schema.getId();
			SchemaRefCounter ref = schemaCache.get(id);
			if (ref == null) {
				try {
					Collection<AttributeDefinition<?>> defCopies = ref1(0, schema.getAttributeDefinitions());
					Collection<SeriesDefinition> ssCopies = ref2(schema.getSeriesDefinitions());
					esh = new SchemaImpl(schema.getName(), defCopies, ssCopies, schema.getSurrogate(), schema.getDependencyList());
				} catch (T2DBException e) {
					throw new RuntimeException("bug", e);
				}
				schemaCache.put(id, new SchemaRefCounter(esh));
			} else {
				esh = ref.schema;
				ref.incr();
			}
		}
		return esh;
	}

	private Collection<AttributeDefinition<?>> ref1(int seriesNr, Collection<AttributeDefinition<?>> defs) throws T2DBException {
		List<AttributeDefinition<?>> defCopies = new ArrayList<AttributeDefinition<?>>();
		for (AttributeDefinition<?> def : defs) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			AttributeDefinition<?> defCopy = 
				new AttributeDefinitionImpl(seriesNr, def.getNumber(), ref(def.getProperty()), def.getValue());
			defCopies.add(defCopy);
		}
		return defCopies;
	}
	
	private Collection<SeriesDefinition> ref2(Collection<SeriesDefinition> sss) throws T2DBException {
		List<SeriesDefinition> ssCopies = new ArrayList<SeriesDefinition>();
		for (SeriesDefinition ss : sss) {
			Collection<AttributeDefinition<?>> defCopies = ref1(ss.getNumber(), ss.getAttributeDefinitions());
			SeriesDefinition ssCopy = new SeriesDefinitionImpl(ss.getNumber(), ss.getDescription(), defCopies);
			ssCopies.add(ssCopy);
		}
		return ssCopies;
	}
	
	private void unRef(SchemaImpl schema) {
		DBObjectId id = schema.getId();
		SchemaRefCounter ref = schemaCache.get(id);
		if (ref.decr() < 1) {
			schemaCache.remove(id);
			for (AttributeDefinition<?> def : schema.getAttributeDefinitions()) {
				unRef(def.getProperty());
			}
			for (SeriesDefinition ss : schema.getSeriesDefinitions()) {
				for (AttributeDefinition<?> def : ss.getAttributeDefinitions()) {
					unRef(def.getProperty());
				}
			}
		}
	}
	
	/****** Property ******/
	
	/**
	 * @param property may not be null
	 * @return the single copy of property in cache
	 */
	private Property<?> ref(Property<?> property) {
		DBObjectId id = property.getId();
		PropertyRefCounter ref = propCache.get(id);
		Property<?> p = null;
		if (ref == null) {
			p = property;
			propCache.put(id, new PropertyRefCounter(p));
			propByNameCache.put(property.getName(), p);
		} else {
			p = ref.property;
			ref.incr();
		}
		return p;
	}
	
	private void unRef(Property<?> property) {
		DBObjectId id = property.getId();
		PropertyRefCounter ref = propCache.get(id);
		if (ref.decr() < 1) {
			propCache.remove(id);
			propByNameCache.remove(property.getName());
		}
	}
	
	/****** Udpate management ******/
	
	/**
	 * When property or schema updates occur just clear all caches. Updates to
	 * such objects are expected to be (very very) rare.
	 */
	private void reset() {
		byNameCache.clear();
		byIdCache.clear();
		schemaCache.clear();
		propCache.clear();
		propByNameCache.clear();
	}
	
	@Override
	public void clear() {
		reset();
	}

	@Override
	public void clear(Chronicle chronicle) {
		DBObjectId id = chronicle.getId();
		if (id != null) {
			ChronicleImpl e = byIdCache.remove(id);
			if (e != null)
				remove(e);
		}
	}

	@Override
	public void clear(Schema schema) {
		if (schemaCache.get(((SurrogateImpl) schema.getSurrogate()).getId()) != null)
			clear(); // keep it simple, schema updates are very rare
	}

	@Override
	public void clear(Property<?> property) {
		if (propCache.get(((SurrogateImpl) property.getSurrogate()).getId()) != null)
			clear(); // keep it simple, property updates are very rare
	}

	@Override
	public void clear(ValueType<?> valueType) {
		clear(); // keep it simple, assume meta data updates are rare
	}

//	/**
//	 * Return the list of chronicles in the cache.
//	 * 
//	 * @return a list of strings describing the chronicles
//	 */
//	public List<String> directory() {
//		List<String> names = new ArrayList<String>(byNameCache.size());
//		for (Map.Entry<String, ChronicleImpl> entry : byNameCache.entrySet()) {
//			String name = entry.getValue().toString();
//			if (name.equals(entry.getKey())) {
//				names.add(String.format("%s - %s", 
//						entry.getValue().getSurrogate().toString(), 
//						entry.getValue().toString()));
//			} else {
//				names.add(String.format("%s - %s [KEY=%s]", 
//						entry.getValue().getSurrogate().toString(), 
//						entry.getValue().toString(),
//						entry.getKey()));
//			}
//		}
//		return names;
//	}
	
	private void message(Level level, String text) {
		if (messageListener != null && messageListener.isListened(level))
			messageListener.log(level, text);
	}

}