/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.observation;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * Controller for the page that shows an administrator's view of all a patients observations
 * (possibly only for a specified concept)
 */
public class PersonObsFormController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	@Override
	protected CommandObject formBackingObject(HttpServletRequest request) throws Exception {
		if (!Context.isAuthenticated()) {
			return new CommandObject();
		}
		
		Person person = Context.getPersonService().getPerson(Integer.valueOf(request.getParameter("personId")));
		List<Concept> concepts = null;
		Concept concept = null;
		if (request.getParameter("conceptId") != null) {
			concept = Context.getConceptService().getConcept(Integer.valueOf(request.getParameter("conceptId")));
			concepts = Collections.singletonList(concept);
		}
		
		ObsService os = Context.getObsService();
		List<Obs> ret = os.getObservations(Collections.singletonList(person), null, concepts, null, null, null, null, null,
		    null, null, null, true);
		Collections.sort(ret, new Comparator<Obs>() {
			
			public int compare(Obs left, Obs right) {
				int temp = left.getConcept().getName().getName().compareTo(right.getConcept().getName().getName());
				if (temp == 0) {
					temp = OpenmrsUtil.compareWithNullAsGreatest(left.getVoided(), right.getVoided());
				}
				if (temp == 0) {
					temp = OpenmrsUtil.compareWithNullAsLatest(left.getObsDatetime(), right.getObsDatetime());
				}
				return temp;
			}
			
		});
		return new CommandObject(person, concept, ret);
	}
	
	public class CommandObject {
		
		private Person person;
		
		private Concept concept;
		
		private List<Obs> observations;
		
		public CommandObject() {
		}
		
		public CommandObject(Person person, Concept concept, List<Obs> observations) {
			super();
			this.person = person;
			this.concept = concept;
			this.observations = observations;
		}
		
		public Person getPerson() {
			return person;
		}
		
		public void setPerson(Person person) {
			this.person = person;
		}
		
		public Concept getConcept() {
			return concept;
		}
		
		public void setConcept(Concept concept) {
			this.concept = concept;
		}
		
		public List<Obs> getObservations() {
			return observations;
		}
		
		public void setObservations(List<Obs> observations) {
			this.observations = observations;
		}
	}
	
}
