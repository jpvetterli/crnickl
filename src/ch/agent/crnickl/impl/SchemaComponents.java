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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.T2DBMsg;
import ch.agent.crnickl.T2DBMsg.D;
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
	private Map<String, T> byName;
	private T[] byNumber;
	private int byNumberBase;
	private Map<Integer, T> edited;
	private Set<Integer> deleted;
	private boolean editMode;
	private int nameIndexThreshold;
	private int numberIndexMaxRange; 
	private T[] template;
	
	/**
	 * Construct a managed collection of schema components. An easy way to
	 * disable indexing is to pass negative performance tuning parameters.
	 * 
	 * @param components
	 *            a collection of initial components to add
	 * @param nameIndexThreshold
	 *            if the number of components is larger than this threshold, a
	 *            lookup index for searching by name is set up
	 * @param numberIndexMaxRange
	 *            if the range of component numbers is not larger than this
	 *            parameter, a lookup array for searching by number is set up
	 * @param template
	 *            template for the lookup array for searching by number
	 */
	public SchemaComponents(Collection<T> components, int nameIndexThreshold, int numberIndexMaxRange, T[] template) {
		this.components = new TreeMap<Integer, T>();
		this.nameIndexThreshold = nameIndexThreshold;
		this.numberIndexMaxRange = numberIndexMaxRange;
		this.template = template;
		
		if (components != null) {
			for (T component : components) {
				this.components.put(component.getNumber(), component);
			}
			makeIndexes();
		}
	}
	
	/**
	 * Construct a managed collection of schema components with no indexing.
	 * For a schema with only a few attributes or series, indexes are not useful.
	 * 
	 * @param components a collection of initial components to add 
	 */
	public SchemaComponents(Collection<T> components) {
		this(components, -1, -1, null);
	}

	private void clearUpdates() {
		if (edited != null)
			edited.clear();
		if (deleted != null)
			deleted.clear();
	}
	
	/**
	 * Return true if all components are ready and if edit mode is not active.
	 * 
	 * @return true if all components are ready and edit mode is not active
	 */
	public boolean isComplete() {
		boolean ready = !editMode;
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
	 * Return all edited components. Components are sorted by number.
	 * 
	 * @return all edited components
	 */
	public Collection<T> getEditedComponents() {
		return edited == null ? new ArrayList<T>() : edited.values();
	}
	
	/**
	 * Return the set of deleted component numbers.
	 * 
	 * @return the set of deleted component numbers
	 */
	public Set<Integer> getDeletedComponents() {
		return deleted == null ? new HashSet<Integer>() : deleted;
	}
	
	/**
	 * Return the component with the given number. Return null if not
	 * found.
	 * 
	 * @param number a positive number
	 * @return the component with the given number
	 */
	public T getComponent(int number) {
		T component = null;
		if (byNumber != null) {
			int i = number - byNumberBase;
			if (i >= 0 && i < byNumber.length)
				 component = byNumber[i];
		} else
			component = components.get(number);
		return component;
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
	 * Return the component with the given number. Return null if not
	 * found. The component is put on a list of edited components and its edit mode is set.
	 * If the component was on the list of deleted components, it is removed from that list.
	 * 
	 * @param number a positive number
	 * @return a component
	 * @throws T2DBException
	 */
	public T edit(int number) {
		edit();
		deleted.remove(number);
		T component = edited.get(number);
		if (component == null) {
			component = getComponent(number);
			if (component != null) {
				component.edit();
				edited.put(number, component);
			}
		}
		return component;
	}
 	
	/**
	 * Add a new component. Return false if there is already a component with the
	 * same number. Put the new component on the list of edited components. If
	 * the component was on the list of deleted components, it is removed from
	 * that list.
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
		if (getComponent(number) == null) {
			if (edited.get(number) == null) {
				deleted.remove(number);
				component.edit();
				edited.put(number, component);
				added = true;
			}
		} 
		return added;
	}

	/**
	 * If the component is found on the list of edited components, remove it and return true.
	 * If the component is found in this collection, put it on the list of 
	 * deleted components and return true.
	 * If the component is not found return false.  
	 * 
	 * @param number a positive number
	 * @return true unless the component was not found
	 */
	public boolean deleteComponent(int number) {
		edit();
		boolean done = false;
		if (edited.remove(number) != null)
			done = true;
		else {
			if (getComponent(number) != null) {
				deleted.add(number);
				done = true;
			}
		}
		return done;
	}
	
	/**
	 * Enter edit mode. Indexes are eliminated.
	 */
	protected void edit() {
		if (!editMode) {
			editMode = true;
			edited = new TreeMap<Integer, T>();
			deleted = new TreeSet<Integer>();
			byName = null;
			byNumber = null;
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
		if (editMode) {
			for (Integer index : deleted) {
				components.remove(index);
			}
			for (Map.Entry<Integer, T> e : edited.entrySet()) {
				if (components.containsKey(e.getKey())) {
					components.get(e.getKey()).edit(e.getValue());
				} else {
					e.getValue().consolidate();
					components.put(e.getKey(), e.getValue());
				}
			}
			clearUpdates();
			editMode = false;
			checkUniqueNames(components.values());
			makeIndexes();
		}
	}
	
	private void checkUniqueNames(Collection<T> components) throws T2DBException {
		Set<String> names = new HashSet<String>(components.size());
		for (T compo : components) {
			if (!names.add(compo.getName()))
				throw T2DBMsg.exception(D.D30130, compo.getName());
		}
	}
	
	private void makeNumberIndex(Collection<T> components, int maxRange, T[] template) {
		byNumber = null;
		if (maxRange > -1) {
			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;
			for (T component : components) {
				min = Math.min(min, component.getNumber());
				max = Math.max(max, component.getNumber());
			}
			int range = max - min + 1;
			if (range <= maxRange) {
				byNumberBase = min;
				List<T> list = new ArrayList<T>(range);
				for (int i = 0; i < range; i++)
					list.add(null);
				for (T component : components) {
					list.set(component.getNumber() - byNumberBase, component);
				}
				byNumber = list.toArray(template);
			}
		}
	}
	
	private void makeNameIndex(Collection<T> components, int threshold) {
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
	
	private void makeIndexes() {
		makeNameIndex(components.values(), nameIndexThreshold);
		makeNumberIndex(components.values(), numberIndexMaxRange, template);
	}

	@Override
	public String toString() {
		return components.values().toString();
	}

}
