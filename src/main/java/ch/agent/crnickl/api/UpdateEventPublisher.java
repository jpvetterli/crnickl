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
package ch.agent.crnickl.api;



/**
 * An UpdateEventPublisher takes subscriptions and cancellations to {@link UpdateEvent}s
 * from {@link UpdateEventSubscriber}s.
 * 
 * @author Jean-Paul Vetterli
 */
public interface UpdateEventPublisher {
	/**
	 * Add a subscriber to event for a given database object type.
	 * Priority subscribers are notified before non-priority subscribers.
	 * It is the client's responsibility to decide when a
	 * priority subscription is warranted.
	 * 
	 * @param subscriber an update event subscriber
	 * @param type the database object type subscribed to 
	 * @param priority if true, handle as a priority subscription
	 */
	void subscribe(UpdateEventSubscriber subscriber, DBObjectType type, boolean priority);
	
	/**
	 * Remove a subscriber.
	 * 
	 * @param subscriber an update event subscriber
	 */
	void unsubscribe(UpdateEventSubscriber subscriber);
	
	/**
	 * Remove all subscribers.
	 */
	void unsubscribeAll();
	
	/**
	 * Publish an event. Subscribers are either notified immediately
	 * or at a later time when the {@link #release()} method is invoked.
	 * 
	 * @param event an update event
	 * @param immediate if true notify subscribers immediately
	 */
	void publish(UpdateEvent event, boolean immediate);
	
	/**
	 * Release deferred events.
	 */
	public void release();

}
