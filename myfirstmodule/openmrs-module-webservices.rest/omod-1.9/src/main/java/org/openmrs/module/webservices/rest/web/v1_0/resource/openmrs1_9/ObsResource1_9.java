/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.ObsResource1_8;

import java.util.Map;

/**
 * {@link org.openmrs.module.webservices.rest.web.annotation.Resource} for Obs, supporting standard
 * CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/obs", order = 2, supportedClass = Obs.class, supportedOpenmrsVersions = {
        "1.9.* - 1.10.*" })
public class ObsResource1_9 extends ObsResource1_8 {

	/**
	 * Annotated setter for Concept
	 *
	 * @param obs
	 * @param value
	 */
	@PropertySetter("concept")
	public static void setConcept(Obs obs, Object value) {
		Object identifier = null;
		if (value instanceof Map) {
			Object uuid = ((Map) value).get(RestConstants.PROPERTY_UUID);
			if (uuid != null) {
				identifier = uuid;
			}
		}

		if (identifier == null) {
			identifier = value;
		}

		obs.setConcept(ConversionUtil.getConverter(Concept.class).getByUniqueId((String) identifier));
	}


    @Override
    protected SimpleObject convertDelegateToRepresentation(Obs delegate, DelegatingResourceDescription rep) {
        try {
            return super.convertDelegateToRepresentation(delegate, rep);
        } catch (ConversionException e) {
            // hack to handle https://issues.openmrs.org/browse/RESTWS-816
            // (if converting to a custom rep fails because an obs is valueDrug or valueLocation, return null for the value of that obs instead of failing hard)
            if (delegate.getValueDrug() != null || "org.openmrs.Location".equals(delegate.getComment())) {
                rep.removeProperty("value");
                SimpleObject result = super.convertDelegateToRepresentation(delegate, rep);
                result.put("value", null);
                return result;
            }
            else {
                throw e;
            }
        }
    }

}
