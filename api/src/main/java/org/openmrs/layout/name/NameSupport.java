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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.GlobalProperty;
import org.openmrs.api.APIException;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.context.Context;
import org.openmrs.layout.LayoutSupport;
import org.openmrs.serialization.SerializationException;
import org.openmrs.util.OpenmrsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @since 1.12
 */
public class NameSupport extends LayoutSupport<NameTemplate> implements GlobalPropertyListener {

	private static final Logger log = LoggerFactory.getLogger(NameSupport.class);
	private static NameSupport singleton;
	private boolean initialized = false;
	private boolean customTemplateConfigured = false;
	
	public NameSupport() {
		if (singleton == null) {
			singleton = this;
		}
	}
	
	public static NameSupport getInstance() {
		synchronized (NameSupport.class) {
			singleton.init();
			if (singleton.customTemplateConfigured) {
				return singleton;
			} else {
				new NameSupport();
			}
			
			if (singleton == null) {
				throw new APIException("Not Yet Instantiated");
			} else {
				return singleton;
			}
		}
	}

	private void init() {
		if (!initialized) {
			Context.getAdministrationService().addGlobalPropertyListener(singleton);
			try {
				// Get layout NameTemplate format to override the default if any
				String layoutTemplateXml = Context.getAdministrationService().getGlobalProperty(
					OpenmrsConstants.GLOBAL_PROPERTY_LAYOUT_NAME_TEMPLATE);
				
				if (StringUtils.isNotBlank(layoutTemplateXml)) {
					// Validating... Custom NameTemplate should be in a Valid XML format
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder builder = dbf.newDocumentBuilder();
					Document doc = builder.parse(new ByteArrayInputStream(layoutTemplateXml.getBytes(StandardCharsets.UTF_8)));
					
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
					customTemplateConfigured = true;
				}
			} catch (ParserConfigurationException | IOException | SAXException e) {
				log.error("Custom NameTemplate should be in a Valid XML format", e);
			} catch (NullPointerException ignored) {}
			initialized = true;
		}
	}

	/**
	 * @param nameTemplates The nameTemplates to set.
	 */
	public void setNameTemplate(List<NameTemplate> nameTemplates) {
		this.layoutTemplates = nameTemplates;
	}
	
	private void setNameTemplate(String xml) {
		NameTemplate nameTemplate = new NameTemplate();
		try {
			nameTemplate = Context.getSerializationService().getDefaultSerializer().deserialize(xml,
				NameTemplate.class);
		} catch (SerializationException | NullPointerException e) {
			log.error("Error in deserializing provided name template", e);
		}
		
		List<NameTemplate> list = new ArrayList<NameTemplate>();
		list.add(nameTemplate);
		setNameTemplate(list);
	}

	/**
	 * @return Returns the nameTemplates.
	 */
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

	/**
	 * @return Returns the defaultLayoutFormat
	 */
	@Override
	public String getDefaultLayoutFormat() {
		try {
			String xml;
			xml = Context.getAdministrationService().getGlobalProperty("layout.name.template");
			if (StringUtils.isBlank(xml)) {
				xml = Context.getAdministrationService().getGlobalProperty("layout.name.format");
				return xml != null && xml.length() > 0 ? xml : this.defaultLayoutFormat;
			}
			setNameTemplate(xml);
		} catch (NullPointerException ignored) {}
		return (this.layoutTemplates != null && this.layoutTemplates.size() > 0) ? this.layoutTemplates.get(0).getCodeName() : defaultLayoutFormat;
	}

	/**
	 * @see org.openmrs.api.GlobalPropertyListener#supportsPropertyName(String)
	 */
	@Override
	public boolean supportsPropertyName(String propertyName) {
		return OpenmrsConstants.GLOBAL_PROPERTY_LAYOUT_NAME_FORMAT.equals(propertyName);
	}

	/**
	 * @see org.openmrs.api.GlobalPropertyListener#globalPropertyChanged(org.openmrs.GlobalProperty)
	 */
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

	/**
	 * @see org.openmrs.api.GlobalPropertyListener#globalPropertyDeleted(String)
	 */
	@Override
	public void globalPropertyDeleted(String propertyName) {
		if (!OpenmrsConstants.GLOBAL_PROPERTY_LAYOUT_NAME_FORMAT.equals(propertyName)) {
			return;
		}
		setNameTemplate(new ArrayList<>());
	}
}
