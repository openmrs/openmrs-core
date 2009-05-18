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
	public Tribe() {
		throw new APIException("The Tribe object is no longer supported.  Install the Tribe module");
	}
	
	/**
	 * constructor with id
	 * 
	 * @deprecated use the Tribe module
	 */
	public Tribe(Integer tribeId) {
		throw new APIException("The Tribe object is no longer supported.  Install the Tribe module");
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof Tribe) {
			Tribe t = (Tribe) obj;
			if (this.getTribeId() != null && t.getTribeId() != null)
				return (this.getTribeId().equals(t.getTribeId()));
			/*return (this.getName().matches(t.getName()) &&
					this.isRetired() == t.isRetired()); */
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getTribeId() == null)
			return super.hashCode();
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
		if (retired == null)
			return false;
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
