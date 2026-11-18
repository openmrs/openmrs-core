/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.annotation;

/**
 * Test bean with a matching (unconstrained) @OpenmrsProfile, used to verify self-enforcing behavior
 * under a module-style scan without OpenMRS TypeFilters.
 */
@OpenmrsProfile
public class OpenmrsProfileModuleScanMatchingBean {}
