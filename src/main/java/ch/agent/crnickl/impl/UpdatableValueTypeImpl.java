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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.T2DBMsg;
import ch.agent.crnickl.T2DBMsg.D;
import ch.agent.crnickl.api.Surrogate;
import ch.agent.crnickl.api.UpdatableValueType;

/**
 * Default implementation of {@link UpdatableValueType}.
 * 
 * @author Jean-Paul Vetterli
 * @param <T> the underlying data type of the values
 */
public class UpdatableValueTypeImpl<T> extends ValueTypeImpl<T> implements UpdatableValueType<T> {

	private boolean delete;
	private String name;
	private Map<T, String> values;
	
	/**
	 * Construct an {@link UpdatableValueType}.
	 * This constructor is meant for {@link ValueTypeImpl#edit}.
	 * 
	 * @param valueType a value type
	 * @throws T2DBException
	 */
	protected UpdatableValueTypeImpl(ValueTypeImpl<T> valueType) throws T2DBException {
		super(valueType);
	}
	
	/**
	 * Construct an {@link UpdatableValueType}.
	 * 
	 * @param name a string
	 * @param restricted if true the value type will keep a list of allowed values
	 * @param scannerClassOrKeyword a keyword identifying a standard value type of the name of a scanner class
	 * @param valuesAndDescriptions a map of allowed values (as strings) and their description
	 * @param surrogate a surrogate
	 * @throws T2DBException
	 */
	public UpdatableValueTypeImpl(String name, boolean restricted, String scannerClassOrKeyword, Map<String, String> valuesAndDescriptions, Surrogate surrogate) throws T2DBException {
		super(name, restricted, scannerClassOrKeyword, valuesAndDescriptions, surrogate);
	}
	
	@Override
	protected void update() throws T2DBException {
		name = null;
		values = null;
		super.update();
	}

	@Override
	public void applyUpdates() throws T2DBException {
		if (delete) {
			getDatabase().getCache().clear(this);
			getDatabase().deleteValueType(this);
			delete = false;
		} else {
			if (getSurrogate().inConstruction()) {
				getDatabase().create(this);
			} else {
				if (name != null || values != null) {
					getDatabase().getCache().clear(this);
					getDatabase().update(this);
				}
			}
			update();
		}
	}

	@Override
	public UpdatableValueType<T> edit() {
		return this;
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
			throw T2DBMsg.exception(D.D10120, getName());
		this.name = name;
	}

	@Override
	public Set<T> getValues() {
		return getValueDescriptions().keySet();
	}

	@Override
	public Map<T, String> getValueDescriptions() {
		if (values != null)
			return values;
		else
			return super.getValueDescriptions();
	}

	@Override
	public void addValue(T value, String description) throws T2DBException {
		if (!isRestricted())
			throw T2DBMsg.exception(D.D10108, getName());
		if (value == null)
			throw new IllegalArgumentException("value null");
		if (delete)
			throw T2DBMsg.exception(D.D10120, getName());
		if (getValues().contains(value))
			throw T2DBMsg.exception(D.D10121, getName(), value);
		if (values == null)
			values = new HashMap<T, String>(super.getValueDescriptions());
		values.put(value, description);
	}

	@Override
	public void updateValue(T value, String description)	throws T2DBException {
		if (!isRestricted())
			throw T2DBMsg.exception(D.D10108, getName());
		if (value == null)
			throw new IllegalArgumentException("value null");
		if (delete)
			throw T2DBMsg.exception(D.D10120, getName());
		if (!getValues().contains(value))
			throw T2DBMsg.exception(D.D10123, getName(), value);
		if (values == null)
			values = new HashMap<T, String>(super.getValueDescriptions());
		values.put(value, description);
	}

	@Override
	public void deleteValue(T value) throws T2DBException {
		if (!isRestricted())
			throw T2DBMsg.exception(D.D10108, getName());
		if (delete)
			throw T2DBMsg.exception(D.D10120, getName());
		if (value == null || !getValues().contains(value))
			throw T2DBMsg.exception(D.D10122, getName(), value);
		if (values == null)
			values = new HashMap<T, String>(super.getValueDescriptions());
		values.remove(value);
	}

	@Override
	public void destroy() throws T2DBException {
		if (name != null || values != null)
			throw T2DBMsg.exception(D.D10119, getName());
		delete = true;
	}

}
