/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

/**
 * In OpenMRS, the convention is to track basic audit information for each object related to who
 * initially created the object and when, and who last changed the object and when. This allows us
 * to check, for example, when a patient record was created, or when a person address was last
 * updated in the system. Any object that needs to keep track of this information should implement
 * this interface.
 * 
 * @since 1.5
 */
public interface Auditable extends Creatable, Changeable {

}
