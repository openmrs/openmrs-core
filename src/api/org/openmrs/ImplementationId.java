/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Every installation of OpenMRS should get a unique implementation id. If multiple sites use the
 * same dictionary/form setup, than those sites should share the same implementation id. The
 * ImplementationId is stored and verified on the openmrs servers.
 */
@Root
public class ImplementationId implements java.io.Serializable {
	
	public static final long serialVersionUID = 3752234110L;
	
	// Fields
	
	private String name;
	
	private String description;
	
	private String implementationId;
	
	private String passphrase;
	
	/**
	 * @return Returns the description.
	 */
	@Element(data = true)
	public String getDescription() {
		return description;
	}
	
	/**
	 * @param description The description to set.
	 */
	@Element(data = true)
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * The implementation id corresponds to the hl7Code of the ConceptSource that this corresponds
	 * to
	 * 
	 * @return the implementationId
	 */
	@Attribute
	public String getImplementationId() {
		return implementationId;
	}
	
	/**
	 * The implementation id corresponds to the hl7Code of the ConceptSource that this corresponds
	 * to
	 * 
	 * @param implementationId the implementationId to set
	 */
	@Attribute
	public void setImplementationId(String implementationId) {
		this.implementationId = implementationId;
	}
	
	/**
	 * @return the passphrase
	 */
	@Element(data = true, required = false)
	public String getPassphrase() {
		return passphrase;
	}
	
	/**
	 * @param passphrase the passphrase to set
	 */
	@Element(data = true, required = false)
	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}
	
	/**
	 * @return Returns the name.
	 */
	@Element(data = true)
	public String getName() {
		return name;
	}
	
	/**
	 * @param name The concept source name to set.
	 */
	@Element(data = true)
	public void setName(String name) {
		this.name = name;
	}
	
}
