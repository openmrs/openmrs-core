package org.openmrs;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Form
 * 
 * @author Ben Wolfe
 * @author Burke Mamlin
 * @version 1.0
 */
public class Form implements java.io.Serializable {

	public static final long serialVersionUID = 845634L;

	// Fields

	private Integer formId;
	private String name;
	private String version;
	private String description;
	private String schemaNamespace;
	private String definition;
	private User creator;
	private Date dateCreated;
	private User changedBy;
	private Date dateChanged;
	private Boolean retired = false;
	private User retiredBy;
	private Date dateRetired;
	private String retiredReason;
	private Set<FormField> formFields;

	// Constructors

	/** default constructor */
	public Form() {
	}

	/** constructor with id */
	public Form(Integer formId) {
		this.formId = formId;
	}

	/**
	 * Compares two objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Form) {
			Form f = (Form) obj;
			if (this.getFormId() != null && f.getFormId() != null)
				return (this.getFormId().equals(f.getFormId()));
			/*
			 * return (this.getName().equals(f.getName()) &&
			 * this.getVersion().equals(f.getVersion()));
			 */
		}
		return false;
	}

	public int hashCode() {
		if (this.getFormId() == null)
			return super.hashCode();
		return this.getFormId().hashCode();
	}

	// Property accessors

	/**
	 * @return Returns the formId.
	 */
	public Integer getFormId() {
		return formId;
	}

	/**
	 * @param formId
	 *            The formId to set.
	 */
	public void setFormId(Integer formId) {
		this.formId = formId;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the version.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            The version to set.
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the schemaNamespace.
	 */
	public String getSchemaNamespace() {
		return schemaNamespace;
	}

	/**
	 * @param schemaNamespace
	 *            The schemaNamespace to set.
	 */
	public void setSchemaNamespace(String schemaNamespace) {
		this.schemaNamespace = schemaNamespace;
	}

	/**
	 * @return Returns the definition.
	 */
	public String getDefinition() {
		return definition;
	}

	/**
	 * @param definition
	 *            The definition to set.
	 */
	public void setDefinition(String definition) {
		this.definition = definition;
	}

	/**
	 * @return Returns the creator.
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator
	 *            The creator to set.
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
	 * @param dateCreated
	 *            The dateCreated to set.
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return Returns the changedBy.
	 */
	public User getChangedBy() {
		return changedBy;
	}

	/**
	 * @param changedBy
	 *            The changedBy to set.
	 */
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}

	/**
	 * @return Returns the dateChanged.
	 */
	public Date getDateChanged() {
		return dateChanged;
	}

	/**
	 * @param dateChanged
	 *            The dateChanged to set.
	 */
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}

	/**
	 * @return Returns the retired status.
	 */
	public Boolean isRetired() {
		return retired;
	}

	/**
	 * @param retired
	 *            The retired status to set.
	 */
	public void setRetired(Boolean retired) {
		this.retired = retired;
	}

	/**
	 * @return Returns the retiredBy.
	 */
	public User getRetiredBy() {
		return retiredBy;
	}

	/**
	 * @param retiredBy
	 *            The retiredBy to set.
	 */
	public void setRetiredBy(User retiredBy) {
		this.retiredBy = retiredBy;
	}

	/**
	 * @return Returns the dateRetired.
	 */
	public Date getDateRetired() {
		return dateRetired;
	}

	/**
	 * @param dateRetired
	 *            The dateRetired to set.
	 */
	public void setDateRetired(Date dateRetired) {
		this.dateRetired = dateRetired;
	}

	/**
	 * @return Returns the retiredReason.
	 */
	public String getRetiredReason() {
		return retiredReason;
	}

	/**
	 * @param retiredReason
	 *            The retiredReason to set.
	 */
	public void setRetiredReason(String retiredReason) {
		this.retiredReason = retiredReason;
	}

	/**
	 * @return Returns the formFields.
	 */
	public Set<FormField> getFormFields() {
		return formFields;
	}

	/**
	 * @param formFields
	 *            The formFields to set.
	 */
	public void setFormFields(Set<FormField> formFields) {
		this.formFields = formFields;
	}

	/**
	 * Adds a FormField to the list of form fields
	 * 
	 * @param FormField
	 *            to be added
	 */
	public void addFormField(FormField formField) {
		if (formFields == null)
			formFields = new HashSet<FormField>();
		if (!formFields.contains(formField) && formField != null)
			this.formFields.add(formField);
	}

	/**
	 * Removes a FormField from the list of form fields
	 * 
	 * @param FormField
	 *            formField to be removed
	 */
	public void removeFormField(FormField formField) {
		if (formFields != null) {
			this.formFields.remove(formField);
		}
	}

}