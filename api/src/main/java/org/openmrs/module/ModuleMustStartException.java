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

import org.openmrs.api.context.Context;

/**
 * This is throw at OpenMRS startup when one or more modules that really need to start didn't start.
 * This can happen with "mandatory" modules that must start once they are installed for the first
 * time and for "openmrs core" required modules that are set as core by the OpenMRS code base and
 * every installation needs it.
 * 
 * @see Context#startup(java.util.Properties)
 * @see MandatoryModuleException
 * @see OpenmrsCoreModuleException
 */
public abstract class ModuleMustStartException extends RuntimeException {
	
	private static final long serialVersionUID = -527601349350158268L;
	
	public ModuleMustStartException(String msg) {
		super(msg);
	};
}
