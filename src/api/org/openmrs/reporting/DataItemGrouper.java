package org.openmrs.reporting;

import java.util.Collection;

import org.openmrs.Patient;

/**
 * A grouping function such as count, min, max
 * @author djazayeri
 */
public abstract class DataItemGrouper implements DataItemTransformer {

	/**
	 * Groups a collection of data items into a scalar data item, in the context of a specific Patient
	 */
	public abstract Object group(Patient p, Collection<Object> c);

	public Object transform(Patient p, Object o) {
		return group(p, (Collection) o);
	}
	
}
