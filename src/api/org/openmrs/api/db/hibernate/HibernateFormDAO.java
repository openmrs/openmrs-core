package org.openmrs.api.db.hibernate;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.openmrs.Field;
import org.openmrs.FieldType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.FormDAO;

public class HibernateFormDAO implements
		FormDAO {

	protected final Log log = LogFactory.getLog(getClass());
	
	private Context context;
	
	public HibernateFormDAO(Context c) {
		this.context = c;
	}

	/**
	 * @see org.openmrs.api.db.FormService#createForm(org.openmrs.Form)
	 */
	public void createForm(Form form) throws DAOException {
		
		Session session = HibernateUtil.currentSession();
		
		form.setCreator(context.getAuthenticatedUser());
		form.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(form);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
		
	}

	/**
	 * @see org.openmrs.api.db.FormService#deleteForm(org.openmrs.Form)
	 */
	public void deleteForm(Form form) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		try {
			HibernateUtil.beginTransaction();
			session.delete(form);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}	
	}

	/**
	 * @see org.openmrs.api.db.FormService#getForm(java.lang.Integer)
	 */
	public Form getForm(Integer formId) throws DAOException {
		
		Session session = HibernateUtil.currentSession();
		
		Form form = new Form();
		form = (Form)session.get(Form.class, formId);
		
		return form;
	}

	/**
	 * @see org.openmrs.api.db.FormService#updateForm(org.openmrs.Form)
	 */
	public void updateForm(Form form) {
		
		if (form.getFormId() == null)
			createForm(form);
		else {
			Session session = HibernateUtil.currentSession();
			
			try {
				HibernateUtil.beginTransaction();
				session.saveOrUpdate(form);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new DAOException(e);
			}
		}
	}

	/**
	 * @see org.openmrs.api.db.FormService#voidForm(org.openmrs.Form, java.lang.String)
	 */
	public void retireForm(Form form, String reason) {
		form.setRetired(true);
		form.setRetiredBy(context.getAuthenticatedUser());
		form.setDateRetired(new Date());
		form.setRetiredReason(reason);
		updateForm(form);
	}

	/**
	 * @see org.openmrs.api.db.FormService#unvoidForm(org.openmrs.Form)
	 */
	public void unretireForm(Form form) throws DAOException {
		form.setRetired(false);
		form.setRetiredBy(null);
		form.setDateRetired(null);
		form.setRetiredReason("");
		updateForm(form);
	}

	/**
	 * @see org.openmrs.api.db.FormService#getFields(org.openmrs.Form)
	 */
	public List<FormField> getFormFields(Form form) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		List<FormField> formFields = session.createCriteria(FormField.class, "ff")
		.add(Expression.eq("ff.form", form))
		.list();
		
		return formFields;
	}

	/**
	 * @see org.openmrs.api.db.FormService#getField(java.lang.Integer)
	 */
	public Field getField(Integer fieldId) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		Field field = new Field();
		field = (Field)session.get(Field.class, fieldId);
		
		return field;
	}

	/**
	 * @see org.openmrs.api.db.FormService#getFields()
	 */
	public List<Field> getFields() throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		List<Field> fields = session.createCriteria(Field.class).list();
		
		return fields;
	}
	
	/**
	 * @see org.openmrs.api.db.FormService#getFieldType(java.lang.Integer)
	 */
	public FieldType getFieldType(Integer fieldTypeId) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		FieldType fieldType = new FieldType();
		fieldType = (FieldType)session.get(FieldType.class, fieldTypeId);
		
		return fieldType;
	}

	/**
	 * @see org.openmrs.api.db.FormService#getFieldTypes()
	 */
	public List<FieldType> getFieldTypes() throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		List fieldTypes = session.createCriteria(FieldType.class).list();
		
		return fieldTypes;
	}

	/**
	 * @see org.openmrs.api.db.FormService#getFormField(java.lang.Integer)
	 */
	public FormField getFormField(Integer formFieldId) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		FormField formField = new FormField();
		formField = (FormField)session.get(FormField.class, formFieldId);
		
		return formField;
	}

	/**
	 * @see org.openmrs.api.db.FormService#getForms()
	 */
	public List<Form> getForms() throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		List forms = session.createCriteria(Form.class).list();
		
		return forms;
	}
	
	/**
	 * @see org.openmrs.api.db.FieldService#createField(org.openmrs.Field)
	 */
	public void createField(Field field) throws DAOException {
		
		Session session = HibernateUtil.currentSession();
		
		field.setCreator(context.getAuthenticatedUser());
		field.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(field);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}
	
	/**
	 * @see org.openmrs.api.db.FormService#updateField(org.openmrs.Field)
	 */
	public void updateField(Field field) throws DAOException {
		if (field.getFieldId() == null)
			createField(field);
		else {
			Session session = HibernateUtil.currentSession();
			
			try {
				HibernateUtil.beginTransaction();
				session.saveOrUpdate(field);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new DAOException(e);
			}
		}
	}

	/**
	 * @see org.openmrs.api.db.FormService#deleteField(org.openmrs.Field)
	 */
	public void deleteField(Field field) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		try {
			HibernateUtil.beginTransaction();
			session.delete(field);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
		
	}
	
}
