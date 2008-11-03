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
package org.openmrs.web.controller.observation;

import java.util.ArrayList;
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
 * Controller for the page that shows an administrator's view of all a patients
 * observations (possibly only for a specified concept)
 */
public class PersonObsFormController extends SimpleFormController {

	/** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
   
	@Override
    protected CommandObject formBackingObject(HttpServletRequest request) throws Exception {
		if (!Context.isAuthenticated())
			return new CommandObject();

		Person person = Context.getPersonService().getPerson(Integer.valueOf(request.getParameter("personId")));
		Concept concept = null;
		if (request.getParameter("conceptId") != null)
			concept = Context.getConceptService().getConcept(Integer.valueOf(request.getParameter("conceptId")));

		ObsService os = Context.getObsService();
		List<Obs> ret = new ArrayList<Obs>( concept == null ?
								os.getObservations(person, true) :
								os.getObservations(person, concept, true) );
		Collections.sort(ret, new Comparator<Obs>() {
			public int compare(Obs left, Obs right) {
				int temp = left.getConcept().getName().getName().compareTo(right.getConcept().getName().getName());
				if (temp == 0)
					temp = OpenmrsUtil.compareWithNullAsGreatest(left.getVoided(), right.getVoided());
	            if (temp == 0)
	            	temp = OpenmrsUtil.compareWithNullAsLatest(left.getObsDatetime(), right.getObsDatetime());
	            return temp;
            }
			
		});
		return new CommandObject(person, concept, ret);
    }

	public class CommandObject {
		private Person person;
		private Concept concept;
		private List<Obs> observations;
		public CommandObject() { }
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
