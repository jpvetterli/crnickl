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
 * Package: ch.agent.crnickl.api
 * Type: Database
 * Version: 1.0.0
 */
package ch.agent.crnickl.api;

import java.util.Collection;
import java.util.logging.Level;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.api.ValueType.StandardValueType;

/**
 * Database is the hub of CrNiCKL (pronounced <q>chronicle</q>). 
 * A client gets one or more Database objects using
 * {@link DatabaseFactory}.
 * Five interfaces play an important role in the data model underlying 
 * CrNiCKL:
 * <ul>
 * <li>{@link Chronicle}
 * <li>{@link Series}
 * <li>{@link Schema}
 * <li>{@link Property}
 * <li>{@link ValueType}
 * </ul>
 * These interfaces provide read access to the objects.
 * When writing to the database, read/write versions of the interfaces 
 * are required:
 * <ul>
 * <li>{@link UpdatableChronicle}
 * <li>{@link UpdatableSeries}
 * <li>{@link UpdatableSchema}
 * <li>{@link UpdatableProperty}
 * <li>{@link UpdatableValueType}
 * </ul>
 * 
 * Read/write objects are obtained from the read only objects using the <code>edit()</code> method. 
 * <p>
 * Two methods are provided for transactions management: {@link #commit()} and {@link #rollback()}.
 * These methods are not used internally by CrNiCKL. Managing transactions and implementing
 * transaction boundaries is the client's responsibility.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public interface Database {

	/**
	 * Configure the database.
	 * 
	 * @param configuration a database configuration
	 * @throws T2DBException
	 */
	void configure(DatabaseConfiguration configuration) throws T2DBException;
	
	/**
	 * Write all updates since the last commit or rollback to permanent storage.
	 * It is the client's responsibility to call this method.
	 * 
	 * @throws T2DBException
	 */
	void commit() throws T2DBException;

	/**
	 * Forget all updates since the last commit or rollback.
	 * It is the client's responsibility to call this method.
	 * 
	 * @throws T2DBException
	 */
	void rollback() throws T2DBException;
	
	/**
	 * Return the naming policy of the database.
	 * 
	 * @return a naming policy
	 */
	NamingPolicy getNamingPolicy();

	/**
	 * Return the event publisher for this database.
	 * 
	 * @return an update event publisher
	 */
	UpdateEventPublisher getUpdateEventPublisher();
	
	/**
	 * Set the message listener used for logging messages. 
	 * When no message listener has been set, or when a null listener is set, 
	 * the database installs a default listener which always drops all messages.
	 * 
	 * @param listener a message listener or null
	 */
	void setMessageListener(MessageListener listener);
	
	/**
	 * Return the message listener. The result is never null.
	 * The default listener behaves like a listener with
	 * a filtering level set to {@link Level#OFF}.
	 * 
	 * @return the message listener
	 */
	MessageListener getMessageListener();
	
	/**
	 * Return the chronicle identified by a surrogate.
	 * 
	 * @param surrogate a surrogate
	 * @return a chronicle
	 * @throws T2DBException
	 */
	Chronicle getChronicle(Surrogate surrogate) throws T2DBException;
	
	/**
	 * Return the series identified by a surrogate.
	 * 
	 * @param surrogate a surrogate
	 * @return a series
	 * @throws T2DBException
	 */
	<T>Series<T> getSeries(Surrogate surrogate) throws T2DBException;
	
	/**
	 * Return the schema identified by a surrogate.
	 * 
	 * @param surrogate a surrogate
	 * @return a schema
	 * @throws T2DBException
	 */
	Schema getSchema(Surrogate surrogate) throws T2DBException;
	
	/**
	 * Return the property identified by a surrogate.
	 * 
	 * @param surrogate a surrogate
	 * @return a property
	 * @throws T2DBException
	 */
	Property<?> getProperty(Surrogate surrogate) throws T2DBException;
	
	/**
	 * Return the value type identified by a surrogate.
	 * 
	 * @param surrogate a surrogate
	 * @return a value type
	 * @throws T2DBException
	 */
	<T>ValueType<T> getValueType(Surrogate surrogate) throws T2DBException;
	
	/**
	 * Return the chronicle with the given full name. 
	 * The <code>mustExist</code> parameter determines behavior when nothing is
	 * found: exception or null result.
	 * 
	 * @param name a full name
	 * @param mustExist
	 *            if true throw an exception instead of returning null
	 * @return a chronicle or null (only when mustExist is false)
	 * @throws T2DBException
	 */
	Chronicle getChronicle(String name, boolean mustExist) throws T2DBException;
	
	/**
	 * Return the series with the given full name. If the series is defined but
	 * has not yet been set up, the result is null. If the series is undefined
	 * in the schema, the result is null or an exception is thrown depending on
	 * the value of the <code>mustExist</code> parameter.
	 * 
	 * @param <T> the data type of the underlying time series
	 * @param name a full name
	 * @param mustExist
	 *            if true throw an exception instead of returning null
	 * @return a series or null (only when mustExist is false)
	 * @throws T2DBException
	 */
	<T>Series<T> getSeries(String name, boolean mustExist) throws T2DBException;
	
	/**
	 * Return an updatable series. If the series exists and is accessible, it is
	 * returned. If it does not exist, the outcome depends on the
	 * <code>mustExist</code> parameter. If set, the series is created on the
	 * fly, unless something is wrong, in which case an exception is thrown. If
	 * not set the method returns null (unless something is wrong). Something is
	 * wrong when the chronicle does not exist or is not accessible, or when the
	 * series is not defined in the schema.
	 * <p>
	 * After a new series has been created, the client must execute
	 * {@link Updatable#applyUpdates()} on the series's chronicle or
	 * on the series itself.
	 * 
	 * @param <T>
	 *            the data type of the underlying time series
	 * @param name
	 *            a full name
	 * @param mustExist
	 *            if true throw an exception instead of returning null
	 * @return an updatable series or null (only when mustExist is false)
	 * @throws T2DBException
	 */
	<T>UpdatableSeries<T> getUpdatableSeries(String name, boolean mustExist) throws T2DBException;
	
	/**
	 * Return the top chronicle. The top chronicle is virtual, it is not
	 * stored in the database. The name of the top chronicle is the name
	 * given to the database during database configuration. The top
	 * chronicle is also known as the <em>name space</em>.
	 * 
	 * @return the top chronicle
	 */
	Chronicle getTopChronicle();
	
	/**
	 * Return properties with names matching a pattern. Patterns use
	 * a simple syntax, with an asterisk standing for zero or more
	 * characters.
	 * 
	 * @param pattern a name pattern
	 * @return a collection of properties
	 * @throws T2DBException
	 */
	Collection<Property<?>> getProperties(String pattern) throws T2DBException;
	
	/**
	 * Return the property with the given name. If it does not exist return null
	 * or throw an exception, depending on the value of the parameter
	 * <code>mustExist</code>.
	 * 
	 * @param name
	 *            the name of the property
	 * @param mustExist
	 *            if true throw an exception instead of returning null
	 * @return a property or null (only when mustExist is false)
	 * @throws T2DBException
	 */
	Property<?> getProperty(String name, boolean mustExist) throws T2DBException;
	
	/**
	 * Return the built-in property for symbols.
	 * 
	 * @return the symbol property
	 * @throws T2DBException
	 */
	Property<?> getSymbolBuiltInProperty() throws T2DBException;
	
	/**
	 * Return the built-in property for time domains.
	 * 
	 * @return the time domain property
	 * @throws T2DBException
	 */
	Property<?> getTimeDomainBuiltInProperty() throws T2DBException;
	
	/**
	 * Return the built-in property for value types.
	 * 
	 * @return the value type property
	 * @throws T2DBException
	 */
	Property<?> getTypeBuiltInProperty() throws T2DBException;

	/**
	 * Return the built-in property for sparsity.
	 * 
	 * @return the sparsity property
	 * @throws T2DBException
	 */
	Property<?> getSparsityBuiltInProperty() throws T2DBException;
	
	/**
	 * Create a property with the parameters specified. The client
	 * must execute {@link UpdatableProperty#applyUpdates()} on the result.
	 * 
	 * @param name the name of the property
	 * @param valueType the value type of the attribute values
	 * @param indexed true if the property is suitable as a search criterion
	 * @return an updatable property
	 * @throws T2DBException
	 */
	<T>UpdatableProperty<T> createProperty(String name, ValueType<T> valueType, boolean indexed) throws T2DBException;
	
	/**
	 * Return schemas with names matching a pattern. Patterns
	 * use a simple syntax, with an asterisk standing for zero or
	 * more characters.
	 * 
	 * @param pattern a name pattern
	 * @return a collection of schemas
	 * @throws T2DBException
	 */
	Collection<Schema> getSchemas(String pattern) throws T2DBException;
	
	/**
	 * Return updatable schemas with names matching a pattern. Patterns
	 * use a simple syntax, with an asterisk standing for zero or
	 * more characters.
	 * 
	 * @param pattern a name pattern
	 * @return a collection of updatable schemas
	 * @throws T2DBException
	 */
	Collection<UpdatableSchema> getUpdatableSchemas(String pattern) throws T2DBException;
	
	/**
	 * Create a schema with the parameters specified. When a base schema is
	 * specified, the new schema extends it by adding, removing, or modifying
	 * attributes and series definitions. The client must execute
	 * {@link UpdatableSchema#applyUpdates()} on the result.
	 * 
	 * @param name
	 *            the name of the schema
	 * @param nameOfBase
	 *            the name of the base schema or null if there is no base schema
	 * @return an updatable schema
	 * @throws T2DBException
	 */
	UpdatableSchema createSchema(String name, String nameOfBase) throws T2DBException; 
	
	/**
	 * Return the value type with the given name. Throw an exception if
	 * it does not exist.
	 * 
	 * @param name a value type name 
	 * @return a value type
	 * @throws T2DBException
	 */
	<T>ValueType<T> getValueType(String name) throws T2DBException;
	
	/**
	 * Return value types with names matching a pattern. Patterns
	 * use a simple syntax, with an asterisk standing for zero or
	 * more characters.
	 * 
	 * @param pattern a name pattern
	 * @return a collection of value types
	 * @throws T2DBException
	 */
	Collection<ValueType<?>> getValueTypes(String pattern) throws T2DBException;
	
	/**
	 * Create a value type with the parameters specified. The value type can be
	 * specified using a string corresponding to a value provided in
	 * {@link StandardValueType} or the name of a class implementing
	 * {@link ValueScanner}. The client must execute
	 * {@link UpdatableValueType#applyUpdates()} on the result.
	 * 
	 * @param name
	 *            the name of the value type
	 * @param restricted
	 *            true if the value type has a list of allowed values
	 * @param scannerClassOrKeyword
	 *            a keyword or the name of a scanner class
	 * @return an updatable value type
	 * @throws T2DBException
	 */
	<T>UpdatableValueType<T> createValueType(String name, boolean restricted, String scannerClassOrKeyword) throws T2DBException;

}
