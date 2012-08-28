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
 * Type: UpdateEventOperation
 * Version: 1.0.0
 */
package ch.agent.crnickl.api;


/**
 * UpdateEventOperation defines the operation described by an {@link UpdateEvent}. 
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public enum UpdateEventOperation {
	/**
	 * The event is the creation of an object. 
	 */
	CREATE, 
	/**
	 * The event is the modification of an object. 
	 */
	MODIFY, 
	/**
	 * The event is the deletion of an object. 
	 */
	DELETE
}
