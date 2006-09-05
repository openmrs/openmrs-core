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
	 * @param regimenDisplayName The regimenDisplayName to set.
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
