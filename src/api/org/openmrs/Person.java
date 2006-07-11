package org.openmrs;


/**
 * User
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public class Person implements java.io.Serializable {

	public static final long serialVersionUID = 13533L;

	// Fields

	protected Integer personId;
	protected Patient patient;
	protected User user;
	
	// Constructors

	// default constructor 
	public Person() {
	}

	public Person(Integer personId) {
		this.personId = personId;
	}
	
	public Person(Patient patient) {
		this.patient = patient;
	}
	
	public Person(User user) {
		this.user = user;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof Person) {
			Person u = (Person)obj;;
			return (getPersonId().equals(u.getPersonId()));
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getPersonId() == null) return super.hashCode();
		return this.getPersonId().hashCode();
	}
	
	// Property accessors
	
	/**
	 * @return Returns the personId.
	 */
	public Integer getPersonId() {
		return personId;
	}

	/**
	 * @param personId The personId to set.
	 */
	public void setPersonId(Integer personId) {
		this.personId = personId;
	}
	
	/**
	 * @return Returns the patient.
	 */
	public Patient getPatient() {
		return patient;
	}

	/**
	 * @param patient The patient to set.
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	/**
	 * @return Returns the user.
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user The user to set.
	 */
	public void setUser(User user) {
		this.user = user;
	}
	
	public String toString() {
		return "person #" + personId.toString();
	}

}