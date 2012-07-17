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
 * Type: UpdatablePropertyImpl
 * Version: 1.1.0
 */
package ch.agent.crnickl.impl;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.T2DBMsg;
import ch.agent.crnickl.T2DBMsg.D;
import ch.agent.crnickl.api.Surrogate;
import ch.agent.crnickl.api.UpdatableProperty;
import ch.agent.crnickl.api.ValueType;

/**
 * Default implementation of {@link UpdatableProperty}.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.1.0
 * @param <T> the underlying data type of attributes having this property
 */
public class UpdatablePropertyImpl<T> extends PropertyImpl<T> implements UpdatableProperty<T> {

	private boolean delete;
	private String name;
	
	/**
	 * Construct an {@link UpdatableProperty}.
	 * 
	 * @param name a string 
	 * @param valueType a value type
	 * @param indexed true if the property is suitable for searches
	 * @param surrogate a surrogate
	 */
	public UpdatablePropertyImpl(String name, ValueType<T> valueType, boolean indexed, Surrogate surrogate) {
		super(name, valueType, indexed, surrogate);
	}
	
	@Override
	protected void update() {
		super.update();
		name = null;
	}

	@Override
	public UpdatableProperty<T> edit() {
		return this;
	}

	@Override
	public void applyUpdates() throws T2DBException {
		if (delete) {
			// cache already cleared
			getDatabase().deleteProperty(this);
			delete = false;
		} else {
			if (getSurrogate().inConstruction())
				getDatabase().create(this);
			if (name != null) {
				getDatabase().getCache().clear(this);
				getDatabase().update(this);
			}
			update();
		}
	}

	@Override
	public String getName() {
		if (name != null)
			return name;
		else
			return super.getName();
	}
	
	@Override
	public void setName(String name) throws T2DBException {
		if (delete)
			throw T2DBMsg.exception(D.D20108, getName());
		this.name = name;
	}

	@Override
	public void destroy() throws T2DBException {
		if (name != null)
			throw T2DBMsg.exception(D.D20107, getName());
		delete = true;
	}

}
