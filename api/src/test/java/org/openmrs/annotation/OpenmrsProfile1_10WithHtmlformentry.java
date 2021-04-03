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
 * Test bean which should be only loaded when running on OpenMRS 1.10 and later with the htmlformentry module
 */
@Component
@OpenmrsProfile(openmrsPlatformVersion = "1.10", modules = { "htmlformentry:2.3" })
public class OpenmrsProfile1_10WithHtmlformentry {}
