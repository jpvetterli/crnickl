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
 * Type: SchemaComponents
 * Version: 1.0.0
 */
package ch.agent.crnickl.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.api.SchemaComponent;

/**
 * SchemaComponents is a managed collection of {@link SchemaComponent} objects.
 * Components can be added, deleted, edited.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 * @param <T> the type of {@link SchemaComponent} managed by the collection
 */
public class SchemaComponents<T extends SchemaComponent> {

	private Map<Integer, T> components;
	private Map<Integer, T> editedComponents;
	private Map<String, T> byName;
	private int nameIndexThreshold;
	
	/**
	 * Construct a managed collection of schema components. An easy way to
	 * disable indexing is to pass a negative performance tuning parameter.
	 * 
	 * @param components
	 *            a collection of initial components to add
	 * @param nameIndexThreshold
	 *            if the number of components is larger than this threshold, a
	 *            lookup index for searching by name is set up
	 */
	public SchemaComponents(Collection<T> components, int nameIndexThreshold) {
		this.components = new TreeMap<Integer, T>();
		this.nameIndexThreshold = nameIndexThreshold;
		
		if (components != null) {
			for (T component : components) {
				this.components.put(component.getNumber(), component);
			}
			makeIndex();
		}
	}
	
	/**
	 * Return true if all components are ready and if edit mode is not active.
	 * 
	 * @return true if all components are ready and edit mode is not active
	 */
	public boolean isComplete() {
		boolean ready = editedComponents == null;
		if (ready) {
			for (T compo : components.values()) {
				if (!compo.isComplete()) {
					ready = false;
					break;
				}
			}
		}
		return ready;
	}

	/**
	 * Return all components. Components are sorted by number.
	 * 
	 * @return the collection of components
	 */
	public Collection<T> getComponents() {
		return components.values();
	}

	/**
	 * Return all components, modified (edited, deleted) or not modified. 
	 * Components are sorted by number. If no component was modified, return null.
	 * When no component was modified, use {@link #getComponents()}.
	 * 
	 * @return all edited components or null
	 */
	public Collection<T> getEditedComponents() {
		unEdit();
		return editedComponents == null ? null : editedComponents.values();
	}
	
	/**
	 * Return the components as a map. If the <code>edited</code>
	 * flag is set, the edited map is returned, else the original map.
	 * The edited map includes components which have been modified and components which
	 * have not been modified. Components deleted are recognized by their absence.
	 * When nothing at all was modified, the result is null.
	 * 
	 * @param edited if true return the edited map, else the original
	 * @return a map or null when the edited was requested but nothing was modified
	 */
	public Map<Integer, T> getMap(boolean edited) {
		if (edited) {
			unEdit();
			return editedComponents;
		} else
			return components;
	}
	
	/**
	 * Return the component with the given number. Return null if not
	 * found.
	 * 
	 * @param number a positive number
	 * @return the component with the given number
	 */
	public T getComponent(int number) {
		return components.get(number);
	}
	
	/**
	 * Return the component with the given name. Return null if not
	 * found.
	 * 
	 * @param name a string
	 * @return the component with the given name
	 */
	public T getComponent(String name) {
		T component = null;
		if (byName == null) {
			for (T c : components.values()) {
				if (c.getName().equals(name)) {
					component = c;
					break;
				}
			}
		} else
			component = byName.get(name);
		return component;
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
			component.edit();
			editedComponents.put(number, component);
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
		return editedComponents.remove(number) != null;
	}
	
	/**
	 * Enter edit mode. The index is discarded.
	 */
	@SuppressWarnings("unchecked")
	protected void edit() {
		if (editedComponents == null) {
			// CAUTION, deep copy required
			editedComponents = new HashMap<Integer, T>();
			for (Integer key : components.keySet()) {
				editedComponents.put(key, (T) components.get(key).copy());
			}
			byName = null;
		}
	}
	
	private void unEdit() {
		if (components.equals(editedComponents)) {
			editedComponents = null;
			makeIndex();
		}
	}
	
	/**
	 * If edit mode is active, consolidate updates into components.
	 * Consolidating means:
	 * <ul>
	 * <li>removing components from this collection
	 * if they are on the list of deleted components,
	 * <li>adding or editing components
	 * which are in the list of edited components,
	 * <li>checking that component names are unique,
	 * <li>clearing the lists of edited and deleted components,
	 * <li>making indexes, and
	 * <li>exiting edit mode.
	 * </ul>
	 */
	public void consolidate() throws T2DBException {
		if (editedComponents != null) {
			components = new TreeMap<Integer, T>(editedComponents);
			editedComponents = null;
			makeIndex();
		}
	}
	
	private void makeIndex(Collection<T> components, int threshold) {
		byName = null;
		if (threshold > -1) {
			if (components.size() > threshold && isComplete()) {
				byName = new HashMap<String, T>(components.size());
				for (T component : components) {
					String name = component.getName();
					if (name == null) // isComplete violation ??
						throw new IllegalArgumentException("bug: "+ component.getNumber());
					byName.put(name, component);
				}
			}
		}
	}
	
	private void makeIndex() {
		makeIndex(components.values(), nameIndexThreshold);
	}

	@Override
	public String toString() {
		return components.values().toString();
	}

}
