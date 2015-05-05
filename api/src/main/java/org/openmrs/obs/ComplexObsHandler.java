/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.obs;

import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.util.OpenmrsConstants;

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
	
	// Complex observation views
	public static final String RAW_VIEW = "RAW_VIEW";
	
	public static final String TITLE_VIEW = "TITLE_VIEW";
	
	public static final String TEXT_VIEW = "TEXT_VIEW";
	
	public static final String HTML_VIEW = "HTML_VIEW";
	
	public static final String PREVIEW_VIEW = "PREVIEW_VIEW";
	
	public static final String URI_VIEW = "URI_VIEW";
	
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
	
	/**
	 * Supported views getter
	 *
	 * @return all views supported by this handler
	 * @since 1.12
	 */
	public String[] getSupportedViews();
	
	/**
	 * View support check
	 *
	 * @param view view type defined by UI and view/handler
	 * @return true if given view is supported by this handler
	 * @since 1.12
	 */
	public boolean supportsView(String view);
}
