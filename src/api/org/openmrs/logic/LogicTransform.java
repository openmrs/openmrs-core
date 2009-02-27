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

import org.openmrs.logic.op.Operator;

/**
 *
 */
public class LogicTransform {
	
	private Operator transformOperator = null;
	
	private Integer numResults = null;
	
	private String sortColumn = null;
	
	public LogicTransform(Operator transformOperator, Integer numResults) {
		this.transformOperator = transformOperator;
		this.numResults = numResults;
	}
	
	public LogicTransform(Operator transformOperator) {
		this.transformOperator = transformOperator;
	}
	
	public Operator getTransformOperator() {
		return transformOperator;
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		if (transformOperator != null) {
			result.append(transformOperator);
		}
		
		if (numResults != null) {
			result.append(" " + numResults);
		}
		
		if (sortColumn != null) {
			result.append(" ordered by " + sortColumn);
		}
		return result.toString();
	}
	
	public Integer getNumResults() {
		return numResults;
	}
	
	public String getSortColumn() {
		return sortColumn;
	}
	
	public void setNumResults(Integer numResults) {
		this.numResults = numResults;
	}
	
	public void setSortColumn(String sortColumn) {
		this.sortColumn = sortColumn;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LogicTransform)) {
			return false;
		}
		
		LogicTransform compTransform = (LogicTransform) obj;
		
		if (!safeEquals(this.transformOperator, compTransform.getTransformOperator())) {
			return false;
		}
		
		if (!safeEquals(numResults, compTransform.getNumResults())) {
			return false;
		}
		
		if (!safeEquals(sortColumn, compTransform.getSortColumn())) {
			return false;
		}
		
		return true;
	}
	
	private boolean safeEquals(Object a, Object b) {
		if (a == null && b == null)
			return true;
		if (a == null || b == null)
			return false;
		return a.equals(b);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((transformOperator == null) ? 0 : transformOperator.hashCode());
		result = prime * result + ((numResults == null) ? 0 : numResults.hashCode());
		result = prime * result + ((sortColumn == null) ? 0 : sortColumn.hashCode());
		return result;
	}
}
