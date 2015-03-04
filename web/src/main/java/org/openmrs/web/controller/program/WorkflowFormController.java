/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.program;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.APIException;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class WorkflowFormController extends SimpleFormController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		binder.registerCustomEditor(java.lang.Integer.class, new CustomNumberEditor(java.lang.Integer.class, true));
	}
	
	/**
	 * This is called prior to displaying a form for the first time. It tells Spring the
	 * form/command object to load into the request
	 *
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		log.debug("called formBackingObject");
		
		ProgramWorkflow wf = null;
		
		if (Context.isAuthenticated()) {
			ProgramWorkflowService ps = Context.getProgramWorkflowService();
			String programWorkflowId = request.getParameter("programWorkflowId");
			if (programWorkflowId != null) {
				wf = ps.getWorkflow(Integer.valueOf(programWorkflowId));
			}
			
			if (wf == null) {
				throw new IllegalArgumentException("Can't find workflow");
			}
		}
		
		if (wf == null) {
			wf = new ProgramWorkflow();
		}
		
		return wf;
	}
	
	/**
	 * The onSubmit function receives the form/command object that was modified by the input form
	 * and saves it to the db
	 *
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,
	        BindException errors) throws Exception {
		log.debug("about to save " + obj);
		HttpSession httpSession = request.getSession();
		String view = getFormView();
		if (Context.isAuthenticated()) {
			ProgramWorkflow wf = (ProgramWorkflow) obj;
			// get list of states to be deleted
			String statesToDelete = request.getParameter("deleteStates");
			Set<Integer> cantBeDeleted = new HashSet<Integer>(); // holds concept ids that cant be deleted
			if (!"".equals(statesToDelete)) {
				// then delete listed states first
				Map<Integer, ProgramWorkflowState> toRemove = new HashMap<Integer, ProgramWorkflowState>();
				for (StringTokenizer std = new StringTokenizer(statesToDelete, "|"); std.hasMoreTokens();) {
					String str = std.nextToken();
					String[] state = str.split(",");
					Integer conceptIdDelete = Integer.valueOf(state[0]);
					
					for (ProgramWorkflowState s : wf.getStates()) {
						if (s.getConcept().getConceptId().equals(conceptIdDelete)) {
							toRemove.put(conceptIdDelete, s);
							break;
						}
					}
					
				}
				
				for (Map.Entry<Integer, ProgramWorkflowState> remove : toRemove.entrySet()) {
					try {
						wf.removeState(remove.getValue());
						Context.getProgramWorkflowService().updateWorkflow(wf);
						httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Workflow.saved");
						log.debug("removed " + remove);
					}
					catch (DataIntegrityViolationException e) {
						httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.object.state.inuse.cannot.delete");
						wf.addState(remove.getValue());
						// add to cant be deleted so it would be skipped from getting retired
						cantBeDeleted.add(remove.getKey());
					}
					catch (APIException e) {
						httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.general");
						wf.addState(remove.getValue());
						// add to cant be deleted so it would be skipped from getting retired
						cantBeDeleted.add(remove.getKey());
					}
				}
				
			}
			// get list of states, and update the command object
			String statesStr = request.getParameter("newStates");
			if (!"".equals(statesStr)) {
				// This is a brute-force algorithm, but n will be small.
				Set<Integer> doneSoFar = new HashSet<Integer>(); // concept ids done so far
				for (StringTokenizer st = new StringTokenizer(statesStr, "|"); st.hasMoreTokens();) {
					String str = st.nextToken();
					String[] tmp = str.split(",");
					Integer conceptId = Integer.valueOf(tmp[0]);
					doneSoFar.add(conceptId);
					ProgramWorkflowState pws = null;
					for (ProgramWorkflowState s : wf.getStates()) {
						if (s.getConcept().getConceptId().equals(conceptId)) {
							pws = s;
							break;
						}
					}
					if (pws == null) {
						pws = new ProgramWorkflowState();
						pws.setConcept(Context.getConceptService().getConcept(conceptId));
						wf.addState(pws);
					} else {
						// un-retire if necessary
						if (pws.isRetired()) {
							pws.setRetired(false);
						}
					}
					pws.setInitial(Boolean.valueOf(tmp[1]));
					pws.setTerminal(Boolean.valueOf(tmp[2]));
				}
				// retire states if we didn't see their concept during the loop above
				for (ProgramWorkflowState s : wf.getStates()) {
					if (!doneSoFar.contains(s.getConcept().getConceptId())) {
						s.setRetired(true);
					}
				}
				try {
					Context.getProgramWorkflowService().updateWorkflow(wf);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Workflow.saved");
				}
				catch (APIException e) {
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.general");
				}
			} else {
				// no new state sent therefore retire all excluding deleted
				for (ProgramWorkflowState s : wf.getStates()) {
					if (!cantBeDeleted.contains(s.getConcept().getConceptId())) {
						s.setRetired(true);
					}
				}
				Context.getProgramWorkflowService().updateWorkflow(wf);
			}
		}
		view = getSuccessView();
		return new ModelAndView(new RedirectView(view));
	}
}
