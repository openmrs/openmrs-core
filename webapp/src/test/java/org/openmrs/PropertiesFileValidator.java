/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;

/**
 * Validate Java properties files.
 * 
 */
public class PropertiesFileValidator {

	private static final String KEY_VALUE_LINE_REGEX = "^[^#]+=.+";

	/**
	 * List duplicated key in file.
	 * @param inputStream file input stream
	 * @return list of duplicated keys
	 * @throws IOException in case of reading content from InputStream
	 */
	public List<String> getDuplicatedKeys(InputStream inputStream) throws IOException {

		List<String> fileLines = getLines(inputStream);
		MultiMap keyValuesMap = getAsPropertiesKeyValueMultiMap(fileLines);

		return filterKeysWithMultipleValues(keyValuesMap);
	}

	private List<String> filterKeysWithMultipleValues(MultiMap keyValuesMap) {

		List<String> result = new ArrayList<String>();

		for (Object entryObject : keyValuesMap.entrySet()) {
			// apache commons multimap in version 3.* do not use generics (
			// version 4.0 has)
			@SuppressWarnings("unchecked")
			Map.Entry<String, Collection<?>> mapEntry = (Entry<String, Collection<?>>) entryObject;
			if (mapEntry.getValue().size() > 1) {
				result.add(mapEntry.getKey());
			}
		}
		return result;
	}

	private MultiMap getAsPropertiesKeyValueMultiMap(List<String> fileLines) {

		MultiMap multiMap = new MultiValueMap();

		for (String line : fileLines) {

			if (isCorectKeyValueLine(line)) {
				Map.Entry<String, String> tuple = extractKeyValue(line);
				multiMap.put(tuple.getKey(), tuple.getValue());
			}
		}

		return multiMap;
	}

	private boolean isCorectKeyValueLine(String line) {
		return line.matches(KEY_VALUE_LINE_REGEX);
	}

	private Entry<String, String> extractKeyValue(String line) {

		String[] keyValueTab = line.split("=");

		return new AbstractMap.SimpleEntry<String, String>(keyValueTab[0], keyValueTab[1]);
	}

	private List<String> getLines(InputStream inputStream) throws IOException {

		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream));

		String line = null;

		List<String> result = new ArrayList<String>();

		while ((line = bufferedReader.readLine()) != null) {
			result.add(line);
		}
		return result;
	}
}
