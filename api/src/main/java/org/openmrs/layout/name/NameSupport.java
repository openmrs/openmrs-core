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

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.openmrs.GlobalProperty;
import org.openmrs.api.APIException;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.context.Context;
import org.openmrs.layout.LayoutSupport;
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
		if (singleton == null) {
			throw new APIException("Not Yet Instantiated");
		} else {
			singleton.init();
			return singleton;
		}
	}

	/**
	 * Initializes layout templates with a custom template configured
	 * via the "layout.name.template" GP.
	 */
	private void init() {
		if (initialized) {
			return;
		}
		Context.getAdministrationService().addGlobalPropertyListener(singleton);
		// Get configured name template to override the existing one if any
		String layoutTemplateXml = Context.getAdministrationService().getGlobalProperty(
			OpenmrsConstants.GLOBAL_PROPERTY_LAYOUT_NAME_TEMPLATE);
		NameTemplate nameTemplate = deserializeXmlTemplate(layoutTemplateXml);
		
		if (nameTemplate != null) {
			updateLayoutTemplates(nameTemplate);
			initialized = true;
		}
	}
	
	/**
	 * Update existing layout templates if present with the provided template
	 */
	private void updateLayoutTemplates(NameTemplate nameTemplate) {
		if (getLayoutTemplates() == null) {
			setLayoutTemplates(new ArrayList<>());
		}
		List<NameTemplate> list = new ArrayList<>();
		// filter out unaffected templates to keep
		list.addAll(getLayoutTemplates().stream().filter(existingTemplate -> existingTemplate.getCodeName() != nameTemplate.getCodeName()).collect(Collectors.toList()));
		list.add(nameTemplate);
		setLayoutTemplates(list);
	}
	
	/**
	 * @return Returns the defaultLayoutFormat
	 */
	private NameTemplate deserializeXmlTemplate(String xml) {
		NameTemplate nameTemplate = null;
		if (StringUtils.isBlank(xml)) {
			return null;
		}
		try {
			nameTemplate = Context.getSerializationService().getDefaultSerializer().deserialize(xml,
				NameTemplate.class);
		} catch (Exception e) {
			log.error("Error in deserializing provided name template", e);
		}
		return nameTemplate;
	}

	/**
	 * @return Returns the defaultLayoutFormat
	 */
	@Override
	public String getDefaultLayoutFormat() {
		String ret = Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LAYOUT_NAME_FORMAT);
		return (ret != null && ret.length() > 0) ? ret : defaultLayoutFormat;
	}

	/**
	 * @see org.openmrs.api.GlobalPropertyListener#supportsPropertyName(String)
	 */
	@Override
	public boolean supportsPropertyName(String propertyName) {
		return OpenmrsConstants.GLOBAL_PROPERTY_LAYOUT_NAME_TEMPLATE.equals(propertyName);
	}

	/**
	 * @see org.openmrs.api.GlobalPropertyListener#globalPropertyChanged(org.openmrs.GlobalProperty)
	 */
	@Override
	public void globalPropertyChanged(GlobalProperty newValue) {
		if (!OpenmrsConstants.GLOBAL_PROPERTY_LAYOUT_NAME_TEMPLATE.equals(newValue.getPropertyValue())) {
			return;
		}
		NameTemplate nameTemplate = deserializeXmlTemplate(newValue.getPropertyValue());
		if (nameTemplate != null) {
			updateLayoutTemplates(nameTemplate);
		}	
	}

	/**
	 * @see org.openmrs.api.GlobalPropertyListener#globalPropertyDeleted(String)
	 */
	@Override
	public void globalPropertyDeleted(String propertyName) {

	}
}
