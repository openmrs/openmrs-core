/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.layout.name;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.GlobalProperty;
import org.openmrs.api.APIException;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.context.Context;
import org.openmrs.layout.LayoutSupport;
import org.openmrs.layout.address.AddressSupport;
import org.openmrs.serialization.SerializationException;
import org.openmrs.util.OpenmrsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 1.12
 */
public class NameSupport extends LayoutSupport<NameTemplate> implements GlobalPropertyListener {
	
	private static NameSupport singleton;
	private boolean initialized = false;
	private static final Logger log = LoggerFactory.getLogger(AddressSupport.class);
	
	public NameSupport() {
		if (singleton == null) {
			singleton = this;
		}
	}
	
	public static NameSupport getInstance() {
		synchronized (NameSupport.class) {
			if (singleton == null) {
				singleton = new NameSupport();
			}
		}
		singleton.init();
		return singleton;
	}

	private void init() {
		if (!initialized) {
			Context.getAdministrationService().addGlobalPropertyListener(singleton);
			String layoutTemplateXml = Context.getAdministrationService().getGlobalPropertyObject(
				OpenmrsConstants.GLOBAL_PROPERTY_LAYOUT_NAME_FORMAT).toString();
			setNameTemplate(layoutTemplateXml);

			List<String> specialTokens = new ArrayList<>();
			specialTokens.add("prefix");
			specialTokens.add("givenName");
			specialTokens.add("middleName");
			specialTokens.add("familyNamePrefix");
			specialTokens.add("familyName");
			specialTokens.add("familyName2");
			specialTokens.add("familyNameSuffix");
			specialTokens.add("degree");

			setSpecialTokens(specialTokens);
			initialized = true;
		}
	}
	
	public void setNameTemplate(List<NameTemplate> nameTemplates) {
		this.layoutTemplates = nameTemplates;
		setDefaultLayoutFormat(nameTemplates.get(0).getCodeName());
	}
	
	private void setNameTemplate(String xml) {
		NameTemplate nameTemplate;
		try {
			nameTemplate = Context.getSerializationService().getDefaultSerializer().deserialize(xml,
				NameTemplate.class);
		} catch (SerializationException e) {
			log.error("Error in deserializing name template", e);
			nameTemplate = new NameTemplate("Error while deserializing name layout template.");
		}
		
		List<NameTemplate> list = new ArrayList<NameTemplate>();
		list.add(nameTemplate);
		setNameTemplate(list);
	}
	
	public List<NameTemplate> getNameTemplate() {
		if (layoutTemplates == null) {
			try {
				String xml = Context.getAdministrationService().getGlobalProperty(
					OpenmrsConstants.GLOBAL_PROPERTY_LAYOUT_NAME_FORMAT);
				setNameTemplate(xml);
			} catch (Exception ignored) {
				//The old NameTemplate prevails
			}
		}
		return  layoutTemplates;
	}
	
	@Override
	public String getDefaultLayoutFormat() {
		return  defaultLayoutFormat;
	}

	@Override
	public boolean supportsPropertyName(String propertyName) {
		return OpenmrsConstants.GLOBAL_PROPERTY_LAYOUT_NAME_FORMAT.equals(propertyName);
	}

	@Override
	public void globalPropertyChanged(GlobalProperty newValue) {
		if (!OpenmrsConstants.GLOBAL_PROPERTY_LAYOUT_NAME_FORMAT.equals(newValue.getProperty())) {
			return;
		}
		try {
			setNameTemplate(newValue.getPropertyValue());
		} catch (Exception e) {
			log.error("Error in new xml global property value", e);
			setNameTemplate(new ArrayList<>());
		}
	}

	@Override
	public void globalPropertyDeleted(String propertyName) {
		if (!OpenmrsConstants.GLOBAL_PROPERTY_LAYOUT_NAME_FORMAT.equals(propertyName)) {
			return;
		}
		setNameTemplate(new ArrayList<>());
	}
}
