package org.openmrs;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsClassLoader;

/**
 * PersonAttribute
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public class PersonAttribute implements java.io.Serializable {
	
	private Log log = LogFactory.getLog(getClass());
	public static final long serialVersionUID = 11231211232111L;

	// Fields
	
	private Integer personAttributeId;
	private Person person;
	private PersonAttributeType attributeType;
	private String value;
	
	private User creator;
	private Date dateCreated;

	private User changedBy;
	private Date dateChanged;
	
	private User voidedBy;
	private Boolean voided = false;
	private Date dateVoided;
	private String voidReason;

	/** default constructor */
	public PersonAttribute() {
	}
	
	public PersonAttribute(Integer personAttributeId) {
		this.personAttributeId = personAttributeId;
	}
	
	/**
	 * Constructor for creating a basic attribute
	 * @param attributeType
	 * @param value
	 */
	public PersonAttribute(PersonAttributeType type, String value) {
		this.attributeType = type;
		this.value = value;
	}

	/** 
	 * Compares two objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj instanceof PersonAttribute) {
			PersonAttribute attr = (PersonAttribute)obj;
			return attr.getPersonAttributeId() != null && attr.getPersonAttributeId().equals(getPersonAttributeId());
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getPersonAttributeId() == null) return super.hashCode();
		int hash = 5;
		hash += 29 * hash + this.getPersonAttributeId().hashCode();
		return hash;
	}

	//property accessors

	/**
	 * @return Returns the creator.
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator The creator to set.
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * @return Returns the dateCreated.
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated The dateCreated to set.
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return Returns the dateVoided.
	 */
	public Date getDateVoided() {
		return dateVoided;
	}

	/**
	 * @param dateVoided The dateVoided to set.
	 */
	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}

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
	 * @return Returns the voided.
	 */
	public Boolean isVoided() {
		return voided;
	}
	
	public Boolean getVoided() {
		return isVoided();
	}

	/**
	 * @param voided The voided to set.
	 */
	public void setVoided(Boolean voided) {
		this.voided = voided;
	}

	/**
	 * @return Returns the voidedBy.
	 */
	public User getVoidedBy() {
		return voidedBy;
	}

	/**
	 * @param voidedBy The voidedBy to set.
	 */
	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}

	/**
	 * @return Returns the voidReason.
	 */
	public String getVoidReason() {
		return voidReason;
	}

	/**
	 * @param voidReason The voidReason to set.
	 */
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
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
	 * @return the changedBy
	 */
	public User getChangedBy() {
		return changedBy;
	}

	/**
	 * @param changedBy the changedBy to set
	 */
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}

	/**
	 * @return the dateChanged
	 */
	public Date getDateChanged() {
		return dateChanged;
	}

	/**
	 * @param dateChanged the dateChanged to set
	 */
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
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

	public String toString() {
		Object o = getHydratedObject();
		if (o instanceof Attributable)
			return "" + o;
		
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
	 * Will try to create an object of class 'PersonAttributeType.format'.  If that 
	 * implements <code>Attributable</code>, hydrate(value) is called.
	 * 
	 * Defaults to just returning getValue()
	 * 
	 * @return hydrated object or getValue()
	 */
	public Object getHydratedObject() {
		try {
			Class c = OpenmrsClassLoader.getInstance().loadClass(getAttributeType().getFormat());
			Object o = c.newInstance();
			if (o instanceof Attributable) {
				Attributable attr = (Attributable)o;
				return attr.hydrate(getValue());
			}
		}
		catch (Throwable t) {
			// pass
		}
		
		log.debug("Returning value: '" + getValue() + "'");
		return getValue();
	}
	
	/**
	 * Convenience method for voiding this attribute
	 * @param reason
	 */
	public void voidAttribute(String reason) {
		setVoided(true);
		setVoidedBy(Context.getAuthenticatedUser());
		setVoidReason(reason);
	}
}