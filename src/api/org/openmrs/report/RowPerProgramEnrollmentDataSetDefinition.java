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

import org.openmrs.Program;
import org.openmrs.cohort.CohortDefinition;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * Definition of a dataset that produces one-row-per-PatientProgram. Output might look like:
 *   patientId, programName, programId, enrollmentDate, completionDate, patientProgramId
 *   123, "HIV PROGRAM", 1, "2008-01-01", null, 5383
 *   123, "TB PROGRAM", 2, "2006-04-11", "2006-10-11", 4253
 * @see RowPerProgramEnrollmentDataSet
 */
@Root
public class RowPerProgramEnrollmentDataSetDefinition implements DataSetDefinition {

	@Attribute(required=true)
	private String name;
	private Collection<Program> programs;
	private CohortDefinition filter;

	public RowPerProgramEnrollmentDataSetDefinition() {
		programs = new HashSet<Program>();
	}
	
	/**
	 * @see org.openmrs.report.DataSetDefinition#getColumnDatatypes()
	 */
	public List<Class> getColumnDatatypes() {
		Class[] ret = new Class[6];
		ret[0] = Integer.class; // patientId
		ret[1] = String.class; // program name
		ret[2] = Integer.class; // program id
		ret[3] = Date.class; // enrollment date
		ret[4] = Date.class; // completion date
		ret[5] = Integer.class; // patientProgramId
		return Arrays.asList(ret);
	}

	/**
	 * @see org.openmrs.report.DataSetDefinition#getColumnKeys()
	 */
	public List<String> getColumnKeys() {
		String[] ret = new String[6];
		ret[0] = "patientId";
		ret[1] = "programName";
		ret[2] = "programId";
		ret[3] = "enrollmentDate";
		ret[4] = "completionDate";
		ret[5] = "patientProgramId";
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

	@ElementList(required=false)
	public Collection<Program> getPrograms() {
    	return programs;
    }

	@ElementList(required=false)
	public void setPrograms(Collection<Program> programs) {
    	this.programs = programs;
    }

	@Element(data=true, required=false)
	public CohortDefinition getFilter() {
    	return filter;
    }

	@Element(data=true, required=false)
	public void setFilter(CohortDefinition filter) {
    	this.filter = filter;
    }

}
