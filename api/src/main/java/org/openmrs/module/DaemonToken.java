/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module;

import java.io.Serializable;

import org.openmrs.api.context.Daemon;

/**
 * Required to run code with elevated privileges in
 * {@link Daemon#runInDaemonThreadAndWait(Runnable, DaemonToken)}.
 * 
 * @since 1.9.2
 */
public class DaemonToken implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final String id;
	
	public DaemonToken(String id) {
		this.id = id;
	}
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
}
