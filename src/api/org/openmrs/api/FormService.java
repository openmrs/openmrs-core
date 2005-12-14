package org.openmrs.api;

import java.util.List;

import org.openmrs.Field;
import org.openmrs.FieldType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.context.ContextFactory;

/**
 * Form-related services
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public class FormService {
	
	private Context context;
	
	public FormService(Context c) {
		this.context = c;
	}
	
	/****************************************************************
	 * DAO Methods
	 ****************************************************************/
	
	/**
	 * Create a new form
	 * @param form
	 * @throws APIException
	 */
	public void createForm(Form form) throws APIException {
		context.getDAOContext().getFormDAO().createForm(form);
	}

	/**
	 * Get form by internal form identifier
	 * @param formId internal identifier
	 * @return requested form
	 * @throws APIException
	 */
	public Form getForm(Integer formId) throws APIException {
		return context.getDAOContext().getFormDAO().getForm(formId);
	}
	
	/**
	 * Save changes to form
	 * @param form
	 * @throws APIException
	 */
	public void updateForm(Form form) throws APIException {
		context.getDAOContext().getFormDAO().updateForm(form);
	}

	/** 
	 * Mark form as voided (effectively deleting form without removing
	 * their data &mdash; since anything the form touched in the database
	 * will still have their internal identifier and point to the voided
	 * form for historical tracking purposes.
	 * 
	 * @param form
	 * @param reason
	 * @throws APIException
	 */
	public void retireForm(Form form, String reason) throws APIException {
		context.getDAOContext().getFormDAO().retireForm(form, reason);
	}
	
	/**
	 * Clear voided flag for form (equivalent to an "undelete" or
	 * Lazarus Effect for form)
	 * 
	 * @param form
	 * @throws APIException
	 */
	public void unretireForm(Form form) throws APIException {
		context.getDAOContext().getFormDAO().unretireForm(form);
	}
	
	/**
	 * Delete form from database. This is included for troubleshooting and
	 * low-level system administration. Ideally, this method should <b>never</b>
	 * be called &mdash; <code>Forms</code> should be <em>retired</em> and
	 * not <em>deleted</em> altogether (since many foreign key constraints
	 * depend on forms, deleting a form would require deleting all traces, and
	 * any historical trail would be lost).
	 * 
	 * This method only clears form roles and attempts to delete the form
	 * record. If the form has been included in any other parts of the database
	 * (through a foreign key), the attempt to delete the form will violate
	 * foreign key constraints and fail.
	 * 
	 * @param form
	 * @throws APIException
	 */
	public void deleteForm(Form form) throws APIException {
		context.getDAOContext().getFormDAO().deleteForm(form);
	}
	
	/**
	 * Get all field types
	 * 
	 * @return field types list
	 * @throws APIException
	 */
	public List<FieldType> getFieldTypes() throws APIException {
		return context.getDAOContext().getFormDAO().getFieldTypes();
	}

	/**
	 * Get fieldType by internal identifier
	 * 
	 * @param fieldType id
	 * @return fieldType with given internal identifier
	 * @throws APIException
	 */
	public FieldType getFieldType(Integer fieldTypeId) throws APIException {
		return context.getDAOContext().getFormDAO().getFieldType(fieldTypeId);
	}
	
	/**
	 * 
	 * @return list of forms in the db
	 * @throws APIException
	 */
	public List<Form> getForms() throws APIException {
		return context.getDAOContext().getFormDAO().getForms();
	}

	/**
	 * @param form
	 * @return list of fields for a specific form
	 * @throws APIException
	 */
	public List<FormField> getFormFields(Form form) throws APIException {
		return context.getDAOContext().getFormDAO().getFormFields(form);
	}
	
	/**
	 * 
	 * @return list of fields in the db
	 * @throws APIException
	 */
	public List<Field> getFields() throws APIException {
		return context.getDAOContext().getFormDAO().getFields();
	}
	
	/**
	 * 
	 * @param fieldId
	 * @return
	 * @throws APIException
	 */
	public Field getField(Integer fieldId) throws APIException {
		return context.getDAOContext().getFormDAO().getField(fieldId);
	}

	
	/**
	 * 
	 * @param field
	 * @throws APIException
	 */
	public void createField(Field field) throws APIException {
		context.getDAOContext().getFormDAO().createField(field);
	}

	/**
	 * 
	 * @param field
	 * @throws APIException
	 */
	public void updateField(Field field) throws APIException {
		context.getDAOContext().getFormDAO().updateField(field);
	}
	
	/**
	 * 
	 * @param field
	 * @throws APIException
	 */
	public void deleteField(Field field) throws APIException {
		context.getDAOContext().getFormDAO().deleteField(field);
	}
	
}
