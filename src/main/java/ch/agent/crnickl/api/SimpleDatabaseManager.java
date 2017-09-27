/*
 *   Copyright 2012-2017 Hauser Olsson GmbH
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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import ch.agent.core.KeyedException;
import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.T2DBMsg;
import ch.agent.crnickl.T2DBMsg.D;
import ch.agent.t2.time.DefaultTimeDomainCatalog;
import ch.agent.t2.time.TimeDomainCatalog;

/**
 * A DatabaseManager sets up a {@link Database} using parameters specified in
 * various ways. 
 * 
 * @author Jean-Paul Vetterli
 */
public class SimpleDatabaseManager {

	private Database database;
	private String dbName;
	private String dbClass;
	private String timeDomainCatalogClass;
	private Map<String, String> parameters;
	
	private Set<String> duplicateFileDetector;
	private Pattern listSep;
	private Pattern kvSep;
	private String fileKey;
	private String dbNameKey;
	private String dbClassKey;
	private String timeDomainCatalogClassKey;

	/**
	 * Construct a database manager with a name, a class, and a map of
	 * configuration parameters. If the name of the time domain catalog class
	 * is null or empty, a default will be used.
	 * 
	 * @param dbName
	 *            the name of the database
	 * @param dbClass
	 *            the name of a {@link Database} class
	 * @param tdcClass
	 *            the name of a {@link TimeDomainCatalog} class or null or empty
	 * @param parameters
	 *            a map of key-value pairs
	 */
	public SimpleDatabaseManager(String dbName, String dbClass, String tdcClass, Map<String, String> parameters) {
		this.dbName = dbName;
		this.dbClass = dbClass;
		this.timeDomainCatalogClass = tdcClass;
		this.parameters = parameters;
	}

	/**
	 * Construct a simple database manager from a parameter string. The string
	 * is interpreted as a list of key-value pairs. In a key-value pair, 
	 * everything after the first key-value separator is part of the value and
	 * can contain more separators.  
	 * <p>
	 * Key-value pairs are interpreted as parameters except if the key is a
	 * <em>file key</em>. In this case the value is interpreted as an absolute
	 * file name. The file contains one key-value pair on each line (with the
	 * same key-value separator). Lines can be continued by escaping the end of
	 * the line with a backslash. Lines starting with a hash sign are ignored.
	 * Files can reference files recursively using the file key mechanism.
	 * <p>
	 * The file named by a file key can be followed by key mappings, like this:
	 * <blockquote> <code>file=/foo/bar.txt, x=a, y=b
	 * </blockquote>
	 * When key mappings are present, only the mapped keys are extracted from the file (x and y) and
	 * they are renamed as specified (a and b). Key remapping is only available inside
	 * files, because the list separators are the same at all levels. If the example
	 * above were included in a parameter string and not a file, x=a would be 
	 * interpreted as an independent parameter and not as a key mapping. 
	 * <p>
	 * The database name and the name of the database class are mandatory.
	 * An exception is thrown if the parameter string cannot be parsed successfully.
	 * 
	 * @param listSeparator
	 *            a regular expression pattern separating list elements
	 * @param kvSeparator
	 *            a regular expression pattern separating a key and a value
	 * @param dbNameKey
	 *            the key for the database name
	 * @param dbClassKey
	 *            the key for the name of the {@link Database} class
	 * @param tdcClassKey
	 *            the key for the name of the {@link TimeDomainCatalog} class
	 * @param fileKey
	 *            a key naming a file
	 * @param parameterString
	 *            a parameter string
	 * @throws T2DBException
	 */
	public SimpleDatabaseManager(String listSeparator, String kvSeparator, String dbNameKey, String dbClassKey, String tdcClassKey, String fileKey, String parameterString) throws T2DBException {
		try {
			if (listSeparator == null || kvSeparator == null || fileKey == null || dbNameKey == null || dbClassKey == null || tdcClassKey == null || parameterString == null)
				throw new IllegalArgumentException("no null argument allowed");
			
			this.listSep = Pattern.compile(listSeparator);
			this.kvSep = Pattern.compile(kvSeparator);
			this.fileKey = fileKey;
			this.dbNameKey = dbNameKey;
			this.dbClassKey = dbClassKey;
			this.timeDomainCatalogClassKey = tdcClassKey;
			
			parameters = new LinkedHashMap<String, String>();
			initialize(parameterString);
			
			if (dbName == null)
				throw T2DBMsg.exception(D.D00113);
			if (dbClass == null)
				throw T2DBMsg.exception(D.D00114);
			if (timeDomainCatalogClass == null || timeDomainCatalogClass.length() == 0)
				timeDomainCatalogClass = DefaultTimeDomainCatalog.class.getName();
		} catch (Exception e) {
			throw T2DBMsg.exception(e, D.D00112, parameterString, listSeparator, kvSeparator);
		}
	}
	
	/**
	 * Construct a simple database manager from a parameter string using default
	 * separators and keys. List elements are separated by a comma, key-value
	 * pairs by an equal sign. The database name key is <q>db.name</q>, the
	 * database class key is <q>db.class</q>, and the file tag is <q>file</q>.
	 * 
	 * @param parameterString
	 *            a parameter string
	 */
	public SimpleDatabaseManager(String parameterString) throws T2DBException {
		this("\\s*,\\s*", "\\s*=\\s*", "db.name", "db.class", "timedomaincatalog.class", "file",
				parameterString);
	}
	
	/**
	 * Return the database corresponding to the configuration.
	 *  
	 * @return a database
	 * @throws KeyedException
	 */
	public Database getDatabase() throws KeyedException {
		if (database == null)
			setUp();
		return database;
	}
	
	/**
	 * Return the map containing configuration parameters. Configuration
	 * parameters consist of all parameters except the name and the (name of
	 * the) class of the database. In principle these parameters are meant for
	 * the database but no law forbids to pass arbitrary application parameters
	 * through SimpleDatabaseManager.
	 * 
	 * @return a map with all parameters
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}
	
	private void initialize(String parameterString) throws Exception {
		duplicateFileDetector = new HashSet<String>();
		List<String> pairs = Arrays.asList(listSep.split(parameterString, -1));
		parseKeyValuePairs(false, pairs, null);
	}
	
	/**
	 * @param loose if true tolerate abscence of key-value separator
	 * @param pairs a list of pairs
	 * @param keyMap map for selecting and renaming keys or null
	 * @throws Exception
	 */
	private void parseKeyValuePairs(boolean loose, List<String> pairs, Map<String, String> keyMap) throws Exception {
		for (String pair : pairs) {
			try {
				parseKeyValuePair(loose, pair, keyMap);
			} catch (Exception e) {
				if (e instanceof T2DBException && ((T2DBException) e).getMsg().getKey().equals(D.D00111))
					throw e;
				throw T2DBMsg.exception(e, D.D00111, pair);
			}
		}
	}
	
	private void parseKeyValuePair(boolean loose, String pair, Map<String, String> keyMap) throws Exception {
		String[] kv = kvSep.split(pair.trim(), 2); // trim before splitting
		if (kv.length != 2 || kv[0].length() == 0) {
			if (loose)
				kv[0] = null;
			else
				throw T2DBMsg.exception(D.D00111, pair);
		} else {
			if (keyMap != null) {
				String mapped = keyMap.get(kv[0]);
				if (mapped != null)
					kv[0] = mapped;
				else
					kv[0] = null;
			}
		}
		if (kv[0] != null) {
			if (kv[0].equals(fileKey)) {
				String[] fileNameAndKeyMappings = listSep.split(kv[1], -1);
				if (fileNameAndKeyMappings.length > 0)
					parseKeyValuePairs(true, extract(fileNameAndKeyMappings[0]), makeMap(fileNameAndKeyMappings));
			} else
				setParameter(kv[0], kv[1]);
		}
	}
	
	/**
	 * Return null if empty.
	 * 
	 * @return a map or null
	 */
	private Map<String, String> makeMap(String[] fileNameAndKeyMappings) throws Exception {
		Map<String, String> map = null;
		if (fileNameAndKeyMappings.length > 1) {
			map = new HashMap<String, String>(fileNameAndKeyMappings.length - 1);
			for (int i = 1; i < fileNameAndKeyMappings.length; i++) {
				String[] kv = kvSep.split(fileNameAndKeyMappings[i].trim(), 2); // trim before splitting
				if (kv.length != 2 || kv[0].length() == 0 || kv[1].length() == 0)
					throw T2DBMsg.exception(D.D00111, fileNameAndKeyMappings[i]);
				map.put(kv[0], kv[1]);
			}
		}
		return map;
	}
	
	/**
	 * Return lines from the named file.
	 * Comments are skipped and continuation lines applied.
	 * 
	 * @param fileName
	 * @return a list of strings
	 * @throws Exception
	 */
	private List<String> extract(String fileName) throws Exception {
		List<String> result = new ArrayList<String>();
		List<String> lines = readLines(fileName);
		StringBuffer accumulator = new StringBuffer();
		for (String line : lines) {
			if (line.startsWith("#"))
				continue;
			if (line.endsWith(" \\")) {
				accumulator.append(line);
				int l = accumulator.length();
				accumulator.delete(l - 1, l);
			} else { 
				accumulator.append(line);
				line = accumulator.toString();
				accumulator.delete(0, accumulator.length());
				if (line.length() == 0)
					continue;
				result.add(line);
			}
		}
		if (accumulator.length() > 0)
			throw T2DBMsg.exception(D.D00115, fileName);
		return result;
	}

	/**
	 * Return all lines from the file as list. The file can be either on the class path 
	 * or in the file system.
	 * 
	 * @param fileName a string
	 * @return a list of lines
	 * @throws Exception
	 */
	private List<String> readLines(String fileName) throws Exception {
		if (!duplicateFileDetector.add(fileName))
			throw T2DBMsg.exception(D.D00116, fileName);
		InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(fileName);
		if (inputStream == null)
		 	inputStream = new FileInputStream(fileName);
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		List<String> lines = new ArrayList<String>();
		String line = null;
		while (true) {
			line = br.readLine();
			if (line == null)
				break;
			lines.add(line);
		}
		br.close();
		return lines;
	}
	
	private void setParameter(String key, String value) {
		if (key.equals(dbNameKey))
			dbName = value;
		else if (key.equals(dbClassKey))
			dbClass = value;
		else if (key.equals(timeDomainCatalogClassKey))
			timeDomainCatalogClass = value;
		else
			parameters.put(key, value);
	}

	private void setUp() throws KeyedException {
		DatabaseFactory dbf = DatabaseFactory.getInstance();
		DatabaseConfiguration config = new DatabaseConfiguration(dbName, dbClass, timeDomainCatalogClass);
		for (Map.Entry<String, String> e : parameters.entrySet()) {
			config.setParameter(e.getKey(), e.getValue());
		}
		dbf.addDatabase(config);
		database = dbf.getDatabase(dbName);
	}

}
