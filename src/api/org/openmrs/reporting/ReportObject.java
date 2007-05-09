package org.openmrs.reporting;

public interface ReportObject {
	public Integer getReportObjectId();
	public void setReportObjectId(Integer id);
	public String getName();
	public void setName(String name);
	public String getDescription();
	public void setDescription(String description);
}
