/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

/**
 * Every installation of OpenMRS should get a unique implementation id. If multiple sites use the
 * same dictionary/form setup, than those sites should share the same implementation id. The
 * ImplementationId is stored and verified on the openmrs servers.
 */
public class ImplementationId implements java.io.Serializable {
	
	public static final long serialVersionUID = 3752234110L;
	
	// Fields
	
	private String name;
	
	private String description;
	
	private String implementationId;
	
	private String passphrase;
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof ImplementationId) {
			ImplementationId other = (ImplementationId) o;
			
			if (getImplementationId() != null && other.getImplementationId() != null) {
				return getImplementationId().equals(other.getImplementationId());
			}
			
			return this == other;
		}
		return false;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (getImplementationId() != null) {
			return getImplementationId().hashCode() * 342 + 3;
		}
		
		return super.hashCode();
	}
	
	/**
	 * Text describing this implementation. (e.g. Source for the AMPATH program in Kenya. Created by
	 * Paul Biondich)
	 *
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Text describing this implementation. (e.g. Source for the AMPATH program in Kenya. Created by
	 * Paul Biondich)
	 *
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * This is the unique id for this implementation. <br>
	 * <br>
	 * The implementation id corresponds to the hl7Code of the ConceptSource that this corresponds
	 * to.<br>
	 * <br>
	 * Must be limited to 20 characters and numbers. The characters "^" and "|" are not allowed.
	 *
	 * @return the implementationId
	 */
	public String getImplementationId() {
		return implementationId;
	}
	
	/**
	 * This is the unique id for this implementation. <br>
	 * <br>
	 * The implementation id corresponds to the hl7Code of the ConceptSource that this corresponds
	 * to. <br>
	 * <br>
	 * Must be limited to 20 characters and numbers. The characters "^" and "|" are not allowed.
	 *
	 * @param implementationId the implementationId to set
	 */
	public void setImplementationId(String implementationId) {
		this.implementationId = implementationId;
	}
	
	/**
	 * This text is a long text string that is used to validate who uses an implementation id.
	 * Multiple installations of openmrs can use the same implementation id, but they must all know
	 * the passphrase. (Note that if an implementation id is shared, it is assumed that those
	 * installations are the same implementation).
	 *
	 * @return the passphrase
	 */
	public String getPassphrase() {
		return passphrase;
	}
	
	/**
	 * This text is a long text string that is used to validate who uses an implementation id.
	 * Multiple installations of openmrs can use the same implementation id, but they must all know
	 * the passphrase. (Note that if an implementation id is shared, it is assumed that those
	 * installations are the same implementation).
	 *
	 * @param passphrase the passphrase to set
	 */
	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}
	
	/**
	 * A descriptive name for this implementation (e.g. AMRS installation in Eldoret, Kenya)
	 *
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * A descriptive name for this implementation (e.g. AMRS installation in Eldoret, Kenya)
	 *
	 * @param name The concept source name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "Impl Id: " + getImplementationId() + " name: " + getName() + " desc: " + getDescription();
	}
}
