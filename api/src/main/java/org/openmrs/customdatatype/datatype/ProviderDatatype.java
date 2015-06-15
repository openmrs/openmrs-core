/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.customdatatype.datatype;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.CustomDatatype.Summary;
import org.openmrs.customdatatype.SerializingCustomDatatype;
import org.springframework.stereotype.Component;
/**
 * Datatype for a Provider, represented by {@link org.openmrs.Provider}
 * @since 1.12
 *
 */
@Component
public class ProviderDatatype extends SerializingCustomDatatype<Provider> {
	
	/**
	 * @see org.openmrs.customdatatype.SerializingCustomDatatype#serialize(java.lang.Object)
	 * @should return a provider uuid during serialization
	 */
	@Override
	public String serialize(Provider typedValue) {
		if (typedValue == null) {
			return null;
		}
		return typedValue.getUuid().toString();
		
	}
	
	/**
	 * @see  org.openmrs.customdatatype.SerializingCustomDatatype#serialize(java.lang.Object)
	 * @should	construct a Provider(org.openmrs.Provider) serialized by this handler
	 */
	@Override
	public Provider deserialize(String serializedValue) {
		if (StringUtils.isEmpty(serializedValue))
			return null;
		Provider provider = Context.getProviderService().getProviderByUuid(serializedValue);
		return provider;
	}
	
	@Override
	public Summary getTextSummary(String referenceString) {
		Provider provider = Context.getProviderService().getProviderByUuid(referenceString);
		return doGetTextSummary(provider);
	}
	
	@Override
	public Summary doGetTextSummary(Provider typedValue) {
		if (typedValue == null)
			return null;
		return new CustomDatatype.Summary(typedValue.getName(), false);
	}
	
}
