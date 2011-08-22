package org.openmrs.attribute.handler;

import org.openmrs.BaseOpenmrsMetadata;

public class EnumeratedOpenmrsMetadata extends BaseOpenmrsMetadata {
	
	private Integer enumeratedObjectId;
	protected String enumeratedObjectMetadataHierarchy;
	
	@Override
	public Integer getId() {
		return enumeratedObjectId;
	}
	
	@Override
	public void setId(Integer id) {
		enumeratedObjectId = id;
	}
	
}
