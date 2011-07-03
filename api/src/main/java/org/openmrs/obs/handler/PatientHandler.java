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
package org.openmrs.obs.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Handler for storing Patient objects as answers for Complex Observations. The Patient Id number of
 * each Patient object is stored in the value_complex column of the Obs table in the database
 */

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class PatientHandler extends CustomDatatypeHandler implements ComplexObsHandler {
	
	public static final Log log = LogFactory.getLog(PatientHandler.class);
	
	/**
	 * The default Constructor method
	 */
	public PatientHandler() {
		super();
	}
	
	/**
	 * The Link to the default dashboard / page where patient instances will be displayed
	 */
	private String displayLink = "/patientDashboard.form?patientId=";
	
	/**
	 * @see org.openmrs.obs.ComplexObsHandler#getObs(org.openmrs.Obs, java.lang.String)
	 */
	@Override
	public Obs saveObs(Obs obs) throws APIException {
		PatientService ps = Context.getPatientService();
		
		Patient patient = ps.getPatient(Integer.parseInt(obs.getValueComplex()));
		
		if (patient == null) {
			throw new APIException("Cannot save complex obs where obsId=" + obs.getObsId() + " Desired Patient id :"
			        + Integer.parseInt(obs.getValueComplex()) + " cannot be found");
		}
		// Set the Title for the valueComplex
		obs.setValueComplex(patient.getPersonName() + "(" + patient.getPatientIdentifier() + ")" + "|"
		        + obs.getValueComplex());
		
		// Remove the ComlexData from the Obs
		obs.setComplexData(null);
		
		return obs;
	}
	
	/**
	 * @see org.openmrs.obs.ComplexObsHandler#saveObs(org.openmrs.Obs)
	 */
	@Override
	public Obs getObs(Obs obs, String view) {
		PatientService ps = Context.getPatientService();
		String[] values = obs.getValueComplex().split("\\|");
		Patient patient = ps.getPatient(Integer.parseInt(values[1]));
		
		if (patient == null) {
			throw new APIException("Cannot retrieve complex obs where obsId=" + obs.getObsId() + " because the patient id :"
			        + Integer.parseInt(values[1]) + " cannot be found.");
		}
		
		ComplexData complexData = new ComplexData(values[0], patient);
		obs.setComplexData(complexData);
		
		return obs;
	}
	
	/**
	 * @see org.openmrs.obs.ComplexObsHandler#purgeComplexData(org.openmrs.Obs)
	 */
	@Override
	public boolean purgeComplexData(Obs obs) {
		// Default value for now.
		// A skeleton method was implemented here because the ComplexObsHandler
		// interface
		// Demands it. What (if necessary) is the best way to implement this
		// method ?
		// TO DO: see comments
		// https://source.openmrs.org/cru/CR-TRUNK-369#CFR-8348
		return true;
	}
	
	/**
	 * Gets the display link.
	 * 
	 * @return the display link
	 */
	public String getDisplayLink() {
		return this.displayLink;
	}
	
	/**
	 * Gets the handler type for each registered handler.
	 * 
	 * @return the handler type
	 */
	public String getHandlerType() {
		return "PatientHandler";
	}
	
	/**
	 * Validate.
	 * 
	 * @param handlerConfig the handler config
	 * @param obs the obs
	 * @return true, if successful
	 */
	@Override
	public boolean validate(String handlerConfig, Obs obs) {
		
		PatientService ps = Context.getPatientService();
		String[] values = obs.getValueComplex().split("\\|");
		
		Patient patient = ps.getPatient(Integer.parseInt(values[0]));
		if (patient == null) {
			throw new APIException("Cannot retrieve complex obs where obsId=" + obs.getObsId() + " because the patient id :"
			        + Integer.parseInt(values[0]) + " cannot be found.");
		}
		if (patient.isDead())
			return false;
		return true;
	}
	
}
