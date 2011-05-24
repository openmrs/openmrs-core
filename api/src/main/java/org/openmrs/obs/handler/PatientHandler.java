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

/**
 * Handler for storing Patient objects as answers for Complex Observations. The Patient Id number of
 * each Patient object is stored in the value_complex column of the Obs table in the database
 */

public class PatientHandler extends DomainObjectHandler implements ComplexObsHandler {
	
	public static final Log log = LogFactory.getLog(AbstractHandler.class);
	
	/**
	 * The default Constructor method
	 */
	public PatientHandler() {
		super();
	}
	
	/**
	 * @see org.openmrs.obs.ComplexObsHandler#getObs(org.openmrs.Obs, java.lang.String)
	 */
	@Override
	public Obs saveObs(Obs obs) throws APIException {
		Object data = obs.getComplexData().getData();
		
		// Set the Title for the valueComplex
		obs.setValueComplex(" Patient |" + data.toString());
		
		// Remove the ComlexData from the Obs
		obs.setComplexData(null);
		
		return obs;
	}
	
	/**
	 * @see org.openmrs.obs.ComplexObsHandler#saveObs(org.openmrs.Obs)
	 */
	@Override
	public Obs getObs(Obs obs, String view) {
		PatientService os = Context.getPatientService();
		String[] values = obs.getValueComplex().split("\\|");
		Patient patient = os.getPatient(Integer.parseInt(values[1]));
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
		// A skeleton method was implemented here because the ComplexObsHandler interface 
		// Demands it. What (if necessary) is the best way to implement this method ?
		return false;
	}
	
}
