package org.openmrs.attribute.handler;

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
