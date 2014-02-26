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
package org.openmrs.layout.web.address;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.context.Context;
import org.openmrs.layout.web.LayoutSupport;
import org.openmrs.serialization.SerializationException;
import org.openmrs.util.OpenmrsConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class AddressSupport extends LayoutSupport<AddressTemplate> implements GlobalPropertyListener {
	
	private static AddressSupport singleton;
	
	private boolean initialized = false;
	
	static Log log = LogFactory.getLog(AddressSupport.class);
	
	private AddressSupport() {
		if (singleton == null) {
			singleton = this;
		}
		log.debug("Setting singleton: " + singleton);
	}
	
	public static AddressSupport getInstance() {
		synchronized (AddressSupport.class) {
			if (singleton == null) {
				singleton = new AddressSupport();
			}
		}
		singleton.init();
		return singleton;
		
	}
	
	private void init() {
		if (!initialized) {
			Context.getAdministrationService().addGlobalPropertyListener(singleton);
			
			String layoutTemplateXml = Context.getAdministrationService().getGlobalProperty(
			    OpenmrsConstants.GLOBAL_PROPERTY_ADDRESS_TEMPLATE);
			setAddressTemplate(layoutTemplateXml);
			
			List<String> specialTokens = new ArrayList<String>();
			specialTokens.add("address1");
			specialTokens.add("address2");
			specialTokens.add("address3");
			specialTokens.add("address4");
			specialTokens.add("address5");
			specialTokens.add("address6");
			specialTokens.add("cityVillage");
			specialTokens.add("countyDistrict");
			specialTokens.add("stateProvince");
			specialTokens.add("country");
			specialTokens.add("latitude");
			specialTokens.add("longitude");
			specialTokens.add("postalCode");
			specialTokens.add("startDate");
			specialTokens.add("endDate");
			
			setSpecialTokens(specialTokens);
			initialized = true;
		}
	}
	
	/**
	 * @return Returns the defaultLayoutFormat
	 */
	public String getDefaultLayoutFormat() {
		return defaultLayoutFormat;
	}
	
	/**
	 * @param addressTemplates The addressTemplates to set.
	 */
	public void setAddressTemplate(List<AddressTemplate> addressTemplates) {
		this.layoutTemplates = addressTemplates;
		setDefaultLayoutFormat(layoutTemplates.get(0).getCodeName());
		
	}
	
	/**
	 * @return Returns the addressTemplates.
	 */
	
	public List<AddressTemplate> getAddressTemplate() {
		if (layoutTemplates == null) {
			try {
				String xml = Context.getAdministrationService().getGlobalProperty(
				    OpenmrsConstants.GLOBAL_PROPERTY_ADDRESS_TEMPLATE);
				setAddressTemplate(xml);
			}
			catch (Exception ex) {
				//The old AddressTemplate prevails
			}
		}
		return layoutTemplates;
	}
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#supportsPropertyName(String)
	 */
	public boolean supportsPropertyName(String propertyName) {
		return OpenmrsConstants.GLOBAL_PROPERTY_ADDRESS_TEMPLATE.equals(propertyName);
	}
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#globalPropertyChanged(org.openmrs.GlobalProperty)
	 */
	public void globalPropertyChanged(GlobalProperty newValue) {
		if (!OpenmrsConstants.GLOBAL_PROPERTY_ADDRESS_TEMPLATE.equals(newValue.getProperty())) {
			return;
		}
		try {
			setAddressTemplate(newValue.getPropertyValue());
		}
		catch (Exception ex) {
			log.error("Error in new xml global property value", ex);
			setAddressTemplate(new Vector<AddressTemplate>());
		}
	}
	
	private void setAddressTemplate(String xml) {
		AddressTemplate addressTemplate;
		try {
			
			addressTemplate = Context.getSerializationService().getDefaultSerializer().deserialize(xml,
			    AddressTemplate.class);
		}
		catch (SerializationException e) {
			log.error("Error in deserializing address template", e);
			addressTemplate = new AddressTemplate("Error while deserializing address layout template.");
		}
		
		List<AddressTemplate> list = new ArrayList<AddressTemplate>();
		list.add(addressTemplate);
		setAddressTemplate(list);
	}
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#globalPropertyDeleted(String)
	 */
	@Override
	public void globalPropertyDeleted(String propertyName) {
		if (!OpenmrsConstants.GLOBAL_PROPERTY_ADDRESS_TEMPLATE.equals(propertyName)) {
			return;
		}
		setAddressTemplate(new Vector<AddressTemplate>());
	}
	
}
