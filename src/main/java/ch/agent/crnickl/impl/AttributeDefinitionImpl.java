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
public class AttributeDefinitionImpl<T> extends SchemaComponentImpl implements AttributeDefinition<T> {

	private int seriesNr;
	private int number;
	private Property<T>  property;
	private Object value;
	private boolean erasing;
	private boolean valueNullChecked;
	
	/**
	 * Construct an attribute definition.
	 * 
	 * @param seriesNr the series number or zero if a chronicle attribute
	 * @param number the attribute number
	 * @param property a property
	 * @param value a default value or null
	 * @throws T2DBException
	 */
	public AttributeDefinitionImpl(int seriesNr, int number, Property<T> property, T value) throws T2DBException {
		super();
		if (number < 1)
			throw T2DBMsg.exception(D.D30117);
		this.number = number;
		this.property = property;
		if (value != null)
			getProperty().getValueType().check(value);
		this.value = value;
		this.seriesNr = seriesNr;
	}
	
	@Override
	public boolean isComplete() {
		boolean complete = false;
		if (!isErasing()) {
			if (property != null) {
				/*
				 * If the value is not null it has already been checked
				 * else it has possibly never been set, so must ensure
				 * null is valid.
				 */
				if (value == null && !valueNullChecked) {
					try {
						checkType(true);
						complete = true;
					} catch (T2DBException e) {
						// complete remains false
					}
				} else
					complete = true;
			}
		}
		return complete;
	}

	@Override
	public boolean isErasing() {
		// unlike in SeriesDefinitionImpl, not lazy
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
	 * Setting erasing mode resets the property and the value.
	 * 
	 * @param erasing if true the definition removes an inherited definition
	 */
	public void setErasing(boolean erasing) {
		checkEdit();
		doSetErasing(erasing);
	}
	
	private void doSetErasing(boolean erasing) {
		this.erasing = erasing;
		if (erasing) {
			this.property = null;
			this.value = null;
		}
	} 

	/**
	 * Set the property.
	 * The definition must be in edit mode.
	 * The property name is also used as the attribute name.
	 * Setting a non-null property resets erasing mode.
	 * 
	 * @param property a property or null
	 * @throws T2DBException
	 */
	public void setProperty(Property<T> property) throws T2DBException {
		String oldName = this.property == null ? null : this.property.getName();
		checkEdit();
		this.property = property;
		try {
			checkType(valueNullChecked);
		} catch (T2DBException e) {
			throw T2DBMsg.exception(e, D.D30133, getValue(), getNumber(), property);
		}
		String newName = this.property == null ? null : this.property.getName();
		try {
			if (!same(oldName, newName))
				nameChanged(true, oldName, newName);
		} catch (T2DBException e) {
			if (seriesNr == 0)
				throw T2DBMsg.exception(e, D.D30151, getNumber());
			else
				throw T2DBMsg.exception(e, D.D30152, getNumber(), seriesNr);
		}
		if (property != null)
			doSetErasing(false);
		valueNullChecked = false;
	}

	/**
	 * Set the default value.
	 * The definition must be in edit mode.
	 * Setting a non-null value resets erasing mode.
	 * 
	 * @param value a value or null
	 * @throws T2DBException
	 */
	public void setValue(Object value) throws T2DBException {
		Object old = this.value;
		if (!same(old, value)) {
			checkEdit();
			this.value = value;
			checkType(true);
			// is this value the name of the series ?
			if (seriesNr > 0 && number == DatabaseBackend.MAGIC_NAME_NR) {
				try {
					nameChanged(false, (String) old, (String) value); 
				} catch (T2DBException e) {
					throw T2DBMsg.exception(e, D.D30153, seriesNr);
				}
			}
		}
		if (value != null)
			doSetErasing(false);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void edit(SchemaComponent component) {
		if (!(component instanceof AttributeDefinition<?>))
			throw new IllegalArgumentException(component == null ? null : component.getClass().getName());
		AttributeDefinition<?> arg = (AttributeDefinition<?>) component;
		if (arg.isErasing())
			doSetErasing(true);
		else {
			if (arg.getProperty() != null)
				property = (Property<T>) arg.getProperty();
			if (arg.getValue() != null)
				value = arg.getValue();
		}
	}

	/**
	 * If both property and default value are set verify that their types agree.
	 * If necessary and possible convert the value. If property or value is null, 
	 * do nothing.
	 * 
	 * @param alsoIfValueNull also check the type if the value is null
	 * @throws T2DBException
	 */
	@SuppressWarnings("unchecked")
	private void checkType(boolean alsoIfValueNull) throws T2DBException {
		if (this.property != null && (alsoIfValueNull || this.value != null)) {
			if (!property.getValueType().isCompatible(value)) {
				if (this.value instanceof String)
					this.value = property.getValueType().scan((String)this.value);
				else
					throw T2DBMsg.exception(D.D30132, value, property, property.getValueType());
			}
			property.getValueType().check((T)value);
			if (value == null)
				valueNullChecked = true;
		}
	}
	
	@Override
	public SchemaComponent copy() {
		try {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			AttributeDefinitionImpl<T> ad = new AttributeDefinitionImpl(this.seriesNr, this.number, this.property, this.value);
			ad.doSetErasing(isErasing());
			return ad;
		} catch (T2DBException e) {
			throw new RuntimeException("bug", e);
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
		result = prime * result + (isErasing() ? 1231 : 1237);
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
		if (isErasing() != other.isErasing())
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
	
	private boolean same(Object x, Object y) {
		return x == null ? y == null : x.equals(y);
	}
	
}
