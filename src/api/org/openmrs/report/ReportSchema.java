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

import java.util.List;
import java.util.Vector;

import org.openmrs.cohort.CohortDefinition;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * This class holds the different parts of a report before generation. A ReportSchema will typically
 * be evaluated upon a Cohort, in the context of an EvaluationContext. @see
 * {@link org.openmrs.api.ReportService#evaluate(ReportSchema, org.openmrs.Cohort, EvaluationContext)}
 * Evaluating a report really means evaluating all the DataSetDefinitions it contains, resulting in
 * a {@link org.openmrs.report.ReportData} The "filter" represents an (optional) extra filter that
 * is applied to the input cohort before the DataSetDefinitions ever see it.
 */
@Root(strict = false)
public class ReportSchema implements Parameterizable {
	
	private static final long serialVersionUID = 932347906334509564L;
	
	private Integer reportSchemaId;
	
	private String name;
	
	private String description;
	
	private CohortDefinition filter;
	
	private List<Parameter> reportParameters;
	
	private List<DataSetDefinition> dataSetDefinitions;
	
	public ReportSchema() {
	}
	
	public void addDataSetDefinition(DataSetDefinition definition) {
		if (getDataSetDefinitions() == null)
			setDataSetDefinitions(new Vector<DataSetDefinition>());
		getDataSetDefinitions().add(definition);
	}
	
	/**
	 * Set the Report Schema Id
	 * 
	 * @param reportSchemaId
	 */
	@Attribute(required = false)
	public void setReportSchemaId(Integer reportSchemaId) {
		this.reportSchemaId = reportSchemaId;
	}
	
	/**
	 * Returns the ReportSchema Id
	 * 
	 * @return
	 */
	@Attribute(required = false)
	public Integer getReportSchemaId() {
		return this.reportSchemaId;
	}
	
	/**
	 * Set a name for the ReportSchema
	 * 
	 * @param name
	 */
	@Element(data = true, required = true)
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns the name of the ReportSchema
	 * 
	 * @return
	 */
	@Element(data = true, required = true)
	public String getName() {
		return this.name;
	}
	
	/**
	 * Set a description for this ReportSchema
	 * 
	 * @param description
	 */
	@Element(data = true, required = true)
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Returns the description of this ReportSchema
	 * 
	 * @return
	 */
	@Element(data = true, required = true)
	public String getDescription() {
		return this.description;
	}
	
	/**
	 * Set the filter
	 * 
	 * @param cohort
	 */
	@Element(required = false)
	public void setFilter(CohortDefinition filter) {
		this.filter = filter;
	}
	
	/**
	 * Returns the filter
	 * 
	 * @return
	 */
	@Element(required = false)
	public CohortDefinition getFilter() {
		return filter;
	}
	
	/**
	 * Sets List<Parameter> reportParameters
	 * 
	 * @param reportParameters
	 */
	@ElementList(required = false, name = "parameters")
	public void setReportParameters(List<Parameter> reportParameters) {
		this.reportParameters = reportParameters;
	}
	
	/**
	 * Get all ReportParameters defined for this schema. This method does not recurse through the
	 * sub objects to find _all_ parameters. Use {@link #getParameters()} for that.
	 * 
	 * @param reportParameters this schema's defined parameters
	 */
	@ElementList(required = false, name = "parameters")
	public List<Parameter> getReportParameters() {
		return reportParameters;
	}
	
	/**
	 * Set List<DataSetDefinition> dataSetDefinitions
	 * 
	 * @param dataSetDefinitions
	 */
	@ElementList(required = true, name = "dataSets")
	public void setDataSetDefinitions(List<DataSetDefinition> definitions) {
		this.dataSetDefinitions = definitions;
	}
	
	/**
	 * Returns List<DataSetDefinition> dataSetDefinitions
	 * 
	 * @return
	 */
	@ElementList(required = true, name = "dataSets")
	public List<DataSetDefinition> getDataSetDefinitions() {
		return this.dataSetDefinitions;
	}
	
	/**
	 * Looks through the datasetdefinitions and cohorts to get the rquired parameters TODO
	 * 
	 * @see org.openmrs.report.Parameterizable#getParameters()
	 */
	public List<Parameter> getParameters() {
		
		List<Parameter> parameters = new Vector<Parameter>();
		
		// loop over cohorts and get parameters
		if (getFilter() != null)
			parameters.addAll(getFilter().getParameters());
		
		// loop over datasetdefinitions and get the parameters
		if (getDataSetDefinitions() != null) {
			for (DataSetDefinition dataSetDef : getDataSetDefinitions()) {
				parameters.addAll(dataSetDef.getParameters());
			}
		}
		
		return parameters;
	}
	
}
