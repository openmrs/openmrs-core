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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.PatientSearch;
import org.openmrs.reporting.PatientSearchReportObject;
import org.openmrs.reporting.ReportObjectXMLDecoder;
import org.openmrs.reporting.ReportObjectXMLEncoder;
import org.openmrs.reporting.SearchArgument;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * 
 */
public class PatientSearchFormController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		binder.registerCustomEditor(java.lang.Integer.class, new CustomNumberEditor(java.lang.Integer.class, true));
	}
	
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,
	                                BindException errors) throws Exception {
		
		String success = "";
		String error = "";
		HttpSession httpSession = request.getSession();
		String view = getSuccessView();
		if (Context.isAuthenticated()) {
			MessageSourceAccessor msa = getMessageSourceAccessor();
			String action = request.getParameter("action");
			if (msa.getMessage("PatientSearch.save").equals(action)) {
				PatientSearchReportObject psroBinded = (PatientSearchReportObject) obj;
				String hiddenName = request.getParameter("hiddenName");
				String hiddenDesc = request.getParameter("hiddenDesc");
				int hasXMLChanged = 0;
				hasXMLChanged = Integer.parseInt(request.getParameter("patientSearchXMLHasChanged"));
				String textAreaXML = request.getParameter("xmlStringTextArea");
				Integer argumentsLength = Integer.valueOf(request.getParameter("argumentsSize"));
				PatientSearch ps = null;
				String valueRoot = "value";
				String hiddenValueRoot = "hiddenValue";
				int testXMLerror = 0;
				List<Integer> needsUpdate = new ArrayList<Integer>();
				
				for (int i = 0; i < argumentsLength; i++) {
					String hv = request.getParameter(hiddenValueRoot + i);
					String v = request.getParameter(valueRoot + i);
					
					if (hv.compareTo(v) != 0)
						needsUpdate.add(i);
				}
				
				String saved = msa.getMessage("PatientSearch.saved");
				String notsaved = msa.getMessage("PatientSearch.notsaved");
				String invalidXML = msa.getMessage("PatientSearch.invalidXML");
				String title = msa.getMessage("PatientSearch.title");
				
				boolean hasNewSearchArg = false;
				String newSearchArgName = (String) request.getParameter("newSearchArgName");
				String newSearchArgValue = (String) request.getParameter("newSearchArgValue");
				String newSearchArgClass = (String) request.getParameter("newSearchArgClass");
				if (StringUtils.hasText(newSearchArgName) || StringUtils.hasText(newSearchArgValue)
				        || StringUtils.hasText(newSearchArgClass)) {
					hasNewSearchArg = true;
				}
				
				if (hiddenName.compareTo(psroBinded.getName()) != 0
				        || hiddenDesc.compareTo(psroBinded.getDescription()) != 0 || needsUpdate.size() > 0
				        || hasXMLChanged == 1 || hasNewSearchArg) {
					
					if (needsUpdate.size() > 0) {
						
						ps = psroBinded.getPatientSearch();
						List<SearchArgument> searchArguments = ps.getArguments();
						
						for (Integer myI : needsUpdate) {
							SearchArgument sA = (SearchArgument) searchArguments.get(myI);
							SearchArgument newSA = new SearchArgument();
							newSA.setName(sA.getName());
							newSA.setPropertyClass(sA.getPropertyClass());
							newSA.setValue(request.getParameter(valueRoot + myI));
							searchArguments.set(myI, newSA);
							
						}
						ps.setArguments(searchArguments);
						psroBinded.setPatientSearch(ps);
					}
					
					if (hasXMLChanged == 1) {
						try {
							ReportObjectXMLDecoder roxd = new ReportObjectXMLDecoder(textAreaXML);
							
							PatientSearchReportObject psroFromXML = (PatientSearchReportObject) roxd
							        .toAbstractReportObject();
							psroBinded.setDescription(psroFromXML.getDescription());
							psroBinded.setName(psroFromXML.getName());
							psroBinded.setPatientSearch(psroFromXML.getPatientSearch());
							psroBinded.setSubType(psroFromXML.getSubType());
							psroBinded.setType(psroFromXML.getType());
							
						}
						catch (Exception ex) {
							log.warn("Invalid Patient Search XML", ex);
							error += title + " " + notsaved + ", " + invalidXML;
							testXMLerror++;
						}
					}
					
					if (hasNewSearchArg) {
						if (StringUtils.hasText(newSearchArgName) && StringUtils.hasText(newSearchArgValue)
						        && StringUtils.hasText(newSearchArgClass)) {
							try {
								psroBinded.getPatientSearch().addArgument(newSearchArgName, newSearchArgValue,
								    Class.forName(newSearchArgClass));
							}
							catch (Exception e) {
								error += msa.getMessage("PatientSearch.invalidSearchArgument");
							}
						} else {
							error += msa.getMessage("PatientSearch.invalidSearchArgument");
						}
						log.debug("Patient Search now has arguments: " + psroBinded.getPatientSearch().getArguments());
					}
					
					if (testXMLerror != 1 || hasNewSearchArg) {
						Context.getReportObjectService().saveReportObject(psroBinded);
						success = saved;
					}
				}
			}
		}
		if (!error.equals(""))
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, error);
		else if (!success.equals(""))
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success);
		
		return new ModelAndView(new RedirectView(view));
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		PatientSearchReportObject psro = null;
		if (Context.isAuthenticated()) {
			String reportId = request.getParameter("patientSearchIdLookup");
			if (reportId != null) {
				psro = (PatientSearchReportObject) Context.getReportObjectService().getReportObject(
				    Integer.valueOf(reportId));
			}
		} else
			psro = new PatientSearchReportObject();
		return psro;
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest,
	 *      java.lang.Object, org.springframework.validation.Errors)
	 */
	protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors errs) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		
		if (Context.isAuthenticated()) {
			ReportObjectXMLEncoder roxe = new ReportObjectXMLEncoder(obj);
			String template = roxe.toXmlString();
			map.put("xmlString", template);
			int charOccurrences = ((template.length() - (template.replaceAll("/string", "").length())) / 7)
			        + ((template.length() - (template.replaceAll("/int", "").length())) / 4)
			        + ((template.length() - (template.replaceAll("/class", "").length())) / 6);
			map.put("xmlStringSize", template.length() - (template.replaceAll("<", "").length()) - charOccurrences);
		}
		return map;
	}
}
