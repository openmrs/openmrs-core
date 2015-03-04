/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.person;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controls the moving/deleting of {@link PersonAttributeType}s.
 */
@Controller
@RequestMapping(value = "/admin/person/personAttributeType.list")
public class PersonAttributeTypeListController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Show the page to the user.
	 * 
	 * @should not fail if not authenticated
	 * @should put all attribute types into map
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.GET)
	public String displayPage(ModelMap modelMap) throws Exception {
		
		AdministrationService as = Context.getAdministrationService();
		
		// some helpful information that gets displayed
		modelMap.put("patientListingAttributeTypes", as
		        .getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_LISTING_ATTRIBUTES));
		modelMap.put("patientViewingAttributeTypes", as
		        .getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_VIEWING_ATTRIBUTES));
		modelMap.put("patientHeaderAttributeTypes", as
		        .getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_HEADER_ATTRIBUTES));
		modelMap.put("userListingAttributeTypes", as
		        .getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_USER_LISTING_ATTRIBUTES));
		modelMap.put("userViewingAttributeTypes", as
		        .getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_USER_VIEWING_ATTRIBUTES));
		
		List<PersonAttributeType> attributeTypeList = new Vector<PersonAttributeType>();
		
		//only fill the Object if the user has authenticated properly
		if (Context.isAuthenticated()) {
			PersonService ps = Context.getPersonService();
			attributeTypeList = ps.getAllPersonAttributeTypes(true);
		}
		
		modelMap.addAttribute("personAttributeTypeList", attributeTypeList);
		
		return "/admin/person/personAttributeTypeList";
	}
	
	/**
	 * The user has selected the bottom save button to update the GPs
	 * 
	 * @param patientListingAttributeTypes patient list gp
	 * @param patientViewingAttributeTypes patient viewing gp
	 * @param patientHeaderAttributeTypes patient header gp
	 * @param userListingAttributeTypes user listing gp
	 * @param userViewingAttributeTypes user viewing gp
	 * @param httpSession the current session
	 * @should save given personListingAttributeTypes
	 */
	@RequestMapping(method = RequestMethod.POST, params = "action=attrs")
	protected String updateGlobalProperties(String patientListingAttributeTypes, String patientViewingAttributeTypes,
	        String patientHeaderAttributeTypes, String userListingAttributeTypes, String userViewingAttributeTypes,
	        HttpSession httpSession) {
		AdministrationService as = Context.getAdministrationService();
		MessageSourceService mss = Context.getMessageSourceService();
		
		as.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_LISTING_ATTRIBUTES,
		        patientListingAttributeTypes));
		as.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_VIEWING_ATTRIBUTES,
		        patientViewingAttributeTypes));
		as.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_HEADER_ATTRIBUTES,
		        patientHeaderAttributeTypes));
		as.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_USER_LISTING_ATTRIBUTES,
		        userListingAttributeTypes));
		as.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_USER_VIEWING_ATTRIBUTES,
		        userViewingAttributeTypes));
		
		httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, mss.getMessage("PersonAttributeType.viewingListing.saved"));
		
		return "redirect:/admin/person/personAttributeType.list";
	}
	
	/**
	 * Moves the selected types up one in the order
	 * 
	 * @param personAttributeTypeId list of ids to move up
	 * @param httpSession the current session
	 * @should move selected ids up one in the list
	 * @should not fail if given first id
	 * @should not fail if not given any ids
	 */
	@RequestMapping(method = RequestMethod.POST, params = "action=moveup")
	public String moveUp(Integer[] personAttributeTypeId, HttpSession httpSession) {
		if (personAttributeTypeId == null) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PersonAttributeType.select");
		} else {
			PersonService ps = Context.getPersonService();
			
			List<PersonAttributeType> attributes = ps.getAllPersonAttributeTypes();
			
			Set<PersonAttributeType> attributesToSave = new HashSet<PersonAttributeType>();
			
			List<Integer> selectedIds = Arrays.asList(personAttributeTypeId);
			
			// assumes attributes are returned in sortWeight order
			
			for (int i = 1; i < attributes.size(); i++) {
				PersonAttributeType current = attributes.get(i);
				if (selectedIds.contains(current.getPersonAttributeTypeId())) {
					PersonAttributeType above = attributes.get(i - 1);
					
					// swap current and the attribute above it
					double temp = current.getSortWeight();
					current.setSortWeight(above.getSortWeight());
					above.setSortWeight(temp);
					Collections.swap(attributes, i, i - 1); // move the actual elements in the list as well
					
					attributesToSave.add(current);
					attributesToSave.add(above);
				}
			}
			
			// now save things
			for (PersonAttributeType pat : attributesToSave) {
				ps.savePersonAttributeType(pat);
			}
		}
		
		return "redirect:/admin/person/personAttributeType.list";
	}
	
	/**
	 * Moves the selected types down in the order
	 * 
	 * @param personAttributeTypeId list of ids to move down
	 * @param httpSession the current session
	 * @should move selected ids down in the list
	 * @should not fail if given last id
	 * @should not fail if not given any ids
	 */
	@RequestMapping(method = RequestMethod.POST, params = "action=movedown")
	public String moveDown(Integer[] personAttributeTypeId, HttpSession httpSession) {
		if (personAttributeTypeId == null) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PersonAttributeType.select");
		} else {
			PersonService ps = Context.getPersonService();
			List<PersonAttributeType> attributes = ps.getAllPersonAttributeTypes();
			
			// assumes attributes are returned in sortWeight order
			
			Set<PersonAttributeType> attributesToSave = new HashSet<PersonAttributeType>();
			
			List<Integer> selectedIds = Arrays.asList(personAttributeTypeId);
			
			for (int i = attributes.size() - 2; i >= 0; i--) {
				PersonAttributeType current = attributes.get(i);
				if (selectedIds.contains(current.getPersonAttributeTypeId())) {
					PersonAttributeType below = attributes.get(i + 1);
					
					// swap current and the attribute below it
					double temp = current.getSortWeight();
					current.setSortWeight(below.getSortWeight());
					below.setSortWeight(temp);
					Collections.swap(attributes, i, i + 1); // move the actual elements in the list as well
					
					attributesToSave.add(current);
					attributesToSave.add(below);
				}
			}
			
			// now save things
			for (PersonAttributeType pat : attributesToSave) {
				ps.savePersonAttributeType(pat);
			}
		}
		
		return "redirect:/admin/person/personAttributeType.list";
	}
	
}
