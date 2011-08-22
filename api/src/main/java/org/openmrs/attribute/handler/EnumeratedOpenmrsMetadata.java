package org.openmrs.attribute.handler;

import org.openmrs.OpenmrsObject;

public interface EnumeratedOpenmrsMetadata extends OpenmrsObject {
	
	Integer getId();
	
	void setId(Integer id);
	
	String getDisplayName();
	
}
