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

import java.util.Map;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Root;

/**
 * Report Data obtained from evaluating a ReportSchema with a given EvaluationContext.
 * 
 * @deprecated see reportingcompatibility module
 */
@Root
@Deprecated
public class ReportData {
	
	private ReportSchema reportSchema;
	
	private EvaluationContext evaluationContext;
	
	@SuppressWarnings("unchecked")
	private Map<String, DataSet> dataSets;
	
	public ReportData() {
	}
	
	@SuppressWarnings("unchecked")
	@ElementMap(required = false)
	public Map<String, DataSet> getDataSets() {
		return dataSets;
	}
	
	@SuppressWarnings("unchecked")
	@ElementMap(required = false)
	public void setDataSets(Map<String, DataSet> dataSets) {
		this.dataSets = dataSets;
	}
	
	/**
	 * @return Returns the EvaluationContext that was used to obtain this ReportData.
	 */
	//@Element(required=false)
	public EvaluationContext getEvaluationContext() {
		return evaluationContext;
	}
	
	/**
	 * Saves the EvaluationContext that was used to obtain this ReportData.
	 * 
	 * @param evaluationContext
	 */
	//@Element(required=false)
	public void setEvaluationContext(EvaluationContext evaluationContext) {
		this.evaluationContext = evaluationContext;
	}
	
	@Element
	public ReportSchema getReportSchema() {
		return reportSchema;
	}
	
	@Element
	public void setReportSchema(ReportSchema reportSchema) {
		this.reportSchema = reportSchema;
	}
	
}
