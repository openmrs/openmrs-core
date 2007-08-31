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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 *
 */
public class CohortListController extends SimpleFormController {

	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
	}

    protected Object formBackingObject(HttpServletRequest request) throws ServletException {
    	List<Cohort> cohorts = new ArrayList<Cohort>();
		if (Context.isAuthenticated()) {
			cohorts = Context.getCohortService().getCohorts();
			Collections.sort(cohorts, new Comparator<Cohort>() {
					public int compare(Cohort a, Cohort b) {
						int temp = a.getVoided().compareTo(b.getVoided());
						if (temp == 0)
							temp = a.getCohortId().compareTo(b.getCohortId());
						return temp;
					}
				});
		}
    	return cohorts;
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
    	
    	String action = request.getParameter("method");
    	
    	if ("delete".equals(action)) {
    		String[] toDelete = request.getParameterValues("cohortId");
    		if (toDelete != null) {
	    		String reason = request.getParameter("voidReason");
	    		for (String s : toDelete) {
	    			Integer id = Integer.valueOf(s);
	    			Cohort cohort = Context.getCohortService().getCohort(id);
	    			Context.getCohortService().voidCohort(cohort, reason);
	    		}
	    		return new ModelAndView(new RedirectView(getSuccessView()));
    		}
    	}
    	
    	return showForm(request, response, errors);
    }
    
	protected Map referenceData(HttpServletRequest request, Object obj, Errors errs) throws Exception {
		return new HashMap<String, Object>();
	}
}
