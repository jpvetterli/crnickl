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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import ch.agent.core.KeyedException;
import ch.agent.core.KeyedMessage;
import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.T2DBMsg;
import ch.agent.crnickl.T2DBMsg.D;
import ch.agent.crnickl.api.AttributeDefinition;
import ch.agent.crnickl.api.Chronicle;
import ch.agent.crnickl.api.DBObject;
import ch.agent.crnickl.api.DBObjectType;
import ch.agent.crnickl.api.DatabaseConfiguration;
import ch.agent.crnickl.api.MessageListener;
import ch.agent.crnickl.api.NamingPolicy;
import ch.agent.crnickl.api.Property;
import ch.agent.crnickl.api.Schema;
import ch.agent.crnickl.api.Series;
import ch.agent.crnickl.api.Surrogate;
import ch.agent.crnickl.api.UpdatableChronicle;
import ch.agent.crnickl.api.UpdatableProperty;
import ch.agent.crnickl.api.UpdatableSchema;
import ch.agent.crnickl.api.UpdatableSeries;
import ch.agent.crnickl.api.UpdatableValueType;
import ch.agent.crnickl.api.UpdateEvent;
import ch.agent.crnickl.api.UpdateEventOperation;
import ch.agent.crnickl.api.UpdateEventPublisher;
import ch.agent.crnickl.api.ValueType;
import ch.agent.t2.time.Range;
import ch.agent.t2.time.TimeIndex;
import ch.agent.t2.timeseries.Observation;
import ch.agent.t2.timeseries.TimeAddressable;
import ch.agent.t2.timeseries.TimeSeriesFactory;

/**
 * Default implementation of {@link DatabaseBackend}.
 * 
 * @author Jean-Paul Vetterli
 *
 */
public abstract class DatabaseBackendImpl implements DatabaseBackend {

	private int hashCode = -1;
	
	private DatabaseCache cache;
	private UpdateEventPublisher eventHub;
	private NameSpace topChronicle;
	private MessageListener messageListener;
	private PermissionChecker permissionChecker;
	private SchemaUpdatePolicy sup;
	private ChronicleUpdatePolicyExtension eupx;
	private ChronicleUpdatePolicy eup;
	private NamingPolicy nm;
	private boolean strictNameSpaceMode;
	private Map<String, ValueAccessMethods<?>> am;
	
	/**
	 * Construct a {@link DatabaseBackend}.
	 * 
	 * @param name the name of the database
	 */
	public DatabaseBackendImpl(String name) {
		topChronicle = new NameSpace(name, String.format("%s (%s)", getClass().getSimpleName(), name), new SurrogateImpl(this, DBObjectType.CHRONICLE, null));
		setMessageListener(null);
		nm = new NamingPolicy();
		am = new HashMap<String, ValueAccessMethods<?>>();
	}
	
	@Override
	public void open() throws T2DBException {
	}

	@Override
	public void close() throws T2DBException {
	}

	@Override
	public void clear() throws T2DBException {
		getCache().clear();
	}

	private MessageListener getDefaultMessageListener() {
		return new MessageListener() {
			@Override
			public void setFilterLevel(Level level) {
				if (!level.equals(Level.OFF))
					throw new UnsupportedOperationException("the default message listener ignores all messages");
			}
			@Override
			public boolean isListened(Level level) {
				return false;
			}
			@Override
			public void log(Level level, KeyedMessage msg) {}
			@Override
			public void log(Level level, String text) {}
			@Override
			public void log(Exception e) {}
		};
	}
	
	private PermissionChecker getDefaultPermissionChecker() {
		return new PermissionChecker() {
			
			@Override
			public void check(Permission permission, Surrogate surrogate) throws T2DBException {
			}
			
			@Override
			public boolean check(Permission permission, Surrogate surrogate, boolean permissionRequired) throws T2DBException {
				return true;
			}
			
			@Override
			public void check(Permission permission, DBObject dBObject) throws T2DBException {
			}
			
			@Override
			public boolean check(Permission permission, DBObject dBObject, boolean permissionRequired) throws T2DBException {
				return true;
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T>ValueAccessMethods<T> getAccessMethods(ValueType<T> valueType) {
		ValueAccessMethods<?> accessMethods = am.get(valueType.getExternalRepresentation());
		return (ValueAccessMethods<T>) accessMethods;
	}

	@Override
	public <T>void setAccessMethods(String valueTypeExternalRepresentation, ValueAccessMethods<T> accessMethods) {
		am.put(valueTypeExternalRepresentation, (ValueAccessMethods<?>)accessMethods);
	}
	
	@Override
	public boolean isStrictNameSpaceMode() {
		return strictNameSpaceMode;
	}
	
	@Override
	public void setStrictNameSpaceMode(boolean strictNameSpaceMode) {
		if (strictNameSpaceMode != this.strictNameSpaceMode)
			getCache().clear(); // avoid a mixture of strict and loose names in the cache
		this.strictNameSpaceMode = strictNameSpaceMode;
	}

	@Override
	public Surrogate makeSurrogate(String string) throws T2DBException {
		return SurrogateImpl.makeSurrogate(this, string);
	}

	/**
	 * Check the validity of a surrogate for this database and a given database object type.
	 * 
	 * @param surrogate a surrogate
	 * @param required a database object type
	 * @throws T2DBException
	 */
	protected void checkSurrogate(Surrogate surrogate, DBObjectType required) throws T2DBException {
		if (surrogate.inConstruction() || !surrogate.getDBObjectType().equals(required))
			throw T2DBMsg.exception(D.D02102, surrogate.getDBObjectType().name(), required.name());
		if (!surrogate.getDBObjectType().equals(required) && surrogate.getDatabase() != this)
			throw T2DBMsg.exception(D.D02103, surrogate.getDatabase().getTopChronicle().getName(true),
					this.getTopChronicle().getName(true));
	}

	protected boolean isChronicleUpdatePolicyExtensionMandatory() {
		return false;
	}
	
	protected boolean isChronicleUpdatePolicyExtensionAllowed() {
		return true;
	}
	
	@Override
	public void configure(DatabaseConfiguration configuration) throws T2DBException {
		String parameter = configuration.getParameter(DatabaseBackend.DB_PARAM_Int_MAX_GAP, false);
		int maxGap = -1;
		try {
			if (parameter != null)
				maxGap = new Integer(parameter);
		} catch (Exception e) {
			throw T2DBMsg.exception(e, D.D00108, DatabaseBackend.DB_PARAM_Int_MAX_GAP, parameter);
		}
		if (maxGap > -1)
			TimeSeriesFactory.getInstance().setMaxGap(maxGap);
		
		int cacheSize = 0;
		float cacheLoadFactor = 0f;
		parameter = configuration.getParameter(DatabaseBackend.DB_PARAM_Int_CACHE_SIZE, false);
		try {
			cacheSize = parameter == null ? DB_PARAM_Int_CACHE_SIZE_DEFAULT : new Integer(parameter);
		} catch (Exception e) {
			throw T2DBMsg.exception(e, D.D00108, DatabaseBackend.DB_PARAM_Int_CACHE_SIZE, parameter);
		}
		parameter = configuration.getParameter(DatabaseBackend.DB_PARAM_Float_CACHE_LOAD_FACTOR, false);
		try {
			cacheLoadFactor = parameter == null ? DB_PARAM_Float_CACHE_LOAD_FACTOR_DEFAULT : new Float(parameter);
		} catch (Exception e) {
			throw T2DBMsg.exception(e, D.D00108, DatabaseBackend.DB_PARAM_Float_CACHE_LOAD_FACTOR, parameter);
		}
		if (cacheSize > 0) {
			if (cacheLoadFactor > 0f)
				cache = new DatabaseCacheImpl(this, cacheSize, cacheLoadFactor);
			else
				cache = new DatabaseCacheImpl(this, cacheSize);
		}
		
		parameter = configuration.getParameter(DatabaseBackend.DB_PARAM_Boolean_STRICT_NAME_SPACE, false);
		try {
			if (parameter == null)
				setStrictNameSpaceMode(DB_PARAM_Boolean_STRICT_NAME_SPACE_DEFAULT);
			else
				setStrictNameSpaceMode(new Boolean(parameter));
		} catch (Exception e) {
			throw T2DBMsg.exception(e, D.D00108, DatabaseBackend.DB_PARAM_Boolean_STRICT_NAME_SPACE, parameter);
		}
		
		eupx = null;
		parameter = configuration.getParameter(DatabaseBackend.DB_PARAM_Class_ChronicleUpdatePolicyExtension, false);
		try {
			if (parameter != null && ((String) parameter).length() > 0) {
				if (!isChronicleUpdatePolicyExtensionAllowed())
					throw T2DBMsg.exception(D.D00107);
				Class<?> extClass;
				extClass = Class.forName((String)parameter);
				eupx = (ChronicleUpdatePolicyExtension) extClass.newInstance();
			}
		} catch (Exception e) {
			throw T2DBMsg.exception(e, D.D00108, DatabaseBackend.DB_PARAM_Class_ChronicleUpdatePolicyExtension, parameter);
		}
		
		if (eupx == null && isChronicleUpdatePolicyExtensionMandatory())
			throw T2DBMsg.exception(D.D00109, DatabaseBackend.DB_PARAM_Class_ChronicleUpdatePolicyExtension);
		
		permissionChecker = null;
		parameter = configuration.getParameter(DatabaseBackend.DB_PARAM_Class_PermissionChecker, false);
		try {
			if (parameter != null && ((String) parameter).length() > 0) {
				Class<?> extClass;
				extClass = Class.forName((String)parameter);
				permissionChecker = (PermissionChecker) extClass.newInstance();
			}
		} catch (Exception e) {
			throw T2DBMsg.exception(e, D.D00108, DatabaseBackend.DB_PARAM_Class_PermissionChecker, parameter);
		}
		validateNameSpace();
	}
	
	@Override
	public boolean isBuiltIn(AttributeDefinition<?> def) {
		return def.getNumber() <= MAX_MAGIC_NR;
	}

	@Override
	public DatabaseCache getCache() {
		return cache;
	}
	
	@Override
	public NamingPolicy getNamingPolicy() {
		return nm;
	}

	@Override
	public ChronicleUpdatePolicy getChronicleUpdatePolicy() {
		if (eup == null)
			eup = new ChronicleUpdatePolicyImpl(this, eupx);
		return eup;
	}
	
	@Override
	public SchemaUpdatePolicy getSchemaUpdatePolicy() {
		if (sup == null)
			sup = new SchemaUpdatePolicyImpl(this);
		return sup;
	}
	
	@Override
	public UpdateEventPublisher getUpdateEventPublisher() {
		if (eventHub == null)
			eventHub = new UpdateEventPublisherImpl();
		return eventHub;
	}
	
	/**
	 * Short cut method to publish a deferred event.
	 * @param event an update event
	 */
	protected void publish(UpdateEvent event) {
		getUpdateEventPublisher().publish(event, false);
	}
	
	@Override
	public void setMessageListener(MessageListener listener) {
		this.messageListener = listener == null ? getDefaultMessageListener() : listener;
		if (cache!= null)
			((DatabaseCacheImpl) cache).setMessageListener(messageListener);
	}

	@Override
	public MessageListener getMessageListener() {
		return messageListener;
	}

	/**
	 * Return the permission checker. The result is never null.
	 * The default permission checker allows all operations.
	 * 
	 * @return the permission checker
	 */
	protected PermissionChecker getPermissionChecker() {
		if (permissionChecker == null)
			permissionChecker = getDefaultPermissionChecker();
		return permissionChecker;
	}

	@Override
	public boolean check(Permission permission, DBObject dBObject,	boolean permissionRequired) throws T2DBException {
		return getPermissionChecker().check(permission, dBObject, permissionRequired);
	}

	@Override
	public void check(Permission permission, DBObject dBObject)	throws T2DBException {
		getPermissionChecker().check(permission, dBObject);
	}

	@Override
	public boolean check(Permission permission, Surrogate surrogate, boolean permissionRequired) throws T2DBException {
		return getPermissionChecker().check(permission, surrogate, permissionRequired);
	}

	@Override
	public void check(Permission permission, Surrogate surrogate) throws T2DBException {
		getPermissionChecker().check(permission, surrogate);
	}

	private void validateNameSpace() throws T2DBException {
		String name = getTopChronicle().getName(true);
		name = getNamingPolicy().checkSimpleName(name, false);
		if (!isStrictNameSpaceMode()) {
			Collection<Chronicle> topMembers = null;
			try {
				topMembers = topChronicle.getMembers();
			} catch (T2DBException e) {
				// probably the database has not been set up yet, so nothing wrong
				topMembers = new ArrayList<Chronicle>();
			}
			// check that there is no top member named like the name space
			for (Chronicle en : topMembers) {
				if (en.getName(false).equals(name)){
					throw T2DBMsg.exception(D.D00110, name);
				}
			}
		}
	}
	
	@Override
	public Chronicle getTopChronicle() {
		if (topChronicle == null)
			throw new IllegalStateException("bug: name space not available");
		return topChronicle;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * When an chronicle exists for the name, throw an exception if reading it is
	 * not permitted.
	 */
	@Override
	public Chronicle getChronicle(String name, boolean mustExist) throws T2DBException {
		return getTopChronicle().findChronicle(name, mustExist);
	}
	
	@Override
	public <T> UpdatableSeries<T> getUpdatableSeries(String name, boolean mustExist) throws T2DBException {
		String[] es = getNamingPolicy().split(name);
		String chronicleName = es[0];
		String seriesName = es[1];
		UpdatableSeries<T> series = null;
		if (chronicleName == null) {
			throw T2DBMsg.exception(D.D50116, name);
		} else {
			Chronicle ent = getChronicle(chronicleName, mustExist);
			if (ent != null) {
				UpdatableChronicle updEnt = ent.edit();
				series = updEnt.updateSeries(seriesName);
				if (series == null && mustExist)
					series = updEnt.createSeries(seriesName);
			}
		}
		return series;
	}

	@Override
	public <T> Series<T> getSeries(String name, boolean mustExist) throws T2DBException {
		Series<T> series = null;
		String[] split = getNamingPolicy().split(name);
		if (split[0] == null)
			throw T2DBMsg.exception(D.D50116, name);
		Chronicle chronicle = getChronicle(split[0], mustExist);
		if (chronicle != null) {
			Series<T>[] s = chronicle.getSeries(new String[]{split[1]}, null, mustExist);
			series = s[0];
		}
		if (series == null && mustExist)
			throw T2DBMsg.exception(D.D50106, name);
		return series;
	}

	@Override
	public Chronicle getChronicle(Surrogate surrogate) throws T2DBException {
		checkSurrogate(surrogate, DBObjectType.CHRONICLE);
		Chronicle chronicle = new ChronicleImpl(surrogate);
		chronicle.getName(false); // side-effect: make sure it exists
		return chronicle;
	}
	
	@Override
	public <T> Range getRange(Series<T> series) throws T2DBException {
		return ((ValueTypeImpl<T>) series.getValueType()).getAccessMethods().getRange(series);
	}

	@Override
	public <T> long getValues(Series<T> series, Range range, TimeAddressable<T> ts) throws T2DBException {
		return ((ValueTypeImpl<T>) series.getValueType()).getAccessMethods().getValues(series, range, ts);
	}
	
	@Override
	public <T> Observation<T> getFirstObservation(Series<T> series, TimeIndex time) throws T2DBException {
		return ((ValueTypeImpl<T>) series.getValueType()).getAccessMethods().getFirst(series, time);
	}

	@Override
	public <T> Observation<T> getLastObservation(Series<T> series, TimeIndex time) throws T2DBException {
		return ((ValueTypeImpl<T>) series.getValueType()).getAccessMethods().getLast(series, time);
	}
	
	@Override
	public <T>boolean update(UpdatableSeries<T> series, Range range) throws T2DBException {
		boolean done = ((ValueTypeImpl<T>) series.getValueType()).getAccessMethods().updateSeries(series, range, getChronicleUpdatePolicy());
		if (done)
			publish(new UpdateEventImpl(UpdateEventOperation.MODIFY, series));
		return done;
	}

	@Override
	public <T>boolean deleteValue(UpdatableSeries<T> series, TimeIndex t) throws T2DBException {
		boolean done = ((ValueTypeImpl<T>) series.getValueType()).getAccessMethods().deleteValue(series, t, getChronicleUpdatePolicy());
		if (done)
			publish(new UpdateEventImpl(UpdateEventOperation.MODIFY, series));
		return done;
	}
	
	@Override
	public <T>long update(UpdatableSeries<T> series, TimeAddressable<T> values) throws T2DBException {
		long count = ((ValueTypeImpl<T>) series.getValueType()).getAccessMethods().updateValues(series, values, getChronicleUpdatePolicy());
		if (count > 0) {
			UpdateEventImpl event = new UpdateEventImpl(UpdateEventOperation.MODIFY, series);
			if (getMessageListener().isListened(Level.FINER))
				event.withComment(values.toString());
			publish(event);
		}
		return count;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method consolidates the chain of schemas by following links to the
	 * parent schema until there is no parent. The result is also known as
	 * "runtime schema".
	 * <p>
	 * Historical note. In a previous version of this system, chronicle level
	 * attributes were merged into series attributes. This is not done any more.
	 * 
	 */
	@Override
	public Schema getSchema(Surrogate surrogate) throws T2DBException {
		checkSurrogate(surrogate, DBObjectType.SCHEMA);
		return getUpdatableSchema(surrogate).resolve();
	}
	
	@Override
	public Property<?> getProperty(String name, boolean mustExist) throws T2DBException {
		Property<?> prop = getProperty(name);
		if (prop == null && mustExist)
			throw T2DBMsg.exception(D.D20109, name);
		return prop;
	}
	
	@Override
	public Property<?> getSymbolBuiltInProperty() throws T2DBException {
		return getCache().lookUpProperty(DatabaseBackend.BUILTIN_PROP_SYMBOL);
	}
	
	@Override
	public Property<?> getTimeDomainBuiltInProperty() throws T2DBException {
		return getCache().lookUpProperty(DatabaseBackend.BUILTIN_PROP_TIME_DOMAIN);
	}

	@Override
	public Property<?> getTypeBuiltInProperty() throws T2DBException {
		return getCache().lookUpProperty(DatabaseBackend.BUILTIN_PROP_TYPE);
	}

	@Override
	public Property<?> getSparsityBuiltInProperty() throws T2DBException {
		return getCache().lookUpProperty(DatabaseBackend.BUILTIN_PROP_SPARSITY);
	}
	
	@Override
	public Collection<Schema> getSchemas(String pattern) throws T2DBException {
		Collection<Surrogate> keys = getSchemaSurrogates(pattern);
		Collection<Schema> result = new ArrayList<Schema>(keys.size());
		for (Surrogate surrogate : keys) {
			try {
				result.add(getSchema(surrogate));
			} catch(KeyedException e) {
				// log but continue, else no chance to inspect the problem 
				getMessageListener().log(e);
			}
		}
		return result;
	}
	
	@Override
	public Collection<UpdatableSchema> getUpdatableSchemas(String pattern) throws T2DBException {
		Collection<Surrogate> keys = getSchemaSurrogates(pattern);
		Collection<UpdatableSchema> result = new ArrayList<UpdatableSchema>(keys.size());
		for (Surrogate surrogate : keys) {
			result.add(getUpdatableSchema(surrogate));
		}
		return result;
	}
	
	@Override
	public UpdatableSchema getUpdatableSchema(Schema schema) throws T2DBException {
		if (schema instanceof UpdatableSchema)
			throw new IllegalArgumentException("schema is an UpdatableSchema"); // bug: should not have been called
		return getUpdatableSchema(schema.getSurrogate());
	}

	@Override
	public <T> UpdatableValueType<T> createValueType(String name, boolean restricted, String scannerClassOrKeyword) throws T2DBException {
		return new UpdatableValueTypeImpl<T>(name, restricted, scannerClassOrKeyword, null,  
				new SurrogateImpl(this, DBObjectType.VALUE_TYPE, null));
	}
	
	@Override
	public <T> UpdatableProperty<T> createProperty(String name,	ValueType<T> valueType, boolean indexed) throws T2DBException {
		Surrogate	surrogate = new SurrogateImpl(this, DBObjectType.PROPERTY, null);
		return new UpdatablePropertyImpl<T>(name, valueType, indexed, surrogate);
	}
	
	@Override
	public UpdatableSchema createSchema(String name, String nameOfBase) throws T2DBException {
		if (getSchemas(name).size() > 0)
			throw T2DBMsg.exception(D.D30108, name);
		UpdatableSchema base = null;
		if (nameOfBase != null && nameOfBase.length() > 0) {
			Collection<UpdatableSchema> list = getUpdatableSchemas(nameOfBase);
			if (list.size() != 1)
				throw T2DBMsg.exception(D.D30109, name, nameOfBase);
			base = list.iterator().next();
		}
		Surrogate surrogate = new SurrogateImpl(this, DBObjectType.SCHEMA, null);
		return new UpdatableSchemaImpl(name, base, null, null, surrogate);
	}
	
	@Override
	public int hashCode() {
		if (hashCode >= 0)
			return  hashCode;
		else
			return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return toString().equals(obj.toString());
	}

	@Override
	public String toString() {
		try {
			return getTopChronicle().getName(true);
		} catch (Exception e){
			throw new RuntimeException("bug", e);
		}
	}

}