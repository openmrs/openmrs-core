/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.reporting;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
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
	 * @param type The <code>String</code> name of the type to set.
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
