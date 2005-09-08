package org.openmrs;

import java.util.Date;
import java.util.Set;

/**
 * FormField 
 */
public class FormField implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Integer formFieldId;
	private Integer fieldNumber;
	private String fieldPart;
	private Integer pageNumber;
	private Integer minOccurs;
	private Integer maxOccurs;
	private Boolean required;
	private Date dateChanged;
	private Date dateCreated;
	private Form form;
	private Set formFields;
	private FormField formField;
	private User userByChangedBy;
	private User userByCreator;
	private Field field;

	// Constructors

	/** default constructor */
	public FormField() {
	}

	/** constructor with id */
	public FormField(Integer formFieldId) {
		this.formFieldId = formFieldId;
	}

	// Property accessors

	/**
	 * 
	 */
	public Integer getFormFieldId() {
		return this.formFieldId;
	}

	public void setFormFieldId(Integer formFieldId) {
		this.formFieldId = formFieldId;
	}

	/**
	 * 
	 */
	public Integer getFieldNumber() {
		return this.fieldNumber;
	}

	public void setFieldNumber(Integer fieldNumber) {
		this.fieldNumber = fieldNumber;
	}

	/**
	 * 
	 */
	public String getFieldPart() {
		return this.fieldPart;
	}

	public void setFieldPart(String fieldPart) {
		this.fieldPart = fieldPart;
	}

	/**
	 * 
	 */
	public Integer getPageNumber() {
		return this.pageNumber;
	}

	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}

	/**
	 * 
	 */
	public Integer getMinOccurs() {
		return this.minOccurs;
	}

	public void setMinOccurs(Integer minOccurs) {
		this.minOccurs = minOccurs;
	}

	/**
	 * 
	 */
	public Integer getMaxOccurs() {
		return this.maxOccurs;
	}

	public void setMaxOccurs(Integer maxOccurs) {
		this.maxOccurs = maxOccurs;
	}

	/**
	 * 
	 */
	public Boolean getRequired() {
		return this.required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
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
	public Form getForm() {
		return this.form;
	}

	public void setForm(Form form) {
		this.form = form;
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
	public FormField getFormField() {
		return this.formField;
	}

	public void setFormField(FormField formField) {
		this.formField = formField;
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
	public Field getField() {
		return this.field;
	}

	public void setField(Field field) {
		this.field = field;
	}

}