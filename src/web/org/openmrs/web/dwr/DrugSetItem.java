package org.openmrs.web.dwr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptSet;

public class DrugSetItem {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private Integer drugSetId;
	private String name;
	private Integer drugCount;
	private String drugSetLabel;
	
	public DrugSetItem() { }
	
	public DrugSetItem(ConceptSet conceptSet) {
		drugSetId = conceptSet.getConcept().getConceptId();
	}

	/**
	 * @return Returns the drugCount.
	 */
	public Integer getDrugCount() {
		return drugCount;
	}

	/**
	 * @param drugCount The drugCount to set.
	 */
	public void setDrugCount(Integer drugCount) {
		this.drugCount = drugCount;
	}

	/**
	 * @return Returns the drugSetLabel.
	 */
	public String getDrugSetLabel() {
		return drugSetLabel;
	}

	/**
	 * @param drugSetLabel The drugSetLabel to set.
	 */
	public void setDrugSetLabel(String drugSetLabel) {
		this.drugSetLabel = drugSetLabel;
	}

	/**
	 * @return Returns the drugSetId.
	 */
	public Integer getDrugSetId() {
		return drugSetId;
	}

	/**
	 * @param drugSetId The drugSetId to set.
	 */
	public void setDrugSetId(Integer drugSetId) {
		this.drugSetId = drugSetId;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

}
