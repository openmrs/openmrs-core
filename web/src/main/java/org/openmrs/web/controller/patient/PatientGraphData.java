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
package org.openmrs.web.controller.patient;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * The class for creating the JSON data for the patient graph
 */
public class PatientGraphData {
	
	private Map<String, Object> graph;
	
	private List<ArrayList<Object>> data;
	
	private Map<String, Double> critical;
	
	private Map<String, Double> absolute;
	
	private Map<String, Double> normal;
	
	/**
	 * Constructor to initialise the JSON object which will represent the flot data
	 */
	public PatientGraphData() {
		graph = new HashMap<String, Object>();
		data = new ArrayList<ArrayList<Object>>();
		critical = new HashMap<String, Double>();
		absolute = new HashMap<String, Double>();
		normal = new HashMap<String, Double>();
		
		graph.put("data", data);
		graph.put("critical", critical);
		graph.put("absolute", absolute);
		graph.put("normal", normal);
	}
	
	/**
	 * Method to add the x and y co-ordinates of the patient graph
	 * 
	 * @param time date of the observation
	 * @param valueNumeric value of the recorded observation
	 */
	public void addValue(long time, Double valueNumeric) {
		ArrayList<Object> value = new ArrayList<Object>();
		value.add(time);
		value.add(valueNumeric);
		data.add(value);
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		
		StringWriter writer = new StringWriter();
		try {
			new ObjectMapper().writeValue(writer, graph);
		}
		catch (Exception e) {}
		return writer.toString();
	}
	
	/**
	 * Method to set the critical high value of the concept
	 * 
	 * @param hiCritical highest value allowed for the concept
	 */
	public void setCriticalHigh(Double hiCritical) {
		critical.put("high", hiCritical);
	}
	
	/**
	 * Method to set the critical low value
	 * 
	 * @param lowCritical lowest value allowed for the concept
	 */
	public void setCriticalLow(Double lowCritical) {
		critical.put("low", lowCritical);
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param hiAbsolute
	 */
	public void setAbsoluteHigh(Double hiAbsolute) {
		absolute.put("high", hiAbsolute);
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param lowAbsolute
	 */
	public void setAbsoluteLow(Double lowAbsolute) {
		absolute.put("low", lowAbsolute);
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param hiNormal
	 */
	public void setNormalHigh(Double hiNormal) {
		normal.put("high", hiNormal);
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param lowNormal
	 */
	public void setNormalLow(Double lowNormal) {
		normal.put("low", lowNormal);
	}
	
}
