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
package ch.agent.crnickl.api;

import java.util.Collection;
import java.util.List;

import ch.agent.crnickl.T2DBException;

/**
 * A Chronicle records the state of some subject at different times. It consists
 * of {@link Series} and {@link Attribute}s.
 * <p>
 * An example of a subject for a chronicle is a traded security, like a share, 
 * with price and trading volume changing all the time, 
 * and with company information and trading currency remaining constant.
 * Another example is the climate at a location, with varying temperature,
 * humidity, pressure, wind speed, and wind direction, and with fixed
 * geographic coordinates.
 * <p>
 * A chronicle, seen as a collection, can include other chronicles as members, 
 * and the members can in
 * turn include more members. All chronicles in a database form a tree, with the root
 * known as the <em>top chronicle</em>. This structure is reflected in the {@link NamingPolicy}
 * of the database, which defines the syntax for naming chronicles. The tree structure
 * allows the design of very large collections of chronicles using only a few schemas.
 * <p>
 * Fast access to chronicles using attribute values is provided 
 * by {@link Property#getChronicles(Object, int)}.
 * 
 * @author Jean-Paul Vetterli
 */
public interface Chronicle extends DBObject {
	
	/**
	 * Return an {@link UpdatableChronicle} corresponding to this chronicle. 
	 * Successfully getting an {@link Updatable} object does not imply
	 * that updates can be successfully applied.
	 * 
	 * @return an updatable chronicle corresponding to this chronicle
	 */
	UpdatableChronicle edit();

	/**
	 * Return true if this is the top chronicle.
	 * 
	 * @return true if this is the top chronicle
	 */
	boolean isTopChronicle();
	
	/**
	 * Return true if this chronicle is member of the collection. A collection
	 * of chronicles is simply a chronicle which contains chronicles. The contained
	 * chronicles can themselves contain more chronicles.
	 * 
	 * @param collection a chronicle
	 * @return true if this chronicle is a member of the collection represented by the parameter
	 * @throws T2DBException
	 */
	boolean isMemberOf(Chronicle collection) throws T2DBException;
	
	/**
	 * Return the direct child of this chronicle with the given simple name. The
	 * <code>mustExist</code> parameter determines behavior when nothing is
	 * found: exception or null result.
	 * 
	 * @param simpleName
	 *            a simple name
	 * @param mustExist
	 *            if true throw an exception instead of returning null
	 * @return a chronicle or null (only when mustExist is false)
	 * @throws T2DBException
	 */
	Chronicle getChronicle(String simpleName, boolean mustExist)
			throws T2DBException;
	
	/**
	 * Find the chronicle with the the given full name. Throw an exception if
	 * there is no such chronicle. When the parameter <code>mustExist</code> 
	 * is false, a non-existing chronicle is tolerated when it is at the end of
	 * the chain of simple names.
	 * 
	 * @param fullName
	 *            the full name of the chronicle
	 * @param mustExist
	 *            if true throw an exception instead of returning null
	 * @return a chronicle or null (only when mustExist is false)
	 * @throws T2DBException
	 */
	Chronicle findChronicle(String fullName, boolean mustExist) throws T2DBException;

	/**
	 * Return the chronicle to which this chronicle belongs. The result is null
	 * if the chronicle is the top-level chronicle. The result is also known as
	 * the <em>parent</em> chronicle.
	 * 
	 * @return the parent chronicle or null
	 */
	Chronicle getCollection() throws T2DBException;
	
	/**
	 * Return the direct members of this chronicle seen as a collection.
	 * 
	 * @return a collection of chronicles
	 * @throws T2DBException
	 */
	Collection<Chronicle> getMembers() throws T2DBException;
	
	/**
	 * Return the name of the chronicle.
	 * 
	 * @param full if true return the full name, else the simple name
	 * @return the full or the simple name of the chronicle
	 * @throws T2DBException
	 */
	String getName(boolean full) throws T2DBException;
	
	/**
	 * Return the list of all names along the chronicle chain.
	 * The last element is this chronicle's simple name. The previous element
	 * is the name of the parent. The element before that is the name 
	 * of the parent's parent, and so on until the top chronicle.
	 * 
	 * @return the list of names along the chronicle chain
	 * 
	 * @throws T2DBException
	 */
	List<String> getNames() throws T2DBException;

	/**
	 * Return the description of the chronicle.
	 * 
	 * @param full
	 *            if true return the full description, else the simple
	 *            description
	 * @return the full or the simple description of the chronicle
	 * @throws T2DBException
	 */
	String getDescription(boolean full) throws T2DBException;
	
	/**
	 * Return the list of all descriptions along the chronicle chain. The last
	 * element is this chronicle's description. The previous element is the
	 * description of the parent. The element before that is the description
	 * of the parent's parent, and so on until the top chronicle.
	 * 
	 * @return the list of descriptions along the chronicle chain
	 * 
	 * @throws T2DBException
	 */
	List<String> getDescriptions() throws T2DBException;
	
	/**
	 * Return the chronicle's schema. The schema can be null. When the 
	 * <code>effective</code> parameter is true, the schema
	 * returned is the first non-null schema encountered along the chronicle chain.
	 * Also in this case the result can be null.
	 *  
	 * @param effective if true return the first non-null schema along the chronicle chain
	 * @return the schema of the chronicle or null
	 * @throws T2DBException
	 */
	Schema getSchema(boolean effective) throws T2DBException;
	
	/**
	 * Return the attribute with the given name. When the parameter mustExist is
	 * true, the result is never null. The value of the attribute can always be
	 * null. When no attribute with the given name is defined, an exception is
	 * thrown.
	 * 
	 * @param name
	 *            a string
	 * @param mustExist
	 *            if true throw an exception instead of returning null
	 * @return an attribute or null (only when mustExist is false)
	 * @throws T2DBException
	 */
	Attribute<?> getAttribute(String name, boolean mustExist) throws T2DBException;
	
	/**
	 * Return all the chronicle's attributes.
	 * 
	 * @return all the attributes of the chronicle
	 * @throws T2DBException
	 */
	Collection<Attribute<?>> getAttributes() throws T2DBException;

	/**
	 * Return the series with the given simple name. The result is null if the
	 * series has not yet been set up in the database. Such a non-existent
	 * series is conceptually equivalent to a series with no values (empty
	 * range). The series name must be defined in the schema, else an exception
	 * is thrown. When the result is not null, it is possibly a new series for
	 * which updates need to be "applied".
	 * 
	 * @param <T>
	 *            the data type of the underlying time series
	 * @param name
	 *            the simple name if the series
	 * @return a series or null
	 * @throws T2DBException
	 */
	<T>Series<T> getSeries(String name) throws T2DBException;
	
	/**
	 * Return an array of series corresponding to the names requested. If a name
	 * is undefined an exception is thrown, unless the parameter
	 * <code>mustBeDefined</code> is <code>false</code> (in which case the
	 * series is treated like a missing series). If the value type does not
	 * correspond to the type requested an exception is thrown. If a series is
	 * missing (there are no values), a null is returned in its place.
	 * 
	 * @param <T> the data type of the underlying time series
	 * @param name the names of the series requested
	 * @param type the expected type or null to bypass enforcing a type
	 * @param mustBeDefined if true throw an exception when a name is undefined
	 * @return an array of series with the same length as the name array
	 * @throws T2DBException
	 */
	<T>Series<T>[] getSeries(String[] name, Class<T> type, boolean mustBeDefined) throws T2DBException;
	
	/**
	 * Return all existing series in this chronicle. The result includes all
	 * series which have been created, whether empty or not.
	 * 
	 * @return all existing series of the chronicle
	 * @throws T2DBException
	 */
	Collection<Series<?>> getSeries() throws T2DBException;
}
