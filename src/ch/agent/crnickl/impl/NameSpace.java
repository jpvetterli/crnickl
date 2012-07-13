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
 * Type: NameSpace
 * Version: 1.0.0
 */
package ch.agent.crnickl.impl;

import java.util.ArrayList;
import java.util.List;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.T2DBMsg;
import ch.agent.crnickl.T2DBMsg.D;
import ch.agent.crnickl.api.Attribute;
import ch.agent.crnickl.api.Chronicle;
import ch.agent.crnickl.api.Schema;
import ch.agent.crnickl.api.Series;
import ch.agent.crnickl.api.Surrogate;
import ch.agent.crnickl.api.UpdatableChronicle;
import ch.agent.crnickl.api.UpdatableSeries;

/**
 * A NameSpace object implements the top-level chronicle.
 * The top-level chronicle exists only virtually, and is never stored.
 *
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public class NameSpace extends UpdatableChronicleImpl {

	private String name;
	private String description;
	
	/**
	 * Construct a name space, also known as top level chronicle.
	 * By convention, the name space object is named after its database. 
	 * 
	 * @param name the name of the database
	 * @param description a string
	 * @param surrogate a surrogate
	 */
	public NameSpace(String name, String description, Surrogate surrogate) {
		super(surrogate);
		this.name = name;
		this.description = description;
	}
	
	/**
	 * Return this object.
	 * 
	 * @return this object
	 */
	@Override
	public UpdatableChronicle edit() {
		return this;
	}

	/**
	 * Return true.
	 * 
	 * @return true
	 */
	@Override
	public boolean isTopChronicle() {
		return true;
	}

	/**
	 * Return null.
	 * 
	 * @return null
	 */
	@Override
	public Chronicle getCollection() throws T2DBException {
		return null;
	}

	/**
	 * Return null.
	 * 
	 * @return null
	 */
	@Override
	public Schema getSchema(boolean effective) throws T2DBException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The simple name and the full name are identical. By convention
	 * the name space is named after the database.
	 *  
	 * @return the name of the top level chronicle
	 */
	@Override
	public String getName(boolean full) throws T2DBException {
		return name;
	}

	@Override
	public List<String> getNames() throws T2DBException {
		List<String> names = new ArrayList<String>();
		if (getDatabase().isStrictNameSpaceMode())
			names.add(name);
		return names;
	}

	@Override
	public String getDescription(boolean full) throws T2DBException {
		return description;
	}

	@Override
	public List<String> getDescriptions() throws T2DBException {
		List<String> descriptions = new ArrayList<String>();
		descriptions.add(description);
		return descriptions;
	}

	/**
	 * Always throws an exception.
	 */
	@Override
	public Attribute<?> getAttribute(String name, boolean mustExist) throws T2DBException {
		throw T2DBMsg.exception(D.D40101);
	}

	/**
	 * Always throws an exception.
	 */
	@Override
	public <T> Series<T> getSeries(String name) throws T2DBException {
		throw T2DBMsg.exception(D.D40101);
	}

	/**
	 * Always throws an exception.
	 */
	@Override
	public <T> UpdatableSeries<T> createSeries(String name) throws T2DBException {
		throw T2DBMsg.exception(D.D40101);
	}

	/**
	 * Always throws an exception.
	 */
	@Override
	public <T> UpdatableSeries<T> updateSeries(String name) throws T2DBException {
		throw T2DBMsg.exception(D.D40101);
	}

	/**
	 * Always throws an exception.
	 */
	@Override
	public void setName(String name) throws T2DBException {
		throw T2DBMsg.exception(D.D40101);
	}

	/**
	 * Always throws an exception.
	 */
	@Override
	public void setDescription(String description) throws T2DBException {
		throw T2DBMsg.exception(D.D40101);
	}

	/**
	 * Always throws an exception.
	 */
	@Override
	public void setAttribute(Attribute<?> value) throws T2DBException {
		throw T2DBMsg.exception(D.D40101);
	}

	/**
	 * Always throws an exception.
	 */
	@Override
	public void delete() throws T2DBException {
		throw T2DBMsg.exception(D.D40101);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}

	@Override
	public String toString() {
		return name;
	}
	
}
