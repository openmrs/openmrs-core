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

import org.springframework.stereotype.Component;

/**
 * Test bean which should be only loaded when running on OpenMRS from 1.6 to 1.8.
 */
@Component
@OpenmrsProfile(openmrsPlatformVersion = "[1.6.* - 1.8.*]")
public class OpenmrsProfile1_6To1_8 {}
