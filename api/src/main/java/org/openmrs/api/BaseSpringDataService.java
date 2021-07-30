package org.openmrs.api;

public interface BaseSpringDataService<T> {
	/**
	 * Set the given <code>jpadao</code> on this generic interface. The generic interface injects 
	 * any type of spring data based dao
	 *
	 * @param jpadao
	 */
	public void setSpringDataJpaDAO(T jpadao);
}
