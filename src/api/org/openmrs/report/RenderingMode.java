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

/**
 * Represents a mode in which a @see org.openmrs.report.ReportRenderer can run. A simple render like
 * a CSV renderer can probably only render in one mode. A more sophisticated renderer might be able
 * to render multiple modes, which would show up with different labels, and at different weights in
 * a dropdown list. In this case a renderer would use the String argument of this class to determine
 * which mode was selected by the user. A higher sortWeight (i.e. closer to Integer.MAX_VALUE) will
 * typically appear at the top of a select list.
 */
public class RenderingMode implements Comparable<RenderingMode> {
	
	private ReportRenderer renderer;
	
	private String label;
	
	private String argument;
	
	private Integer sortWeight;
	
	public RenderingMode() {
		argument = "";
		sortWeight = 0;
	}
	
	/**
	 * @param renderer;
	 * @param label
	 * @param argument
	 * @param sortWeight
	 */
	public RenderingMode(ReportRenderer renderer, String label, String argument, Integer sortWeight) {
		this.renderer = renderer;
		this.label = label;
		this.argument = argument;
		this.sortWeight = sortWeight;
	}
	
	public ReportRenderer getRenderer() {
		return renderer;
	}
	
	public void setRenderer(ReportRenderer renderer) {
		this.renderer = renderer;
	}
	
	public String getArgument() {
		return argument;
	}
	
	public void setArgument(String argument) {
		this.argument = argument;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public Integer getSortWeight() {
		return sortWeight;
	}
	
	public void setSortWeight(Integer sortWeight) {
		this.sortWeight = sortWeight;
	}
	
	/**
	 * Higher sortWeight comes first
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(RenderingMode other) {
		return other.sortWeight.compareTo(sortWeight);
	}
	
}
