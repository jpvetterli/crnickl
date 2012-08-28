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
 * Type: AttributeDefinitionImpl
 * Version: 1.0.0
 */
package ch.agent.crnickl.impl;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.T2DBMsg;
import ch.agent.crnickl.T2DBMsg.D;
import ch.agent.crnickl.api.Attribute;
import ch.agent.crnickl.api.AttributeDefinition;
import ch.agent.crnickl.api.Property;
import ch.agent.crnickl.api.SchemaComponent;

/**
 * Default implementation of {@link AttributeDefinition}.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 * @param <T>
 *            the underlying data type of the attribute
 */
public class AttributeDefinitionImpl<T> implements AttributeDefinition<T>, SchemaComponent {

	private boolean editMode;
	private boolean erasing;
	private int number;
	private Property<T>  property;
	private Object value;
	private boolean valueSet; // when null
	
	/**
	 * Construct an attribute definition.
	 * 
	 * @param number the attribute number
	 * @param property a property
	 * @param value a default value or null
	 * @throws T2DBException
	 */
	public AttributeDefinitionImpl(int number, Property<T> property, T value) throws T2DBException {
		if (number < 1)
			throw T2DBMsg.exception(D.D30117);
		this.number = number;
		this.property = property;
		if (value != null)
			getProperty().getValueType().check(value);
		this.value = value;
	}
	
	@Override
	public boolean isComplete() {
		return !erasing && property != null;
	}

	@Override
	public boolean isErasing() {
		return erasing;
	}

	@Override
	public int getNumber() {
		return number;
	}
	
	@Override
	public Property<T> getProperty() {
		return property;
	}
	
	@Override
	public String getName() {
		return property == null ? null : property.getName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getValue() {
		return (T) value;
	}
	
	@Override
	public Attribute<T> getAttribute() {
		if (!isComplete())
			throw new IllegalStateException();
		return new AttributeImpl<T>(this);
	}

	@Override
	public String toString() {
		return property == null ? "" + number : property.getName() + " " + value;
	}

	@Override
	public int compareTo(AttributeDefinition<T> o) {
		if (getNumber() > o.getNumber())
			return 1;
		else if (getNumber() < o.getNumber())
			return -1;
		else
			return 0;
	}

	/**
	 * Set the erasing mode of the definition.
	 * The definition must be in edit mode.
	 * 
	 * @param erasing if true the definition removes an inherited definition
	 */
	public void setErasing(boolean erasing) {
		checkEdit();
		this.erasing = erasing;
	}

	/**
	 * Set the property.
	 * The definition must be in edit mode.
	 * 
	 * @param property a property or null
	 * @throws T2DBException
	 */
	public void setProperty(Property<T> property) throws T2DBException {
		checkEdit();
		this.property = property;
		try {
			checkType();
		} catch (T2DBException e) {
			throw T2DBMsg.exception(e, D.D30133, getValue(), getNumber(), property);
		}
	}

	/**
	 * Set the default value.
	 * The definition must be in edit mode.
	 * 
	 * @param value a value or null
	 * @throws T2DBException
	 */
	public void setValue(Object value) throws T2DBException {
		checkEdit();
		this.value = value;
		this.valueSet = true;
		checkType();
	}
	
	/**
	 * Test if a default value has been set.
	 * 
	 * @return true if a default value has been set.
	 */
	public boolean isValueSet() {
		return valueSet;
	}
	
	@Override
	public void edit() {
		this.editMode = true;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void edit(SchemaComponent component) {
		if (!(component instanceof AttributeDefinition<?>))
			throw new IllegalArgumentException(component == null ? null : component.getClass().getName());
		AttributeDefinition<?> arg = (AttributeDefinition<?>) component;
		if (arg.isErasing())
			erasing = true;
		else {
			if (arg.getProperty() != null)
				property = (Property<T>) arg.getProperty();
			if (arg.getValue() != null)
				value = arg.getValue();
		}
	}

	@Override
	public void consolidate() throws T2DBException {
	}

	private void checkEdit() {
		if (!editMode)
			throw new IllegalStateException();
	}
	
	/**
	 * If both property and default value are set verify that their types agree.
	 * If necessary and possible convert the value. If property or value is null, 
	 * do nothing.
	 * 
	 * @throws T2DBException
	 */
	@SuppressWarnings("unchecked")
	private void checkType() throws T2DBException {
		if (this.property != null && this.value != null) {
			if (!property.getValueType().isCompatible(value)) {
				if (this.value instanceof String)
					this.value = property.getValueType().scan((String)this.value);
				else
					throw T2DBMsg.exception(D.D30132, value, property, property.getValueType());
			}
			property.getValueType().check((T)value);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <S> AttributeDefinition<S> typeCheck(Class<S> type) throws T2DBException {
		getProperty().typeCheck(type);
		return (AttributeDefinition<S>) this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (erasing ? 1231 : 1237);
		result = prime * result
				+ ((property == null) ? 0 : property.hashCode());
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
		AttributeDefinitionImpl<?> other = (AttributeDefinitionImpl<?>) obj;
		if (erasing != other.erasing)
			return false;
		if (property == null) {
			if (other.property != null)
				return false;
		} else if (!property.equals(other.property))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	
}
