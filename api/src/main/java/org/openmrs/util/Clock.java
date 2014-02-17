/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.util;

import java.util.Date;

/**
 * Facilitates unit testing the application logic depending on current time.
 * Default implementation returning <code>new Date()</code> will be used in application code.
 * FakeClock or stubbed clock instance will be used in unit tests.
 */
public interface Clock {
	
	/**
	 * Get current time.
	 *
	 * @return <code>Date</code> representing the current time
	 */
	Date getCurrentTime();
}
