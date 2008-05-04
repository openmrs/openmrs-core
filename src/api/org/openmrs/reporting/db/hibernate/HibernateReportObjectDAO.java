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
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.ReportObjectWrapper;
import org.openmrs.reporting.db.ReportObjectDAO;

public class HibernateReportObjectDAO implements
		ReportObjectDAO {

	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	public HibernateReportObjectDAO() { }

	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) { 
		this.sessionFactory = sessionFactory;
	}
	
	public List<AbstractReportObject> getAllReportObjects() {


		
		List<AbstractReportObject> reportObjects = new Vector<AbstractReportObject>();
		//List<ReportObjectWrapper> wrappedObjects = new Vector<ReportObjectWrapper>();
		//wrappedObjects.addAll((ArrayList<ReportObjectWrapper>)sessionFactory.getCurrentSession().createQuery("from ReportObjectWrapper order by date_created, name").list());
		List<ReportObjectWrapper> wrappedObjects = sessionFactory.getCurrentSession().createQuery("from ReportObjectWrapper order by date_created, name").list();
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
		ReportObjectWrapper wrappedReportObject = (ReportObjectWrapper)sessionFactory.getCurrentSession().get(ReportObjectWrapper.class, reportObjId);

		if (wrappedReportObject == null)
			return null;
		
		AbstractReportObject reportObject = wrappedReportObject.getReportObject();
		if ( reportObject.getReportObjectId() == null )
			reportObject.setReportObjectId(wrappedReportObject.getReportObjectId());
		
		return reportObject;
	}

	public Integer createReportObject(AbstractReportObject reportObj) throws DAOException {
		log.debug("Saving: " + reportObj);
		
		ReportObjectWrapper wrappedReportObject = new ReportObjectWrapper(reportObj);
		wrappedReportObject.setCreator(Context.getAuthenticatedUser());
		wrappedReportObject.setDateCreated(new Date());
		sessionFactory.getCurrentSession().save(wrappedReportObject);
		return wrappedReportObject.getReportObjectId();
	}

	public void deleteReportObject(AbstractReportObject reportObj) throws DAOException {
		ReportObjectWrapper wrappedReportObject = new ReportObjectWrapper(reportObj);
		// TODO - The creator/created date needs to be set here otherwise we get an exception
		// This doesn't matter really since we're just deleting the report object anyway
		wrappedReportObject.setCreator(Context.getAuthenticatedUser());
		wrappedReportObject.setDateCreated(new Date());
		sessionFactory.getCurrentSession().delete(wrappedReportObject);
	}

	public void updateReportObject(AbstractReportObject reportObj) throws DAOException {
		if (reportObj.getReportObjectId() == null)
			createReportObject(reportObj);
		else {
			log.debug("ATTEMPTING TO SAVE reportObj: " + reportObj);
			//ReportObjectWrapper wrappedReportObject = new ReportObjectWrapper(reportObj);		
			ReportObjectWrapper wrappedReportObject = 
				(ReportObjectWrapper)sessionFactory.getCurrentSession().get(ReportObjectWrapper.class, reportObj.getReportObjectId());
			wrappedReportObject.setReportObject(reportObj);
			wrappedReportObject.setChangedBy(Context.getAuthenticatedUser());
			wrappedReportObject.setDateChanged(new Date());

			//wrappedReportObject = (ReportObjectWrapper)sessionFactory.getCurrentSession().merge(wrappedReportObject);
			sessionFactory.getCurrentSession().update(wrappedReportObject);
		}
	}

	public List<AbstractReportObject> getReportObjectsByType(String reportObjectType) throws DAOException {
		List<AbstractReportObject> reportObjects = new Vector<AbstractReportObject>();

		Query query = sessionFactory.getCurrentSession().createQuery("from ReportObjectWrapper ro where ro.type=:type order by date_created, name");
		query.setString("type", reportObjectType);
		List<ReportObjectWrapper> wrappedObjects = query.list();
		for ( ReportObjectWrapper wrappedObject : wrappedObjects ) {
			AbstractReportObject reportObject = wrappedObject.getReportObject();
			if ( reportObject.getReportObjectId() == null ) {
				reportObject.setReportObjectId(wrappedObject.getReportObjectId());
			}
			reportObjects.add(reportObject);
		}
		return reportObjects;
	}
}
