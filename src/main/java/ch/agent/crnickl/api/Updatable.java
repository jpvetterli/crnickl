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
 * Type: Updatable
 * Version: 1.0.0
 */
package ch.agent.crnickl.api;

import ch.agent.crnickl.T2DBException;

/**
 * An Updatable database object keeps tracks of updates and can apply them all at once. 
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public interface Updatable {
	
	/**
	 * Apply pending updates. Any error, including lack of authorization,
	 * results in an exception being thrown. This method does not manage
	 * transactions nor does it impose transaction boundaries. The management of
	 * transactions using {@link Database#commit()} and
	 * {@link Database#rollback()} is the client's responsibility.
	 * 
	 * @throws T2DBException
	 */
	public void applyUpdates() throws T2DBException;
	
}
