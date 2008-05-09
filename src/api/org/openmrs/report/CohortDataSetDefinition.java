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
package org.openmrs.report;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.openmrs.Cohort;
import org.openmrs.cohort.CohortDefinition;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Root;

/**
 * Metadata that defines a CohortDataSet. (I.e. a list of cohorts, each of which has a name) 
 * 
 * For example a CohortDatasetDefinition might represent:
 *    "1. Total # of Patients" -> (CohortDefinition) everyone
 *    "1.a. Male Adults" -> (CohortDefinition) Male AND Adult
 *    "1.b. Female Adults" -> (CohortDefinition) Female AND Adult
 *    "1.c. Male Children" -> (CohortDefinition) Male AND NOT Adult
 *    "1.d. Female Children" -> (CohortDefinition) Female AND NOT Adult
 *    ...
 *    
 * @see CohortDataSet
 * @see CohortDataSetProvider
 */
@Root
public class CohortDataSetDefinition implements DataSetDefinition {

    private static final long serialVersionUID = -658417752199413012L;
    
    @Attribute(required=true)
	private String name;
    private Map<String, String> descriptions;
    private Map<String, CohortDefinition> strategies;
	
	/**
	 * Default constructor
	 */
	public CohortDataSetDefinition() {
		strategies = new LinkedHashMap<String, CohortDefinition>();
		descriptions = new LinkedHashMap<String, String>();
	}
	
	/**
	 * Add the given cohort as a "column" to this definition with the given key.
	 * The name is also added as the description.
	 * 
	 * @param name key to refer by which to refer to this cohort
	 * @param cohortDefinition The patients for this column
	 */
	public void addStrategy(String name, CohortDefinition cohortDefinition) {
		addStrategy(name, name, cohortDefinition);
	}
	 
	/**
	 * Add the given cohort as a "column" to this definition with the given key
	 * and the given description.
	 * 
	 * @param name
	 * @param description
	 * @param cohortDefinition
	 */
	public void addStrategy(String name, String description, CohortDefinition cohortDefinition) {
		strategies.put(name, cohortDefinition);
		descriptions.put(name, description);
	}
	
	/**
	 * @see org.openmrs.report.DataSetDefinition#getColumnKeys()
	 */
	public List<String> getColumnKeys() {
		return new Vector<String>(strategies.keySet());
	}
	
	/**
     * @see org.openmrs.report.DataSetDefinition#getColumnDatatypes()
     */
    public List<Class> getColumnDatatypes() {
    	//return (List<Class>) Collections.nCopies(strategies.size(), Cohort.class);
    	List<Class> ret = new ArrayList<Class>();
    	for (int i = strategies.size(); i > 0; --i)
    		ret.add(Cohort.class);
    	return ret;
    }

	/**
	 * @see org.openmrs.report.DataSetDefinition#getName()
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @see org.openmrs.report.DataSetDefinition#setName(java.lang.String)
	 */
	public void setName(String name) {
    	this.name = name;
    }
	
	/**
	 * @see org.openmrs.report.Parameterizable#getParameters()
	 */
	public List<Parameter> getParameters() {
		List<Parameter> ret = new Vector<Parameter>();
		if (strategies != null)
			for (CohortDefinition c : strategies.values())
				ret.addAll(c.getParameters());
		return ret;
	}

	/**
	 * Sets a description for the cohort name if it exists.
	 * Returns true if a cohort exists with the @param name 
	 * else returns false.
	 * 
	 * @param name
	 * @param description
	 * @return
	 */
	public boolean setDescription(String name, String description) {
		if (strategies.containsKey(name)) {
			descriptions.put(name, description);
			return true;
		}
		return false;
	}
	
	/**
	 * Returns a description for the @param cohort strategy name.
	 * 
	 * @param name
	 * @return
	 */
	public String getDescription(String name) {
		return descriptions.get(name);
	}
	
	/**
	 * Returns the map of cohort strategy names, descriptions.
	 * 
	 * @return
	 */
	@ElementMap(required=false, keyType=String.class, valueType=String.class)
	public Map<String, String> getDescriptions( ) {
		return this.descriptions;
	}

	/**
	 * Get the key-value pairs of names to defined cohorts
	 * 
	 * @return
	 */
	@ElementMap(required=true, keyType=String.class, valueType=CohortDefinition.class)
	public Map<String, CohortDefinition> getStrategies() {
    	return strategies;
    }

	/**
	 * Set the key-value pairs of names to cohorts
	 * 
	 * @param strategies
	 */
	@ElementMap(required=true, keyType=String.class, valueType=CohortDefinition.class)
	public void setStrategies(Map<String, CohortDefinition> strategies) {
    	this.strategies = strategies;
    }
     
	/**
	 * Set the key-value pairs of names to cohort descriptions
	 * 
	 * @param descriptions
	 */
	@ElementMap(required=false, keyType=String.class, valueType=String.class)
	public void setDescriptions(Map<String, String> descriptions) {
		this.descriptions = descriptions;
    }

	
}
