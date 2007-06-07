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
