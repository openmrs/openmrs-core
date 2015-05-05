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
import org.openmrs.hl7.HL7Constants;
import org.openmrs.hl7.HL7InQueue;
import org.openmrs.hl7.HL7Service;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class Hl7InQueueListController {
	
	/**
	 * Logger for this class and subclasses
	 */
	private static final Log log = LogFactory.getLog(Hl7InQueueListController.class);
	
	/**
	 * Render the pending HL7 queue messages page
	 *
	 * @param modelMap
	 * @return
	 */
	@RequestMapping("/admin/hl7/hl7InQueuePending.htm")
	public String listPendingHL7s(ModelMap modelMap) {
		modelMap.addAttribute("messageState", HL7Constants.HL7_STATUS_PENDING);
		return "/admin/hl7/hl7InQueueList";
	}
	
	/**
	 * Render the suspended HL7 queue messages page
	 *
	 * @param modelMap
	 * @return
	 */
	@RequestMapping("/admin/hl7/hl7InQueueHeld.htm")
	public String listSuspendedHL7s(ModelMap modelMap) {
		modelMap.addAttribute("messageState", HL7Constants.HL7_STATUS_DELETED);
		return "/admin/hl7/hl7OnHoldList";
	}
	
	/**
	 * suspends or restores a HL7InQueue based on current status
	 *
	 * @param id HL7InQueueId for identifying the HL7 message
	 * @return formatted success or failure message for display
	 * @throws Exception
	 */
	@RequestMapping("/admin/hl7/toggleHL7InQueue.json")
	public @ResponseBody
	Map<String, Object> toggleHL7InQueue(@RequestParam("hl7InQueueId") int id) throws Exception {
		HL7Service hL7Service = Context.getHL7Service();
		MessageSourceService mss = Context.getMessageSourceService();
		StringBuffer success = new StringBuffer();
		StringBuffer error = new StringBuffer();
		
		// Argument to pass to the success/error message
		Object[] args = new Object[] { id };
		
		try {
			//Update the hl7 message's status based on existing status
			HL7InQueue hl7InQueue = hL7Service.getHL7InQueue(id);
			if (hl7InQueue.getMessageState().equals(HL7Constants.HL7_STATUS_PENDING)) {
				hl7InQueue.setMessageState(HL7Constants.HL7_STATUS_DELETED);
			} else {
				hl7InQueue.setMessageState(HL7Constants.HL7_STATUS_PENDING);
			}
			hL7Service.saveHL7InQueue(hl7InQueue);
			
			//Display a message for the operation
			if (hl7InQueue.getMessageState().equals(HL7Constants.HL7_STATUS_PENDING)) {
				success.append(mss.getMessage("Hl7inQueue.queueList.restored", args, Context.getLocale()) + "<br/>");
			} else {
				success.append(mss.getMessage("Hl7inQueue.queueList.held", args, Context.getLocale()) + "<br/>");
			}
		}
		catch (APIException e) {
			log.warn("Error updating a queue entry", e);
			error.append(mss.getMessage("Hl7inQueue.queueList.error", args, Context.getLocale()) + "<br/>");
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
	 * method for returning a batch of HL7s from the queue based on datatable parameters
	 *
	 * @param iDisplayStart start index for search
	 * @param iDisplayLength amount of terms to return
	 * @param sSearch search term(s)
	 * @param sEcho check digit for datatables
	 * @param messageState HL7InQueue state to look up
	 * @return batch of HL7InQueue objects to be converted to JSON
	 * @throws IOException
	 */
	@RequestMapping("/admin/hl7/hl7InQueueList.json")
	public @ResponseBody
	Map<String, Object> getHL7InQueueBatchAsJson(@RequestParam("iDisplayStart") int iDisplayStart,
	        @RequestParam("iDisplayLength") int iDisplayLength, @RequestParam("sSearch") String sSearch,
	        @RequestParam("sEcho") int sEcho, @RequestParam("messageState") int messageState) throws IOException {
		
		// get the data
		List<HL7InQueue> hl7s = Context.getHL7Service().getHL7InQueueBatch(iDisplayStart, iDisplayLength, messageState,
		    sSearch);
		
		// form the results dataset
		List<Object> results = new ArrayList<Object>();
		for (HL7InQueue hl7 : hl7s) {
			results.add(splitHL7InQueue(hl7));
		}
		
		// build the response
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("iTotalRecords", Context.getHL7Service().countHL7InQueue(messageState, null));
		response.put("iTotalDisplayRecords", Context.getHL7Service().countHL7InQueue(messageState, sSearch));
		response.put("sEcho", sEcho);
		response.put("aaData", results.toArray());
		
		// send it
		return response;
	}
	
	/**
	 * create an object array for a given HL7InQueue
	 *
	 * @param q HL7InQueue object
	 * @return object array for use with datatables
	 */
	private Object[] splitHL7InQueue(HL7InQueue q) {
		// try to stick to basic types; String, Integer, etc (not Date)
		return new Object[] { q.getHL7InQueueId().toString(), q.getHL7Source().getName(),
		        Context.getDateFormat().format(q.getDateCreated()), q.getHL7Data() };
	}
	
}
