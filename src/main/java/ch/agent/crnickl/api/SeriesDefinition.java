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
 * Type: SeriesDefinition
 * Version: 1.0.0
 */
package ch.agent.crnickl.api;

import java.util.Collection;

import ch.agent.crnickl.T2DBException;
import ch.agent.t2.time.TimeDomain;

/**
 * A SeriesDefinition is a component part of a {@link Schema} and defines a {@link Series}.
 * Like a schema, a series definition can define a number of {@link Attribute}s.
 *
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public interface SeriesDefinition extends SchemaComponent, IncompleteSchema, Comparable<SeriesDefinition> {
	
	/**
	 * Return the description of the series.
	 * 
	 * @return the series description
	 * @throws T2DBException
	 */
	String getDescription();
	
	/**
	 * Return the time domain of the series.
	 * 
	 * @return the time domain of the series
	 */
	TimeDomain getTimeDomain();
	
	/**
	 * Return the value type of the series.
	 * 
	 * @return the value type of the series
	 */
	ValueType<?> getValueType();

	/**
	 * Return true to force the series to use sparse time series. By default
	 * series leave the choice to a lower layer of the software.
	 * 
	 * @return true if time series are sparse
	 */
	boolean isSparse();
	
	/**
	 * Return the collection of attribute definitions which are not built-in.
	 * The collection is sorted by attribute number.
	 * 
	 * @return the collection of non-built-in attribute definitions
	 */
	Collection<AttributeDefinition<?>> getCustomAttributeDefinitions();

	
}
