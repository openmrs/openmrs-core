/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.test;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.util.OpenmrsUtil;

/**
 * Utilities requires for unit tests
 */
public class Util {
	
	public static void log(String label, Object object) {
		String toPrint;
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.getSerializationConfig().set(SerializationConfig.Feature.INDENT_OUTPUT, true);
			toPrint = mapper.writeValueAsString(object);
		}
		catch (Exception ex) {
			toPrint = "" + object;
		}
		if (label != null)
			toPrint = label + ": " + toPrint;
		System.out.println(toPrint);
	}
	
	/**
	 * @param object
	 * @param path something like "obs[0]/concept/uuid"
	 * @return
	 */
	public static Object getByPath(Object object, String path) {
		return getByPath(object, path.split("/"));
	}
	
	/**
	 * @param object
	 * @param pathElements something like { "obs[0]", "concept", "uuid" }
	 * @return
	 */
	public static Object getByPath(Object object, String[] pathElements) {
		// I tried to use PropertyUtils.getIndexedProperty but I couldn't get it to work
		try {
			for (int i = 0; i < pathElements.length; ++i) {
				String property;
				Integer index;
				if (pathElements[i].indexOf("[") > 0) {
					property = pathElements[i].substring(0, pathElements[i].indexOf("["));
					index = Integer.valueOf(pathElements[i].substring(pathElements[i].indexOf("[") + 1,
					    pathElements[i].indexOf("]")));
				} else {
					property = pathElements[i];
					index = null;
				}
				if (property != null)
					object = PropertyUtils.getProperty(object, property);
				if (index != null) {
					if (object instanceof List) {
						object = ((List<?>) object).get(index);
					} else if (object instanceof Object[]) {
						object = ((Object[]) object)[index];
					}
				}
			}
			return object;
		}
		catch (Exception ex) {
			throw new RuntimeException("Failed to get path " + OpenmrsUtil.join(Arrays.asList(pathElements), " . "), ex);
		}
	}
	
	/**
	 * @param result the SimpleObject that contains results
	 * @return
	 * @throws Exception
	 */
	public static List<Object> getResultsList(SimpleObject result) throws Exception {
		return (List<Object>) PropertyUtils.getProperty(result, "results");
	}
	
	/**
	 * @param result
	 * @return
	 * @throws Exception
	 */
	public static int getResultsSize(SimpleObject result) throws Exception {
		return getResultsList(result).size();
	}
	
}
