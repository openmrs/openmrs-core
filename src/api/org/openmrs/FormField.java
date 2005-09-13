package org.openmrs;

import java.util.Date;

/**
 * FormField 
 */
public class FormField implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Integer formFieldId;
	private FormField formField;
	private Form form;
	private Field field;
	private Integer fieldNumber;
	private String fieldPart;
	private Integer pageNumber;
	private Integer minOccurs;
	private Integer maxOccurs;
	private Boolean required;
	private Date dateChanged;
	private Date dateCreated;
	private User changedBy;
	private User creator;

	// Constructors

	/** default constructor */
	public FormField() {
	}

	/** constructor with id */
	public FormField(Integer formFieldId) {
		this.formFieldId = formFieldId;
	}

	public boolean equals(Object obj) {
		if (obj instanceof FormField)
		{
			FormField f = (FormField)obj;
			if (this.getFormFieldId() != null && f.getFormFieldId() != null)
				return (this.getFormFieldId() == f.getFormFieldId());
			return (this.getFormField().equals(f.getFormField()) &&
					this.getForm().equals(f.getForm()) &&
					this.getField().equals(f.getField()));
		}
		return false;
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
	public Boolean isRequired() {
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
	public FormField getFormField() {
		return this.formField;
	}

	public void setFormField(FormField formField) {
		this.formField = formField;
	}

	/**
	 * 
	 */
	public User getChangedBy() {
		return this.changedBy;
	}

	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}

	/**
	 * 
	 */
	public User getCreator() {
		return this.creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
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