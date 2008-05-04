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
package org.openmrs.reporting;

import java.util.HashMap;
import java.util.Map;

public class PatientDataSet {

	private Map<Integer, PatientDataHolder> map;
	
	public PatientDataSet() {
		map = new HashMap<Integer, PatientDataHolder>();
	}
	
	public PatientSet getPatientSet() {
		PatientSet ps = new PatientSet();
		ps.copyPatientIds(map.keySet());
		return ps;
	}
	
	public void putDataItem(Integer patientId, String key, Object value) {
		map.get(patientId).putValue(key, value);
	}
	
	public Object getDataItem(Integer patientId, String key) {
		return map.get(patientId).getValue(key);
	}
	
	public void putDataSeries(String key, Map<Integer, Object> series) {
		for (Map.Entry<Integer, Object> e : series.entrySet()) {
			PatientDataHolder holder = map.get(e.getKey());
			if (holder == null) {
				holder = new PatientDataHolder(null);
				map.put(e.getKey(), holder);
			}
			holder.putValue(key, e.getValue());
		}
	}
	
	public Map<Integer, Object> getDataSeries(String key) {
		Map<Integer, Object> ret = new HashMap<Integer, Object>();
		for (Map.Entry<Integer, PatientDataHolder> e : map.entrySet()) {
			ret.put(e.getKey(), e.getValue().getValue(key));
		}
		return ret;
	}

	public String toString() {
		StringBuffer ret = new StringBuffer();
		int count = 0;
		for (Map.Entry<Integer, PatientDataHolder> e : map.entrySet()) {
			ret.append("(#" + (++count) + ") ");
			// ret.append(e.getKey());
			// ret.append(": ");
			ret.append(e.getValue());
			ret.append("\n");
		}
		return ret.toString();
	}
	
}
