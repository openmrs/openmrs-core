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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.OpenmrsUtil;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

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
@Root(strict = false)
public class PersonAttribute extends BaseOpenmrsData implements java.io.Serializable, Comparable<PersonAttribute> {
	
	private transient Log log = LogFactory.getLog(getClass());
	
	public static final long serialVersionUID = 11231211232111L;
	
	// Fields
	
	private Integer personAttributeId;
	
	private Person person;
	
	private PersonAttributeType attributeType;
	
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
		target.setVoided(isVoided());
		target.setDateVoided(getDateVoided());
		target.setVoidReason(getVoidReason());
		return target;
	}
	
	/**
	 * Compares two objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj instanceof PersonAttribute) {
			PersonAttribute attr = (PersonAttribute) obj;
			return attr.getPersonAttributeId() != null && attr.getPersonAttributeId().equals(getPersonAttributeId());
		}
		return false;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (this.getPersonAttributeId() == null)
			return super.hashCode();
		int hash = 5;
		hash += 29 * hash + this.getPersonAttributeId().hashCode();
		return hash;
	}
	
	/**
	 * Compares this PersonAttribute object to the given otherAttribute. This method differs from
	 * {@link #equals(Object)} in that this method compares the inner fields of each attribute for
	 * equality. Note: Null/empty fields on <code>otherAttribute</code> /will not/ cause a false
	 * value to be returned
	 * 
	 * @param otherAttribute PersonAttribute with which to compare
	 * @return boolean true/false whether or not they are the same attributes
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
				Method method = attributeClass.getMethod(methodAttribute, new Class[] {});
				
				Object thisValue = method.invoke(this);
				Object otherValue = method.invoke(otherAttribute);
				
				if (otherValue != null)
					returnValue &= otherValue.equals(thisValue);
				
			}
			catch (NoSuchMethodException e) {
				log.warn("No such method for comparison " + methodAttribute, e);
			}
			catch (IllegalAccessException e) {
				log.error("Error while comparing attributes", e);
			}
			catch (InvocationTargetException e) {
				log.error("Error while comparing attributes", e);
			}
			
		}
		
		return returnValue;
	}
	
	// property accessors
	
	/**
	 * @return Returns the person.
	 */
	@Element(required = true)
	public Person getPerson() {
		return person;
	}
	
	/**
	 * @param person The person to set.
	 */
	@Element(required = true)
	public void setPerson(Person person) {
		this.person = person;
	}
	
	/**
	 * @return the attributeType
	 */
	@Element(required = true)
	public PersonAttributeType getAttributeType() {
		return attributeType;
	}
	
	/**
	 * @param attributeType the attributeType to set
	 */
	@Element(required = true)
	public void setAttributeType(PersonAttributeType attributeType) {
		this.attributeType = attributeType;
	}
	
	/**
	 * @return the value
	 */
	@Element(data = true, required = false)
	public String getValue() {
		return value;
	}
	
	/**
	 * @param value the value to set
	 */
	@Element(data = true, required = false)
	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 * @should return toString of hydrated value
	 */
	@SuppressWarnings("unchecked")
	public String toString() {
		Object o = getHydratedObject();
		if (o instanceof Attributable)
			return ((Attributable) o).getDisplayString();
		else if (o != null)
			return o.toString();
		
		return this.value;
	}
	
	/**
	 * @return the personAttributeId
	 */
	@Attribute(required = true)
	public Integer getPersonAttributeId() {
		return personAttributeId;
	}
	
	/**
	 * @param personAttributeId the personAttributeId to set
	 */
	@Attribute(required = true)
	public void setPersonAttributeId(Integer personAttributeId) {
		this.personAttributeId = personAttributeId;
	}
	
	/**
	 * Will try to create an object of class 'PersonAttributeType.format'. If that implements
	 * <code>Attributable</code>, hydrate(value) is called. Defaults to just returning getValue()
	 * 
	 * @return hydrated object or getValue()
	 */
	@SuppressWarnings("unchecked")
	public Object getHydratedObject() {
		try {
			Class c = OpenmrsClassLoader.getInstance().loadClass(getAttributeType().getFormat());
			Object o = c.newInstance();
			if (o instanceof Attributable) {
				Attributable attr = (Attributable) o;
				return attr.hydrate(getValue());
			}
		}
		catch (Throwable t) {
			log.warn("Unable to hydrate value: " + getValue() + " for type: " + getAttributeType(), t);
		}
		
		log.debug("Returning value: '" + getValue() + "'");
		return getValue();
	}
	
	/**
	 * Convenience method for voiding this attribute
	 * 
	 * @param reason
	 */
	public void voidAttribute(String reason) {
		setVoided(true);
		setVoidedBy(Context.getAuthenticatedUser());
		setVoidReason(reason);
		setDateVoided(new Date());
	}
	
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(PersonAttribute other) {
		int retValue = 0;
		retValue = isVoided().compareTo(other.isVoided());
		if (retValue == 0)
			retValue = OpenmrsUtil.compareWithNullAsLatest(getDateCreated(), other.getDateCreated());
		if (retValue == 0)
			retValue = getAttributeType().getPersonAttributeTypeId().compareTo(
			    other.getAttributeType().getPersonAttributeTypeId());
		if (retValue == 0)
			retValue = OpenmrsUtil.compareWithNullAsGreatest(getValue(), other.getValue());
		if (retValue == 0)
			retValue = OpenmrsUtil.compareWithNullAsGreatest(getPersonAttributeId(), other.getPersonAttributeId());
		
		return retValue;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		
		return getPersonAttributeId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setPersonAttributeId(id);
		
	}
}
