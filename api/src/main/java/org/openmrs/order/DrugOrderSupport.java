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

import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class DrugOrderSupport implements GlobalPropertyListener {
	
	private static DrugOrderSupport singleton;
	
	private static Log log = LogFactory.getLog(DrugOrderSupport.class);
	
	List<RegimenSuggestion> standardRegimens;
	
	List<RegimenSuggestion> suggestedRegimens;
	
	private XStream xstream;
	
	private DrugOrderSupport() {
		if (singleton == null) {
			singleton = this;
			xstream = new XStream(new DomDriver());
			xstream.alias("regimenSuggestion", RegimenSuggestion.class);
			xstream.alias("drugSuggestion", DrugSuggestion.class);
		}
	}
	
	public static DrugOrderSupport getInstance() {
		synchronized (DrugOrderSupport.class) {
			if (singleton == null) {
				singleton = new DrugOrderSupport();
				Context.getAdministrationService().addGlobalPropertyListener(singleton);
			}
		}
		return singleton;
	}
	
	/**
	 * @return Returns the standardRegimens.
	 */
	public List<RegimenSuggestion> getStandardRegimens() {
		if (standardRegimens == null) {
			try {
				String xml = Context.getAdministrationService().getGlobalProperty(
				    OpenmrsConstants.GLOBAL_PROPERTY_STANDARD_DRUG_REGIMENS);
				setStandardRegimens(xml);
			}
			catch (Exception ex) {
				setStandardRegimens(new Vector<RegimenSuggestion>());
			}
		}
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
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#supportsPropertyName(java.lang.String)
	 */
	public boolean supportsPropertyName(String propertyName) {
		return OpenmrsConstants.GLOBAL_PROPERTY_STANDARD_DRUG_REGIMENS.equals(propertyName);
	}
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#globalPropertyChanged(org.openmrs.GlobalProperty)
	 */
	public void globalPropertyChanged(GlobalProperty newValue) {
		try {
			setStandardRegimens(newValue.getPropertyValue());
		}
		catch (Exception ex) {
			log.error("Error in new xml global property value", ex);
			setStandardRegimens(new Vector<RegimenSuggestion>());
		}
	}
	
	@SuppressWarnings("unchecked")
	private void setStandardRegimens(String xml) {
		List<RegimenSuggestion> list = (List<RegimenSuggestion>) xstream.fromXML(xml);
		setStandardRegimens(list);
	}
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#globalPropertyDeleted(java.lang.String)
	 */
	public void globalPropertyDeleted(String propertyName) {
		if (!OpenmrsConstants.GLOBAL_PROPERTY_STANDARD_DRUG_REGIMENS.equals(propertyName))
			return;
		setStandardRegimens(new Vector<RegimenSuggestion>());
	}
}
