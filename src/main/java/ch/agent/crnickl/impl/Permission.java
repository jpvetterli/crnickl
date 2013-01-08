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

/* They apply at various levels:
* <ul>
* <li>the "system", represented by the name space (also known as top chronicle);
* <li>a specific chronicle, schema, property, or value type;
* <li>a collection of chronicles.
* </ul>
* When applying to a collection of chronicles, a permission is valid for all its
* direct and indirect members, unless overridden by a more restrictive
* permission. Permissions are ordered by increasing order of power: NONE, READ,
* DISCOVER, MODIFY, AUTHORIZE. A given permission implies all lesser
* permissions.
*/ 
/**
 * Permissions identify access levels.
 * 
 * @author Jean-Paul Vetterli
 */
public enum Permission {
	/*
	 * NONE permission is the absence of any permission.
	 */
	NONE,
	/*
	 * READ permission is required for read access.
	 * Specifically:
	 * <ul>
	 * <li>on an <b>chronicle</b> for accessing its attributes and
	 * series,
	 * <li>on a <b>schema</b> for using it in an chronicle,
	 * <li>on a <b>property</b> for using it in a schema,
	 * <li>on a <b>value type</b> for using it in a property.
	 * </ul>
	 * The information from schemas, properties, and value types is always implicitly
	 * accessible as required when accessing an chronicle.
	 */
	READ,
	/*
	 * DISCOVER permission is required for listing contents.
	 * Specifically:
	 * <ul>
	 * <li>on an <b>chronicle</b> to list its member chronicles;
	 * <li>on the <b>name space</b> to list 
	 * <ul>
	 * <li>schemas,
	 * <li>properties, 
	 * <li>and value types.
	 * </ul>
	 * </ul>
	 */
	DISCOVER,
	/*
	 * MODIFY permission is required for write access.
	 * Specifically:
	 * <ul>
	 * <li>on a <b>chronicle</b> for 
	 * <ul>
	 * <li>deleting the chronicle,
	 * <li>modifying an attribute,
	 * <li>adding a member chronicle,
	 * <li>adding and deleting values of a series;
	 * </ul> 
	 * <li>on the <b>name space</b> for
	 * <ul>
	 * <li>adding first-level chronicles,
	 * <li>adding schemas,
	 * <li>adding properties,
	 * <li>adding value types;
	 * </ul> 
	 * <li>on a <b>property</b> for modifying, and deleting it;
	 * <li>on a <b>schema</b> for modifying, and deleting it;
	 * <li>on a <b>value type</b> for modifying, and deleting it.
	 * </ul>
	 * </ul>
	 * Adding a series to an chronicle is equivalent to adding the first value.
	 */
	MODIFY,
	/*
	 * CREATE permission is required for creating new objects.
	 */
	CREATE,
	/*
	 * AUTHORIZE permission is required for modifying authorizations.
	 */
	AUTHORIZE
}
