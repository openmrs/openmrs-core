/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.StringType;
import org.hibernate.type.TextType;
import org.hibernate.type.Type;
import org.openmrs.GlobalProperty;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.APIException;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.api.db.DAOException;
import org.openmrs.util.DatabaseUtil;
import org.openmrs.util.HandlerUtil;
import org.openmrs.util.OpenmrsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Hibernate specific database methods for the AdministrationService
 *
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.api.db.AdministrationDAO
 * @see org.openmrs.api.AdministrationService
 */
public class HibernateAdministrationDAO implements AdministrationDAO, ApplicationContextAware {
	
	private static final Logger log = LoggerFactory.getLogger(HibernateAdministrationDAO.class);
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	private Configuration configuration;
	
	private ApplicationContext applicationContext;
	
	public HibernateAdministrationDAO() {
	}
	
	/**
	 * Set session factory
	 *
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#getGlobalProperty(java.lang.String)
	 */
	@Override
	public String getGlobalProperty(String propertyName) throws DAOException {
		GlobalProperty gp = getGlobalPropertyObject(propertyName);
		
		// if no gp exists, return a null value
		if (gp == null) {
			return null;
		}
		
		return gp.getPropertyValue();
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#getGlobalPropertyObject(java.lang.String)
	 */
	@Override
	public GlobalProperty getGlobalPropertyObject(String propertyName) {
		if (isDatabaseStringComparisonCaseSensitive()) {
			Criteria criteria = sessionFactory.getCurrentSession().createCriteria(GlobalProperty.class);
			return (GlobalProperty) criteria.add(Restrictions.eq("property", propertyName).ignoreCase())
			        .uniqueResult();
		} else {
			return (GlobalProperty) sessionFactory.getCurrentSession().get(GlobalProperty.class, propertyName);
		}
	}
	
	@Override
	public GlobalProperty getGlobalPropertyByUuid(String uuid) throws DAOException {

		return (GlobalProperty) sessionFactory.getCurrentSession()
		        .createQuery("from GlobalProperty t where t.uuid = :uuid").setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#getAllGlobalProperties()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<GlobalProperty> getAllGlobalProperties() throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(GlobalProperty.class);
		return criteria.addOrder(Order.asc("property")).list();
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#getGlobalPropertiesByPrefix(java.lang.String)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<GlobalProperty> getGlobalPropertiesByPrefix(String prefix) {
		return sessionFactory.getCurrentSession().createCriteria(GlobalProperty.class)
		        .add(Restrictions.ilike("property", prefix, MatchMode.START)).list();
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#getGlobalPropertiesBySuffix(java.lang.String)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<GlobalProperty> getGlobalPropertiesBySuffix(String suffix) {
		return sessionFactory.getCurrentSession().createCriteria(GlobalProperty.class)
		        .add(Restrictions.ilike("property", suffix, MatchMode.END)).list();
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#deleteGlobalProperty(GlobalProperty)
	 */
	@Override
	public void deleteGlobalProperty(GlobalProperty property) throws DAOException {
		sessionFactory.getCurrentSession().delete(property);
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#saveGlobalProperty(org.openmrs.GlobalProperty)
	 */
	@Override
	public GlobalProperty saveGlobalProperty(GlobalProperty gp) throws DAOException {
		GlobalProperty gpObject = getGlobalPropertyObject(gp.getProperty());
		if (gpObject != null) {
			gpObject.setPropertyValue(gp.getPropertyValue());
			gpObject.setDescription(gp.getDescription());
			sessionFactory.getCurrentSession().update(gpObject);
			return gpObject;
		} else {
			sessionFactory.getCurrentSession().save(gp);
			return gp;
		}
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#executeSQL(java.lang.String, boolean)
	 */
	@Override
	public List<List<Object>> executeSQL(String sql, boolean selectOnly) throws DAOException {
		
		// (solution for junit tests that usually use hsql
		// hsql does not like the backtick.  Replace the backtick with the hsql
		// escape character: the double quote (or nothing).
		if (HibernateUtil.isHSQLDialect(sessionFactory)) {
			sql = sql.replace("`", "");
		}
		return DatabaseUtil.executeSQL(sessionFactory.getCurrentSession(), sql, selectOnly);
	}
	
	@Override
	public int getMaximumPropertyLength(Class<? extends OpenmrsObject> aClass, String fieldName) {
		if (configuration == null) {
			HibernateSessionFactoryBean sessionFactoryBean = (HibernateSessionFactoryBean) applicationContext
			        .getBean("&sessionFactory");
			configuration = sessionFactoryBean.getConfiguration();
		}
		
		PersistentClass persistentClass = configuration.getClassMapping(aClass.getName().split("_")[0]);
		if (persistentClass == null) {
			throw new APIException("Couldn't find a class in the hibernate configuration named: " + aClass.getName());
		} else {
			int fieldLength;
			try {
				fieldLength = ((Column) persistentClass.getProperty(fieldName).getColumnIterator().next()).getLength();
			}
			catch (Exception e) {
				log.debug("Could not determine maximum length", e);
				return -1;
			}
			return fieldLength;
		}
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#validate(java.lang.Object, Errors)
	 * @should Pass validation if field lengths are correct
	 * @should Fail validation if field lengths are not correct
	 * @should Fail validation for location class if field lengths are not correct
	 * @should Pass validation for location class if field lengths are correct
	 */
	
	//@SuppressWarnings({ "deprecation", "unchecked", "rawtypes" })
	@Override
	public void validate(Object object, Errors errors) throws DAOException {
		Class entityClass = object.getClass();
		ClassMetadata metadata = sessionFactory.getClassMetadata(entityClass);
		if (metadata != null) {
			String[] propNames = metadata.getPropertyNames();
			Object identifierType = metadata.getIdentifierType();
			String identifierName = metadata.getIdentifierPropertyName();
			if (identifierType instanceof StringType || identifierType instanceof TextType) {
				int maxLength = getMaximumPropertyLength(entityClass, identifierName);
				String identifierValue = (String) metadata.getIdentifier(object,
				    (SessionImplementor) sessionFactory.getCurrentSession());
				if (identifierValue != null) {
					int identifierLength = identifierValue.length();
					if (identifierLength > maxLength) {
						
						errors.rejectValue(identifierName, "error.exceededMaxLengthOfField", new Object[] { maxLength },
						    null);
					}
				}
			}
			for (String propName : propNames) {
				Type propType = metadata.getPropertyType(propName);
				if (propType instanceof StringType || propType instanceof TextType) {
					String propertyValue = (String) metadata.getPropertyValue(object, propName);
					if (propertyValue != null) {
						int maxLength = getMaximumPropertyLength(entityClass, propName);
						int propertyValueLength = propertyValue.length();
						if (propertyValueLength > maxLength) {
							errors.rejectValue(propName, "error.exceededMaxLengthOfField", new Object[] { maxLength },
									null);
						}
					}
				}
			}
		}
		FlushMode previousFlushMode = sessionFactory.getCurrentSession().getFlushMode();
		sessionFactory.getCurrentSession().setFlushMode(FlushMode.MANUAL);
		try {
			for (Validator validator : getValidators(object)) {
				validator.validate(object, errors);
			}
			
		}
		
		finally {
			sessionFactory.getCurrentSession().setFlushMode(previousFlushMode);
		}
		
	}
	
	/**
	 * Fetches all validators that are registered
	 *
	 * @param obj the object that will be validated
	 * @return list of compatible validators
	 */
	protected List<Validator> getValidators(Object obj) {
		List<Validator> matchingValidators = new ArrayList<>();

		List<Validator> validators = HandlerUtil.getHandlersForType(Validator.class, obj.getClass());
		
		for (Validator validator : validators) {
			if (validator.supports(obj.getClass())) {
				matchingValidators.add(validator);
			}
		}
		
		return matchingValidators;
	}
	
	@Override
	public boolean isDatabaseStringComparisonCaseSensitive() {
		GlobalProperty gp = (GlobalProperty) sessionFactory.getCurrentSession().get(GlobalProperty.class,
		    OpenmrsConstants.GP_CASE_SENSITIVE_DATABASE_STRING_COMPARISON);
		if (gp != null) {
			return Boolean.valueOf(gp.getPropertyValue());
		} else {
			return true;
		}
	}
}
