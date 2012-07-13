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
 * Type: PropertyImpl
 * Version: 1.0.0
 */
package ch.agent.crnickl.impl;

import java.util.List;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.T2DBMsg;
import ch.agent.crnickl.T2DBMsg.D;
import ch.agent.crnickl.api.Chronicle;
import ch.agent.crnickl.api.Property;
import ch.agent.crnickl.api.Surrogate;
import ch.agent.crnickl.api.UpdatableProperty;
import ch.agent.crnickl.api.ValueType;

/**
 * Default implementation of {@link Property}.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 * @param <T> the underlying data type of attributes having this property
 */
public class PropertyImpl<T> extends DBObjectImpl implements Property<T> {

	private String name;
	private ValueType<T> valueType;
	private boolean indexed;
	
	/**
	 * Construct a {@link Property}.
	 * 
	 * @param name a string 
	 * @param valueType a value type
	 * @param indexed true if the property is suitable for searching
	 * @param surrogate a surrogate
	 */
	public PropertyImpl(String name, ValueType<T> valueType, boolean indexed, Surrogate surrogate) {
		super(surrogate);
		this.name = name;
		this.valueType = valueType;
		this.indexed = indexed;
	}
	
	/**
	 * Refresh state.
	 */
	protected void update() {
		name = getName();
	}
	
	@Override
	public ValueType<T> getValueType() {
		return valueType;
	}

	@Override
	public UpdatableProperty<T> edit() {
		return new UpdatablePropertyImpl<T>(name, valueType, indexed, getSurrogate());
	}

	@Override
	public boolean isIndexed() {
		/*
		 * Note. A memory-based database can index a property on the fly when
		 * processing an AttributeRestriction. This will be addressed in
		 * UpdatableProperty.
		 */
		return indexed;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public T scan(String value) throws T2DBException {
		try {
			return getValueType().scan(value);
		} catch (T2DBException e) {
			throw T2DBMsg.exception(e, D.D20110, value, name);
		}
	}
	
	@Override
	public void check(T value) throws T2DBException {
		try {
			getValueType().check(value);
		} catch (T2DBException e) {
			throw T2DBMsg.exception(e, D.D20110, value, name);
		}
	}

	@Override
	public List<Chronicle> getChronicles(T value, int maxSize) throws T2DBException {
		return getDatabase().getChroniclesByAttributeValue(this, value, maxSize);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <S> Property<S> typeCheck(Class<S> type) throws T2DBException {
		try {
			if (type.isAssignableFrom(getValueType().getType()))
				return (Property<S>) this;
		} catch (Exception e) {
		}
		throw T2DBMsg.exception(D.D10101, getName(), type.getName(), getValueType().getType().getName());
	}
	
	@Override
	public String toString() {
		return getName();
	}

}
