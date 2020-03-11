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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Boost;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.search.LuceneAnalyzers;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A PersonAttribute is meant as way for implementations to add arbitrary information about a
 * user/patient to their database. PersonAttributes are essentially just key-value pairs. However,
 * the PersonAttributeType can be defined in such a way that the value portion of this
 * PersonAttribute is a foreign key to another database table (like to the location table, or
 * concept table). This gives a PersonAttribute the ability to link to any other part of the
 * database A Person can have zero to n PersonAttribute(s).
 * 
 * @see org.openmrs.PersonAttributeType
 * @see org.openmrs.Attributable
 */
@Indexed
public class PersonAttribute extends BaseChangeableOpenmrsData implements java.io.Serializable, Comparable<PersonAttribute> {
	
	public static final long serialVersionUID = 11231211232111L;
	
	private static final Logger log = LoggerFactory.getLogger(PersonAttribute.class);
	
	// Fields
	@DocumentId
	private Integer personAttributeId;

	@IndexedEmbedded(includeEmbeddedObjectId = true)
	private Person person;

	@IndexedEmbedded
	private PersonAttributeType attributeType;

	@Fields({
			@Field(name = "valuePhrase", analyzer = @Analyzer(definition = LuceneAnalyzers.PHRASE_ANALYZER), boost = @Boost(8f)),
			@Field(name = "valueExact", analyzer = @Analyzer(definition = LuceneAnalyzers.EXACT_ANALYZER), boost = @Boost(4f)),
			@Field(name = "valueStart", analyzer = @Analyzer(definition = LuceneAnalyzers.START_ANALYZER), boost = @Boost(2f)),
			@Field(name = "valueAnywhere", analyzer = @Analyzer(definition = LuceneAnalyzers.ANYWHERE_ANALYZER))
	})
	private String value;
	
	/** default constructor */
	public PersonAttribute() {
	}
	
	public PersonAttribute(Integer personAttributeId) {
		this.personAttributeId = personAttributeId;
	}
	
	/**
	 * Constructor for creating a basic attribute
	 * 
	 * @param type PersonAttributeType
	 * @param value String
	 */
	public PersonAttribute(PersonAttributeType type, String value) {
		this.attributeType = type;
		this.value = value;
	}
	
	/**
	 * Shallow copy of this PersonAttribute. Does NOT copy personAttributeId
	 * 
	 * @return a shallows copy of <code>this</code>
	 */
	public PersonAttribute copy() {
		return copyHelper(new PersonAttribute());
	}
	
	/**
	 * The purpose of this method is to allow subclasses of PersonAttribute to delegate a portion of
	 * their copy() method back to the superclass, in case the base class implementation changes.
	 * 
	 * @param target a PersonAttribute that will have the state of <code>this</code> copied into it
	 * @return Returns the PersonAttribute that was passed in, with state copied into it
	 */
	protected PersonAttribute copyHelper(PersonAttribute target) {
		target.setPerson(getPerson());
		target.setAttributeType(getAttributeType());
		target.setValue(getValue());
		target.setCreator(getCreator());
		target.setDateCreated(getDateCreated());
		target.setChangedBy(getChangedBy());
		target.setDateChanged(getDateChanged());
		target.setVoidedBy(getVoidedBy());
		target.setVoided(getVoided());
		target.setDateVoided(getDateVoided());
		target.setVoidReason(getVoidReason());
		return target;
	}
	
	/**
	 * Compares this PersonAttribute object to the given otherAttribute. This method differs from
	 * {@link #equals(Object)} in that this method compares the inner fields of each attribute for
	 * equality. Note: Null/empty fields on <code>otherAttribute</code> /will not/ cause a false
	 * value to be returned
	 * 
	 * @param otherAttribute PersonAttribute with which to compare
	 * @return boolean true/false whether or not they are the same attributes
	 * <strong>Should</strong> return true if attributeType value and void status are the same
	 */
	@SuppressWarnings("unchecked")
	public boolean equalsContent(PersonAttribute otherAttribute) {
		boolean returnValue = true;
		
		// these are the methods to compare.
		String[] methods = { "getAttributeType", "getValue", "getVoided" };
		
		Class attributeClass = this.getClass();
		
		// loop over all of the selected methods and compare this and other
		for (String methodAttribute : methods) {
			try {
				Method method = attributeClass.getMethod(methodAttribute);
				
				Object thisValue = method.invoke(this);
				Object otherValue = method.invoke(otherAttribute);
				
				if (otherValue != null) {
					returnValue &= otherValue.equals(thisValue);
				}
				
			}
			catch (NoSuchMethodException e) {
				log.warn("No such method for comparison " + methodAttribute, e);
			}
			catch (IllegalAccessException | InvocationTargetException e) {
				log.error("Error while comparing attributes", e);
			}

		}
		
		return returnValue;
	}
	
	// property accessors
	
	/**
	 * @return Returns the person.
	 */
	public Person getPerson() {
		return person;
	}
	
	/**
	 * @param person The person to set.
	 */
	public void setPerson(Person person) {
		this.person = person;
	}
	
	/**
	 * @return the attributeType
	 */
	public PersonAttributeType getAttributeType() {
		return attributeType;
	}
	
	/**
	 * @param attributeType the attributeType to set
	 */
	public void setAttributeType(PersonAttributeType attributeType) {
		this.attributeType = attributeType;
	}
	
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 * <strong>Should</strong> return toString of hydrated value
	 */
	@Override
	public String toString() {
		Object o = getHydratedObject();
		if (o instanceof Attributable) {
			return ((Attributable) o).getDisplayString();
		} else if (o != null) {
			return o.toString();
		}
		
		return this.value;
	}
	
	/**
	 * @return the personAttributeId
	 */
	public Integer getPersonAttributeId() {
		return personAttributeId;
	}
	
	/**
	 * @param personAttributeId the personAttributeId to set
	 */
	public void setPersonAttributeId(Integer personAttributeId) {
		this.personAttributeId = personAttributeId;
	}
	
	/**
	 * Will try to create an object of class 'PersonAttributeType.format'. If that implements
	 * <code>Attributable</code>, hydrate(value) is called. Defaults to just returning getValue()
	 * 
	 * @return hydrated object or getValue()
	 * <strong>Should</strong> load class in format property
	 * <strong>Should</strong> still load class in format property if not Attributable
	 */
	@SuppressWarnings("unchecked")
	public Object getHydratedObject() {
		
		if (getValue() == null) {
			return null;
		}
		
		try {
			Class c = OpenmrsClassLoader.getInstance().loadClass(getAttributeType().getFormat());
			try {
				Object o = c.newInstance();
				if (o instanceof Attributable) {
					Attributable attr = (Attributable) o;
					return attr.hydrate(getValue());
				}
			}
			catch (InstantiationException e) {
				// try to hydrate the object with the String constructor
				log.trace("Unable to call no-arg constructor for class: " + c.getName());
				return c.getConstructor(String.class).newInstance(getValue());
			}
		}
		catch (Exception e) {
			
			// No need to warn if the input was blank
			if (StringUtils.isBlank(getValue())) {
				return null;
			}
			
			log.warn("Unable to hydrate value: " + getValue() + " for type: " + getAttributeType(), e);
		}
		
		log.debug("Returning value: '" + getValue() + "'");
		return getValue();
	}
	
	/**
	 * Convenience method for voiding this attribute
	 * 
	 * @param reason
	 * <strong>Should</strong> set voided bit to true
	 */
	public void voidAttribute(String reason) {
		setVoided(true);
		setVoidedBy(Context.getAuthenticatedUser());
		setVoidReason(reason);
		setDateVoided(new Date());
	}
	
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * <strong>Should</strong> return negative if other attribute is voided
	 * <strong>Should</strong> return negative if other attribute has earlier date created
	 * <strong>Should</strong> return negative if this attribute has lower attribute type than argument
	 * <strong>Should</strong> return negative if other attribute has lower value
	 * <strong>Should</strong> return negative if this attribute has lower attribute id than argument
	 * <strong>Should</strong> not throw exception if attribute type is null
	 * Note: this comparator imposes orderings that are inconsistent with equals
	 */
	@Override
	public int compareTo(PersonAttribute other) {
		DefaultComparator paDComparator = new DefaultComparator();
		return paDComparator.compare(this, other);
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		
		return getPersonAttributeId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setPersonAttributeId(id);
		
	}
	
	/**
	 Provides a default comparator.
	 @since 1.12
	 **/
	public static class DefaultComparator implements Comparator<PersonAttribute>, Serializable {

		private static final long serialVersionUID = 1L;
		
		@Override
		public int compare(PersonAttribute pa1, PersonAttribute pa2) {
			int retValue;
			if ((retValue = OpenmrsUtil.compareWithNullAsGreatest(pa1.getAttributeType(), pa2.getAttributeType())) != 0) {
				return retValue;
			}
			
			if ((retValue = pa1.getVoided().compareTo(pa2.getVoided())) != 0) {
				return retValue;
			}
			
			if ((retValue = OpenmrsUtil.compareWithNullAsLatest(pa1.getDateCreated(), pa2.getDateCreated())) != 0) {
				return retValue;
			}
			
			if ((retValue = OpenmrsUtil.compareWithNullAsGreatest(pa1.getValue(), pa2.getValue())) != 0) {
				return retValue;
			}
			
			return OpenmrsUtil.compareWithNullAsGreatest(pa1.getPersonAttributeId(), pa2.getPersonAttributeId());
		}
	}
	
}
