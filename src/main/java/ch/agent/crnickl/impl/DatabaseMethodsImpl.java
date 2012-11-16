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
 * Type: DatabaseMethodsImpl
 * Version: 1.0.0
 */
package ch.agent.crnickl.impl;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.T2DBMsg;
import ch.agent.crnickl.T2DBMsg.D;
import ch.agent.crnickl.api.DBObject;
import ch.agent.crnickl.api.DBObjectId;
import ch.agent.crnickl.api.DBObjectType;
import ch.agent.crnickl.api.Database;
import ch.agent.crnickl.api.Surrogate;

/**
 * Default implementation of {@link DatabaseMethods} and {@link PermissionChecker}.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public class DatabaseMethodsImpl implements DatabaseMethods, PermissionChecker {

	/**
	 * Throw an exception if the DBObject is null.
	 * @param object an object
	 * @param s its surrogate 
	 * @param referrer the surrogate of the object's referrer or null 
	 * @throws T2DBException
	 */
	public void checkIntegrity(DBObject object, Surrogate s, Surrogate referrer) throws T2DBException {
		if (object == null) {
			if (referrer == null)
				throw T2DBMsg.exception(D.D02106, s.toString());
			else
				throw T2DBMsg.exception(D.D02107, s.toString(), referrer.toString());
		}
	}
	
	@Override
	public Surrogate makeSurrogate(Database db, DBObjectType dot, DBObjectId id) {
		return new SurrogateImpl((DatabaseBackend) db, dot, id);
	}

	@Override
	public Surrogate makeSurrogate(DBObject dBObject, DBObjectId id) {
		Surrogate s = dBObject.getSurrogate();
		return makeSurrogate(s.getDatabase(), s.getDBObjectType(), id);
	}

	@Override
	public boolean check(Permission permission, DBObject dBObject, boolean permissionRequired) throws T2DBException {
		return ((DBObjectImpl) dBObject).getDatabase().check(permission, dBObject, permissionRequired);
	}

	@Override
	public void check(Permission permission, DBObject dBObject)	throws T2DBException {
		((DBObjectImpl) dBObject).getDatabase().check(permission, dBObject);
	}

	@Override
	public boolean check(Permission permission, Surrogate surrogate, boolean permissionRequired) throws T2DBException {
		return ((DatabaseBackend) surrogate.getDatabase()).check(permission, surrogate, permissionRequired);
	}

	@Override
	public void check(Permission permission, Surrogate surrogate) throws T2DBException {
		((DatabaseBackend) surrogate.getDatabase()).check(permission, surrogate);
	}
}
