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
				String xml = Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_STANDARD_DRUG_REGIMENS);
				setStandardRegimens(xml);
			} catch (Exception ex) {
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
	    } catch (Exception ex) {
	    	log.error("Error in new xml global property value", ex);
	    	setStandardRegimens(new Vector<RegimenSuggestion>());
	    }
    }
    
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
