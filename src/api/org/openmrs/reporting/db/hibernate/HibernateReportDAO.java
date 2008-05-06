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
package org.openmrs.reporting.db.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.reporting.Report;
import org.openmrs.reporting.db.ReportDAO;

public class HibernateReportDAO implements
		ReportDAO {

	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	public HibernateReportDAO() { }

	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) { 
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.report.db.ReportService#createReport(org.openmrs.reporting.Report)
	 */
	public void createReport(Report report) throws DAOException {
		report.setCreator(Context.getAuthenticatedUser());
		report.setDateCreated(new Date());
		sessionFactory.getCurrentSession().save(report);
	}

	/**
	 * @see org.openmrs.api.EncounterService#deleteReport(org.openmrs.reporting.Report)
	 */
	public void deleteReport(Report report) throws DAOException {
		sessionFactory.getCurrentSession().delete(report);
	}

	/**
	 * @see org.openmrs.api.ReportService#getReport(java.lang.Integer)
	 */
	public Report getReport(Integer reportId) throws DAOException {
		return (Report)sessionFactory.getCurrentSession().get(Report.class, reportId);
	}

	/**
	 * @see org.openmrs.api.ReportService#updateReport(org.openmrs.reporting.Report)
	 */
	public void updateReport(Report report) throws DAOException {

		if (report.getCreator() == null)
			createReport(report);
		else
			sessionFactory.getCurrentSession().saveOrUpdate(report);
	}

	public Set<Report> getAllReports() {
		Set<Report> reports = new HashSet<Report>();
		reports.addAll((ArrayList<Report>)sessionFactory.getCurrentSession().createQuery("from Report order by date_created, name").list());
		
		return reports;
	}
}
