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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.cohort.CohortDefinition;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * Definition of a dataset that produces one-row-per-obs. Output might look like: patientId,
 * question, questionConceptId, answer, answerConceptId, obsDatetime, encounterId 123,
 * "WEIGHT (KG)", 5089, 70, null, "2007-05-23", 2345 123, "OCCUPATION", 987, "STUDENT", 988,
 * "2008-01-30", 2658
 * 
 * @see RowPerObsDataSet
 */
@Root
public class RowPerObsDataSetDefinition implements DataSetDefinition {
	
	private static final long serialVersionUID = 1L;
	
	@Attribute(required = true)
	private String name;
	
	private Collection<Concept> questions;
	
	private CohortDefinition filter;
	
	private Date fromDate;
	
	private Date toDate;
	
	public RowPerObsDataSetDefinition() {
		questions = new HashSet<Concept>();
	}
	
	/**
	 * @see org.openmrs.report.DataSetDefinition#getColumnDatatypes()
	 */
	@SuppressWarnings("unchecked")
	public List<Class> getColumnDatatypes() {
		Class[] ret = new Class[8];
		ret[0] = Integer.class; // patientId
		ret[1] = String.class; // question concept name
		ret[2] = Integer.class; // question concept id
		ret[3] = Object.class; // answer
		ret[4] = Integer.class; // answer concept id
		ret[5] = Date.class; // obsDatetime
		ret[6] = Integer.class; // encounterId
		ret[7] = Integer.class; // obsGroupId
		return Arrays.asList(ret);
	}
	
	/**
	 * @see org.openmrs.report.DataSetDefinition#getColumnKeys()
	 */
	public List<String> getColumnKeys() {
		String[] ret = new String[8];
		ret[0] = "patientId";
		ret[1] = "question";
		ret[2] = "questionConceptId";
		ret[3] = "answer";
		ret[4] = "answerConceptId";
		ret[5] = "obsDatetime";
		ret[6] = "encounterId";
		ret[7] = "obsGroupId";
		return Arrays.asList(ret);
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
		return new ArrayList<Parameter>();
	}
	
	/**
	 * @return the filter
	 */
	@Element(data = true, required = false)
	public CohortDefinition getFilter() {
		return filter;
	}
	
	/**
	 * @param filter the filter to set
	 */
	@Element(data = true, required = false)
	public void setFilter(CohortDefinition filter) {
		this.filter = filter;
	}
	
	/**
	 * @return the fromDate
	 */
	@Element(data = true, required = false)
	public Date getFromDate() {
		return fromDate;
	}
	
	/**
	 * @param fromDate the fromDate to set
	 */
	@Element(data = true, required = false)
	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}
	
	/**
	 * @return the questions
	 */
	@ElementList(required = true)
	public Collection<Concept> getQuestions() {
		return questions;
	}
	
	/**
	 * @param questions the questions to set
	 */
	@ElementList(required = true)
	public void setQuestions(Collection<Concept> questions) {
		this.questions = questions;
	}
	
	/**
	 * @return the toDate
	 */
	@Element(data = true, required = false)
	public Date getToDate() {
		return toDate;
	}
	
	/**
	 * @param toDate the toDate to set
	 */
	@Element(data = true, required = false)
	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}
	
}
