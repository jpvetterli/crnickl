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
import ch.agent.crnickl.api.DBObject;
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

	@Override
	public int getId(DBObject dBObject) {
		try {
			int id = (((SurrogateImpl) dBObject.getSurrogate()).getId()).intValue();
			if (id < 1)
				throw new RuntimeException("bug (database integrity violation)");
			return id;
		} catch(ClassCastException e) {
			throw new RuntimeException("bug: " + dBObject.toString(), e);
		}
	}

	@Override
	public int getIdOrZero(DBObject dBObject) {
		try {
			int id = 0;
			if (dBObject != null) {
				SurrogateImpl key = (SurrogateImpl) dBObject.getSurrogate();
				if (!key.inConstruction()) {
					id = key.getId().intValue();
					if (id < 1)
						throw new RuntimeException("bug (database integrity violation)");
				}
			}
			return id;
		} catch(ClassCastException e) {
			throw new RuntimeException("bug: " + dBObject.toString(), e);
		}
	}

	@Override
	public int getId(Surrogate surrogate) {
		try {
			int id = (((SurrogateImpl) surrogate).getId()).intValue();
			if (id < 1)
				throw new RuntimeException("bug (database integrity violation)");
			return id;
		} catch(ClassCastException e) {
			throw new RuntimeException("bug: " + surrogate.toString(), e);
		}
	}

	@Override
	public Surrogate makeSurrogate(Database db, DBObjectType dot, int id) {
		return new SurrogateImpl((DatabaseBackend) db, dot, id);
	}

	@Override
	public Surrogate makeSurrogate(DBObject dBObject, int id) {
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
