package org.openmrs.api.hibernate;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.openmrs.Field;
import org.openmrs.FieldType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.APIException;
import org.openmrs.api.FormService;
import org.openmrs.context.Context;

public class HibernateFormService implements
		FormService {

	protected final Log log = LogFactory.getLog(getClass());
	
	private Context context;
	
	public HibernateFormService(Context c) {
		this.context = c;
	}

	/**
	 * @see org.openmrs.api.FormService#createForm(org.openmrs.Form)
	 */
	public void createForm(Form form) throws APIException {
		
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		form.setCreator(context.getAuthenticatedUser());
		form.setDateCreated(new Date());
		session.save(form);
		
		tx.commit();
		HibernateUtil.disconnectSession();
	}

	/**
	 * @see org.openmrs.api.FormService#deleteForm(org.openmrs.Form)
	 */
	public void deleteForm(Form form) throws APIException {
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		session.delete(form);
		
		tx.commit();
		HibernateUtil.disconnectSession();
	}

	/**
	 * @see org.openmrs.api.FormService#getForm(java.lang.Integer)
	 */
	public Form getForm(Integer formId) throws APIException {
		
		Session session = HibernateUtil.currentSession();
		
		Form form = new Form();
		form = (Form)session.get(Form.class, formId);
		
		HibernateUtil.disconnectSession();
		
		return form;
	}

	/**
	 * @see org.openmrs.api.FormService#updateForm(org.openmrs.Form)
	 */
	public void updateForm(Form form) {
		
		if (form.getFormId() == null)
			createForm(form);
		else {
			Session session = HibernateUtil.currentSession();
			
			session.saveOrUpdate(form);
			//HibernateUtil.disconnectSession();
		}
	}

	/**
	 * @see org.openmrs.api.FormService#voidForm(org.openmrs.Form, java.lang.String)
	 */
	public void retireForm(Form form, String reason) {
		form.setRetired(false);
		form.setRetiredBy(context.getAuthenticatedUser());
		form.setDateRetired(new Date());
		form.setRetiredReason(reason);
		updateForm(form);
	}

	/**
	 * @see org.openmrs.api.FormService#unvoidForm(org.openmrs.Form)
	 */
	public void unretireForm(Form form) throws APIException {
		form.setRetired(true);
		form.setRetiredBy(null);
		form.setDateRetired(null);
		form.setRetiredReason("");
		updateForm(form);
	}

	/**
	 * @see org.openmrs.api.FormService#getField(java.lang.Integer)
	 */
	public Field getField(Integer fieldId) throws APIException {
		Session session = HibernateUtil.currentSession();
		
		Field field = new Field();
		field = (Field)session.get(Field.class, fieldId);
		
		HibernateUtil.disconnectSession();
		
		return field;
	}

	/**
	 * @see org.openmrs.api.FormService#getFields()
	 */
	public List<Field> getFields() throws APIException {
		Session session = HibernateUtil.currentSession();
		
		List fields = session.createCriteria(Field.class).list();
		
		HibernateUtil.disconnectSession();
		
		return fields;
	}

	/**
	 * @see org.openmrs.api.FormService#getFieldType(java.lang.Integer)
	 */
	public FieldType getFieldType(Integer fieldTypeId) throws APIException {
		Session session = HibernateUtil.currentSession();
		
		FieldType fieldType = new FieldType();
		fieldType = (FieldType)session.get(FieldType.class, fieldTypeId);
		
		HibernateUtil.disconnectSession();
		
		return fieldType;
	}

	/**
	 * @see org.openmrs.api.FormService#getFieldTypes()
	 */
	public List<FieldType> getFieldTypes() throws APIException {
		Session session = HibernateUtil.currentSession();
		
		List fieldTypes = session.createCriteria(FieldType.class).list();
		
		HibernateUtil.disconnectSession();
		
		return fieldTypes;
	}

	/**
	 * @see org.openmrs.api.FormService#getFormField(java.lang.Integer)
	 */
	public FormField getFormField(Integer formFieldId) throws APIException {
		Session session = HibernateUtil.currentSession();
		
		FormField formField = new FormField();
		formField = (FormField)session.get(FormField.class, formFieldId);
		
		HibernateUtil.disconnectSession();
		
		return formField;
	}

	/**
	 * @see org.openmrs.api.FormService#getForms()
	 */
	public List<Form> getForms() throws APIException {
		Session session = HibernateUtil.currentSession();
		
		List forms = session.createCriteria(Form.class).list();
		
		HibernateUtil.disconnectSession();
		
		return forms;
	}
	
	/**
	 * @see org.openmrs.api.FieldService#createField(org.openmrs.Field)
	 */
	public void createField(Field field) throws APIException {
		
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		field.setCreator(context.getAuthenticatedUser());
		field.setDateCreated(new Date());
		session.save(field);
		
		tx.commit();
		HibernateUtil.disconnectSession();
	}
	
	/**
	 * @see org.openmrs.api.FormService#updateField(org.openmrs.Field)
	 */
	public void updateField(Field field) throws APIException {
		if (field.getFieldId() == null)
			createField(field);
		else {
			Session session = HibernateUtil.currentSession();
			
			session.saveOrUpdate(field);
			//HibernateUtil.disconnectSession();
		}
	}

	/**
	 * @see org.openmrs.api.FormService#deleteField(org.openmrs.Field)
	 */
	public void deleteField(Field field) throws APIException {
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		session.delete(field);
		
		tx.commit();
		HibernateUtil.disconnectSession();
	}
	
}
