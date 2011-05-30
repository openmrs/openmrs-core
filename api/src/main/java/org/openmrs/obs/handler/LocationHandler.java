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

import org.openmrs.Obs;
import org.openmrs.Location;
import org.openmrs.api.APIException;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;

/**
 * Handler for storing Location objects as answers for Complex Observations. The Location Id number
 * of each Location object is stored in the value_complex column of the Obs table in the database
 */

public class LocationHandler extends DomainObjectHandler implements ComplexObsHandler {
	
	/**
	 * The default Constructor method
	 */
	public LocationHandler() {
		super();
	}
	
	/**
	 * @see org.openmrs.obs.ComplexObsHandler#getObs(org.openmrs.Obs, java.lang.String)
	 */
	@Override
	public Obs saveObs(Obs obs) throws APIException {
		LocationService ls = Context.getLocationService();
		Object data = obs.getComplexData().getData();
		
		if (data == null) {
			throw new APIException("Cannot save complex obs where obsId=" + obs.getObsId()
			        + " because its ComplexData.getData() is null.");
		}
		
		Location location = ls.getLocation(Integer.parseInt(data.toString()));
		
		if (location == null) {
			throw new APIException("Cannot save complex obs where obsId=" + obs.getObsId()
			        + " because the location instance is null.");
		}
		
		// Set the Title for the valueComplex
		obs.setValueComplex(location.getAddress1() + "-" + location.getAddress2() + "(" + location.getLocationId() + ")"
		        + "|" + data.toString());
		
		// Remove the ComlexData from the Obs
		obs.setComplexData(null);
		
		return obs;
	}
	
	/**
	 * @see org.openmrs.obs.ComplexObsHandler#saveObs(org.openmrs.Obs)
	 */
	@Override
	public Obs getObs(Obs obs, String view) {
		LocationService ls = Context.getLocationService();
		String[] values = obs.getValueComplex().split("\\|");
		Location location = ls.getLocation(Integer.parseInt(values[1]));
		
		if (location == null) {
			throw new APIException("Cannot retrieve complex obs where obsId=" + obs.getObsId()
			        + " because the location instance is null.");
		}
		ComplexData complexData = new ComplexData(values[0], location);
		
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
