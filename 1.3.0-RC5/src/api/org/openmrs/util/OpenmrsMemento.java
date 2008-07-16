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

/**
 * This class is used to save the current state of an object/class.
 * 
 * Before the the current classloader is destroyed, the state of, for example,
 * the scheduled items needs to be saved.  Then, after restoring the classloader
 * and api, the state can be restored
 * 
 * @see OpenmrsClassLoader.destroyInstance()
 * @see OpenmrsClassLoader.saveState()
 * @see OpenmrsClassLoader.restoreState()
 * 
 * @author bwolfe
 *
 */

public abstract class OpenmrsMemento {

	public OpenmrsMemento() {};
	
	public abstract Object getState();
	
	public abstract void setState(Object state);
}
