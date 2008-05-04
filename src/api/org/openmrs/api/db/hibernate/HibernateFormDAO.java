/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api.db.hibernate;

import java.util.Collection;
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
	 * Returns the form object originally passed in, which will have been persisted.
	 * @see org.openmrs.api.db.FormService#createForm(org.openmrs.Form)
	 */
	public Form createForm(Form form) throws DAOException {
		sessionFactory.getCurrentSession().save(form);
		return form;
	}
	
	/**
	 * @see org.openmrs.api.db.FormService#duplicateForm(org.openmrs.Form)
	 */
	public Form duplicateForm(Form form) throws DAOException {
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
	 * @see org.openmrs.api.FormService#getFormField(org.openmrs.Form, org.openmrs.Concept, java.util.Collection, boolean)
	 */
	@SuppressWarnings("unchecked")
    public FormField getFormField(Form form, Concept concept, Collection<FormField> ignoreFormFields, boolean force) throws APIException {
		if (form == null) {
			log.debug("form is null, no fields will be matched");
			return null;
		}
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(FormField.class, "ff")
			.createAlias("field", "field")
			.add(Expression.eq("field.concept", concept))
			.add(Expression.eq("form", form));
		
		// get the list of all formfields with this concept for this form
		List<FormField> formFields = crit.list();
		
		String err = "FormField warning.  No FormField matching concept '" + concept + "' for form '" + form + "'";
		
		if (formFields.size() < 1) {
			log.debug(err);
			return null;
		}
		
		// save the first formfield in case we're not a in a "force" situation
		FormField backupPlan = formFields.get(0);
		
		// remove the formfields we're supposed to ignore from the return list
		formFields.removeAll(ignoreFormFields);
		
		// if we ended up removing all of the formfields, check to see if we're
		// in a "force" situation
		if (formFields.size() < 1) {
			if (force == false)
				return backupPlan;
			else {
				log.debug(err);
				return null;
			}
		}
		else { // if formFields.size() is still greater than 0
			FormField ff = (FormField)formFields.get(0); 
			return ff;
		}
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
		crit.addOrder(Order.desc("formId"));
		
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

	/**
     * @see org.openmrs.api.db.FormDAO#findForms(java.lang.String, boolean, boolean)
     */
	@SuppressWarnings("unchecked")
	public List<Form> findForms(String text, boolean includeUnpublished, boolean includeRetired) {
    	Criteria crit = sessionFactory.getCurrentSession().createCriteria(Form.class);
		
		if (includeUnpublished == false)
			crit.add(Expression.eq("published", true));
		
		if (!includeRetired)
			crit.add(Expression.eq("retired", false));
		
		crit.add(Expression.like("name", text, MatchMode.ANYWHERE));
		
		crit.addOrder(Order.asc("name"));
		crit.addOrder(Order.desc("formId"));
		
		return crit.list();
    }
	
}
