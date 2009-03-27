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

public class RegimenSuggestion {
	
	private List<DrugSuggestion> drugComponents;
	
	private String displayName;
	
	private String codeName;
	
	private String canReplace;
	
	/**
	 * @return Returns the drugComponents.
	 */
	public List<DrugSuggestion> getDrugComponents() {
		return drugComponents;
	}
	
	/**
	 * @param drugComponents The drugComponents to set.
	 */
	public void setDrugComponents(List<DrugSuggestion> drugComponents) {
		this.drugComponents = drugComponents;
	}
	
	/**
	 * @return Returns the regimenDisplayName.
	 */
	public String getDisplayName() {
		return displayName;
	}
	
	/**
	 * @param displayName The regimenDisplayName to set.
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	/**
	 * @return Returns the codeName.
	 */
	public String getCodeName() {
		return codeName;
	}
	
	/**
	 * @param codeName The codeName to set.
	 */
	public void setCodeName(String codeName) {
		this.codeName = codeName;
	}
	
	/**
	 * @return Returns the canReplace.
	 */
	public String getCanReplace() {
		return canReplace;
	}
	
	/**
	 * @param canReplace The canReplace to set.
	 */
	public void setCanReplace(String canReplace) {
		this.canReplace = canReplace;
	}
	
}
