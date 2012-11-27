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
 * Type: AttributeImpl
 * Version: 1.0.0
 */
package ch.agent.crnickl.impl;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.T2DBMsg;
import ch.agent.crnickl.T2DBMsg.D;
import ch.agent.crnickl.api.Attribute;
import ch.agent.crnickl.api.Property;
import ch.agent.crnickl.api.ValueType;

/**
 * Default implementation of {@link Attribute}.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 * @param <T> the underlying data type of the attribute
 */
public class AttributeImpl<T> implements Attribute<T> {

	private AttributeDefinitionImpl<T> definition;
	private T value;
	private String description;
	
	/**
	 * Construct an {@link Attribute}.
	 * 
	 * @param attributeDefinition an attribute definition
	 */
	public AttributeImpl(AttributeDefinitionImpl<T> attributeDefinition) {
		if (attributeDefinition == null)
			throw new IllegalArgumentException("attributeDefinition null");
		this.definition = attributeDefinition;
		value = attributeDefinition.getValue();
	}

	@Override
	public Property<T> getProperty() {
		return definition.getProperty();
	}

	@Override
	public T get() {
		return value;
	}
	
	@Override
	public void reset() {
		value = null;
	}

	@Override
	public void set(T value) throws T2DBException {
		definition.getProperty().check(value);
		this.value = value;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void scan(Object value) throws T2DBException {
		ValueType<T> vt = definition.getProperty().getValueType();
		if (vt.isCompatible(value))
			set((T) value);
		else {
			if (value instanceof String)
				set(vt.scan((String) value));
			else
				throw T2DBMsg.exception(D.D10114, value, vt.getType().getName());
		}
	}

	@Override
	public String getDescription(boolean effective) {
		String result = description;
		if (result == null && effective){
			ValueType<T> vt = getProperty().getValueType();
			if (vt.isRestricted())
				result = vt.getValueDescriptions().get(value);
		}
		return result;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <S> Attribute<S> typeCheck(Class<S> type) throws T2DBException {
		getProperty().typeCheck(type);
		return (Attribute<S>) this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((definition == null) ? 0 : definition.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		@SuppressWarnings("rawtypes")
		AttributeImpl other = (AttributeImpl) obj;
		if (definition == null) {
			if (other.definition != null)
				return false;
		} else if (!definition.equals(other.definition))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		try {
			return getProperty().getName() + "=" + (value == null ? null : definition.getProperty().getValueType().toString(value));
		} catch (T2DBException e) {
			throw new RuntimeException(e);
		}
	}

}
