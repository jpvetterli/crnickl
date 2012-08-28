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
 * Type: MessageListener
 * Version: 1.0.0
 */
package ch.agent.crnickl.api;

import java.util.EventListener;
import java.util.logging.Level;

import ch.agent.core.KeyedMessage;

/**
 * A MessageListener is used by CrNiCKL for
 * logging messages. Applications provide the implementation.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public interface MessageListener extends EventListener {
	
	/**
	 * Configure the message listener to filter out messages at
	 * a logging level lower than the argument.
	 *  
	 * @param level minimum level required for messages to be logged
	 */
	void setFilterLevel(Level level);

	/**
	 * Return true if a message at the given level passes the filter.
	 * 
	 * @param level a logging level
	 * @return true if messages at the give level will be logged
	 */
	boolean isListened(Level level);

	/**
	 * Log a keyed message at the level specified.
	 * 
	 * @param level a logging level
	 * @param msg a keyed message
	 */
	void log(Level level, KeyedMessage msg);

	/**
	 * Log a text at the level specified.
	 * 
	 * @param level a logging level
	 * @param text a string
	 */
	void log(Level level, String text);

	/**
	 * Log an exception. Implementations typically log the exception message at
	 * the SEVERE level and possibly dump a stack trace in the log.
	 * 
	 * @param e
	 *            an exception
	 */
	void log(Exception e);
}
