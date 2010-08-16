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
package org.openmrs.obs;

import org.openmrs.Obs;
import org.openmrs.api.APIException;

/**
 * Interface for handling complex obs. Implementing classes are responsible for the storage and
 * retrieval of ComplexData associated with an Obs that is complex -- where Obs.isComplex() returns
 * true. <br/>
 * <br/>
 * These handler classes are delegated to by the ObsService and should never be called directly. <br/>
 * <br/>
 * Use case:
 * 
 * <pre>
 *   Obs complexObs = Context.getObsService().getComplexObs(123, OpenmrsConstants.RAW_VIEW);
 *   ComplexData complexData = complexObs.getComplexData();
 *   Object largeStoredObject = complexData.getData();
 * </pre>
 * 
 * @since 1.5
 */
public interface ComplexObsHandler {
	
	/**
	 * Save a complex obs. This extracts the ComplexData from an Obs, stores it to a location
	 * determined by the hander, and returns the Obs with the ComplexData nullified.
	 * 
	 * @param obs
	 * @return the Obs with the ComplexData nullified
	 */
	public Obs saveObs(Obs obs) throws APIException;
	
	/**
	 * Fetches the ComplexData from the location indicated from Obs.value_complex, attaches
	 * ComplexData onto the Obs and returns the Obs. <br/>
	 * The ComplexData is returned in the format specified by the view (which can be null). <br/>
	 * This view is typically a contract between the view and the handler that has been registered,
	 * so they those two know the types of views that can be handled.
	 * 
	 * @param obs an obs without complex data filled in
	 * @param view nullable view type. This is defined by the ui and view/handler
	 * @return the obs with complex data filled in
	 * @see org.openmrs.util.OpenmrsConstants
	 */
	public Obs getObs(Obs obs, String view);
	
	/**
	 * Completely removes the ComplexData Object from its storage location. <br/>
	 * <br/>
	 * TODO: If we cannot delete the complex data object because of an error, do we want to return
	 * the Obs, a boolean false, or an Exception?
	 * 
	 * @param obs
	 */
	public boolean purgeComplexData(Obs obs);
	
}
