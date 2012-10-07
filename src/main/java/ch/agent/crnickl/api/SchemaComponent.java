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
 * Package: ch.agent.crnickl.api
 * Type: SchemaComponent
 * Version: 1.0.0
 */
package ch.agent.crnickl.api;

import ch.agent.crnickl.T2DBException;

/**
 * SchemaComponent provides common behavior to {@link AttributeDefinition}
 * and {@link SeriesDefinition}.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public interface SchemaComponent {

	/**
	 * Return the name of the component. The name can be null when the component is not
	 * complete. The name is unique within its container.
	 * 
	 * @return the name of the component or null
	 */
	String getName();

	/**
	 * Return the number of the component. The number is always positive and is unique
	 * within its container.
	 * 
	 * @return a positive number
	 */
	int getNumber();

	/**
	 * Return true if the component is complete and can be used. All the
	 * components of a {@link Schema} defining an actual {@link Chronicle} must
	 * be complete. If the component is an {@link AttributeDefinition}, it is
	 * complete when the property of the attribute is defined and the default
	 * value is valid. If the component is a {@link SeriesDefinition}, it is
	 * complete when the name, the type, and the time domain of the series are
	 * defined, and all the series attributes are complete.
	 * 
	 * @return true if the component can be used
	 */
	boolean isComplete();

	/**
	 * Return true if the definition removes an inherited definition.
	 * This method is relevant during execution of 
	 * {@link UpdatableSchema#resolve()}. 
	 * 
	 * @return true if the definition removes an inherited definition
	 */
	boolean isErasing();
	
	/**
	 * Enter edit mode. 
	 */
	void edit();
	
	/**
	 * Edit the component, taking the parameter as a template. 
	 * The parameter must be of the same class as this component.
	 *  
	 * @param component a component serving as template
	 */
	void edit(SchemaComponent component) throws T2DBException;
	
	/**
	 * This method is not used any more and will be eliminated in a future
	 * version.
	 */
	@Deprecated
	void consolidate() throws T2DBException;
	
	/**
	 * Return a copy.
	 * 
	 * @return a copy
	 */
	SchemaComponent copy();
}
