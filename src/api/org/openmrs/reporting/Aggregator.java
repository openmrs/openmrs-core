package org.openmrs.reporting;

import java.util.Collection;
import java.util.Map;

/**
 * Support things like count(), min(), max(), avg(), etc.
 * @author djazayeri
 */
public interface Aggregator {

	public Map<Object, Object> aggregate(Map<Object, Collection> input);
	
}
