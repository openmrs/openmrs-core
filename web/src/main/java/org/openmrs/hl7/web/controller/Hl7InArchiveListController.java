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
import org.openmrs.api.context.Context;
import org.openmrs.hl7.HL7Constants;
import org.openmrs.hl7.HL7InArchive;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * controller for the HL7 archives list view; only displays HL7s that have not been migrated to the
 * filesystem
 */
@Controller
public class Hl7InArchiveListController {
	
	/**
	 * Logger for this class and subclasses
	 */
	private static final Log log = LogFactory.getLog(Hl7InArchiveListController.class);
	
	/**
	 * Render the archived HL7 messages page
	 *
	 * @param modelMap
	 * @return
	 */
	@RequestMapping("/admin/hl7/hl7InArchives.htm")
	public String listArchivedHL7s(ModelMap modelMap) {
		return "/admin/hl7/hl7InArchiveList";
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
	@RequestMapping("/admin/hl7/hl7InArchiveList.json")
	public @ResponseBody
	Map<String, Object> getHL7InArchiveBatchAsJson(@RequestParam("iDisplayStart") int iDisplayStart,
	        @RequestParam("iDisplayLength") int iDisplayLength, @RequestParam("sSearch") String sSearch,
	        @RequestParam("sEcho") int sEcho) throws IOException {
		
		// get the data
		List<HL7InArchive> hl7s = Context.getHL7Service().getHL7InArchiveBatch(iDisplayStart, iDisplayLength,
		    HL7Constants.HL7_STATUS_PROCESSED, sSearch);
		
		// form the results dataset
		List<Object> results = new ArrayList<Object>();
		for (HL7InArchive hl7 : hl7s) {
			results.add(splitHL7InArchive(hl7));
		}
		
		// build the response
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("iTotalRecords", Context.getHL7Service().countHL7InArchive(HL7Constants.HL7_STATUS_PROCESSED, null));
		response.put("iTotalDisplayRecords", Context.getHL7Service().countHL7InArchive(HL7Constants.HL7_STATUS_PROCESSED,
		    sSearch));
		response.put("sEcho", sEcho);
		response.put("aaData", results.toArray());
		
		// send it
		return response;
	}
	
	/**
	 * create an object array for a given HL7InArchive
	 *
	 * @param q HL7InArchive object
	 * @return object array for use with datatables
	 */
	private Object[] splitHL7InArchive(HL7InArchive q) {
		// try to stick to basic types; String, Integer, etc (not Date)
		return new Object[] { Integer.toString(q.getHL7InArchiveId()), q.getHL7Source().getName(),
		        Context.getDateFormat().format(q.getDateCreated()), q.getHL7Data() };
	}
	
}
