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
import ch.agent.crnickl.api.Chronicle;
import ch.agent.crnickl.api.DBObject;
import ch.agent.crnickl.api.DBObjectId;
import ch.agent.crnickl.api.DBObjectType;
import ch.agent.crnickl.api.Database;
import ch.agent.crnickl.api.Surrogate;

/**
 * Default implementation of {@link Surrogate}.
 *  
 * @author Jean-Paul Vetterli
 */
public class SurrogateImpl implements Surrogate {

	/**
	 * An InvalidDBObject implements an invalid {@link DBObject}.
	 */
	public class InvalidDBObject implements DBObject {

		private Surrogate surrogate;
		private String hint;
		
		public InvalidDBObject(Surrogate surrogate, String hint) {
			super();
			this.surrogate = surrogate;
			this.hint = hint;
		}

		@Override
		public DBObjectId getId() {
			return surrogate.getId();
		}

		@Override
		public Database getDatabase() {
			return surrogate.getDatabase();
		}

		@Override
		public Surrogate getSurrogate() {
			return surrogate;
		}

		@Override
		public boolean inConstruction() {
			return surrogate.inConstruction();
		}

		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public String toString() {
			if (hint != null)
				return String.format("%s (%s)", surrogate.toString(), hint);
			else
				return surrogate.toString();
		}
		
	}
	
	private String string;
	private int hashCode;
	
	private DatabaseBackend db;
	private DBObjectType dot;
	private DBObjectId id;
	
	/**
	 * Construct a {@link Surrogate}.
	 * 
	 * @param db a database backend
	 * @param dot a database object type
	 * @param id an id
	 */
	public SurrogateImpl(DatabaseBackend db, DBObjectType dot, DBObjectId id) {
		super();
		this.db = db;
		this.dot = dot;
		this.id = id;
	}
	
	@Override
	public void upgrade(Surrogate surrogate) {
		if (!inConstruction())
			throw new IllegalStateException();
		if (((SurrogateImpl) surrogate).dot != dot || ((SurrogateImpl) surrogate).id == null)
			throw new RuntimeException("bug: " + surrogate.toString());
		this.id = ((SurrogateImpl) surrogate).id;
		this.hashCode = -1;
		this.string = null;
	}

	@Override
	public DatabaseBackend getDatabase() {
		return db;
	}
	
	@Override
	public DBObjectType getDBObjectType() {
		return dot;
	}
	
	@Override
	public DBObjectId getId() {
		return id;
	}
	
	@Override
	public boolean inConstruction() {
		return id == null;
	}
	
	@Override
	public DBObject getObject() {
		if (inConstruction())
			return null;
		try {
			switch (dot) {
			case CHRONICLE:
				return getDatabase().getChronicle(this);
			case SERIES:
				return getDatabase().getSeries(this);
			case SCHEMA:
				return getDatabase().getSchema(this);
			case PROPERTY:
				return getDatabase().getProperty(this);
			case VALUE_TYPE:
				return getDatabase().getValueType(this);
			default:
				throw new RuntimeException("bug: " + dot.name());
			}
		} catch (T2DBException e) {
			return new InvalidDBObject(this, e.getMsg().toString());
		}
	}
	
	@Override
	public int hashCode() {
		if (inConstruction())
			throw new IllegalStateException();
		if (hashCode <= 0) {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((db == null) ? 0 : db.hashCode());
			result = prime * result + ((dot == null) ? 0 : dot.hashCode());
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			hashCode = result;
		}
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SurrogateImpl other = (SurrogateImpl) obj;
		if (db == null) {
			if (other.db != null)
				return false;
		} else if (!db.equals(other.db))
			return false;
		if (dot != other.dot)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;

	}	
	
	@Override
	public String toString() {
		if (string == null) {
			Chronicle top = db.getTopChronicle();
			string = String.format("%s-%d-%s", top == null ? null : top.toString() , dot.ordinal(), 
					(id == null ? "null" : id.toString()));
		}
		return string;
	}
	
}
