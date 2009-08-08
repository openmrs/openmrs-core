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
package org.openmrs.web.dwr;

/**
 * If a dwr class throws this error, the user will see a popup with the text of the error message.
 * All other exceptions thrown will only produce a small notice in the status bar of the browser.
 */
public class DWRException extends Exception {
	
	private static final long serialVersionUID = 6820574097052501261L;
	
	/**
	 * @see java.lang.Exception#Exception(java.lang.String)
	 */
	public DWRException(String message) {
		super(message);
	}
}
