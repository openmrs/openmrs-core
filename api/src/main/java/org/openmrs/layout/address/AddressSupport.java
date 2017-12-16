/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.layout.address;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.GlobalProperty;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.context.Context;
import org.openmrs.layout.LayoutSupport;
import org.openmrs.serialization.SerializationException;
import org.openmrs.util.OpenmrsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 1.12
 */
public class AddressSupport extends LayoutSupport<AddressTemplate> implements GlobalPropertyListener {
	
	private static AddressSupport singleton;
	
	private boolean initialized = false;
	
	static Logger log = LoggerFactory.getLogger(AddressSupport.class);
	
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
			
			List<String> specialTokens = new ArrayList<>();
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
	@Override
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
	@Override
	public boolean supportsPropertyName(String propertyName) {
		return OpenmrsConstants.GLOBAL_PROPERTY_ADDRESS_TEMPLATE.equals(propertyName);
	}
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#globalPropertyChanged(org.openmrs.GlobalProperty)
	 */
	@Override
	public void globalPropertyChanged(GlobalProperty newValue) {
		if (!OpenmrsConstants.GLOBAL_PROPERTY_ADDRESS_TEMPLATE.equals(newValue.getProperty())) {
			return;
		}
		try {
			setAddressTemplate(newValue.getPropertyValue());
		}
		catch (Exception ex) {
			log.error("Error in new xml global property value", ex);
			setAddressTemplate(new ArrayList<>());
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
		
		List<AddressTemplate> list = new ArrayList<>();
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
		setAddressTemplate(new ArrayList<>());
	}
	
}
