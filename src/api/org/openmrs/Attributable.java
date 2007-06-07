package org.openmrs;

import java.util.List;

/**
 * This class allows for a 
 * @author bwolfe
 *
 */
public interface Attributable<E> {
	
	public E hydrate(String s);
	
	public String serialize();
	
	public List<E> getPossibleValues();
	
	public List<E> findPossibleValues(String searchText);
	
}
