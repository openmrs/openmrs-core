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
package org.openmrs.report.db.hibernate;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.openmrs.report.ReportSchemaXml;
import org.openmrs.report.db.ReportDAO;

/**
 * Hibernate specific database access methods for objects in the report package
 */
public class HibernateReportDAO implements ReportDAO {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.report.db.ReportDAO#deleteReportSchemaXml(org.openmrs.report.ReportSchemaXml)
	 */
	public void deleteReportSchemaXml(ReportSchemaXml reportSchemaXml) {
		sessionFactory.getCurrentSession().delete(reportSchemaXml);
	}
	
	/**
	 * @see org.openmrs.report.db.ReportDAO#getReportSchemaXml(java.lang.Integer)
	 */
	public ReportSchemaXml getReportSchemaXml(Integer reportSchemaXmlId) {
		//return (ReportSchemaXml) sessionFactory.getCurrentSession()
		//                                       .get(ReportSchemaXml.class, reportSchemaXmlId);
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ReportSchemaXml.class, "rsx").add(
		    Expression.eq("rsx.reportSchemaId", reportSchemaXmlId));
		return (ReportSchemaXml) criteria.uniqueResult();
	}
	
	/**
	 * @see org.openmrs.report.db.ReportDAO#saveReportSchemaXml(org.openmrs.report.ReportSchemaXml)
	 */
	public void saveReportSchemaXml(ReportSchemaXml reportSchemaXml) {
		sessionFactory.getCurrentSession().saveOrUpdate(reportSchemaXml);
	}
	
	/**
	 * @see org.openmrs.report.db.ReportDAO#getReportSchemaXmls()
	 */
	@SuppressWarnings("unchecked")
	public List<ReportSchemaXml> getReportSchemaXmls() {
		return sessionFactory.getCurrentSession().createQuery("from ReportSchemaXml").list();
	}
	
}
