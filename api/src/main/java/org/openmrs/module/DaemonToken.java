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
