/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.validator;

import org.openmrs.EncounterRole;
import org.openmrs.annotation.Handler;

/**
 * Validator for {@link org.openmrs.EncounterRole} class
 *
 * @since 1.9
 */
@Handler(supports = { EncounterRole.class }, order = 50)
public class EncounterRoleValidator extends RequireNameValidator {

}
