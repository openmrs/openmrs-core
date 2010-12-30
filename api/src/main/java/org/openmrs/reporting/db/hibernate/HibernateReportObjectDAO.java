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

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.ReportObjectWrapper;
import org.openmrs.reporting.db.ReportObjectDAO;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class HibernateReportObjectDAO implements ReportObjectDAO {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	public HibernateReportObjectDAO() {
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
	 * @see org.openmrs.reporting.db.ReportObjectDAO#getAllReportObjects()
	 */
	@SuppressWarnings("unchecked")
	public List<AbstractReportObject> getAllReportObjects() {
		List<AbstractReportObject> reportObjects = new Vector<AbstractReportObject>();
		//List<ReportObjectWrapper> wrappedObjects = new Vector<ReportObjectWrapper>();
		//wrappedObjects.addAll((ArrayList<ReportObjectWrapper>)sessionFactory.getCurrentSession().createQuery("from ReportObjectWrapper order by date_created, name").list());
		List<ReportObjectWrapper> wrappedObjects = sessionFactory.getCurrentSession().createQuery(
		    "from ReportObjectWrapper order by date_created, name").list();
		for (ReportObjectWrapper wrappedObject : wrappedObjects) {
			try {
				AbstractReportObject reportObject = (AbstractReportObject) wrappedObject.getReportObject();
				if (reportObject.getReportObjectId() == null) {
					reportObject.setReportObjectId(wrappedObject.getReportObjectId());
				}
				reportObjects.add(reportObject);
			}
			catch (Exception ex) {
				log.error("Error retrieving report object with id=" + wrappedObject.getReportObjectId(), ex);
			}
		}
		return reportObjects;
	}
	
	/**
	 * @see org.openmrs.reporting.db.ReportObjectDAO#getReportObject(java.lang.Integer)
	 */
	public AbstractReportObject getReportObject(Integer reportObjId) throws DAOException {
		ReportObjectWrapper wrappedReportObject = (ReportObjectWrapper) sessionFactory.getCurrentSession().get(
		    ReportObjectWrapper.class, reportObjId);
		
		if (wrappedReportObject == null)
			return null;
		
		AbstractReportObject reportObject = wrappedReportObject.getReportObject();
		if (reportObject.getReportObjectId() == null)
			reportObject.setReportObjectId(wrappedReportObject.getReportObjectId());
		
		return reportObject;
	}
	
	/**
	 * @see org.openmrs.reporting.db.ReportObjectDAO#deleteReportObject(org.openmrs.reporting.AbstractReportObject)
	 */
	public void deleteReportObject(AbstractReportObject reportObj) throws DAOException {
		ReportObjectWrapper wrappedReportObject = new ReportObjectWrapper(reportObj);
		// TODO - The creator/created date needs to be set here otherwise we get an exception
		// This doesn't matter really since we're just deleting the report object anyway
		User user = Context.getAuthenticatedUser();
		Date now = new Date();
		wrappedReportObject.setCreator(user);
		wrappedReportObject.setDateCreated(now);
		sessionFactory.getCurrentSession().delete(wrappedReportObject);
	}
	
	/**
	 * @see org.openmrs.reporting.db.ReportObjectDAO#saveReportObject(org.openmrs.reporting.AbstractReportObject)
	 */
	public AbstractReportObject saveReportObject(AbstractReportObject reportObj) throws DAOException {
		
		ReportObjectWrapper wrappedReportObject;
		
		User user = Context.getAuthenticatedUser();
		Date now = new Date();
		if (reportObj.getReportObjectId() == null) {
			wrappedReportObject = new ReportObjectWrapper(reportObj);
			wrappedReportObject.setCreator(user);
			wrappedReportObject.setDateCreated(now);
			wrappedReportObject.setUuid(UUID.randomUUID().toString());
			
		} else {
			wrappedReportObject = (ReportObjectWrapper) sessionFactory.getCurrentSession().get(ReportObjectWrapper.class,
			    reportObj.getReportObjectId());
			wrappedReportObject.setReportObject(reportObj);
			wrappedReportObject.setChangedBy(user);
			wrappedReportObject.setDateChanged(now);
			wrappedReportObject.setUuid(reportObj.getUuid());
		}
		
		//wrappedReportObject = (ReportObjectWrapper)sessionFactory.getCurrentSession().merge(wrappedReportObject);
		sessionFactory.getCurrentSession().saveOrUpdate(wrappedReportObject);
		
		reportObj.setReportObjectId(wrappedReportObject.getReportObjectId());
		return reportObj;
	}
	
	/**
	 * @see org.openmrs.reporting.db.ReportObjectDAO#getReportObjectsByType(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<AbstractReportObject> getReportObjectsByType(String reportObjectType) throws DAOException {
		List<AbstractReportObject> reportObjects = new Vector<AbstractReportObject>();
		
		Query query = sessionFactory.getCurrentSession().createQuery(
		    "from ReportObjectWrapper ro where ro.type=:type order by date_created, name");
		query.setString("type", reportObjectType);
		List<ReportObjectWrapper> wrappedObjects = query.list();
		for (ReportObjectWrapper wrappedObject : wrappedObjects) {
			try {
				AbstractReportObject reportObject = wrappedObject.getReportObject();
				if (reportObject.getReportObjectId() == null) {
					reportObject.setReportObjectId(wrappedObject.getReportObjectId());
				}
				reportObjects.add(reportObject);
			}
			catch (Exception e) {
				// Catch exceptions if there are exceptions deserializing any individual report object, and warn
				log.warn("Unable to deserialize report object: " + wrappedObject.getName() + " (" + wrappedObject.getId()
				        + ")");
			}
		}
		return reportObjects;
	}
}
