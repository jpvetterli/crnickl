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
package ch.agent.crnickl.impl;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.T2DBMsg;
import ch.agent.crnickl.T2DBMsg.D;
import ch.agent.crnickl.api.Database;
import ch.agent.crnickl.api.NamingPolicy;
import ch.agent.crnickl.api.Surrogate;
import ch.agent.crnickl.api.UpdatableValueType;
import ch.agent.crnickl.api.ValueScanner;
import ch.agent.crnickl.api.ValueType;
import ch.agent.t2.time.DateTime;
import ch.agent.t2.time.Day;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.time.TimeDomainCatalog;
import ch.agent.t2.time.TimeDomainCatalogSingleton;

/**
 * Default implementation of {@link ValueType}.
 * In agreement with the convention mentioned in the {@link ValueScanner}
 * class comment, the value type is passed to value scanner constructors
 * when such constructors are available.
 * 
 * @author Jean-Paul Vetterli
 * @param <T> the underlying data type of the values
 */
public class ValueTypeImpl<T> extends DBObjectImpl implements ValueType<T> {
	
	private static final int MAX_NAME_LENGTH = 25;
	private static final String NAME_PATTERN = "[A-Za-z0-9_][A-Za-z0-9_,\\-]*";
	
	/**
	 * A value scanner for boolean data. The scanner parses strings as
	 * {@link java.lang.Boolean}. The string "true" (case insensitive) is
	 * interpreted as true, all others as false.
	 */
	public static class BooleanScanner implements ValueScanner<Boolean> {
		
		@Override
		public Class<Boolean> getType() {
			return Boolean.class;
		}

		@Override
		public void check(Boolean value) {
		}

		@Override
		public Boolean scan(String value) throws T2DBException {
			return new Boolean(value);
		}

		@Override
		public String toString(Boolean value) {
			return value == null ? null : value.toString();
		}
		
	}
	
	/**
	 * A value scanner for numeric data. The scanner parses strings as
	 * {@link java.lang.Double}. A hyphen ("-") is interpreted as Double.NaN.
	 */
	public static class NumberScanner implements ValueScanner<Double> {
		
		@Override
		public Class<Double> getType() {
			return Double.class;
		}

		@Override
		public void check(Double value) {
		}

		@Override
		public Double scan(String value) throws T2DBException {
			try {
				return new Double(value);
			} catch (NumberFormatException e) {
				if (value != null && value.equals("-"))
					return Double.NaN;
				throw T2DBMsg.exception(D.D10103, value);
			}
		}

		@Override
		public String toString(Double value) {
			return value == null ? null : value.toString();
		}
		
	}
	
	/**
	 * A value scanner for textual data.
	 * After considering the alternatives,
	 * the design committee has decided to implement 
	 * textual data with {@link java.lang.String}.
	 */
	public static class TextScanner implements ValueScanner<String> {

		@Override
		public Class<String> getType() {
			return String.class;
		}

		@Override
		public void check(String value) {
		}

		@Override
		public String scan(String value) {
			return value;
		}

		@Override
		public String toString(String value) {
			return value;
		}
		
	}
	
	/**
	 * A value scanner for textual data representing simple names.
	 */
	public static class NameScanner implements ValueScanner<String> {

		private Matcher validNameMatcher;
		
		public NameScanner() {
			validNameMatcher = Pattern.compile(NAME_PATTERN).matcher("");
		}

		@Override
		public Class<String> getType() {
			return String.class;
		}

		@Override
		public void check(String value) throws T2DBException {
			if (value == null || value.length() == 0	|| value.length() > MAX_NAME_LENGTH)
				throw T2DBMsg.exception(D.D10105, value, 0, MAX_NAME_LENGTH);
			validNameMatcher.reset(value);
			if (!validNameMatcher.matches())
				throw T2DBMsg.exception(D.D10104, value, NAME_PATTERN);
		}

		@Override
		public String scan(String value) throws T2DBException {
			check(value);
			return value;
		}

		@Override
		public String toString(String value) throws T2DBException {
			check(value);
			return value;
		}
	}
	
	/**
	 * A value scanner for data representing {@link TimeDomain}s.
	 */
	public static class TimeDomainScanner implements ValueScanner<TimeDomain> {

		private TimeDomainCatalog catalog;
		
		public TimeDomainScanner() {
			catalog = TimeDomainCatalogSingleton.instance();
		}

		@Override
		public Class<TimeDomain> getType() {
			return TimeDomain.class;
		}

		@Override
		public void check(TimeDomain value) {
		}

		@Override
		public TimeDomain scan(String value) throws T2DBException {
			try {
				return catalog.get(value);
			} catch (Exception e) {
				throw T2DBMsg.exception(e, D.D10107, value, ValueType.class.getName());
			}
		}

		@Override
		public String toString(TimeDomain value) {
			return value == null ? null : value.getLabel();
		}
		
	}
	
	/**
	 * A value scanner for data representing {@link Day}s.
	 */
	public static class DateScanner implements ValueScanner<Day> {

		@Override
		public Class<Day> getType() {
			return Day.class;
		}

		@Override
		public void check(Day value) {
		}

		@Override
		public Day scan(String value) throws T2DBException {
			try {
				return new Day(value);
			} catch (Exception e) {
				throw T2DBMsg.exception(e, D.D10107, value, ValueType.class.getName());
			}
		}

		@Override
		public String toString(Day value) {
			return value == null ? null : value.toString();
		}
		
	}
	
	/**
	 * A value scanner for data representing {@link DateTime}s.
	 */
	public static class DateTimeScanner implements ValueScanner<DateTime> {

		@Override
		public Class<DateTime> getType() {
			return DateTime.class;
		}

		@Override
		public void check(DateTime value) {
		}

		@Override
		public DateTime scan(String value) throws T2DBException {
			try {
				return new DateTime(value);
			} catch (Exception e) {
				throw T2DBMsg.exception(e, D.D10107, value, ValueType.class.getName());
			}
		}

		@Override
		public String toString(DateTime value) {
			return value == null ? null : value.toString();
		}
		
	}
	
	/**
	 * A value scanner for data representing {@link ValueType}s.
	 */
	@SuppressWarnings("rawtypes")
	public static class TypeScanner implements ValueScanner<ValueType> {

		private Database database;
		
		public TypeScanner(Database database) {
			this.database = database;
		}

		@Override
		public Class<ValueType> getType() {
			return ValueType.class;
		}

		@Override
		public void check(ValueType value) {
		}

		@Override
		public ValueType<?> scan(String value) throws T2DBException {
			try {
				return database.getValueType(value);
			} catch (Exception e) {
				throw T2DBMsg.exception(e, D.D10107, value, ValueType.class.getName());
			}
		}

		@Override
		public String toString(ValueType value) {
			return value == null ? null : value.getName();
		}
		
	}
	
	private ValueScanner<T> scanner;
	private ValueAccessMethods<T> am; // can be null, but getting throws an exception
	private String name;
	private boolean restricted;
	private Map<T, String> values;
	private StandardValueType keyword; // null means custom scanner

	/**
	 * This constructor is intended for {@link UpdatableValueTypeImpl} when implementing
	 * the {@link #edit} method.
	 * 
	 * @param valueType
	 * @throws T2DBException
	 */
	protected ValueTypeImpl(ValueTypeImpl<T> valueType) throws T2DBException {
		super(valueType.getSurrogate());
		this.scanner = valueType.scanner;
		this.am = valueType.am;
		this.name = valueType.name;
		this.restricted = valueType.restricted;
		this.values = valueType.values;
		this.keyword = valueType.keyword;
	}

	/**
	 * Construct a {@link ValueType}.
	 * 
	 * @param name a string
	 * @param restricted if true the value type will keep a list of allowed values
	 * @param scannerClassOrKeyword a keyword identifying a standard value type of the name of a scanner class
	 * @param valuesAndDescriptions a map of allowed values (as strings) and their description
	 * @param surrogate a surrogate
	 * @throws T2DBException
	 */
	public ValueTypeImpl(String name, boolean restricted, String scannerClassOrKeyword, 
			Map<String, String> valuesAndDescriptions, Surrogate surrogate) throws T2DBException {
		super(surrogate);
		this.name = name;
		this.restricted = restricted;
		this.scanner = findStandardScanner(scannerClassOrKeyword);
		setValues(valuesAndDescriptions);
		am = getDatabase().getAccessMethods(this);
	}

	/**
	 * Refresh state.
	 * 
	 * @throws T2DBException
	 */
	protected void update() throws T2DBException {
		this.name = getName();
		ValueType<T> vt = getDatabase().getValueType(getSurrogate());
		// load all values again, it's expected to be a rare operation 
		values = vt.getValueDescriptions();
	}
	
	@SuppressWarnings("unchecked")
	private void setValues(Map<String, String> valuesAndDescriptions) throws T2DBException {
		if (valuesAndDescriptions == null || valuesAndDescriptions.size() == 0)
			this.values = new HashMap<T, String>();
		else {
			if (!isRestricted())
				throw T2DBMsg.exception(D.D10108, getName());
			if (getType().equals(String.class)) 
				this.values = new LinkedHashMap<T, String>((Map<T, String>)valuesAndDescriptions);
			else {
				this.values = new LinkedHashMap<T, String>();
				for (Map.Entry<String, String> e : valuesAndDescriptions.entrySet()) {
					/*
					 * Don't use this.scan at this point, check will fail
					 * because the value is not yet in the list of valid values.
					 * On the other hand, the value must be valid, so catch scan
					 * failures. Such failures can occur with bad bootstrap data
					 * for example.
					 */
					T value = this.scanner.scan(e.getKey());
					this.values.put(value, e.getValue());
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private ValueScanner<T> findStandardScanner(StandardValueType type) throws T2DBException {
		ValueScanner<T> sc = null;
		switch (type) {
		case BOOLEAN:
			sc = scanner = (ValueScanner<T>) new BooleanScanner();
			break;
		case DATE:
			sc = scanner = (ValueScanner<T>) new DateScanner();
			break;
		case DATETIME:
			sc = scanner = (ValueScanner<T>) new DateTimeScanner();
			break;
		case NAME:
			sc = scanner = (ValueScanner<T>) new NameScanner();
			break;
		case NUMBER:
			sc = scanner = (ValueScanner<T>) new NumberScanner();
			break;
		case TEXT:
			sc = scanner = (ValueScanner<T>) new TextScanner();
			break;
		case TIMEDOMAIN:
			sc = scanner = (ValueScanner<T>) new TimeDomainScanner();
			break;
		case TYPE:
			sc = scanner = (ValueScanner<T>) new TypeScanner(getSurrogate().getDatabase());
			break;
		default:
			throw new RuntimeException(type.name());
		}
		keyword = type;
		return sc;
	}
	
	private ValueScanner<T> findStandardScanner(String scannerClassOrKeyword) throws T2DBException {
		StandardValueType type = null;
		ValueScanner<T> scanner= null;
		try {
			type = StandardValueType.valueOf(scannerClassOrKeyword);
		} catch (IllegalArgumentException e) {}
		
		if (type == null) {
			keyword = null;
			try {
				@SuppressWarnings("unchecked")
				Class<ValueScanner<T>> scannerClass = (Class<ValueScanner<T>>) Class.forName(scannerClassOrKeyword);
				try {
					Constructor<ValueScanner<T>> constructor = scannerClass.getConstructor(ValueType.class);
					scanner = constructor.newInstance(this);
				} catch (NoSuchMethodException e) {
					scanner = scannerClass.newInstance();
				}
			} catch (Exception e) {
				throw T2DBMsg.exception(e, D.D10110, name, scannerClassOrKeyword);
			}
		} else
			scanner = findStandardScanner(type);
		return scanner;
	}
	
	@Override
	public UpdatableValueType<T> edit() {
		try {
			return new UpdatableValueTypeImpl<T>(this);
		} catch (T2DBException e) {
			throw new RuntimeException("bug", e);
		}
	}
	
	/**
	 * Return the value access methods object for this type.
	 * The value type object plays the role of a cache for
	 * the value access method object, which is expensive to 
	 * construct.
	 * 
	 * @return a value access methods object
	 */
	public <S>ValueAccessMethods<T> getAccessMethods() throws T2DBException {
		if (am == null)
			throw T2DBMsg.exception(D.D10102, toString());
		return am;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public boolean isRestricted() {
		return restricted;
	}

	@Override
	@Deprecated
	public boolean isBuiltIn() {
		return false;
	}

	@Override
	public Class<T> getType() {
		return scanner.getType();
	}
	
	@Override
	public ValueScanner<T> getScanner() {
		return scanner;
	}

	@Override
	public Set<T> getValues() {
		return values.keySet();
	}
	
	@Override
	public Map<T, String> getValueDescriptions() {
		return values;
	}

	@Override
	public boolean isCompatible(Object obj) {
		return obj == null || getType().isInstance(obj);
	}

	@Override
	public T scan(String value) throws T2DBException {
		T result = null; 
		if (value != null && value.length() == 0 && !isRestricted())
			value = null;
		if (value != null) {
			result = scanner.scan(value);
			if (result == null) {
				// interpret value "" as null, except for String
				if (value.length() != 0)
					throw T2DBMsg.exception(D.D10114, value, getName());
			} else
				check(result);
		}
		return result;
	}

	@Override
	public void check(T value) throws T2DBException {
		if (isRestricted() && !values.containsKey(value))
			throw T2DBMsg.exception(D.D10115, value, getName());
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toString(Object value) throws T2DBException {
		String result = null;
		try {
			if (!isRestricted() || values.containsKey(value))
				result = scanner.toString((T)value);
		} catch (Exception e) {
			throw T2DBMsg.exception(e, D.D10114, value, getName());
		}
		return result;
	}
	
	@Override
	public Collection<String> getValues(String format) {
		NamingPolicy namingPolicy = getDatabase().getNamingPolicy();
		try {
			Map<T, String> map = getValueDescriptions();
			List<String> list = new ArrayList<String>(map.size());
			for (Map.Entry<T, String> e : map.entrySet()) {
				String value = toString(e.getKey());
				String descr = e.getValue();
				if (descr != null && descr.length() > 0) {
					if (format == null)
						value = namingPolicy.joinValueAndDescription(value, descr);
					else
						value = String.format(format, value, descr);
				}
				list.add(value);
			}
			Collections.sort(list);
			return list;
		} catch (T2DBException e) {
			throw new RuntimeException("bug", e);
		}
	}

	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public String getExternalRepresentation() {
		if (keyword == null)
			return scanner.getClass().getName();
		else
			return keyword.name();
	}
	
	@Override
	public StandardValueType getStandardValueType() {
		return keyword;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <S> ValueType<S> typeCheck(Class<S> type) throws T2DBException {
		try {
			if (type.isAssignableFrom(getScanner().getType()))
				return (ValueType<S>) this;
		} catch (Exception e) {
		}
		throw T2DBMsg.exception(D.D10101, getName(), type.getName(), getScanner().getType().getName());
	}

}
