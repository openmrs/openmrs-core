package org.openmrs.attribute.handler;

import org.openmrs.attribute.InvalidAttributeValueException;

public class CivilStatusAttributeHandler implements
		AttributeHandler<CivilStatusAttributeHandler.CivilStatus> {

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
	
	public enum CivilStatus implements StringEnum {
		
		CIVIL_STATUS_SINGLE("Single"), CIVIL_STATUS_MARRIED("Married"), CIVIL_STATUS_DIVORCED(
				"Divorced"), CIVIL_STATUS_WIDOWED("Widowed"), CIVIL_STATUS_DOMESTIC_PARTNERSHIP("Domestic partnership"), CIVIL_STATUS_COHABITING("Cohabiting"), CIVIL_STATUS_CIVIL_UNION("Civil union"), CIVIL_STATUS_UNMARRIED_PARTNERS("Unmarried partners");

		private final String value;

		private CivilStatus(final String cs) {
			value = cs;
		}

		@Override
		public String getValue() {
			return value;
		}

	}

}
