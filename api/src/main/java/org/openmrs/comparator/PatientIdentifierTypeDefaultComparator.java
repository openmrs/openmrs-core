/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.comparator;

import java.io.Serializable;
import java.util.Comparator;

import org.openmrs.PatientIdentifierType;

/**
 * Orders {@link PatientIdentifierType} by retired (true last), required (true first), name and id.
 *
 * @since 1.9.2, 1.8.5
 */
public class PatientIdentifierTypeDefaultComparator implements Comparator<PatientIdentifierType>, Serializable {

	private static final long serialVersionUID = 1L;

	private static final Comparator<PatientIdentifierType> ORDER = Comparator
	        .comparing(PatientIdentifierType::getRetired, Comparator.nullsFirst(Comparator.<Boolean> naturalOrder()))
	        .thenComparing(PatientIdentifierType::getRequired, Comparator.nullsLast(Comparator.<Boolean> reverseOrder()))
	        .thenComparing(pit -> pit.getName() != null ? pit.getName().toLowerCase() : null,
	            Comparator.nullsLast(Comparator.<String> naturalOrder()))
	        .thenComparing(PatientIdentifierType::getPatientIdentifierTypeId,
	            Comparator.nullsLast(Comparator.<Integer> naturalOrder()));

	/**
	 * Orders by retired (true last), required (true first), name and id.
	 * <p>
	 * <strong>Should</strong> order properly
	 */
	@Override
	public int compare(PatientIdentifierType pit1, PatientIdentifierType pit2) {
		return ORDER.compare(pit1, pit2);
	}
}
