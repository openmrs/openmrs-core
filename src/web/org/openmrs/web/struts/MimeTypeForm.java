package org.openmrs.web.struts;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.openmrs.MimeType;

public class MimeTypeForm extends ActionForm implements Serializable {
	
	public static final long serialVersionUID = 123123L;

	// Fields

	private Integer mimeTypeId;
	private String mimeType;
	private String description;

	// Constructors

	/**
	 * @see org.apache.struts.action.ActionForm#validate(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public ActionErrors validate(ActionMapping arg0, HttpServletRequest arg1) {
		
		ActionErrors errors = new ActionErrors();
		
		if (mimeType == "" || mimeType.equals("")){
			errors.add("mimeType", new ActionMessage("error.MimeType.mimeType"));
		}
		
		if (description == "" || description.equals("")) {
			errors.add("description", new ActionMessage("error.MimeType.description"));
			
		}
		
		return errors;
	}

	/** default constructor */
	public MimeTypeForm() {
	}

	/** constructor with id */
	public MimeTypeForm(Integer mimeTypeId) {
		this.mimeTypeId = mimeTypeId;
	}

	/** 
	 * Compares two objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj instanceof MimeType) {
			MimeType m = (MimeType)obj;
			return (mimeTypeId.equals(m.getMimeTypeId()));
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getMimeTypeId() == null) return super.hashCode();
		return this.getMimeTypeId().hashCode();
	}

	// Property accessors

	/**
	 * @return Returns the mimeTypeId.
	 */
	public Integer getMimeTypeId() {
		return mimeTypeId;
	}

	/**
	 * @param mimeTypeId The mimeTypeId to set.
	 */
	public void setMimeTypeId(Integer mimeTypeId) {
		this.mimeTypeId = mimeTypeId;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the mimeType.
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * @param mimeType The mimeType to set.
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	
	
}