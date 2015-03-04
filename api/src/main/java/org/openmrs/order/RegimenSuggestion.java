/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.order;

import java.io.Serializable;
import java.util.List;

public class RegimenSuggestion implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
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
