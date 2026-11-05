/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import org.openmrs.Obs;
import org.springframework.stereotype.Component;

/**
 * ImmutableEntityInterceptor for Obs, it rejects a flush that changes an Obs in anything other than
 * its void fields and groupMembers, whether or not the Obs is voided, which is why ObsService saves
 * an edit as a new Obs and voids the original. Only a voided Obs moves to another person in place,
 * to follow its encounter or in a patient merge.
 *
 * @see ImmutableEntityInterceptor
 * @since 2.0.0
 */
@Component("immutableObsInterceptor")
public class ImmutableObsInterceptor extends ImmutableEntityInterceptor {

	private static final String[] MUTABLE_PROPERTY_NAMES = new String[] { "voided", "dateVoided", "voidedBy", "voidReason",
	        "groupMembers" };

	/**
	 * @see ImmutableEntityInterceptor#getSupportedType()
	 */
	@Override
	protected Class<?> getSupportedType() {
		return Obs.class;
	}

	/**
	 * @see ImmutableEntityInterceptor#getMutablePropertyNames()
	 */
	@Override
	protected String[] getMutablePropertyNames() {
		return MUTABLE_PROPERTY_NAMES;
	}

	/**
	 * Lets a voided obs move to another person only to the patient of its own encounter, or in a
	 * patient merge, so a group member does not follow the encounter of its group. A live obs keeps its
	 * person, since the obs service saves a changed live obs as a new version. {@code personId} is the
	 * read-only copy of the person's id, so it is held to the same rule.
	 * <p>
	 * <strong>Should</strong> fail if an obs loses its person<br/>
	 * <strong>Should</strong> reject a person change on a live obs<br/>
	 * <strong>Should</strong> reject a person change on a group member without an encounter outside a
	 * patient merge<br/>
	 * <strong>Should</strong> hold a group member with an encounter to the patient of that encounter
	 *
	 * @see ImmutableEntityInterceptor#isAllowedChange(Object, String, Object, Object)
	 */
	@Override
	protected boolean isAllowedChange(Object entity, String property, Object previousValue, Object currentValue) {
		if (!"person".equals(property) && !"personId".equals(property)) {
			return false;
		}
		Obs obs = (Obs) entity;
		return obs.getVoided()
		        && isAllowedPersonChange(obs.getEncounter(), toPersonId(previousValue), toPersonId(currentValue));
	}

}
