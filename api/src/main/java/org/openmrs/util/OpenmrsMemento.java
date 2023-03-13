/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

/**
 * This class is used to save the current state of an object/class. Before the the current
 * classloader is destroyed, the state of, for example, the scheduled items needs to be saved. Then,
 * after restoring the classloader and api, the state can be restored
 * 
 * @see OpenmrsClassLoader#destroyInstance()
 * @see OpenmrsClassLoader#saveState()
 * @see OpenmrsClassLoader#restoreState()
 */

public abstract class OpenmrsMemento {
	
	public OpenmrsMemento() {
	}
	
	public abstract Object getState();
	
	public abstract void setState(Object state);
}
