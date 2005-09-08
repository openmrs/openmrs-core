package org.openmrs;

import java.util.Date;
import java.util.Set;

/**
 * Form 
 */
public class Form implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Integer formId;
	private String name;
	private String version;
	private String description;
	private String schemaNamespace;
	private String definition;
	private Boolean retired;
	private Date dateChanged;
	private Date dateCreated;
	private Set formFields;
	private User userByCreator;
	private User userByChangedBy;

	// Constructors

	/** default constructor */
	public Form() {
	}

	/** constructor with id */
	public Form(Integer formId) {
		this.formId = formId;
	}

	// Property accessors

	/**
	 * 
	 */
	public Integer getFormId() {
		return this.formId;
	}

	public void setFormId(Integer formId) {
		this.formId = formId;
	}

	/**
	 * 
	 */
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 */
	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * 
	 */
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * 
	 */
	public String getSchemaNamespace() {
		return this.schemaNamespace;
	}

	public void setSchemaNamespace(String schemaNamespace) {
		this.schemaNamespace = schemaNamespace;
	}

	/**
	 * 
	 */
	public String getDefinition() {
		return this.definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	/**
	 * 
	 */
	public Boolean getRetired() {
		return this.retired;
	}

	public void setRetired(Boolean retired) {
		this.retired = retired;
	}

	/**
	 * 
	 */
	public Date getDateChanged() {
		return this.dateChanged;
	}

	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}

	/**
	 * 
	 */
	public Date getDateCreated() {
		return this.dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * 
	 */
	public Set getFormFields() {
		return this.formFields;
	}

	public void setFormFields(Set formFields) {
		this.formFields = formFields;
	}

	/**
	 * 
	 */
	public User getUserByCreator() {
		return this.userByCreator;
	}

	public void setUserByCreator(User userByCreator) {
		this.userByCreator = userByCreator;
	}

	/**
	 * 
	 */
	public User getUserByChangedBy() {
		return this.userByChangedBy;
	}

	public void setUserByChangedBy(User userByChangedBy) {
		this.userByChangedBy = userByChangedBy;
	}

}