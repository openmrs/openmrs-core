package org.openmrs.attribute.handler;

public enum Race implements StringEnum{
	RACE_CAUASOID("Cauasoid"), RACE_NEGROID("Negroid"), RACE_MONGOLOID("Mongoloid"), RACE_AUSTRALOID("Australoid"); 
	private final String value;

	private Race(final String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
