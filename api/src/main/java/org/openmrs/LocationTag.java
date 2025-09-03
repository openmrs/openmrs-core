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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.envers.Audited;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * An LocationTag allows categorization of {@link Location}s
 * 
 * @see Location
 * @since 1.5
 */
@Audited
@Entity
@Table(name = "location_tag")
@AttributeOverrides({
	@AttributeOverride( name = "name", column = @Column(name = "name", nullable = false, length = 50)),
	@AttributeOverride( name = "dateCreated", column = @Column(name = "date_created", nullable = false, length = 19)),
	@AttributeOverride( name = "dateRetired", column = @Column( name = "date_retired", length = 19))
})
public class LocationTag extends BaseChangeableOpenmrsMetadata {
	
	public static final long serialVersionUID = 7654L;
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "location_tag_id_seq")
	@GenericGenerator(
			name = "location_tag_id_seq",
			strategy = "native",
			parameters = @Parameter(name = "sequence", value = "location_tag_location_tag_id_seq")
	)
	@Column(name = "location_tag_id",  nullable = false)
	private Integer locationTagId;
	
	// Constructors
	
	/** default constructor */
	public LocationTag() {
	}
	
	/** constructor with id */
	public LocationTag(Integer locationTagId) {
		this.locationTagId = locationTagId;
	}
	
	/**
	 * Required values constructor. This is the minimum number of values that must be non-null in
	 * order to have a successful save to the database
	 * 
	 * @param name the name of this encounter type
	 * @param description a short description of why this encounter type exists
	 */
	public LocationTag(String name, String description) {
		setName(name);
		setDescription(description);
	}
	
	// Property accessors
	
	/**
	 * @return Returns the locationTagId.
	 */
	public Integer getLocationTagId() {
		return locationTagId;
	}
	
	/**
	 * @param locationTagId The locationTagId to set.
	 */
	public void setLocationTagId(Integer locationTagId) {
		this.locationTagId = locationTagId;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getLocationTagId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setLocationTagId(id);
		
	}
}
