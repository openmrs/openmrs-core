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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.logic.datasource.LogicDataSource;

/**
 * Logic cache composite key. Keys can be created for rules or data elements.
 * Keys contain information about when the entry was last updated and when the
 * entry expires.
 */
class LogicCacheEntryKey {

	protected final Log log = LogFactory.getLog(getClass());

	public enum LogicCacheEntryType {
		RULE, DATA_ELEMENT
	}

	private LogicCacheEntryType type;//whether it comes from a rule.eval or a datasource.read
	
	private LogicCriteria criteria;
	private long expires;
	
	private LogicDataSource dataSource; //specific to DATA_ELEMENT type
	private Map<String, Object> parameters; //specific to RULE type
	
	/**
	 * Creates a key for a rule.eval type evaluation
	 * @param criteria
	 * @param parameters
	 * @param ttl
	 */
	LogicCacheEntryKey(LogicCriteria criteria, Map<String, Object> parameters,
	        int ttl) {
		this.type = LogicCacheEntryType.RULE;
		long modified = new Date().getTime();
		this.expires = modified + ttl;
		this.criteria = criteria;
		this.parameters = parameters;
	}

	/**
	 * Creates a key for a datasource.read type evaluation
	 * @param dataSource
	 * @param critera
	 */
	LogicCacheEntryKey(LogicDataSource dataSource,
	        LogicCriteria critera) {
		this.type = LogicCacheEntryType.DATA_ELEMENT;
		long modified = new Date().getTime();
		this.expires = modified + dataSource.getDefaultTTL();
		this.dataSource = dataSource;
		this.criteria = critera;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof LogicCacheEntryKey))
			return false;
		LogicCacheEntryKey cek = (LogicCacheEntryKey) obj;
		//make sure they are the same type of key
		if (cek.type != type)
			return false;
		//make sure the logic criteria is the same
		if (!safeEquals(cek.criteria, criteria))
			return false;
		if (type == LogicCacheEntryType.RULE) {
			if (!safeEquals(cek.parameters, parameters))
				return false;
			return true;
		} else if (type == LogicCacheEntryType.DATA_ELEMENT) {
			if (!safeEquals(cek.dataSource, dataSource))
				return false;
			return true;
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
		        + ((criteria == null) ? 0 : criteria.hashCode());
		result = prime * result
		        + ((dataSource == null) ? 0 : dataSource.hashCode());
		result = prime * result
		        + ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	private boolean safeEquals(Object a, Object b) {
		if (a == null && b == null)
			return true;
		if (a == null || b == null)
			return false;
		return a.equals(b);
	}

	public long getExpires() {
		return expires;
	}

	public String toString() {

		return String.valueOf("[" + type + ","  + parameters + ","
		        + criteria.getRootToken() + "," + dataSource + "]@" + hashCode());
	}
}
