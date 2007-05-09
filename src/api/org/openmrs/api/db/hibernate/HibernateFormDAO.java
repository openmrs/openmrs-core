package org.openmrs.api.db.hibernate;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
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
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.FormDAO;

public class HibernateFormDAO implements
		FormDAO {

	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	public HibernateFormDAO() { }

	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) { 
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.api.db.FormService#createForm(org.openmrs.Form)
	 */
	public Form createForm(Form form) throws DAOException {
		return (Form) sessionFactory.getCurrentSession().merge(form);
	}

	/**
	 * @see org.openmrs.api.db.FormService#deleteForm(org.openmrs.Form)
	 */
	public void deleteForm(Form form) throws DAOException {
		sessionFactory.getCurrentSession().delete(form);
	}

	/**
	 * @see org.openmrs.api.db.FormService#getForm(java.lang.Integer)
	 */
	public Form getForm(Integer formId) throws DAOException {
		return (Form)sessionFactory.getCurrentSession().get(Form.class, formId);
	}

	/**
	 * @see org.openmrs.api.db.FormService#updateForm(org.openmrs.Form)
	 */
	public void updateForm(Form form) {
		
		if (form.getFormId() == null)
			createForm(form);
		else
			sessionFactory.getCurrentSession().merge(form);
	}

	/**
	 * @see org.openmrs.api.db.FormService#getFields(org.openmrs.Form)
	 */
	@SuppressWarnings("unchecked")
	public List<FormField> getFormFields(Form form) throws DAOException {
		return sessionFactory.getCurrentSession().createCriteria(FormField.class, "ff")
			.add(Expression.eq("ff.form", form))
			.list();
	}

	/**
	 * @see org.openmrs.api.db.FormDAO#findFields(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<Field> findFields(String search) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Field.class);
		criteria.add(Restrictions.like("name", search, MatchMode.ANYWHERE));
		criteria.addOrder(Order.asc("name"));
		return criteria.list();
	}

	/**
	 * @see org.openmrs.api.db.FormDAO#findFields(org.openmrs.Concept)
	 */
	@SuppressWarnings("unchecked")
	public List<Field> findFields(Concept concept) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Field.class);
		criteria.add(Expression.eq("concept", concept));
		criteria.addOrder(Order.asc("name"));
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.FormService#getField(java.lang.Integer)
	 */
	public Field getField(Integer fieldId) throws DAOException {
		return (Field)sessionFactory.getCurrentSession().get(Field.class, fieldId);
	}

	/**
	 * @see org.openmrs.api.db.FormService#getFields()
	 */
	@SuppressWarnings("unchecked")
	public List<Field> getFields() throws DAOException {
		return sessionFactory.getCurrentSession().createCriteria(Field.class).list();
	}
	
	/**
	 * @see org.openmrs.api.db.FormService#getFieldType(java.lang.Integer)
	 */
	public FieldType getFieldType(Integer fieldTypeId) throws DAOException {
		return (FieldType)sessionFactory.getCurrentSession().get(FieldType.class, fieldTypeId);
	}

	/**
	 * @see org.openmrs.api.db.FormService#getFieldTypes()
	 */
	@SuppressWarnings("unchecked")
	public List<FieldType> getFieldTypes() throws DAOException {
		return sessionFactory.getCurrentSession().createCriteria(FieldType.class).list();
	}

	/**
	 * @see org.openmrs.api.db.FormService#getFormField(java.lang.Integer)
	 */
	public FormField getFormField(Integer formFieldId) throws DAOException {
		return (FormField)sessionFactory.getCurrentSession().get(FormField.class, formFieldId);
	}

	
	/**
	 * @see org.openmrs.api.db.FormService#getFormField(org.openmrs.Form,org.openmrs.Concept)
	 */
	public FormField getFormField(Form form, Concept concept) throws APIException {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(FormField.class)
			.createAlias("field", "field")
			.add(Expression.eq("field.concept", concept))
			.add(Expression.eq("form", form));

		if (crit.list().size() < 1) {
			String err = "FormField warning.  No FormField matching concept '" + concept + "' for form '" + form + "'";
			log.warn(err);
			return null;
		}
		
		FormField ff = (FormField)crit.list().get(0); 
		return ff;
	}
	
	/**
	 * @see org.openmrs.api.db.FormService#getForms()
	 */
	@SuppressWarnings("unchecked")
	public List<Form> getForms() throws DAOException {
		return sessionFactory.getCurrentSession().createCriteria(Form.class).addOrder(Order.asc("name")).addOrder(Order.asc("formId")).list();
	}
	
	/**
	 * @see org.openmrs.api.db.FormService#getForms(boolean,boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<Form> getForms(boolean onlyPublished, boolean includeRetired) throws DAOException {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(Form.class);
		
		if (onlyPublished)
			crit.add(Expression.eq("published", true));
		
		if (!includeRetired)
			crit.add(Expression.eq("retired", false));
		
		crit.addOrder(Order.asc("name"));
		crit.addOrder(Order.asc("formId"));
		
		return crit.list();
	}
	
	/**
	 * @see org.openmrs.api.db.FormDAO#getForms(org.openmrs.Concept)
	 */
	@SuppressWarnings("unchecked")
	public List<Form> getForms(Concept c) throws DAOException {
		String q = "select distinct field.forms from Field field where field.concept = :concept";
		Query query = sessionFactory.getCurrentSession().createQuery(q);
		query.setEntity("concept", c);
		
		return query.list();
	}

	/**
	 * @see org.openmrs.api.db.FieldService#createField(org.openmrs.Field)
	 */
	public void createField(Field field) throws DAOException {
		sessionFactory.getCurrentSession().save(field);
	}
	
	/**
	 * @see org.openmrs.api.db.FormService#updateField(org.openmrs.Field)
	 */
	public void updateField(Field field) throws DAOException {
		if (field.getFieldId() == null)
			createField(field);
		else
			sessionFactory.getCurrentSession().merge(field);
	}

	/**
	 * @see org.openmrs.api.db.FormService#deleteField(org.openmrs.Field)
	 */
	public void deleteField(Field field) throws DAOException {
		sessionFactory.getCurrentSession().delete(field);
	}
	
	/**
	 * @see org.openmrs.api.db.FormFieldService#createFormField(org.openmrs.FormField)
	 */
	public void createFormField(FormField formField) throws DAOException {
		sessionFactory.getCurrentSession().save(formField);
	}
	
	/**
	 * @see org.openmrs.api.db.FormService#updateFormField(org.openmrs.FormField)
	 */
	public void updateFormField(FormField formField) throws DAOException {
		if (formField.getFormFieldId() == null)
			createFormField(formField);
		else
			sessionFactory.getCurrentSession().merge(formField);	// save if needs saving
		
		log.debug("formField cache mode: " + sessionFactory.getCurrentSession().getCacheMode());
	}

	/**
	 * @see org.openmrs.api.db.FormService#deleteFormField(org.openmrs.FormField)
	 */
	public void deleteFormField(FormField formField) throws DAOException {
		sessionFactory.getCurrentSession().delete(formField);
	}
	
}
