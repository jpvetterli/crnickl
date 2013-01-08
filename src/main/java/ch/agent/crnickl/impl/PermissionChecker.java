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

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.api.DBObject;
import ch.agent.crnickl.api.Surrogate;

/**
 * PermissionChecker provides methods used by the implementation for checking access permissions.
 *  
 * @author Jean-Paul Vetterli
 */
public interface PermissionChecker {

	/**
	 * Check if a permission is available on a database object. Depending on
	 * <code>permissionRequired</code> throw an exception or return false on
	 * failure.
	 * 
	 * @param permission
	 *            a permission
	 * @param dBObject
	 *            a database object
	 * @param permissionRequired
	 *            if true throw an exception on failure else return false
	 * @return true if the permission is available, else false
	 * @throws T2DBException
	 */
	boolean check(Permission permission, DBObject dBObject,
			boolean permissionRequired) throws T2DBException;

	/**
	 * Check if a permission is available on a database object. Throw an
	 * exception if not.
	 * 
	 * @param permission
	 *            a permission
	 * @param dBObject
	 *            a database object
	 * @throws T2DBException
	 */
	void check(Permission permission, DBObject dBObject) throws T2DBException;
	
	/**
	 * Check if a permission is available on a database object. Depending on
	 * <code>permissionRequired</code> throw an exception or return false on
	 * failure.
	 * 
	 * @param permission
	 *            a permission
	 * @param surrogate
	 *            a surrogate identifying a database object
	 * @param permissionRequired
	 *            if true throw an exception on failure else return false
	 * @return true if the permission is available, else false
	 * @throws T2DBException
	 */
	boolean check(Permission permission, Surrogate surrogate, boolean permissionRequired) throws T2DBException;

	/**
	 * Check if a permission is available on a database object. Throw an
	 * exception if not.
	 * 
	 * @param permission
	 *            a permission
	 * @param surrogate
	 *            if true throw an exception on failure else return false
	 * @throws T2DBException
	 */
	void check(Permission permission, Surrogate surrogate) throws T2DBException;
	
}
