package org.openmrs.api.db.hibernate;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Concept;
import org.openmrs.Field;
import org.openmrs.FieldType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.FormDAO;

public class HibernateFormDAO implements
		FormDAO {

	protected final Log log = LogFactory.getLog(getClass());
	
	private Context context;
	
	public HibernateFormDAO() { }

	
	public HibernateFormDAO(Context c) {
		this.context = c;
	}

	/**
	 * @see org.openmrs.api.db.FormService#createForm(org.openmrs.Form)
	 */
	public void createForm(Form form) throws DAOException {
		
		Session session = HibernateUtil.currentSession();
		
		updateFormProperties(form);
		
		try {
			HibernateUtil.beginTransaction();
			session.clear();
			session.merge(form);
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
		
		if (form.isRetired() && form.getRetiredBy() == null) {
			retireForm(form, form.getRetiredReason());
		}
		else if (!form.isRetired() && form.getRetiredBy() != null) {
			unretireForm(form);
		}
		else {
			
			if (form.getFormId() == null)
				createForm(form);
			else {
				Session session = HibernateUtil.currentSession();
				
				updateFormProperties(form);
				
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
	}
	
	private void updateFormProperties(Form form) {
		if (form.getCreator() == null) {
			form.setCreator(context.getAuthenticatedUser());
			form.setDateCreated(new Date());
		}
		form.setChangedBy(context.getAuthenticatedUser());
		form.setDateChanged(new Date());
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
	 * @see org.openmrs.api.db.FormDAO#findFields(java.lang.String)
	 */
	public List<Field> findFields(String search) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		Criteria criteria = session.createCriteria(Field.class);
		criteria.add(Restrictions.like("name", search, MatchMode.ANYWHERE));
		criteria.addOrder(Order.asc("name"));
		return criteria.list();
	}

	/**
	 * @see org.openmrs.api.db.FormDAO#findFields(org.openmrs.Concept)
	 */
	public List<Field> findFields(Concept concept) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		Criteria criteria = session.createCriteria(Field.class);
		criteria.add(Expression.eq("concept", concept));
		criteria.addOrder(Order.asc("name"));
		return criteria.list();
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
	 * @see org.openmrs.api.db.FormService#getFormField(org.openmrs.Form,org.openmrs.Concept)
	 */
	public FormField getFormField(Form form, Concept concept) throws APIException {
		Session session = HibernateUtil.currentSession();
		
		Criteria crit = session.createCriteria(FormField.class)
			.createAlias("field", "field")
			.add(Expression.eq("field.concept", concept))
			.add(Expression.eq("form", form));

		if (crit.list().size() < 1) {
			String err = "FormField warning.  No FormField matching concept " + concept.getConceptId() + " for form " + form.getFormId();
			log.warn(err);
			return null;
		}
		
		FormField ff = (FormField)crit.list().get(0); 
		return ff;
	}
	
	/**
	 * @see org.openmrs.api.db.FormService#getForms()
	 */
	public List<Form> getForms() throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		List forms = session.createCriteria(Form.class).addOrder(Order.asc("name")).addOrder(Order.asc("formId")).list();
		
		return forms;
	}
	
	/**
	 * @see org.openmrs.api.db.FormService#getForms(boolean)
	 */
	public List<Form> getForms(boolean published) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		Criteria crit = session.createCriteria(Form.class);
		
		crit.add(Expression.eq("published", published));
		crit.addOrder(Order.asc("name"));
		crit.addOrder(Order.asc("formId"));
		
		return crit.list();
	}
	
	/**
	 * @see org.openmrs.api.db.FormDAO#getForms(org.openmrs.Concept)
	 */
	public List<Form> getForms(Concept c) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		Query query = session.createQuery("from Form as form inner join form.formFields as ff inner join ff.field as f where f.concept = :concept");
		query.setEntity("concept", c);
		
		return query.list();
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
			session.clear();
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
				field.setChangedBy(context.getAuthenticatedUser());
				field.setDateChanged(new Date());
				session.clear();
				session.merge(field);
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
	
	/**
	 * @see org.openmrs.api.db.FormFieldService#createFormField(org.openmrs.FormField)
	 */
	public void createFormField(FormField formField) throws DAOException {
		
		Session session = HibernateUtil.currentSession();
		
		formField.setCreator(context.getAuthenticatedUser());
		formField.setDateCreated(new Date());
		
		try {
			HibernateUtil.beginTransaction();
			session.clear();
			session.save(formField);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}
	
	/**
	 * @see org.openmrs.api.db.FormService#updateFormField(org.openmrs.FormField)
	 */
	public void updateFormField(FormField formField) throws DAOException {
		if (formField.getField().getCreator() == null) {
			Field field = formField.getField();
			field.setCreator(context.getAuthenticatedUser());
			field.setDateCreated(new Date());
		}
		
		Session session = HibernateUtil.currentSession();

		formField.setChangedBy(context.getAuthenticatedUser());
		formField.setDateChanged(new Date());
		
		if (formField.getFormFieldId() == null)
			createFormField(formField);
		else {
			
			formField.setCreator(context.getAuthenticatedUser());
			formField.setDateCreated(new Date());

			try {
				HibernateUtil.beginTransaction();
				session.clear();
				session.merge(formField);	// save if needs saving
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new DAOException(e);
			}
		}
		session.evict(formField);
		log.debug("formField cache mode: " + session.getCacheMode());
	}

	/**
	 * @see org.openmrs.api.db.FormService#deleteFormField(org.openmrs.FormField)
	 */
	public void deleteFormField(FormField formField) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		try {
			HibernateUtil.beginTransaction();
			session.clear();
			session.delete(formField);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
		
	}
	
}
