package org.openmrs.reporting.db.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.ReportObjectWrapper;
import org.openmrs.reporting.db.ReportObjectDAO;

public class HibernateReportObjectDAO implements
		ReportObjectDAO {

	protected final Log log = LogFactory.getLog(getClass());
	
	private Context context;
	
	public HibernateReportObjectDAO(Context c) {
		this.context = c;
	}

	public Set<AbstractReportObject> getAllReportObjects() {
		Session session = HibernateUtil.currentSession();
		
		Set<AbstractReportObject> reportObjects = new HashSet<AbstractReportObject>();
		Set<ReportObjectWrapper> wrappedObjects = new HashSet<ReportObjectWrapper>();
		wrappedObjects.addAll((ArrayList<ReportObjectWrapper>)session.createQuery("from ReportObjectWrapper order by date_created, name").list());
		for ( ReportObjectWrapper wrappedObject : wrappedObjects ) {
			AbstractReportObject reportObject = (AbstractReportObject)wrappedObject.getReportObject();
			if ( reportObject.getReportObjectId() == null ) {
				reportObject.setReportObjectId(wrappedObject.getReportObjectId());
			}
			reportObjects.add(reportObject);
		}
		return reportObjects;
	}

	public AbstractReportObject getReportObject(Integer reportObjId) throws DAOException {
		Session session = HibernateUtil.currentSession();

		ReportObjectWrapper wrappedReportObject = new ReportObjectWrapper();
		wrappedReportObject = (ReportObjectWrapper)session.get(ReportObjectWrapper.class, reportObjId);
		
		AbstractReportObject reportObject = (wrappedReportObject == null) ? null : wrappedReportObject.getReportObject();
		if ( reportObject.getReportObjectId() == null ) reportObject.setReportObjectId(wrappedReportObject.getReportObjectId());
		
		return reportObject;
	}

	public void createReportObject(AbstractReportObject reportObj) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		reportObj.setCreator(context.getAuthenticatedUser());
		reportObj.setDateCreated(new Date());
		
		ReportObjectWrapper wrappedReportObject = new ReportObjectWrapper(reportObj);
		try {
			HibernateUtil.beginTransaction();
			session.save(wrappedReportObject);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	public void deleteReportObject(AbstractReportObject reportObj) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		ReportObjectWrapper wrappedReportObject = new ReportObjectWrapper(reportObj);		
		
		try {
			HibernateUtil.beginTransaction();
			//System.out.println("\n\n\nABOUT TO DELETE REPORT OBJECT\n\n\n");
			session.delete(wrappedReportObject);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	public void updateReportObject(AbstractReportObject reportObj) throws DAOException {
		if (reportObj.getCreator() == null)
			createReportObject(reportObj);
		else {
			Session session = HibernateUtil.currentSession();
			
			ReportObjectWrapper wrappedReportObject = new ReportObjectWrapper(reportObj);		

			try {
				HibernateUtil.beginTransaction();
				wrappedReportObject = (ReportObjectWrapper)session.merge(wrappedReportObject);
				session.update(wrappedReportObject);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new DAOException(e);
			}
		}
	}

	public Set<AbstractReportObject> getReportObjectsByType(String reportObjectType) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		Set<AbstractReportObject> reportObjects = new HashSet<AbstractReportObject>();
		Set<ReportObjectWrapper> wrappedObjects = new HashSet<ReportObjectWrapper>();
		Query query = session.createQuery("from ReportObjectWrapper ro where ro.type=:type order by date_created, name");
		query.setString("type", reportObjectType);
		wrappedObjects.addAll((ArrayList<ReportObjectWrapper>)query.list());
		for ( ReportObjectWrapper wrappedObject : wrappedObjects ) {
			AbstractReportObject reportObject = (AbstractReportObject)wrappedObject.getReportObject();
			if ( reportObject.getReportObjectId() == null ) {
				reportObject.setReportObjectId(wrappedObject.getReportObjectId());
			}
			reportObjects.add(reportObject);
		}
		return reportObjects;
	}
}
