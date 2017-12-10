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

import java.util.List;

import org.hibernate.SessionFactory;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.TemplateDAO;
import org.openmrs.notification.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateTemplateDAO implements TemplateDAO {
	
	private static final Logger log = LoggerFactory.getLogger(HibernateTemplateDAO.class);
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	public HibernateTemplateDAO() {
	}
	
	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Template> getTemplates() {
		log.info("Getting all templates from the database");
		return sessionFactory.getCurrentSession().createQuery("from Template").list();
	}
	
	@Override
	public Template getTemplate(Integer id) {
		log.info("Get template " + id);
		return (Template) sessionFactory.getCurrentSession().get(Template.class, id);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Template> getTemplatesByName(String name) {
		log.info("Get template " + name);
		return sessionFactory.getCurrentSession().createQuery("from Template as template where template.name = ?")
		        .setString(0, name).list();
	}
	
	@Override
	public void createTemplate(Template template) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(template);
	}
	
	@Override
	public void updateTemplate(Template template) throws DAOException {
		if (template.getId() == null) {
			createTemplate(template);
		} else {
			template = (Template) sessionFactory.getCurrentSession().merge(template);
			sessionFactory.getCurrentSession().saveOrUpdate(template);
		}
	}
	
	@Override
	public void deleteTemplate(Template template) throws DAOException {
		sessionFactory.getCurrentSession().delete(template);
	}
	
}
