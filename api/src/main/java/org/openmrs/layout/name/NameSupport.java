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
public class NameSupport extends LayoutSupport<NameTemplate> implements GlobalPropertyListener {

	private static final Logger log = LoggerFactory.getLogger(NameSupport.class);
	private static NameSupport singleton;
	private boolean initialized = false;
	
	public NameSupport() {
		if (singleton == null) {
			singleton = this;
		}
	}
	
	public static NameSupport getInstance() {
		synchronized (NameSupport.class) {
			if (singleton == null) {
				new NameSupport();
			}
		}
		singleton.init();
		return singleton;
	}

	private void init() {
		if (!initialized) {
			try {
				Context.getAdministrationService().addGlobalPropertyListener(singleton);
				// Get layout NameTemplate format to override the default if any
				String layoutTemplateXml = Context.getAdministrationService().getGlobalProperty(
					OpenmrsConstants.GLOBAL_PROPERTY_LAYOUT_NAME_FORMAT);
				setNameTemplate(layoutTemplateXml);
			} catch (NullPointerException ignored) {}

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
	}
	
	private void setNameTemplate(String xml) {
		NameTemplate nameTemplate = new NameTemplate();
		try {
			nameTemplate = Context.getSerializationService().getDefaultSerializer().deserialize(xml,
				NameTemplate.class);
		} catch (SerializationException e) {
			log.error("Error in deserializing provided name template, loading default template", e);
			try {
				nameTemplate = Context.getSerializationService().getDefaultSerializer().deserialize(OpenmrsConstants.DEFAULT_NAME_TEMPLATE,
					NameTemplate.class);
			} catch (SerializationException ignored) {}
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
		String ret = Context.getAdministrationService().getGlobalProperty("layout.name.format");
		return (ret != null && ret.length() > 0) ? ret : defaultLayoutFormat;
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
