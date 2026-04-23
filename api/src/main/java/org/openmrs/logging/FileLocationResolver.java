/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.logging;

import org.apache.logging.log4j.core.Appender;

/**
 * Resolves the file name (path) from a Log4j2 file appender. Implementations adapt specific
 * appender types (e.g. RollingFileAppender, FileAppender) so callers can obtain the log file
 * location without branching on appender type.
 *
 * @since 2.4.4, 2.5.1, 2.6.0
 */
public interface FileLocationResolver {

	/**
	 * Whether this resolver can extract a file name from the given appender.
	 *
	 * @param appender the Log4j2 appender (may be any file-oriented type)
	 * @return true if {@link #getFileName(Appender)} can return a non-null path for this appender
	 */
	boolean supports(Appender appender);

	/**
	 * Returns the file name (path) used by the appender, or null if not applicable.
	 * Should only be called when {@link #supports(Appender)} returns true for the same appender.
	 *
	 * @param appender the Log4j2 appender
	 * @return the log file path, or null
	 */
	String getFileName(Appender appender);
}
