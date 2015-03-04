/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.report;

/**
 * Represents a mode in which a @see org.openmrs.report.ReportRenderer can run. A simple render like
 * a CSV renderer can probably only render in one mode. A more sophisticated renderer might be able
 * to render multiple modes, which would show up with different labels, and at different weights in
 * a dropdown list. In this case a renderer would use the String argument of this class to determine
 * which mode was selected by the user. A higher sortWeight (i.e. closer to Integer.MAX_VALUE) will
 * typically appear at the top of a select list.
 * 
 * @deprecated see reportingcompatibility module
 */
@Deprecated
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
	 * @param renderer
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
