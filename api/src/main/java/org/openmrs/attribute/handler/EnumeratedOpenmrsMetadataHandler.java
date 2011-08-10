package org.openmrs.attribute.handler;

import java.util.ArrayList;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.attribute.InvalidAttributeValueException;

public class EnumeratedOpenmrsMetadataHandler implements
		AttributeHandler<EnumeratedOpenmrsMetadata> {
	private ArrayList<EnumeratedOpenmrsMetadata> values;
	
	public EnumeratedOpenmrsMetadataHandler() {
		values = new ArrayList<EnumeratedOpenmrsMetadata>();
	}
	
	@Override
	public String getDatatypeHandled() {
		return "enumerated-metadata";
	}

	@Override
	public void setConfiguration(String handlerConfig) {
		
	}

	@Override
	public void validate(EnumeratedOpenmrsMetadata typedValue)
			throws InvalidAttributeValueException {
		if (CollectionUtils.isNotEmpty(values) && !values.contains(typedValue))
			throw new InvalidAttributeValueException("Atribute is not in the available value list.");
	}

	@Override
	public String serialize(Object typedValue) {
		EnumeratedOpenmrsMetadata asEnumeratedMetadata = (EnumeratedOpenmrsMetadata) typedValue;
		validate(asEnumeratedMetadata);
		return asEnumeratedMetadata.getUuid();
	}

	@Override
	public EnumeratedOpenmrsMetadata deserialize(String stringValue)
			throws InvalidAttributeValueException {
		EnumeratedOpenmrsMetadata enumMetadata = new EnumeratedOpenmrsMetadata();
		enumMetadata.setUuid(stringValue);
		return enumMetadata;
	}

	
}
