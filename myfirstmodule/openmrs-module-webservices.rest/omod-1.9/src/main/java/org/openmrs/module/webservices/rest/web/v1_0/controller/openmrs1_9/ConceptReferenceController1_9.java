/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_9;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * This controller allows the fetching of concepts via reference strings that can be either a UUID
 * or concept mapping. It then returns a map of those reference strings to the underlying concept.
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/conceptreferences")
public class ConceptReferenceController1_9 extends BaseRestController {
	
	@RequestMapping(method = { RequestMethod.GET })
	@ResponseBody
	public Object search(HttpServletRequest request, HttpServletResponse response) {
		ConceptService conceptService = Context.getConceptService();
		
		RequestContext requestContext = RestUtil.getRequestContext(request, response);
		
		String[] conceptReferences = new String[0];
		
		if ("GET".equalsIgnoreCase(request.getMethod())) {
			String references = requestContext.getParameter("references");
			if (StringUtils.isNotBlank(references)) {
				conceptReferences = references.split(",");
			}
		}
		
		if (conceptReferences.length > 0) {
			SimpleObject results = new SimpleObject(conceptReferences.length);
			
			for (String conceptReference : conceptReferences) {
				if (StringUtils.isBlank(conceptReference)) {
					continue;
				}
				// handle UUIDs
				if (isValidUuid(conceptReference)) {
					Concept concept = conceptService.getConceptByUuid(conceptReference);
					if (concept != null) {
						addResult(results, conceptReference, concept, requestContext.getRepresentation());
						continue;
					}
				}
				// handle mappings
				int idx = conceptReference.indexOf(':');
				if (idx >= 0 && idx < conceptReference.length() - 1) {
					String conceptSource = conceptReference.substring(0, idx);
					String conceptCode = conceptReference.substring(idx + 1);
					Concept concept = conceptService.getConceptByMapping(conceptCode, conceptSource, false);
					if (concept != null) {
						addResult(results, conceptReference, concept, requestContext.getRepresentation());
					}
				}
			}
			
			if (results.size() == 0) {
				return new SimpleObject(0);
			}
			
			return results;
		}
		
		return new SimpleObject(0);
	}
	
	private void addResult(SimpleObject results, String conceptReference, Concept concept, Representation rep) {
		results.put(conceptReference,
				ConversionUtil.convertToRepresentation(concept, rep == null ? new DefaultRepresentation() : rep));
	}
	
	private static boolean isValidUuid(String uuid) {
		return uuid != null && (uuid.length() == 36 || uuid.length() == 38 || uuid.indexOf(' ') < 0
				|| uuid.indexOf('.') < 0);
	}
}
