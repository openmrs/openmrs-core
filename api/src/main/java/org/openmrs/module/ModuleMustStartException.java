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
	}
}
