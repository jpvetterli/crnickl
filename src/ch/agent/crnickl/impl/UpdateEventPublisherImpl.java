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
 * Type: UpdateEventPublisherImpl
 * Version: 1.0.0
 */
package ch.agent.crnickl.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.agent.crnickl.api.DBObjectType;
import ch.agent.crnickl.api.UpdateEvent;
import ch.agent.crnickl.api.UpdateEventPublisher;
import ch.agent.crnickl.api.UpdateEventSubscriber;

/**
 * Default implementation of {@link UpdateEventPublisher}.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public class UpdateEventPublisherImpl implements UpdateEventPublisher {

	/**
	 * A Subscription keeps track of the event types an {@link UpdateEventSubscriber}
	 * is interested in.
	 */
	protected class Subscription {

		private boolean[] types;
		private boolean priority;
		
		/**
		 * Construct a subscription.
		 * 
		 */
		public Subscription() {
			types = new boolean[DBObjectType.values().length];
		}
		
		/**
		 * Return true if it is a priority subscription.
		 * @return true for a priority subscription
		 */
		public boolean isPriority() {
			return priority;
		}

		/**
		 * Set the priority.
		 * 
		 * @param priority if true, the subscription will be treated before non-priority subscriptions
 		 */
		public void setPriority(boolean priority) {
			this.priority = priority;
		}

		/**
		 * Return true if the event type is subscribed.
		 * Recall that the event type is the database object type of the event source.
		 * 
		 * @param type a database object type
		 * @return true if the type is subscibed
		 */
		public boolean isSubscribed(DBObjectType type) {
			return types[type.ordinal()];
		}

		/**
		 * Subscribe to an event type.
		 * 
		 * @param type a database object type
		 */
		public void subscribe(DBObjectType type) {
			types[type.ordinal()] = true;
		}
		
		/**
		 * Cancel the subscription to an event type.
		 * 
		 * @param type a database object type
		 */
		public void unsubscribe(DBObjectType type) {
			types[type.ordinal()] = false;
		}
	}
	
	private Map<UpdateEventSubscriber, Subscription> subscriptions;
	private Map<UpdateEventSubscriber, Subscription> prioritySubscriptions;
	private List<UpdateEvent> events;
	
	/**
	 * Construct an {@link UpdateEventPublisher}.
	 * 
	 */
	public UpdateEventPublisherImpl() {
		subscriptions = new HashMap<UpdateEventSubscriber, Subscription>();
		prioritySubscriptions = new HashMap<UpdateEventSubscriber, Subscription>();
		events = new ArrayList<UpdateEvent>();
	}

	@Override
	public void subscribe(UpdateEventSubscriber subscriber,	DBObjectType type, boolean priority) {
		Subscription s = subscriptions.get(subscriber);
		if (s == null)
			s = prioritySubscriptions.get(subscriber);
		if (s == null) {
			s = new Subscription();
			if (priority) {
				s.setPriority(true);
				prioritySubscriptions.put(subscriber, s);
			} else
				subscriptions.put(subscriber, s);
		} else {
			// upgrade subscription
			if (priority && !s.isPriority()){
				s.setPriority(true);
				subscriptions.remove(subscriber);
				prioritySubscriptions.put(subscriber, s);
			}
		}
		s.subscribe(type);
	}

	@Override
	public void unsubscribe(UpdateEventSubscriber subscriber) {
		subscriptions.remove(subscriber);
		prioritySubscriptions.remove(subscriber);
	}

	@Override
	public void unsubscribeAll() {
		subscriptions.clear();
		prioritySubscriptions.clear();
	}

	@Override
	public void publish(UpdateEvent event, boolean immediate) {
		if (immediate) {
			notify(event, prioritySubscriptions);
			notify(event, subscriptions);
		} else
			events.add(event);
	}
	
	@Override
	public void release() {
		for (UpdateEvent event : events) {
			notify(event, prioritySubscriptions);
		}
		for (UpdateEvent event : events) {
			notify(event, subscriptions);
		}
		clear();
	}
	
	private void notify(UpdateEvent event, Map<UpdateEventSubscriber, Subscription> subs) {
		for (Map.Entry<UpdateEventSubscriber, Subscription> s : subs.entrySet()) {
			if (s.getValue().isSubscribed(event.getType())) {
				s.getKey().notify(event);
			}
		}
	}

	/**
	 * Forget all events collected without notifying subscribers.
	 * 
	 * @return the number of events collected
	 */
	public int clear() {
		int size = events.size();
		events.clear();
		return size;
	}
	
}
