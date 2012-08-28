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
 * Type: DBObjectType
 * Version: 1.0.0
 */
package ch.agent.crnickl.api;


/**
 * DBObjectType identifies the type of {@link DBObject}
 * a {@link Surrogate} represents.
 *  
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public enum DBObjectType {
	/**
	 * Identifies a {@link Chronicle}.
	 */
	CHRONICLE, 
	/**
	 * Identifies a {@link Series}.
	 */
	SERIES, 
	/**
	 * Identifies a {@link Schema}.
	 */
	SCHEMA, 
	/**
	 * Identifies a {@link Property}.
	 */
	PROPERTY, 
	/**
	 * Identifies a {@link ValueType}.
	 */
	VALUE_TYPE
}
