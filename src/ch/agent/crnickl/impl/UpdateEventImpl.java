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
 * Type: UpdateEventImpl
 * Version: 1.0.0
 */
package ch.agent.crnickl.impl;

import ch.agent.crnickl.api.DBObject;
import ch.agent.crnickl.api.DBObjectType;
import ch.agent.crnickl.api.Surrogate;
import ch.agent.crnickl.api.UpdateEvent;
import ch.agent.crnickl.api.UpdateEventOperation;

/**
 * Default implementation of {@link UpdateEvent}.
 *
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public class UpdateEventImpl implements UpdateEvent {

	private DBObjectType type;
	private UpdateEventOperation op;
	private DBObject source;
	private String comment;
	
	/**
	 * Construct an {@link UpdateEvent}.
	 * 
	 * @param op an operation
	 * @param source a database object
	 */
	public UpdateEventImpl(UpdateEventOperation op, DBObject source) {
		if (source == null)
			throw new IllegalArgumentException("source null");
		this.source = source;
		this.type = this.source.getSurrogate().getDBObjectType();
		this.op = op;
	}

	/**
	 * Set a comment.
	 * 
	 * @param comment a string
	 * @return the update event
	 */
	public UpdateEvent withComment(String comment) {
		this.comment = comment;
		return this;
	}

	@Override
	public DBObjectType getType() {
		return type;
	}

	@Override
	public UpdateEventOperation getOperation() {
		return op;
	}

	@Override
	public Surrogate getSurrogate() {
		return source.getSurrogate();
	}
	@Override
	public String getComment() {
		return comment;
	}
	
	@Override
	public DBObject getSource() {
		return source;
	}

	@Override
	public DBObject getSourceOrNull() {
		DBObject dBObject = getSource();
		return dBObject.isValid() ? dBObject : null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((op == null) ? 0 : op.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UpdateEventImpl other = (UpdateEventImpl) obj;
		if (op != other.op)
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		String result = null;
		try {
			if (comment == null)
			    result = String.format("%s %s %s", op.name(), type.name(), source.toString());
			else
				result =  String.format("%s %s %s (%s)", op.name(), type.name(), source.toString(), comment);
		} catch (Exception e) {
			if (comment == null)
				result =  String.format("%s %s %s", op.name(), type.name(), getSurrogate().toString());
			else
				result =  String.format("%s %s %s (%s)", op.name(), type.name(), getSurrogate().toString(), comment);
		}
		return result;
	}

}
