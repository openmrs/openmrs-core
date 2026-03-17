/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.logging.resolver;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.RandomAccessFileAppender;
import org.openmrs.logging.FileLocationResolver;

/**
 * Resolves file location from a Log4j2 RandomAccessFileAppender.
 */
public class RandomAccessFileAppenderResolver implements FileLocationResolver {

	@Override
	public boolean supports(Appender appender) {
		return appender instanceof RandomAccessFileAppender;
	}

	@Override
	public String getFileName(Appender appender) {
		return ((RandomAccessFileAppender) appender).getFileName();
	}
}
