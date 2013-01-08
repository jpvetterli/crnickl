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

import ch.agent.crnickl.api.DBObject;
import ch.agent.crnickl.api.DBObjectId;
import ch.agent.crnickl.api.Surrogate;

/**
 * Default implementation of {@link DBObject}.
 * 
 * @author Jean-Paul Vetterli
 */
public class DBObjectImpl implements DBObject {

	private SurrogateImpl surr;
	
	/**
	 * Construct a {@link DBObject}.
	 * 
	 * @param surrogate a surrogate
	 */
	public DBObjectImpl(Surrogate surrogate) {
		if (surrogate == null)
			throw new IllegalArgumentException("surr null");
		this.surr = (SurrogateImpl) surrogate;
	}

	@Override
	public DBObjectId getId() {
		return surr.getId();
	}

	@Override
	public Surrogate getSurrogate() {
		return surr;
	}
	
	@Override
	public DatabaseBackend getDatabase() {
		return surr.getDatabase();
	}

	@Override
	public boolean inConstruction() {
		return surr.inConstruction();
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public int hashCode() {
		return surr.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		// compare with instanceof, not getClass, because we want Foo and UpdatableFoo to be equal
		if (!(obj instanceof DBObjectImpl))
			return false;
		if (!isValid() || !((DBObjectImpl) obj).isValid())
			return false;
		return surr.equals(((DBObjectImpl) obj).surr);
	}

}
