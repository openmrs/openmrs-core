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
package org.openmrs.notification;


/**
 * Interface that defines the message preparator's functionality.
 * 
 * @author Justin Miranda
 */
public interface MessagePreparator {
	
	/**
	 * Prepare a message using a template.
	 * 
	 * @param template
	 * @return
	 */
	public Message prepare(Template template) throws MessageException;

} 