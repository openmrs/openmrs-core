package org.openmrs.attribute.handler;

import org.openmrs.attribute.InvalidAttributeValueException;

public class GenderAttributeHandler implements
		AttributeHandler<GenderAttributeHandler.Gender> {

	@Override
	public String getDatatypeHandled() {
		return "gender";
	}

	@Override
	public void setConfiguration(String handlerConfig) {
		
	}

	@Override
	public void validate(Gender typedValue)
			throws InvalidAttributeValueException {

	}

	@Override
	public String serialize(Object typedValue) {
		Gender g =(Gender) typedValue;
		return g.getValue();
	}

	@Override
	public Gender deserialize(String stringValue)
			throws InvalidAttributeValueException {
		return Gender.valueOf(stringValue);
	}
	
	public enum Gender implements StringEnum {
		
		MALE("Male"), FEMALE("Female");
		
		private final String value;

		private Gender(final String g) {
			value = g;
		}

		@Override
		public String getValue() {
			return value;
		}

	}

}
