package org.openmrs.reporting;

/**
 * A grouping function such as count, min, max
 * @author djazayeri
 */
public interface DataItemGrouper {

	/**
	 * Groups a collection of data items into a scalar data item, in the context of a specific Patient
	 */
	Object group(org.openmrs.Patient p, java.util.Collection<Object> c);

}
