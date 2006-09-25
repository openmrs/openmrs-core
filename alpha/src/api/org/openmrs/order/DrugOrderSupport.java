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
