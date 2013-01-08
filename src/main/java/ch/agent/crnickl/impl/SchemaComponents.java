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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.T2DBMsg;
import ch.agent.crnickl.T2DBMsg.D;
import ch.agent.crnickl.api.SchemaComponent;

/**
 * SchemaComponents is a managed collection of {@link SchemaComponent} objects.
 * Components can be added, deleted, edited. Components have a name, which can
 * be modified in edit mode. Names, when not null, must be unique. Note that a 
 * complete component has a non-null name. 
 * 
 * @author Jean-Paul Vetterli
 * @param <T>
 *            the type of {@link SchemaComponent} managed by the collection
 */
public class SchemaComponents<T extends SchemaComponent> implements SchemaComponentContainer, Containable {

	private Map<Integer, T> components;
	private Map<Integer, T> editedComponents;
	private Map<String, T> byName;
	private SchemaComponentContainer container;
	
	/**
	 * Construct a managed collection of schema components. 
	 * 
	 * @param components
	 *            a collection of initial components to add
	 * @throws T2DBException if components contain a duplicate name or number 
	 */
	public SchemaComponents(Collection<T> components) throws T2DBException {
		this.components = new TreeMap<Integer, T>();
		this.byName = new HashMap<String, T>();

		if (components != null) {
			for (T component : components) {
				if (this.components.put(component.getNumber(), component) != null)
					throw T2DBMsg.exception(D.D30135, component.getNumber());
			}
			makeIndex();
		}
	}
	
	@Override
	public void setContainer(SchemaComponentContainer container) {
		this.container = container;
	}

	@Override
	public void nameChanged(boolean  attribute, String oldName, String newName) throws T2DBException {
		if (attribute) {
			makeIndex(); // kiss (updating schemas is not on the critical path)
		} else {
			if (container != null)
				container.nameChanged(false, oldName, newName);
			else
				makeIndex(); // kiss
		}
	}

	/**
	 * Return true if all components are complete.
	 * 
	 * @return true if all components are complete
	 */
	public boolean isComplete() {
		boolean complete = true;
		for (T compo : getMap().values()) {
			if (!compo.isComplete()) {
				complete = false;
				break;
			}
		}
		assert (!complete || getMap().size() == byName.size());
		return complete;
	}

	/**
	 * Return all components. Components are sorted by number.
	 * 
	 * @return the collection of components
	 */
	public Collection<T> getComponents() {
		return getMap().values();
	}
	
	protected Map<Integer, T> getMap() {
		return editedComponents == null ? components : editedComponents;
	}
	
	/**
	 * Return the component with the given number. Return null if not
	 * found.
	 * 
	 * @param number a positive number
	 * @return the component with the given number
	 */
	public T getComponent(int number) {
		return getMap().get(number);
	}
	
	/**
	 * Return the component with the given name. Return null if not
	 * found.
	 * 
	 * @param name a string
	 * @return the component with the given name
	 */
	public T getComponent(String name) {
		return name == null ? null : byName.get(name);
	}

	/**
	 * Enter edit mode and edit the component specified.
	 * Return the component with the given number or null if not
	 * found. If the component is not null, its edit mode is set.
	 * 
	 * @param number a positive number
	 * @return a component or null
	 * @throws T2DBException
	 */
	public T editComponent(int number) {
		edit();
		T component = editedComponents.get(number);
		if (component != null)
			component.edit();
		return component;
	}
 	
	/**
	 * Add a new component. If there is no component with the same number, set
	 * its edit mode and add it to the collection of components and return true.
	 * Else do nothing and return false.
	 * 
	 * @param component
	 *            a component
	 * @return true if the component was added else false
	 * @throws T2DBException
	 */
	public boolean addComponent(T component) throws T2DBException {
		boolean added = false;
		edit();
		Integer number = component.getNumber();
		if (editedComponents.get(number) == null) {
			String name = component.getName();
			if (getComponent(name) != null)
				throw T2DBMsg.exception(D.D30130, name);
			byName.put(name, component);
			component.edit();
			editedComponents.put(number, component);
			if (component instanceof Containable)
				((Containable) component).setContainer(this);
			added = true;
		}
		return added;
	}

	/**
	 * If a component with the given number exists remove it and return true.
	 * Else return false.
	 * 
	 * @param number a positive number
	 * @return true unless the component was not found
	 */
	public boolean deleteComponent(int number) {
		edit();
		T component = editedComponents.remove(number);
		if (component != null) {
			String name = component.getName();
			if (name != null)
				byName.remove(name);
			if (component instanceof Containable)
				((Containable) component).setContainer(null);
		}
		return component != null;
	}
	
	/**
	 * Enter edit mode. The index is discarded.
	 */
	@SuppressWarnings("unchecked")
	protected void edit() {
		if (editedComponents == null) {
			editedComponents = new HashMap<Integer, T>();
			for (Integer key : components.keySet()) {
				// IMPORTANT: deep copy
				T component = (T) components.get(key).copy();
				if (component instanceof Containable)
					((Containable) component).setContainer(this);
				editedComponents.put(key, component);
			}
		}
	}
	
	/**
	 * Leave edit mode.
	 */
	public void consolidate() {
		if (editedComponents != null) {
			components = editedComponents;
			for (T component : components.values()) {
				if (component instanceof Containable)
					((Containable) component).setContainer(null);
			}
			editedComponents = null;
		}
	}

	private void makeIndex() throws T2DBException {
		byName.clear();
		for (T component : getMap().values()) {
			String name = component.getName();
			if (name != null) {
				if (byName.put(name, component) != null)
					throw T2DBMsg.exception(D.D30130, name);
			}
		}
	}
	
	@Override
	public String toString() {
		return components.values().toString();
	}
	
}
