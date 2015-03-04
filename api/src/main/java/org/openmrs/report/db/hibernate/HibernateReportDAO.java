/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
 * 
 * @deprecated see reportingcompatibility module
 */
@Deprecated
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
