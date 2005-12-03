package org.openmrs.reporting;

public class Report {

	private Integer reportId;
	private String name;
	private String description;

	public Report() { }
	
	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the reportId.
	 */
	public Integer getReportId() {
		return reportId;
	}

	/**
	 * @param reportId The reportId to set.
	 */
	public void setReportId(Integer reportId) {
		this.reportId = reportId;
	}	
	
}
