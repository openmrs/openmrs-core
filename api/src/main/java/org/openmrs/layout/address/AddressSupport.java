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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.openmrs.GlobalProperty;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.layout.LayoutSupport;
import org.openmrs.serialization.SerializationException;
import org.openmrs.util.OpenmrsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 1.12
 */
public class AddressSupport extends LayoutSupport<AddressTemplate> implements org.openmrs.api.GlobalPropertyListener {

    private static AddressSupport singleton;

    private volatile boolean initialized = false;

    private static final Logger log = LoggerFactory.getLogger(AddressSupport.class);

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

    private synchronized void init() {
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
        if (layoutTemplates == null || layoutTemplates.isEmpty() || layoutTemplates.get(0) == null) {
            setDefaultLayoutFormat(null);
        } else {
            setDefaultLayoutFormat(layoutTemplates.get(0).getCodeName());
        }

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
            } catch (Exception ex) {
                //The old AddressTemplate prevails
                log.debug("Error while attempting to load address template on first access", ex);
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
     * @see
     * org.openmrs.api.GlobalPropertyListener#globalPropertyChanged(org.openmrs.GlobalProperty)
     */
    @Override
    public void globalPropertyChanged(GlobalProperty newValue) {
        if (!OpenmrsConstants.GLOBAL_PROPERTY_ADDRESS_TEMPLATE.equals(newValue.getProperty())) {
            return;
        }
        try {
            setAddressTemplate(newValue.getPropertyValue());
        } catch (Exception ex) {
            log.error("Error in new xml global property value", ex);
            setAddressTemplate(new ArrayList<>());
        }
    }

    /**
     * Centralized deserialization using multiple fallbacks:
     * - try raw xml
     * - try unescapeXml(xml)
     * - try unescapeHtml4(xml)
     *
     * This method throws SerializationException or APIException to the caller.
     * It's package-visible to make it testable.
     */
    AddressTemplate deserializeAddressTemplate(String xml) throws SerializationException, APIException {
        if (StringUtils.isBlank(xml)) {
            xml = OpenmrsConstants.DEFAULT_ADDRESS_TEMPLATE;
        }

        // attempt 1: raw xml
        try {
            return Context.getSerializationService()
                    .getDefaultSerializer()
                    .deserialize(xml, AddressTemplate.class);
        } catch (SerializationException | APIException rawEx) {
            log.debug("Raw deserialize failed, will try unescapeXml()", rawEx);

            // attempt 2: unescape XML entities
            try {
                String unescapedXml = StringEscapeUtils.unescapeXml(xml);
                return Context.getSerializationService()
                        .getDefaultSerializer()
                        .deserialize(unescapedXml, AddressTemplate.class);
            } catch (SerializationException | APIException unescapeXmlEx) {
                log.debug("unescapeXml() deserialize failed, will try unescapeHtml4()", unescapeXmlEx);

                // attempt 3: unescape HTML entities
                String unescapedHtml = StringEscapeUtils.unescapeHtml4(xml);
                return Context.getSerializationService()
                        .getDefaultSerializer()
                        .deserialize(unescapedHtml, AddressTemplate.class);
            }
        }
    }

    private void setAddressTemplate(String xml) {
        AddressTemplate addressTemplate;

        try {
            addressTemplate = deserializeAddressTemplate(xml);
        } catch (SerializationException | APIException e) {
            // avoid writing the entire GP into logs; truncate to first 200 chars to help debugging
            final String truncated = xml == null ? "null" : (xml.length() > 200 ? xml.substring(0, 200) + "..." : xml);
            log.error("Error in deserializing address template (truncated original content: {}).", truncated, e);
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
