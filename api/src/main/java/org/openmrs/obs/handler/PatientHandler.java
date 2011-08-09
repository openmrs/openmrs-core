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
import org.apache.commons.lang.StringUtils;
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
 * Handler for storing Patient objects as the value for Complex Observations. The Patient Id of each
 * Patient object is stored in the value_complex column of the Obs table. This value may be preceded
 * by an optional displayable value. This class is the most basic level of implementation for
 * PatientHandlers. For a more improved version, see
 * org.openmrs.obs.handler.PatientHandler.PatientFieldGenObsHandler or consider extending
 * PatientHandler class to meet your requirements. There may be several classes which extend
 * PatientHandler. Out of these, only one will be loaded by Spring. The class to be loaded will be
 * decided based on the @Order annotation value. As default, PatientHandler will have the lowest
 * possible priority, and will be overridden by PatientFieldGenObsHandler, which lets us use
 * fieldGen tags for data entry.
 */

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class PatientHandler extends CustomDatatypeHandler implements ComplexObsHandler {
	
	public static final Log log = LogFactory.getLog(PatientHandler.class);
	
	/** The Constant HANDLER_TYPE. Used to differentiate between handler types */
	public static final String HANDLER_TYPE = "PatientHandler";
	
	/** The Constant DISPLAY_LINK. Used as a link to display the selected patient */
	public static final String DISPLAY_LINK = "/patientDashboard.form?patientId=";
	
	public PatientHandler() {
		super();
	}
	
	/**
	 * This method is used to save the Obs. Firstly, the selected Patient Instance is retrieived.
	 * then, the valueComplex String to be persisted is created based upon this instance. The
	 * valueComplex String is created via a call to a method in Obs.java
	 * 
	 * @see org.openmrs.obs.ComplexObsHandler#getObs(org.openmrs.Obs, java.lang.String)
	 */
	@Override
	public Obs saveObs(Obs obs) throws APIException {
		PatientService ps = Context.getPatientService();
		
		Patient patient = ps.getPatient(Integer.parseInt(obs.getComplexValueKey()));
		
		if (patient == null) {
			throw new APIException("Cannot save complex obs where obsId=" + obs.getObsId() + " Desired Patient id :"
			        + Integer.parseInt(obs.getComplexValueKey()) + " cannot be found");
		}
		
		obs.setComplexValueText(patient.getPersonName() + "(" + patient.getPatientIdentifier() + ")");
		
		// Retreive complexValueText and complexValueKey values
		String complexValueText = obs.getComplexValueText();
		String complexValueKey = obs.getComplexValueKey();
		
		obs.setValueComplex(complexValueText, complexValueKey);
		// Remove the ComlexData from the Obs
		obs.setComplexData(null);
		
		return obs;
	}
	
	/**
	 * This method retrieves the patient instance persisted in the database. The patient is
	 * retrieved using the ComplexValueKey. It is passed into ComplexData object, and returned with
	 * the Obs.
	 * 
	 * @see org.openmrs.obs.ComplexObsHandler#saveObs(org.openmrs.Obs)
	 */
	@Override
	public Obs getObs(Obs obs, String view) {
		Patient patient = null;
		PatientService ps = Context.getPatientService();
		
		String key = obs.getComplexValueKey();
		
		if (key != null && !StringUtils.isEmpty(key))
			patient = ps.getPatient(Integer.parseInt(key));
		
		if (patient != null) {
			ComplexData complexData = new ComplexData(obs.getComplexValueText(), patient);
			obs.setComplexData(complexData);
		} else
			log.info("Warning : specified patient cannot be found - returning no ComplexData for " + obs.getObsId()
			        + " Is this to be used for editing purposes ?");
		
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
	 * Gets the link used to display a selected patient to an user.
	 * 
	 * @return the display link
	 */
	public String getDisplayLink() {
		return PatientHandler.DISPLAY_LINK;
	}
	
	/**
	 * Gets the handler type for each registered handler.
	 * 
	 * @return the handler type
	 */
	public String getHandlerType() {
		return PatientHandler.HANDLER_TYPE;
	}
	
	/**
	 * Validate.
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
	
	/**
	 * This method is used to return the persisted data only. The Patient instance is retreived
	 * using data from the Obs passed in. This instance is returned to the user. If there is no
	 * matching patient, then the method returns null.
	 */
	@Override
	public Object getValue(Obs obs) {
		Patient patient = null;
		if (obs.getValueComplex() != null && !StringUtils.isEmpty(obs.getValueComplex())) {
			PatientService ps = Context.getPatientService();
			String key = obs.getComplexValueKey();
			
			for (int i = 0; i < key.length(); i++) {
				if (!Character.isDigit(key.charAt(i)))
					return null;
			}
			
			patient = ps.getPatient(Integer.parseInt(key));
			
			return patient;
		} else {
			return null;
		}
	}
}
