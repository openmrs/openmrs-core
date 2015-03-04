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

import org.openmrs.api.APIException;

/**
 * @deprecated This object is no longer supported. This functionality has been replaced by the Tribe
 *             module. The installation of that module converts this tribe patient attribute to a
 *             PersonAttribute and all values are copied over
 */
@Deprecated
// duplicate this tag so the module can know if this class is deprecated
public class Tribe implements java.io.Serializable {
	
	public static final long serialVersionUID = 113232L;
	
	// Fields
	
	private Integer tribeId;
	
	private Boolean retired = false;
	
	private String name;
	
	// Constructors
	
	/**
	 * default constructor
	 *
	 * @deprecated use the Tribe module
	 */
	@Deprecated
	public Tribe() {
		throw new APIException("Tribe.object.not.supported", (Object[]) null);
	}
	
	/**
	 * constructor with id
	 *
	 * @deprecated use the Tribe module
	 */
	@Deprecated
	public Tribe(Integer tribeId) {
		throw new APIException("Tribe.object.not.supported", (Object[]) null);
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof Tribe) {
			Tribe t = (Tribe) obj;
			if (this.getTribeId() != null && t.getTribeId() != null) {
				return this.getTribeId().equals(t.getTribeId());
			}
			/*return (this.getName().matches(t.getName()) &&
					this.isRetired() == t.isRetired()); */
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getTribeId() == null) {
			return super.hashCode();
		}
		return this.getTribeId().hashCode();
	}
	
	// Property accessors
	
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
	 * @return Returns the retired status.
	 */
	public Boolean isRetired() {
		if (retired == null) {
			return false;
		}
		return retired;
	}
	
	public Boolean getRetired() {
		return isRetired();
	}
	
	/**
	 * @param retired The retired status to set.
	 */
	public void setRetired(Boolean retired) {
		this.retired = retired;
	}
	
	/**
	 * @return Returns the tribeId.
	 */
	public Integer getTribeId() {
		return tribeId;
	}
	
	/**
	 * @param tribeId The tribeId to set.
	 */
	public void setTribeId(Integer tribeId) {
		this.tribeId = tribeId;
	}
	
}
