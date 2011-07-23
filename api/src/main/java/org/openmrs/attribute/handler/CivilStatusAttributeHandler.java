package org.openmrs.attribute.handler;

import org.openmrs.attribute.InvalidAttributeValueException;

public class CivilStatusAttributeHandler implements
		AttributeHandler<CivilStatus> {

	@Override
	public String getDatatypeHandled() {
		return "civil status";
	}

	@Override
	public void setConfiguration(String handlerConfig) {
		
	}

	@Override
	public void validate(CivilStatus typedValue)
			throws InvalidAttributeValueException {

	}

	@Override
	public String serialize(Object typedValue) {
		CivilStatus cs =(CivilStatus) typedValue;
		return cs.getValue();
	}

	@Override
	public CivilStatus deserialize(String stringValue)
			throws InvalidAttributeValueException {
		return CivilStatus.valueOf(stringValue);
	}

}
