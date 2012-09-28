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
 * Type: DatabaseBackend
 * Version: 1.1.0
 */
package ch.agent.crnickl.impl;

import java.util.Collection;
import java.util.List;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.api.Attribute;
import ch.agent.crnickl.api.AttributeDefinition;
import ch.agent.crnickl.api.Chronicle;
import ch.agent.crnickl.api.Database;
import ch.agent.crnickl.api.Property;
import ch.agent.crnickl.api.Schema;
import ch.agent.crnickl.api.Series;
import ch.agent.crnickl.api.SeriesDefinition;
import ch.agent.crnickl.api.Surrogate;
import ch.agent.crnickl.api.UpdatableChronicle;
import ch.agent.crnickl.api.UpdatableProperty;
import ch.agent.crnickl.api.UpdatableSchema;
import ch.agent.crnickl.api.UpdatableSeries;
import ch.agent.crnickl.api.UpdatableValueType;
import ch.agent.crnickl.api.ValueType;
import ch.agent.t2.time.Range;
import ch.agent.t2.time.TimeIndex;
import ch.agent.t2.timeseries.Observation;
import ch.agent.t2.timeseries.TimeAddressable;

/**
 * DatabaseBackend extends {@link Database} with methods used only in the implementation.
 * Unless commented otherwise objects needing to be accessed must exist and be accessible
 * to the user, objects to be created may not already exist, and objects to be 
 * deleted may not have important dependencies.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.1.0
 */
public interface DatabaseBackend extends Database, PermissionChecker {
	
	public static final String BUILTIN_PROP_SYMBOL = "Symbol";
	public static final String BUILTIN_PROP_TIME_DOMAIN = "Calendar";
	public static final String BUILTIN_PROP_TYPE = "Type";
	public static final String BUILTIN_PROP_SPARSITY = "Sparsity";
	
	/**
	 * The number for the NAME built-in value type and corresponding property and attribute.
	 */
	public static final int MAGIC_NAME_NR = 1;
	/**
	 * The number for the TYPE built-in value type and corresponding property and attribute.
	 */
	public static final int MAGIC_TYPE_NR = 2;
	/**
	 * The number for the TIMEDOMAIN built-in value type and corresponding property and attribute.
	 */
	public static final int MAGIC_TIMEDOMAIN_NR = 3;
	/**
	 * The number for the SPARSITY boolean built-in value type and corresponding property and attribute.
	 */
	public static final int MAGIC_SPARSITY_NR = 4;
	/**
	 * The largest reserved number for value types, properties and attributes. 
	 */
	public static final int MAX_MAGIC_NR = 9;

	/** 
	 * The name of the external parameter specifying the threshold for indexing names.
	 * <p>
	 * @see #getNameIndexThreshold()
	 */
	public static final String DB_PARAM_Int_NAME_INDEX_THRESHOLD = "dbNameIndexThreshold";
	/** 
	 * The name of the external parameter specifying the cache size. The actual cache size is often larger,
	 * because after taking the load factor into account, the actual size must be a power of 2.
	 * For example, a cache size of 96 with a load factor of 0.75, corresponds to 128, a power of 2;
	 * specifying 97 with a load factor of 0.75 would result in an actual size of 256.
	 */
	public static final String DB_PARAM_Int_CACHE_SIZE = "dbCacheSize";
	/** 
	 * The name of the external parameter specifying the cache load factor.
	 */
	public static final String DB_PARAM_Float_CACHE_LOAD_FACTOR = "dbCacheLoadFactor";
	/** 
	 * The name of the external parameter specifying whether to prefix full names with the database name.
	 * <p>
	 * @see #isStrictNameSpaceMode()
	 */
	public static final String DB_PARAM_Boolean_STRICT_NAME_SPACE = "dbStrictNameSpace";
	/** 
	 * The name of the external parameter naming the class implementing ChronicleUpdatePolicyExtension.
	 */
	public static final String DB_PARAM_Class_ChronicleUpdatePolicyExtension = "dbChronicleUpdatePolicyExtension";
	/** 
	 * The name of the external parameter naming the class implementing PermissionChecker.
	 */
	public static final String DB_PARAM_Class_PermissionChecker = "dbPermissionChecker";
	/**
	 * The default threshold for the name index.
	 */
	public static final int DB_PARAM_Int_NAME_INDEX_THRESHOLD_DEFAULT = 5;
	/**
	 * The default cache size.
	 * <p>
	 * 
	 */
	public static final int DB_PARAM_Int_CACHE_SIZE_DEFAULT = 96; // 96/0.75 = 128, a power of 2 so won't be tweaked
	/**
	 * The default cache load factor.
	 */
	public static final float DB_PARAM_Float_CACHE_LOAD_FACTOR_DEFAULT = 0.75f;
	/**
	 * By default, the database name does not need to be prefixed to full names.
	 */
	public static final boolean DB_PARAM_Boolean_STRICT_NAME_SPACE_DEFAULT = false;

	/**
	 * Return the threshold above which <em>indexed</em> access by name to series and attributes 
	 * will be supported. A negative number is interpreted as a request not to provide
	 * indexed access.
	 * 
	 * @return the threshold above which to provide by-name indexing in schemas
	 */
	int getNameIndexThreshold();

	/**
	 * Test if strict name space mode has been configured. In strict name space mode, 
	 * full names of chronicles start with the name of the database.
	 * 
	 * @return true if strict name space mode is configured
	 */
	boolean isStrictNameSpaceMode();

	/**
	 * Set strict name space mode. In strict name space mode, full names of
	 * chronicles start with the name of the database. By default strict mode is
	 * off.
	 * 
	 * @param strictNameSpaceMode
	 *            if true set strict name space mode
	 */
	void setStrictNameSpaceMode(boolean strictNameSpaceMode);
	
	/**
	 * Return the series access methods object for the given value type. Return null
	 * when no access methods have been configured for the type.
	 * 
	 * @param valueType
	 *            a value type
	 * @return an access methods object or null
	 */
	<T>ValueAccessMethods<T> getAccessMethods(ValueType<T> valueType);
	
	/**
	 * Set the series access methods object for the given value type. This
	 * method is used during database configuration.
	 * <p>
	 * Value types are identified using {@link ValueType#getExternalRepresentation()}.
	 * 
	 * @param valueTypeExternalRepresentation a string used as external representation of value type
	 * @param accessMethods an access methods object
	 */
	<T>void setAccessMethods(String valueTypeExternalRepresentation, ValueAccessMethods<T> accessMethods);

	/**
	 * Test whether an attribute definition is built-in.
	 * @param def an attribute definition
	 * @return true if the attribute definition is built-in else false
	 */
	boolean isBuiltIn(AttributeDefinition<?> def);
	
	/**
	 * Return the database cache.
	 * The result is null when the cache is not configured.
	 * 
	 * @return the database cache or null
	 */
	DatabaseCache getCache();
	
	/**
	 * Return the chronicle update policy object.
	 * 
	 * @return the chronicle update policy object
	 */
	ChronicleUpdatePolicy getChronicleUpdatePolicy();

	/**
	 * Return the schema update policy.
	 * 
	 * @return the schema update policy
	 */
	SchemaUpdatePolicy getSchemaUpdatePolicy();
	
	/**
	 * Get the property with the given name from the database. 
	 * The result is null when there is no such property.
	 * 
	 * @param name
	 *            a string
	 * @return a property or null
	 * @throws T2DBException
	 */
	Property<?> getProperty(String name) throws T2DBException;
	
	/**
	 * Create a property in the database.
	 * 
	 * @param property a property
	 * @throws T2DBException
	 */
	void create(UpdatableProperty<?> property) throws T2DBException;
	
	/**
	 * Delete a property from the database. 
	 * The property may not be a built-in property.
	 * 
	 * @param property a property
	 * @throws T2DBException
	 */
	void deleteProperty(UpdatableProperty<?> property) throws T2DBException;
	
	/**
	 * Modify a property in the database.
	 * 
	 * @param property a property
	 * @throws T2DBException
	 */
	void update(UpdatableProperty<?> property) throws T2DBException;
	
	/**
	 * Create a value type in the database.
	 * 
	 * @param valueType a value type
	 * @throws T2DBException
	 */
	<T>void create(UpdatableValueType<T> valueType) throws T2DBException;
	
	/**
	 * Delete a value type from the database. The value type may 
	 * not be a built-in value type.
	 * 
	 * @param valueType a value type
	 * @throws T2DBException
	 */
	void deleteValueType(UpdatableValueType<?> valueType) throws T2DBException;
	
	/**
	 * Update a value type. The name can be updated. In a restricted value
	 * type, values can be added, their description can be modified, and they
	 * can be deleted. It is not allowed to delete values in use.
	 * 
	 * @param valueType a value type
	 * @throws T2DBException
	 */
	void update(UpdatableValueType<?> valueType) throws T2DBException;
	
	/**
	 * Create a chronicle in the database. The method saves the chronicle's name, description,
	 * collection, and schema. It does not saves its attributes or series. The
	 * chronicle's surrogate is upgraded.
	 * 
	 * @param chronicle a chronicle
	 * @throws T2DBException
	 */
	void create(UpdatableChronicle chronicle) throws T2DBException;
	
	/**
	 * Remove the value and description for an attribute of a chronicle.
	 * The attribute keeps its default value as defined in the schema.
	 * 
	 * @param chronicle a chronicle
	 * @param def an attribute definition
	 * @throws T2DBException
	 */
	void deleteAttributeValue(UpdatableChronicle chronicle, AttributeDefinition<?> def) throws T2DBException;

	/**
	 * Set the value and description of an attribute of a chronicle.
	 * The value parameter corresponds to the actual attribute 
	 * value formatted by {@link ValueType#toString(Object)}.
	 * 
	 * @param chronicle a chronicle
	 * @param def an attribute definition
	 * @param value a string
	 * @param description a string
	 * @throws T2DBException
	 */
	void update(UpdatableChronicle chronicle, AttributeDefinition<?> def, String value, String description) throws T2DBException;

	/**
	 * Update the name and description of a chronicle.
	 * 
	 * @param chronicle a chronicle
	 * @throws T2DBException
	 */
	void update(UpdatableChronicle chronicle) throws T2DBException;
	
	/**
	 * Delete a chronicle from the database. The chronicle must not be in use.
	 * 
	 * @param chronicle
	 * @throws T2DBException
	 */
	void deleteChronicle(UpdatableChronicle chronicle) throws T2DBException;

	/**
	 * Get a chronicle from the database.
	 * 
	 * @param chronicle a chronicle
	 * @return the chronicle, fresh out of the database 
	 * @throws T2DBException
	 */
	Chronicle getChronicle(Chronicle chronicle) throws T2DBException;
	
	/**
	 * Get the chronicle with the given parent and simple name from the database.
	 * 
	 * @param parent a chronicle
	 * @param simpleName a simple name
	 * @return a chronicle
	 * @throws T2DBException
	 */
	Chronicle getChronicleOrNull(Chronicle parent, String simpleName) throws T2DBException;

	/**
	 * Get all chronicles with a given parent.
	 * 
	 * @param parent a chronicle
	 * @return all direct children chronicles of the parent
	 * @throws T2DBException
	 */
	Collection<Chronicle> getChroniclesByParent(Chronicle parent) throws T2DBException;

	/**
	 * Get a number of chronicles with a given property value from the database.
	 * 
	 * @param property a property
	 * @param value a value
	 * @param maxSize the maximum size of the result 
	 * @return a list of chronicles
	 * @throws T2DBException
	 */
	<T>List<Chronicle> getChroniclesByAttributeValue(Property<T> property, T value, int maxSize) throws T2DBException;
	
	/**
	 * Get an array of series in a chronicle from the database. The numbers
	 * identify the series within the schema. Non-positive numbers are skipped,
	 * with the corresponding element null in the result. An array of names must
	 * be passed. These are the series names as defined in the schema and are
	 * needed to construct the series object in the result.
	 * 
	 * @param chronicle
	 *            a chronicle
	 * @param names
	 *            an array of names
	 * @param numbers
	 *            an array of numbers
	 * @return an array of series
	 * @throws T2DBException
	 */
	<T> Series<T>[] getSeries(Chronicle chronicle, String[] names, int[] numbers) throws T2DBException;
	
	/**
	 * Return true if a value can be found for the attribute in one of
	 * chronicles listed. If no value can be found return false. If there is 
	 * a value for more than one chronicle, the first value found in the list wins.
	 * 
	 * @param chronicles
	 *            a list of chronicles
	 * @param attribute
	 *            an attribute
	 * @return true if a value was found for the attribute
	 * @throws T2DBException
	 */
	boolean getAttributeValue(List<Chronicle> chronicles, Attribute<?> attribute) throws T2DBException;
	
	/**
	 * Load values into the time series in the range specified. To load all
	 * values, specify a null range. Return the number of values loaded.
	 * 
	 * @param series
	 *            a series
	 * @param range
	 *            a range or null
	 * @param ts
	 *            a time series
	 * @return the number of values loaded
	 * @throws T2DBException
	 */
	<T> long getValues(Series<T> series, Range range, TimeAddressable<T> ts)
			throws T2DBException;
	
	/**
	 * Return the observation at a given time index or the first following
	 * observation. If the time index is null return the first observation.
	 * Return null if there is nothing.
	 * 
	 * @param time
	 *            a time index
	 * @return an observation or null
	 * @throws T2DBException
	 */
	<T>Observation<T> getFirstObservation(Series<T> series, TimeIndex time) throws T2DBException;
	
	/**
	 * Return the observation at a given time index or the last preceding
	 * observation. If the time index is null return the last observation.
	 * Return null if there is nothing.
	 * 
	 * @param time
	 *            a time index
	 * @return an observation or null
	 * @throws T2DBException
	 */
	<T> Observation<T> getLastObservation(Series<T> serie, TimeIndex time)
			throws T2DBException;
	
	/**
	 * Return the range of the series. The result is never null. When the series
	 * has no values the range is empty.
	 * 
	 * @param series
	 *            a series
	 * @return a range
	 * @throws T2DBException
	 */
	<T> Range getRange(Series<T> series) throws T2DBException;
	
	/**
	 * Create a series in the database.
	 * 
	 * @param series a series
	 * @throws T2DBException
	 */
	<T>void create(UpdatableSeries<T> series) throws T2DBException;
	
	/**
	 * Update a series in the database with values from a time series.
	 * 
	 * @param series a series
	 * @param values a time series of values
	 * @return the number of values updated
	 * @throws T2DBException
	 */
	<T>long update(UpdatableSeries<T> series, TimeAddressable<T> values) throws T2DBException;
	
	/**
	 * Reduce the range of a series in the database. Return true if something was done.
	 * 
	 * @param series a series
	 * @param range a range
	 * @return true if the range was modified
	 * @throws T2DBException
	 */
	<T>boolean update(UpdatableSeries<T> series, Range range) throws T2DBException;
	
	/**
	 * Delete a value from a series in the database.
	 * 
	 * @param series a series
	 * @param t a time index
	 * @return true if something done
	 * @throws T2DBException
	 */
	<T>boolean deleteValue(UpdatableSeries<T> series, TimeIndex t) throws T2DBException;
	
	/**
	 * Delete a series from the database.
	 * 
	 * @param series a series
	 * @throws T2DBException
	 */
	<T>void deleteSeries(UpdatableSeries<T> series) throws T2DBException;
	
	/**
	 * Create a schema in the database.
	 * 
	 * @param schema a schema
	 * @throws T2DBException
	 */
	void create(UpdatableSchema schema) throws T2DBException;
	
	/**
	 * Update a schema in the database.
	 * 
	 * @param schema
	 *            a schema
	 * @throws T2DBException
	 */
	void update(UpdatableSchema schema) throws T2DBException;

	/**
	 * Delete a schema from the database.
	 * 
	 * @param schema a schema
	 * @throws T2DBException
	 */
	void deleteSchema(UpdatableSchema schema) throws T2DBException;
	
	/**
	 * Get a schema from the database.
	 * 
	 * @param surrogate a schema's surrogate
	 * @return a schema
	 * @throws T2DBException
	 */
	UpdatableSchema getUpdatableSchema(Surrogate surrogate) throws T2DBException;
	
	/**
	 * Return a collection of schema surrogates with names matching a pattern.
	 * 
	 * @param pattern a simple pattern where "*" stands for zero or more characters 
	 * @return a collection of schema surrogates 
	 * @throws T2DBException
	 */
	Collection<Surrogate> getSchemaSurrogates(String pattern) throws T2DBException;
	
	/**
	 * Return the updatable schema corresponding to a schema.
	 * 
	 * @param schema a schema
	 * @return an updatable schema
	 * @throws T2DBException
	 */
	UpdatableSchema getUpdatableSchema(Schema schema) throws T2DBException;
	
	/**
	 * Return all chronicles referencing a given schema.
	 * The result is a collection of {@link Surrogate} objects.
	 * <p>
	 * This is technical method used in schema management.
	 * 
	 * @param schema a schema
	 * @return a collection of chronicle surrogates
	 * @throws T2DBException
	 */
	Collection<Surrogate> findChronicles(Schema schema) throws T2DBException;

	/**
	 * Return all chronicles with an attribute value for the given property and
	 * schema.
	 * <p>
	 * This is technical method used in schema management.
	 * 
	 * @param property
	 *            a property
	 * @param schema
	 *            a schema
	 * @return a collection of chronicle surrogates
	 * @throws T2DBException
	 */
	Collection<Surrogate> findChronicles(Property<?> property, Schema schemas) throws T2DBException;

	/**
	 * Return all chronicles with a series corresponding to the series definition 
	 * and with a schema dependent on the given schema.
	 * <p>
	 * This is technical method used in schema management.
	 * 
	 * @param ss a series definition
	 * @param schema a schema
	 * @return a collection of chronicle surrogates
	 * @throws T2DBException
	 */
	Collection<Surrogate> findChronicles(SeriesDefinition ss, Schema schema) throws T2DBException;
	
}