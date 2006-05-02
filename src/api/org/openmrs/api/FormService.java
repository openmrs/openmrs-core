package org.openmrs.api;

import java.util.List;

import org.openmrs.Concept;
import org.openmrs.Field;
import org.openmrs.FieldType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOContext;
import org.openmrs.api.db.FormDAO;
import org.openmrs.formentry.FormSchemaBuilder;
import org.openmrs.util.OpenmrsConstants;

/**
 * Form-related services
 * 
 * @author Ben Wolfe
 * @author Burke Mamlin
 * @version 1.0
 */
public class FormService {
	
	private Context context;
	private DAOContext daoContext;
	
	public FormService(Context c, DAOContext d) {
		this.context = c;
		this.daoContext = d;
	}
	
	/**
	 * Convenience method for retrieving FormDAO
	 * 
	 * @return context's FormDAO
	 */
	private FormDAO dao() {
		//if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_FORMS))
		//	throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_FORMS);
		
		return daoContext.getFormDAO();
	}
	
	/**
	 * Returns XML Schema for form based on the defined fields
	 * 
	 * @param form
	 * @return XML Schema for form
	 */
	public String getSchema(Form form) {
		return new FormSchemaBuilder(context, form).getSchema();
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
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_ADD_FORMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_ADD_FORMS);
		dao().createForm(form);
	}

	/**
	 * Get form by internal form identifier
	 * @param formId internal identifier
	 * @return requested form
	 * @throws APIException
	 */
	public Form getForm(Integer formId) throws APIException {
		return dao().getForm(formId);
	}
	
	public List<Form> getForms(boolean published) throws APIException {
		return dao().getForms(published);
	}
	
	/**
	 * Save changes to form
	 * @param form
	 * @throws APIException
	 */
	public void updateForm(Form form) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_FORMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_FORMS);
		dao().updateForm(form);
	}
	
	public Form duplicateForm(Form form) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_ADD_FORMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_ADD_FORMS);
		
		// Map of /Old FormFieldId/ to /New FormField Object/
		//TreeMap<Integer, FormField> formFieldMap = new TreeMap<Integer, FormField>();
		//formFieldMap.put(null, null); //for parentless formFields

		for (FormField formField : form.getFormFields()) {
			//formFieldMap.put(formField.getFormFieldId(), formField);
			formField.setFormFieldId(null);
			//formField.setParent(formFieldMap.get(formField.getParent().getFormFieldId()));
		}

		form.setFormId(null);
		dao().createForm(form);
		
		return form;
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
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_FORMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_FORMS);
		dao().retireForm(form, reason);
	}
	
	/**
	 * Clear voided flag for form (equivalent to an "undelete" or
	 * Lazarus Effect for form)
	 * 
	 * @param form
	 * @throws APIException
	 */
	public void unretireForm(Form form) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_FORMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_FORMS);
		dao().unretireForm(form);
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
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_DELETE_FORMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_DELETE_FORMS);
		dao().deleteForm(form);
	}
	
	/**
	 * Get all field types
	 * 
	 * @return field types list
	 * @throws APIException
	 */
	public List<FieldType> getFieldTypes() throws APIException {
		return dao().getFieldTypes();
	}

	/**
	 * Get fieldType by internal identifier
	 * 
	 * @param fieldType id
	 * @return fieldType with given internal identifier
	 * @throws APIException
	 */
	public FieldType getFieldType(Integer fieldTypeId) throws APIException {
		return dao().getFieldType(fieldTypeId);
	}
	
	/**
	 * 
	 * @return list of forms in the db
	 * @throws APIException
	 */
	public List<Form> getForms() throws APIException {
		return dao().getForms();
	}
	
	/**
	 * Returns the forms with which this form is associated
	 * @return
	 * @throws APIException
	 */
	public List<Form> getForms(Concept c) throws APIException {
		return dao().getForms(c);
	}

	/**
	 * @param form
	 * @return list of fields for a specific form
	 * @throws APIException
	 */
	public List<FormField> getFormFields(Form form) throws APIException {
		return dao().getFormFields(form);
	}
	
	/**
	 * 
	 * @return list of fields in the db matching part of search term
	 * @throws APIException
	 */
	public List<Field> findFields(String searchPhrase) throws APIException {
		return dao().findFields(searchPhrase);
	}
	
	/**
	 * 
	 * @return list of fields in the db for given concept
	 * @throws APIException
	 */
	public List<Field> findFields(Concept concept) throws APIException {
		return dao().findFields(concept);
	}
	
	
	/**
	 * 
	 * @return list of fields in the db
	 * @throws APIException
	 */
	public List<Field> getFields() throws APIException {
		return dao().getFields();
	}
	
	/**
	 * 
	 * @param fieldId
	 * @return
	 * @throws APIException
	 */
	public Field getField(Integer fieldId) throws APIException {
		return dao().getField(fieldId);
	}

	
	/**
	 * 
	 * @param field
	 * @throws APIException
	 */
	public void createField(Field field) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_FORMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_FORMS);
		dao().createField(field);
	}

	/**
	 * 
	 * @param field
	 * @throws APIException
	 */
	public void updateField(Field field) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_FORMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_FORMS);
		dao().updateField(field);
	}
	
	/**
	 * 
	 * @param field
	 * @throws APIException
	 */
	public void deleteField(Field field) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_FORMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_FORMS);
		dao().deleteField(field);
	}
	
	/**
	 * 
	 * @param fieldId
	 * @return
	 * @throws APIException
	 */
	public FormField getFormField(Integer formFieldId) throws APIException {
		return dao().getFormField(formFieldId);
	}
	
	/**
	 * Finds the FormField defined for this form/concept combination 
	 * 
	 * @param form
	 * @param concept
	 * @return Formfield for this concept
	 * @throws APIException
	 */
	public FormField getFormField(Form form, Concept concept) throws APIException {
		return dao().getFormField(form, concept);
	}
	
	/**
	 * 
	 * @param formField
	 * @throws APIException
	 */
	public void createFormField(FormField formField) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_FORMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_FORMS);
		dao().createFormField(formField);
	}

	/**
	 * 
	 * @param formField
	 * @throws APIException
	 */
	public void updateFormField(FormField formField) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_FORMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_FORMS);
		dao().updateFormField(formField);
	}
	
	/**
	 * 
	 * @param formField
	 * @throws APIException
	 */
	public void deleteFormField(FormField formField) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_FORMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_FORMS);
		dao().deleteFormField(formField);
	}
	
}