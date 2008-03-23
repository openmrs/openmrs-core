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
package org.openmrs.order;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DrugOrderSupport {

	private static DrugOrderSupport singleton;
	
	private static Log log = LogFactory.getLog(DrugOrderSupport.class);
	
	List<RegimenSuggestion> standardRegimens;
	List<RegimenSuggestion> suggestedRegimens;
	
	public DrugOrderSupport() {
		if (singleton == null)
			singleton = this;
	}
	
	public static DrugOrderSupport getInstance() {
		if (singleton == null)
			throw new RuntimeException("Not Yet Instantiated");
		else
			return singleton;
	}
	
	/**
	 * @return Returns the standardRegimens.
	 */
	public List<RegimenSuggestion> getStandardRegimens() {
		return standardRegimens;
	}
	/**
	 * @param standardRegimens The standardRegimens to set.
	 */
	public void setStandardRegimens(List<RegimenSuggestion> standardRegimens) {
		this.standardRegimens = standardRegimens;
	}
	/**
	 * @return Returns the suggestedRegimens.
	 */
	public List<RegimenSuggestion> getSuggestedRegimens() {
		return suggestedRegimens;
	}
	/**
	 * @param suggestedRegimens The suggestedRegimens to set.
	 */
	public void setSuggestedRegimens(List<RegimenSuggestion> suggestedRegimens) {
		this.suggestedRegimens = suggestedRegimens;
	}
}
