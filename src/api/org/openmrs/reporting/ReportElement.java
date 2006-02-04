package org.openmrs.reporting;

import org.openmrs.oldreporting.Analysis;

public class ReportElement {

	private Analysis analysis;
	private String formatDescriptor;
	private String layoutDescriptor;

	public ReportElement() { }
	
	public ReportElement(Analysis analysis, String formatDescriptor, String layoutDescriptor) {
		this.analysis = analysis;
		this.formatDescriptor = formatDescriptor;
		this.layoutDescriptor = layoutDescriptor;
	}

	/**
	 * @return Returns the analysis.
	 */
	public Analysis getAnalysis() {
		return analysis;
	}
	/**
	 * @param analysis The analysis to set.
	 */
	public void setAnalysis(Analysis analysis) {
		this.analysis = analysis;
	}
	/**
	 * @return Returns the formatDescriptor.
	 */
	public String getFormatDescriptor() {
		return formatDescriptor;
	}
	/**
	 * @param formatDescriptor The formatDescriptor to set.
	 */
	public void setFormatDescriptor(String formatDescriptor) {
		this.formatDescriptor = formatDescriptor;
	}
	/**
	 * @return Returns the layoutDescriptor.
	 */
	public String getLayoutDescriptor() {
		return layoutDescriptor;
	}
	/**
	 * @param layoutDescriptor The layoutDescriptor to set.
	 */
	public void setLayoutDescriptor(String layoutDescriptor) {
		this.layoutDescriptor = layoutDescriptor;
	}
	
}
