package org.openmrs.reporting.db.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.reporting.Report;
import org.openmrs.reporting.db.ReportDAO;

public class HibernateReportDAO implements
		ReportDAO {

	protected final Log log = LogFactory.getLog(getClass());
	
	private Context context;
	
	public HibernateReportDAO(Context c) {
		this.context = c;
	}

	/**
	 * @see org.openmrs.report.db.ReportService#createReport(org.openmrs.reporting.Report)
	 */
	public void createReport(Report report) throws DAOException {
		
		Session session = HibernateUtil.currentSession();
		
		report.setCreator(context.getAuthenticatedUser());
		report.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(report);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	/**
	 * @see org.openmrs.api.EncounterService#deleteReport(org.openmrs.reporting.Report)
	 */
	public void deleteReport(Report report) throws DAOException {
		
		Session session = HibernateUtil.currentSession();
		
		try {
			HibernateUtil.beginTransaction();
			session.delete(report);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
		
	}

	/**
	 * @see org.openmrs.api.ReportService#getReport(java.lang.Integer)
	 */
	public Report getReport(Integer reportId) throws DAOException {
		
		Session session = HibernateUtil.currentSession();
		
		Report report = new Report();
		report = (Report)session.get(Report.class, reportId);
		
		return report;
	}

	/**
	 * @see org.openmrs.api.ReportService#updateReport(org.openmrs.reporting.Report)
	 */
	public void updateReport(Report report) throws DAOException {

		if (report.getCreator() == null)
			createReport(report);
		else {
			Session session = HibernateUtil.currentSession();
			
			//Report.setChangedBy(context.getAuthenticatedUser());
			//Report.setDateChanged(new Date());
			try {
				HibernateUtil.beginTransaction();
				session.saveOrUpdate(report);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new DAOException(e);
			}
		}
	}

	public Set<Report> getAllReports() {
		Session session = HibernateUtil.currentSession();
		
		Set<Report> reports = new HashSet<Report>();
		reports.addAll((ArrayList<Report>)session.createQuery("from Report order by date_created, name").list());
		
		return reports;
	}
}
