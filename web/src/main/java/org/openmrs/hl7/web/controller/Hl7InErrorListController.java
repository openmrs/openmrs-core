/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.hl7.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.HL7InError;
import org.openmrs.hl7.HL7InQueue;
import org.openmrs.hl7.HL7Service;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class Hl7InErrorListController {
	
	/**
	 * Logger for this class and subclasses
	 */
	private static final Log log = LogFactory.getLog(Hl7InErrorListController.class);
	
	/**
	 * Render the HL7 error queue messages page
	 *
	 * @param modelMap
	 * @return
	 */
	@RequestMapping("/admin/hl7/hl7InError.htm")
	public String listErrorHL7s() {
		return "/admin/hl7/hl7InErrorList";
	}
	
	/**
	 * submits an HL7InError back to the HL7 queue
	 *
	 * @param id HL7InErrorId for identifying the HL7 message
	 * @return formatted success or failure message for display
	 * @throws Exception
	 */
	@RequestMapping("/admin/hl7/resubmitHL7InError.json")
	public @ResponseBody
	Map<String, Object> resubmitHL7InError(@RequestParam("hl7InErrorId") int id) throws Exception {
		HL7Service hL7Service = Context.getHL7Service();
		MessageSourceService mss = Context.getMessageSourceService();
		StringBuffer success = new StringBuffer();
		StringBuffer error = new StringBuffer();
		
		// Argument to pass to the success/error message
		Object[] args = new Object[] { id };
		
		try {
			//Restore Selected Message to the in queue table
			HL7InError hl7InError = hL7Service.getHL7InError(Integer.valueOf(id));
			HL7InQueue hl7InQueue = new HL7InQueue(hl7InError);
			hL7Service.saveHL7InQueue(hl7InQueue);
			
			//Remove selected Message from the error table
			hL7Service.purgeHL7InError(hl7InError);
			
			//Display a message for the operation
			success.append(mss.getMessage("Hl7inError.errorList.restored", args, Context.getLocale()) + "<br/>");
		}
		catch (APIException e) {
			log.warn("Error Processing erred message", e);
			error.append(mss.getMessage("Hl7inError.errorList.error", args, Context.getLocale()) + "<br/>");
		}
		
		Map<String, Object> results = new HashMap<String, Object>();
		
		if (!"".equals(success.toString())) {
			results.put(WebConstants.OPENMRS_MSG_ATTR, success.toString());
		}
		if (!"".equals(error.toString())) {
			results.put(WebConstants.OPENMRS_ERROR_ATTR, error.toString());
		}
		
		return results;
	}
	
	/**
	 * method for returning a batch of HL7s from the queue based on datatable parameters; sorting is
	 * unavailable at this time
	 *
	 * @param iDisplayStart start index for search
	 * @param iDisplayLength amount of terms to return
	 * @param sSearch search term(s)
	 * @param sEcho check digit for datatables
	 * @return batch of HL7InError objects to be converted to JSON
	 * @throws IOException
	 */
	@RequestMapping("/admin/hl7/hl7InErrorList.json")
	public @ResponseBody
	Map<String, Object> getHL7InErrorBatchAsJson(@RequestParam("iDisplayStart") int iDisplayStart,
	        @RequestParam("iDisplayLength") int iDisplayLength, @RequestParam("sSearch") String sSearch,
	        @RequestParam("sEcho") int sEcho) throws IOException {
		
		// get the data
		List<HL7InError> hl7s = Context.getHL7Service().getHL7InErrorBatch(iDisplayStart, iDisplayLength, sSearch);
		
		// form the results dataset
		List<Object> results = new ArrayList<Object>();
		for (HL7InError hl7 : hl7s) {
			results.add(splitHL7InError(hl7));
		}
		
		// build the response
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("iTotalRecords", Context.getHL7Service().countHL7InError(null));
		response.put("iTotalDisplayRecords", Context.getHL7Service().countHL7InError(sSearch));
		response.put("sEcho", sEcho);
		response.put("aaData", results.toArray());
		
		// send it
		return response;
	}
	
	/**
	 * create an object array for a given HL7InError
	 *
	 * @param q HL7InError object
	 * @return object array for use with datatables
	 */
	private Object[] splitHL7InError(HL7InError q) {
		// try to stick to basic types; String, Integer, etc (not Date)
		return new Object[] { q.getHL7InErrorId().toString(), q.getHL7Source().getName(),
		        Context.getDateFormat().format(q.getDateCreated()), q.getHL7Data(), q.getError(), q.getErrorDetails() };
	}
	
}
