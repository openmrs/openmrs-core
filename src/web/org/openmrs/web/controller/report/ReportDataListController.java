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
package org.openmrs.web.controller.report;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.Cohort;
import org.openmrs.Person;
import org.openmrs.report.DataSet;
import org.openmrs.report.ReportData;
import org.openmrs.web.WebConstants;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class ReportDataListController extends SimpleFormController {
	
	/**
	 * 
	 * The onSubmit function receives the form/command object that was modified
	 *   by the input form and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		
		String view = getFormView();

		return new ModelAndView(new RedirectView(view));
	}

	/**
	 * 
	 * This is called prior to displaying a form for the first time.  It tells Spring
	 *   the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {

    	String key = ServletRequestUtils.getStringParameter(request, "indicator", "empty");
    	
    	// If there is no parameter named 'indicator' then return an empty Person stub.
    	if (null == key || "empty".equals(key)) {
    		Person p = new Person();
    		p.setPersonId(-1);
    		return p;
    	}
    	
    	// Get the ReportData that is in the current Session.
    	ReportData report = (ReportData)request.getSession().getAttribute(WebConstants.OPENMRS_REPORT_DATA);
 
    	// Extract the Collection<DataSet> from the ReportData and put it into an ArrayList so it is usable.
    	ArrayList<DataSet> cohortDataSets = new ArrayList<DataSet>();
    	Map<String,DataSet> dataSetMap = report.getDataSets();
    	cohortDataSets.addAll(dataSetMap.values());
    	
    	// For each CohortDataSet.cohortData in the DataSet row...
    	Iterator<Map<String,Cohort>> iterator = cohortDataSets.get(0).iterator();
    	Cohort cohort = null;
    	while (iterator.hasNext()) {
    		Map<String, Cohort> cohortData = iterator.next();
    		// ... if the cohortData contains the indicator key...
    		if (cohortData.containsKey(key)) {
    			// ... then use that Cohort.
    			cohort = cohortData.get(key);
    		}
    	}
    	
    	// Set the 'patientIds' attribute of the request to the Cohort.personIds
    	Set<Integer> ids = cohort.getMemberIds();
    	String personIds = ids.toString();
    	personIds = personIds.replaceAll("\\[", "");
    	personIds = personIds.replaceAll("\\]", "");
    	personIds = personIds.replaceAll(", ", ",");
    	personIds = personIds.trim();
    	request.setAttribute("patientIds", personIds);
    	
    	// return the ReportData 
    	return report;
    }
    
	
}