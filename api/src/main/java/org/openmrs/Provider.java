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

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.DiscriminatorOptions;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a person who may provide care to a patient during an encounter
 *
 * @since 1.9
 */
@Entity
@Table(name = "provider")
@DiscriminatorColumn(name = "provider_id", discriminatorType = DiscriminatorType.INTEGER)
@DiscriminatorOptions(insert = false)
/*
 * Provider inherits name and description properties from a superclass, name is optional but marked as required in a
 * superclass so we need to override that. Description is not used but exists in a superclass and is marked as
 * persistent, so hibernate generates a query containing the column which of course fails to execute because
 * of the missing column, so we introduce this dummy table containing the column to 'please' hibernate.
 */
@SecondaryTable(name = "provider_unused_fields")
@AttributeOverrides({ @AttributeOverride(name = "name", column = @Column),
        @AttributeOverride(name = "description", column = @Column(table = "provider_unused_fields", name = "description", insertable = false, updatable = false)) })
public class Provider extends BaseCustomizableMetadata<ProviderAttribute> {
	
	private static final Logger log = LoggerFactory.getLogger(Provider.class);
	
	@Id
	@GeneratedValue
	@Column(name = "provider_id")
	private Integer providerId;
	
	@ManyToOne
	@JoinColumn(name = "person_id")
	@Cascade(CascadeType.SAVE_UPDATE)
	private Person person;
	
	private String identifier;
	
	@ManyToOne
	@JoinColumn(name = "role_id")
	private Concept role;
	
	@ManyToOne
	@JoinColumn(name = "speciality_id")
	private Concept speciality;
	
	@Access(AccessType.PROPERTY)
	@OneToMany(mappedBy = "provider", cascade = javax.persistence.CascadeType.ALL, orphanRemoval = true)
	@LazyCollection(LazyCollectionOption.TRUE)
	@OrderBy("voided asc")
	@BatchSize(size = 100)
	private Set<ProviderAttribute> attributes = new LinkedHashSet<>();
	
	public Provider() {
	}
	
	public Provider(Integer providerId) {
		this.providerId = providerId;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getProviderId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setProviderId(id);
	}
	
	/**
	 * @param providerId the providerId to set
	 */
	public void setProviderId(Integer providerId) {
		this.providerId = providerId;
	}
	
	/**
	 * @return the providerId
	 */
	public Integer getProviderId() {
		return providerId;
	}
	
	/**
	 * @param person the person to set
	 */
	public void setPerson(Person person) {
		this.person = person;
	}
	
	/**
	 * @return the person
	 */
	public Person getPerson() {
		return person;
	}
	
	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}
	
	/**
	 * Sets the role concept
	 * 
	 * @since 2.2
	 * @param role the role to set
	 *                
	 */
	public void setRole(Concept role){
		this.role = role;
	}

	/**
	 * Gets the role concept
	 * 
	 * @since 2.2
	 * @return the role
	 * 
	 */
	public Concept getRole(){
		return role;
	}

	/**
	 * Sets the speciality concept
	 * 
	 * @since 2.2
	 * @param speciality the speciality to set 
	 */
	public void setSpeciality(Concept speciality){
		this.speciality = speciality;
	}

	/**
	 * Gets the speciality concept
	 * 
	 * @since 2.2
	 * @return the speciality
	 */
	public Concept getSpeciality(){
		return speciality;
	}
	
	@Override
	public String toString() {
		String provider = String.valueOf(providerId)
				+ " providerName:"
				+ ((person != null) ? person.getNames() : "");
		return "[Provider: providerId:" + provider + " ]";
	}
	
	/**
	 * @see org.openmrs.BaseOpenmrsMetadata#getName()
	 * @should return person full name if person is not null or null otherwise
	 */
	
	@Override
	public String getName() {
		if (getPerson() != null && getPerson().getPersonName() != null) {
			return getPerson().getPersonName().getFullName();
		} else {
			log.warn("We no longer support providers who are not linked to person. Set the name on the linked person");
			return null;
		}
	}
}
