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

import ch.agent.crnickl.T2DBException;
import ch.agent.t2.time.TimeDomain;

/**
 * An UpdatableSchema is a schema which can be modified. A schema can only be
 * modified if it is not in use or if the modification has no consequence on
 * existing use. For example a series can always be added but can only be
 * deleted if no chronicle using the schema has actually created the series.
 * <p>
 * Updatable schemas are not only used in client applications like schema editors but
 * also by CrNiCKL internally. Any schema can be defined as an extension to
 * another schema. Schemas are kept in permanent storage without resolving these
 * extensions. Before a schema is made available for use, CrNiCKL resolves the extensions.
 * Extensions are visible to clients only in edit mode.
 * <p>
 * A hypothetical example will help to illustrate how schemas are constructed. The example
 * uses 3 schemas: a, b, and c. Schema a defines two series, s1 and s2, and one attribute, a1.
 * Schema b extends schema a; it removes series s2, and adds series s3. Schema c extends
 * schema b; it adds attribute a2 and redefines attribute a1 (for example with a different
 * default value). Given this design, a chronicle with effective schema c, 
 * will have two series, s1 and s3, and two attributes, a1 and a2. It will not be aware
 * of the existence of schemas a and b.  
 * 
 * @author Jean-Paul Vetterli
 */
public interface UpdatableSchema extends Schema, Updatable {

	/**
	 * Destroy the schema. The operation fails if the schema is used.
	 * 
	 * @throws T2DBException
	 */
	void destroy() throws T2DBException;
	
	/**
	 * Resolve into a schema. Resolving an updatable schema resolves its base
	 * schema recursively, then adds, edits, and deletes attributes and series
	 * as specified in this updatable schema. Once resolved, the schema must be
	 * complete as determined by {@link Schema#isComplete()}.
	 * 
	 * @return a schema
	 * @throws T2DBException
	 */
	Schema resolve() throws T2DBException;

	/**
	 * Set the name of the schema. May not be null or empty.
	 * 
	 * @param name a string
	 * @throws T2DBException
	 */
	void setName(String name) throws T2DBException;
	
	/**
	 * Set the base of the schema. When not null, the base schema may 
	 * not be in construction (see {@link DBObject#inConstruction()}).
	 * 
	 * @param base an updatable schema or null
	 * @throws T2DBException
	 */
	void setBase(UpdatableSchema base) throws T2DBException;

	/**
	 * Add a new attribute to the schema.
	 * 
	 * @param attrNr a positive number not already used
	 * @return an attribute definition
	 * @throws T2DBException
	 */
	AttributeDefinition<?> addAttribute(int attrNr) throws T2DBException;	
	
	/**
	 * Delete an attribute from the schema.
	 * 
	 * @param attrNr the number of an existing attribute
	 * @throws T2DBException
	 */
	void deleteAttribute(int attrNr) throws T2DBException;
	
	/**
	 * Set an attribute as erasing. An erasing attribute 
	 * deletes an attribute with the same number inherited from 
	 * a base schema during schema resolution.  
	 * 
	 * @param attrNr the number of an existing attribute
	 * @throws T2DBException
	 */
	void eraseAttribute(int attrNr) throws T2DBException;
	
	/**
	 * Set the property of an attribute.
	 * 
	 * @param attrNr the number of an existing attribute
	 * @param property a property
	 * @throws T2DBException
	 */
	<T>void setAttributeProperty(int attrNr, Property<T> property) throws T2DBException;	
	
	/**
	 * Set the default value of an attribute.
	 * 
	 * @param attrNr the number of an existing attribute
	 * @param defaultValue a value
	 * @throws T2DBException
	 */
	<T>void setAttributeDefault(int attrNr, T defaultValue) throws T2DBException;	
	
	/**
	 * Add a new series to the schema.
	 * 
	 * @param seriesNr a positive number not already used
	 * @return a series definition
	 * @throws T2DBException
	 */
	SeriesDefinition addSeries(int seriesNr) throws T2DBException;	
	
	/**
	 * Delete a series from the schema.
	 * 
	 * @param seriesNr the number of an existing series
	 * @throws T2DBException
	 */
	void deleteSeries(int seriesNr) throws T2DBException;
	
	/**
	 * Set a series as erasing. An erasing series deletes
	 * a series with the same number inherited from 
	 * a base schema during schema resolution.  
	 * 
	 * @param seriesNr the number of an existing series
	 * @throws T2DBException
	 */
	void eraseSeries(int seriesNr) throws T2DBException;

	/**
	 * Set the name of a series.
	 * 
	 * @param seriesNr the number of an existing series
	 * @param name a string
	 * @throws T2DBException
	 */
	void setSeriesName(int seriesNr, String name) throws T2DBException;	
	
	/**
	 * Set the description of the series.
	 * 
	 * @param seriesNr the number of an existing series
	 * @param description a string
	 * @throws T2DBException
	 */
	void setSeriesDescription(int seriesNr, String description) throws T2DBException;	
	
	/**
	 * Set the value type of the series.
	 * 
	 * @param seriesNr the number of an existing series
	 * @param type a value type
	 * @throws T2DBException
	 */
	void setSeriesType(int seriesNr, ValueType<?> type) throws T2DBException;	
	
	/**
	 * Set the value type of the series using the name of the value type.
	 * 
	 * @param seriesNr the number of an existing series
	 * @param type the name of a value type defined in the database
	 * @throws T2DBException
	 */
	void setSeriesType(int seriesNr, String type) throws T2DBException;	
	
	/**
	 * Set the time domain of the series.
	 * 
	 * @param seriesNr the number of an existing series
	 * @param timeDomain a time domain
	 * @throws T2DBException
	 */
	void setSeriesTimeDomain(int seriesNr, TimeDomain timeDomain) throws T2DBException;	
	
	/**
	 * Set the sparsity of the series. By default series are not sparse.
	 * 
	 * @param seriesNr the number of an existing series
	 * @param sparse true to force the series to use sparse time series, else false
	 * @throws T2DBException
	 */
	void setSeriesSparsity(int seriesNr, boolean sparse) throws T2DBException;	
	
	/**
	 * Add a new attribute to a series.
	 * 
	 * @param seriesNr the number of an existing series
	 * @param attrNr a positive number not yet in use within the series
	 * @return an attribute definition
	 * @throws T2DBException
	 */
	AttributeDefinition<?> addAttribute(int seriesNr, int attrNr) throws T2DBException;	
	
	/**
	 * Delete an attribute from a series.
	 * 
	 * @param seriesNr the number of an existing series
	 * @param attrNr the number of an existing attribute within the series
	 * @throws T2DBException
	 */
	void deleteAttribute(int seriesNr, int attrNr) throws T2DBException;
	
	/**
	 * Set a series attribute as erasing.
	 * 
	 * @param seriesNr the number of an existing series
	 * @param attrNr the number of an existing attribute within the series
	 * @throws T2DBException
	 */
	void eraseAttribute(int seriesNr, int attrNr) throws T2DBException;
	
	/**
	 * Set the property of a series attribute.
	 * 
	 * @param seriesNr the number of an existing series
	 * @param attrNr the number of an existing attribute within the series
	 * @param property a property
	 * @throws T2DBException
	 */
	void setAttributeProperty(int seriesNr, int attrNr, Property<?> property) throws T2DBException;	
	
	/**
	 * Set the default value of a series attribute.
	 * 
	 * @param seriesNr the number of an existing series
	 * @param attrNr the number of an existing attribute within the series
	 * @param defaultValue a value
	 * @throws T2DBException
	 */
	void setAttributeDefault(int seriesNr, int attrNr, Object defaultValue) throws T2DBException;	
	
	/**
	 * Return the base schema if any.
	 * 
	 * @return an updatable schema or null
	 */
	UpdatableSchema getBase();
	
}
