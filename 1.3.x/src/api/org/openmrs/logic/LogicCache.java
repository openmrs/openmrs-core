/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.logic;

import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.logic.datasource.LogicDataSource;
import org.openmrs.logic.result.Result;

/**
 * 
 * A caching mechanism used by LogicContext to avoid recalculating a re-fetching
 * the same results more than once during a single request of the logic service.
 * 
 */
public class LogicCache {

	private static Log log = LogFactory.getLog(LogicCache.class);

	// TODO: implement the cache -- currently no caching is performed

	private Map<LogicCacheEntryKey, Map<Integer, Result>> cache;

	public Result get(Patient patient,
	        LogicCriteria criteria, Map<String, Object> parameters) {
		LogicCacheEntryKey key = new LogicCacheEntryKey(criteria, parameters, 0);
		return get(key, patient.getPatientId());
	}

	public Result get(Patient patient,
	        LogicDataSource dataSource, LogicCriteria criteria) {
		LogicCacheEntryKey key = new LogicCacheEntryKey(dataSource,
		                                                criteria);
		Result r = get(key, patient.getPatientId());
		log.debug("Searching cache for " + key.toString() + " - "
		        + (r == null ? "NOT" : "") + " found");
		logCacheContents();
		return r;
	}

	public void put(LogicDataSource dataSource, LogicCriteria criteria,
	        Map<Integer, Result> resultMap) {
		LogicCacheEntryKey key = new LogicCacheEntryKey(dataSource,
		                                                criteria);
		put(key, resultMap);
	}

	public void put(LogicCriteria criteria, Map<String, Object> parameters, int ttl,
	        Map<Integer, Result> resultMap) {
		LogicCacheEntryKey key = new LogicCacheEntryKey(criteria,
		                                                parameters,
		                                                ttl);
		put(key, resultMap);
	}

	private Result get(LogicCacheEntryKey key, Integer patientId) {
		Map<Integer, Result> entry = getCache().get(key);
		log.debug("Logic cache: " + (entry == null ? "NOT FOUND" : "FOUND"));
		if (entry == null)
			return null;
		Result r = entry.get(patientId);
		if (r == null)
			r = Result.emptyResult();
		return r;
	}

	private void put(LogicCacheEntryKey key, Map<Integer, Result> value) {
		log.debug("Adding to logic cache: " + key.toString());
		getCache().put(key, value);
	}

	private Map<LogicCacheEntryKey, Map<Integer, Result>> getCache() {
		if (cache == null)
			cache = new Hashtable<LogicCacheEntryKey, Map<Integer, Result>>();
		return cache;
	}

	/**
	 * 
	 * Clean out expired values from the cache
	 * 
	 */
	public void clean() {
		long now = new Date().getTime();
		for (LogicCacheEntryKey key : getCache().keySet())
			if (key.getExpires() < now)
				getCache().remove(key);
	}
	
	private void logCacheContents() {
		log.debug("Logic Cache - " + getCache().size() + " entries");
		for (LogicCacheEntryKey key : getCache().keySet())
			log.debug("  " + key.toString() + " - " + getCache().get(key));
	}
}
