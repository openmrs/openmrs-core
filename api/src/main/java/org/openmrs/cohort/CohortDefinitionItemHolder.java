/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.cohort;

/**
 * This class maintains a key-name pair for a cohort definition, so that cohort definitions can be
 * "selected" within a client application. For example, the webapp needs to be able to display
 * cohorts as a drop down list.
 * 
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class CohortDefinitionItemHolder {
	
	private String key;
	
	private String name;
	
	private CohortDefinition cohortDefinition;
	
	/**
	 * Public constructor.
	 */
	public CohortDefinitionItemHolder() {
	}
	
	/**
	 * Public constructor.
	 * 
	 * @param key
	 * @param name
	 * @param cohortDefinition
	 */
	public CohortDefinitionItemHolder(String key, String name, CohortDefinition cohortDefinition) {
		this.key = key;
		this.name = name;
		this.cohortDefinition = cohortDefinition;
	}
	
	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return the cohortDefinition
	 */
	public CohortDefinition getCohortDefinition() {
		return cohortDefinition;
	}
	
	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @param cohortDefinition the cohortDefinition to set
	 */
	public void setCohortDefinition(CohortDefinition cohortDefinition) {
		this.cohortDefinition = cohortDefinition;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new StringBuffer().append("[").append("key=").append(key).append(", ").append("name=").append(name).append(
		    ", ").append("cohortDefinition=").append(
		    cohortDefinition != null ? cohortDefinition.getClass() : "No cohort definition assigned.").append("]")
		        .toString();
	}
	
}
