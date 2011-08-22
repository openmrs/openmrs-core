package org.openmrs.attribute.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.attribute.InvalidAttributeValueException;

public class EnumeratedOpenmrsMetadataAttributeHandler implements AttributeHandler<EnumeratedOpenmrsMetadata> {
	
	protected String hierarchy;
	
	protected String allowedValues;
	
	@Override
	public String getDatatypeHandled() {
		return "enumerated-metadata";
	}
	
	@Override
	public void setConfiguration(String handlerConfig) {
		if (handlerConfig == null) {
			throw new InvalidAttributeValueException("Configuration string is mandatory");
		}
		String[] args = handlerConfig.split(":");
		String[] ids = args[1].split(",");
		StringBuilder sb = new StringBuilder();
		for (String itr : ids) {
			sb.append("," + itr + ",");
		}
		allowedValues = sb.toString();
		if (handlerConfig.startsWith("LocationId:")) {
			hierarchy = "Location";
		} else if (handlerConfig.startsWith("ConceptId:")) {
			hierarchy = "Concept";
		}
	}
	
	@Override
	public void validate(EnumeratedOpenmrsMetadata typedValue) throws InvalidAttributeValueException {
		//if (CollectionUtils.isNotEmpty(values) && !values.contains(typedValue))
		//	throw new InvalidAttributeValueException("Atribute is not in the available value list.");
	}
	
	@Override
	public String serialize(Object typedValue) {
		EnumeratedOpenmrsMetadata asEnumeratedMetadata = (EnumeratedOpenmrsMetadata) typedValue;
		validate(asEnumeratedMetadata);
		return asEnumeratedMetadata.getUuid();
	}
	
	@Override
	public EnumeratedOpenmrsMetadata deserialize(String stringValue) throws InvalidAttributeValueException {
		if ("Location".equals(hierarchy)) {
			return Context.getLocationService().getLocation(Integer.valueOf(stringValue));
		} else if ("Concept".equals(hierarchy)) {
			return Context.getConceptService().getConcept(Integer.valueOf(stringValue));
		}
		throw new RuntimeException(hierarchy);
	}
	
}
