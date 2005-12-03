/**
 * 
 */
package org.openmrs.reporting;

import java.util.*;

/**
 * @author djazayeri
 * Runs through an ordered list of PatientFilters, doing AND or OR as specified. You can make
 * arbitrarily-grouped expressions of filters by composing multiple <code>PatientFilterChain</code>s together
 */
public class PatientFilterChain implements PatientFilter {

	public enum CombineMethod { INTERSECTION, UNION }
	
	private Collection<PatientFilter> filters;
	private CombineMethod combineHow;
		
	/**
	 * 
	 * @param filters	All filters to be run on the PatientSet
	 * @param combineHow	Whether to return the union or intersection of all the filters.  
	 */
	public PatientFilterChain(Collection<PatientFilter> filters, CombineMethod combineHow) {
		this.filters = filters;
		this.combineHow = combineHow;
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.reporting.PatientFilter#filter(org.openmrs.reporting.PatientSet)
	 */
	public PatientSet filter(PatientSet input) {
		if (combineHow == CombineMethod.INTERSECTION) {
			PatientSet temp = input;
			for (Iterator<PatientFilter> i = filters.iterator(); i.hasNext(); ) {
				temp = i.next().filter(temp);
			}
			return temp;
		} else {
			PatientSet temp = new PatientSet();
			for (Iterator<PatientFilter> i = filters.iterator(); i.hasNext(); ) {
				temp.addAll(i.next().filter(input));
			}
			return temp;
		}
	}

}
