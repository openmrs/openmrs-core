package org.openmrs.module;

/**
 * This interface must be implemented by modules and referenced
 * by the Module-Activator property in the module manifest
 * @version 1.0
 */
public interface Activator {

	/**
	 * Called on module start/load
	 */
	public void startup();

	/**
	 * Called on module end
	 */
	public void shutdown();

}
