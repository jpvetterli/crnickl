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
package ch.agent.crnickl;

import ch.agent.core.KeyedException;
import ch.agent.core.KeyedMessage;

/**
 * A T2DBException is a checked exception thrown by methods in CrNiCKL.
 * For programming errors, like null pointer, out-of-bounds, and many
 * illegal parameter errors, the software throws unchecked exceptions.
 * 
 * @author Jean-Paul Vetterli
 */
public class T2DBException extends KeyedException {
	
	private static final long serialVersionUID = -637297554920305224L;

	/**
	 * Construct an exception with a keyed message.
	 * 
	 * @param message a {@link KeyedMessage}
	 */
	public T2DBException(KeyedMessage message) {
		super(message);
	}
	
	/**
	 * Construct an exception with a keyed message and the causing exception.
	 * 
	 * @param message a {@link KeyedMessage}
	 * @param cause a {@link Throwable}
	 */
	public T2DBException(KeyedMessage message, Throwable cause) {
		super(message, cause);
	}
}