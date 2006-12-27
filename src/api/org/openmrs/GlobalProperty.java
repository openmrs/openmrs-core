package org.openmrs;

public class GlobalProperty {
	private String property = "";
	private String propertyValue = "";
	
	
	/**
	 * Default empty constructor
	 *
	 */
	public GlobalProperty() {	}
	
	/**
	 * 
	 * @param property
	 * @param value
	 */
	public GlobalProperty(String property, String value) {
		this.property = property;
		this.propertyValue = value;
	}
	
	/**
	 * @return Returns the property.
	 */
	public String getProperty() {
		return property;
	}
	/**
	 * @param property The property to set.
	 */
	public void setProperty(String property) {
		this.property = property;
	}
	/**
	 * @return Returns the propertyValue.
	 */
	public String getPropertyValue() {
		return propertyValue;
	}
	/**
	 * @param propertyValue The propertyValue to set.
	 */
	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}
	
	public boolean equals(Object o) {
		if (o instanceof GlobalProperty) {
			GlobalProperty gp = (GlobalProperty)o;
			return (property != null && property.equals(gp.getProperty()));
			/*
			if (property != null && gp.getProperty() != null) {
				String lowerProperty = property.toLowerCase();
				String lowerOtherProperty = gp.getProperty().toLowerCase(); 
				return lowerProperty.equals(lowerOtherProperty);
			}
			*/
		}
		
		return false;
	}
	
	public int hashCode() {
		if (this.property == null) return super.hashCode();
		
		int hash = 5 * this.property.hashCode();
		
		return hash; 
	}
}
