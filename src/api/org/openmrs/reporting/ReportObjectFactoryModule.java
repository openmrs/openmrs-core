package org.openmrs.reporting;

public class ReportObjectFactoryModule {
	private String name;
	private String displayName;
	private String className;
	private String validatorClass;
	private String type;
	
	/**
	 * @return Returns the className.
	 */
	public String getClassName() {
		return className;
	}
	/**
	 * @param className The className to set.
	 */
	public void setClassName(String className) {
		this.className = className;
	}
	/**
	 * @return Returns the displayName.
	 */
	public String getDisplayName() {
		return displayName;
	}
	/**
	 * @param displayName The displayName to set.
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return Returns the typeName.
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param typeName The typeName to set.
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return Returns the validatorClass.
	 */
	public String getValidatorClass() {
		return validatorClass;
	}
	/**
	 * @param validatorClass The validatorClass to set.
	 */
	public void setValidatorClass(String validatorClass) {
		this.validatorClass = validatorClass;
	}
	
	
}
