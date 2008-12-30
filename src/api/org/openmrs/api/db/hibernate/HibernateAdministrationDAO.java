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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.DataEntryStatistic;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.GlobalProperty;
import org.openmrs.ImplementationId;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.api.db.DAOException;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.Report;
import org.openmrs.reporting.ReportObjectWrapper;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 * Hibernate specific database methods for the AdministrationService
 * 
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.api.db.AdministrationDAO
 * @see org.openmrs.api.AdministrationService
 */
public class HibernateAdministrationDAO implements AdministrationDAO {
	
	protected Log log = LogFactory.getLog(getClass());
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
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
	 * @see org.openmrs.api.db.AdministrationService#createReport(org.openmrs.reporting.Report)
	 */
	public void createReport(Report r) throws DAOException {
		r.setCreator(Context.getAuthenticatedUser());
		r.setDateCreated(new Date());
		sessionFactory.getCurrentSession().save(r);
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#updateReport(org.openmrs.reporting.Report)
	 */
	public void updateReport(Report r) throws DAOException {
		if (r.getReportId() == null)
			createReport(r);
		else {
			sessionFactory.getCurrentSession().saveOrUpdate(r);
		}
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#deleteReport(org.openmrs.reporting.Report)
	 */
	public void deleteReport(Report r) throws DAOException {
		sessionFactory.getCurrentSession().delete(r);
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#mrnGeneratorLog(java.lang.String,java.lang.Integer,java.lang.Integer)
	 */
	public void mrnGeneratorLog(String site, Integer start, Integer count) {
		try {
			String sql = "insert into `";
			sql += OpenmrsConstants.DATABASE_BUSINESS_NAME + "`.ext_mrn_log ";
			sql += "(date_generated, generated_by, site, mrn_first, mrn_count) values (?, ?, ?, ?, ?)";
			
			PreparedStatement ps = sessionFactory.getCurrentSession().connection().prepareStatement(sql);
			
			ps.setTimestamp(1, new Timestamp(new Date().getTime()));
			ps.setInt(2, Context.getAuthenticatedUser().getUserId());
			ps.setString(3, site);
			ps.setInt(4, start);
			ps.setInt(5, count);
			ps.execute();
		}
		catch (Exception e) {
			throw new DAOException("Error generating mrn log", e);
		}
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#getMRNGeneratorLog()
	 */
	public Collection getMRNGeneratorLog() {
		Collection<Map<String, Object>> log = new Vector<Map<String, Object>>();
		
		try {
			Map<String, Object> row;
			
			String sql = "select * from `";
			sql += OpenmrsConstants.DATABASE_BUSINESS_NAME + "`.ext_mrn_log ";
			sql += "order by mrn_log_id desc";
			
			PreparedStatement ps = sessionFactory.getCurrentSession().connection().prepareStatement(sql);
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				row = new HashMap<String, Object>();
				row.put("date", rs.getTimestamp("date_generated"));
				row.put("user", rs.getString("generated_by"));
				row.put("site", rs.getString("site"));
				row.put("first", rs.getInt("mrn_first"));
				row.put("count", rs.getInt("mrn_count"));
				log.add(row);
			}
		}
		catch (Exception e) {
			throw new DAOException("Error getting mrn log", e);
		}
		
		return log;
	}
	
	public void createReportObject(AbstractReportObject ro) throws DAOException {
		
		ReportObjectWrapper wrappedReportObject = new ReportObjectWrapper(ro);
		wrappedReportObject.setCreator(Context.getAuthenticatedUser());
		wrappedReportObject.setDateCreated(new Date());
		wrappedReportObject.setVoided(false);
		
		sessionFactory.getCurrentSession().save(wrappedReportObject);
	}
	
	public void updateReportObject(AbstractReportObject ro) throws DAOException {
		if (ro.getReportObjectId() == null)
			createReportObject(ro);
		else {
			sessionFactory.getCurrentSession().clear();
			ReportObjectWrapper wrappedReportObject = new ReportObjectWrapper(ro);
			wrappedReportObject.setChangedBy(Context.getAuthenticatedUser());
			wrappedReportObject.setDateChanged(new Date());
			sessionFactory.getCurrentSession().saveOrUpdate(wrappedReportObject);
		}
	}
	
	public void deleteReportObject(Integer reportObjectId) throws DAOException {
		ReportObjectWrapper wrappedReportObject = new ReportObjectWrapper();
		wrappedReportObject = (ReportObjectWrapper) sessionFactory.getCurrentSession().get(ReportObjectWrapper.class,
		    reportObjectId);
		
		sessionFactory.getCurrentSession().delete(wrappedReportObject);
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#getGlobalProperty(java.lang.String)
	 */
	public String getGlobalProperty(String propertyName) throws DAOException {
		GlobalProperty gp = (GlobalProperty) sessionFactory.getCurrentSession().get(GlobalProperty.class, propertyName);
		
		// if no gp exists, return a null value
		if (gp == null)
			return null;
		
		return gp.getPropertyValue();
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#getGlobalPropertyObject(java.lang.String)
	 */
	public GlobalProperty getGlobalPropertyObject(String propertyName) {
		GlobalProperty gp = (GlobalProperty) sessionFactory.getCurrentSession().get(GlobalProperty.class, propertyName);
		
		// if no gp exists, hibernate returns a null value
		
		return gp;
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#getAllGlobalProperties()
	 */
	@SuppressWarnings("unchecked")
	public List<GlobalProperty> getAllGlobalProperties() throws DAOException {
		return sessionFactory.getCurrentSession().createCriteria(GlobalProperty.class).list();
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#deleteGlobalProperty(GlobalProperty)
	 */
	public void deleteGlobalProperty(GlobalProperty property) throws DAOException {
		sessionFactory.getCurrentSession().delete(property);
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#saveGlobalProperty(org.openmrs.GlobalProperty)
	 */
	public GlobalProperty saveGlobalProperty(GlobalProperty gp) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(gp);
		return gp;
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#getDataEntryStatistics(java.util.Date,
	 *      java.util.Date, java.lang.String, java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<DataEntryStatistic> getDataEntryStatistics(Date fromDate, Date toDate, String encounterColumn,
	                                                       String orderColumn, String groupBy) throws DAOException {
		
		// for all encounters, find user, form name, and number of entries
		
		// default userColumn to creator
		if (encounterColumn == null)
			encounterColumn = "creator";
		encounterColumn = encounterColumn.toLowerCase();
		
		List<DataEntryStatistic> ret = new ArrayList<DataEntryStatistic>();
		
		/*
		if (groupBy == null) groupBy = "";
		if (groupBy.length() != 0)
			groupBy = "enc." + groupBy;
		
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(Encounter.class, "enc");
		
		ProjectionList projections = Projections.projectionList();
		if (groupBy.length() > 0)
			projections.add(Projections.groupProperty(groupBy));
		projections.add(Projections.groupProperty("enc." + encounterColumn));
		projections.add(Projections.groupProperty("enc.form"));
		projections.add(Projections.groupProperty("enc.encounterType"));
		projections.add(Projections.count("enc." + encounterColumn));
		
		crit.setProjection(projections);
		
		if (fromDate != null)
			crit.add(Expression.ge("enc.dateCreated", fromDate));
		if (toDate != null) {
			crit.add(Expression.le("enc.dateCreated", toDate));
		}
		
		List<Object[]> l = crit.list();
		for (Object[] holder : l) {
			DataEntryStatistic s = new DataEntryStatistic();
			int offset = 0;
			if (groupBy.length() > 0) {
				s.setGroupBy(holder[0]);
				offset = 1;
			}
			
			s.setUser((User) holder[0 + offset]);
			Form form = (Form)holder[1 + offset];
			EncounterType encType = (EncounterType)holder[2 + offset];
			s.setEntryType(form != null ? form.getName() : (encType != null ? encType.getName() : "null" ));
			s.setNumberOfEntries((Integer) holder[3 + offset]);
			log.debug("OLD Num encounters is " + s.getNumberOfEntries());
			s.setNumberOfObs(0);
			ret.add(s);
		}
		*/

		// data entry stats with extended info
		// check if there's anything else to group by
		if (groupBy == null)
			groupBy = "";
		if (groupBy.length() != 0)
			groupBy = "e." + groupBy + ", ";
		log.debug("GROUP BY IS " + groupBy);
		
		String hql = "select " + groupBy + "e." + encounterColumn + ", e.encounterType"
		        + ", e.form, count(distinct e.encounterId), count(o.obsId) " + "from Obs o right join o.encounter as e ";
		if (fromDate != null || toDate != null) {
			String s = "where ";
			if (fromDate != null)
				s += "e.dateCreated >= :fromDate ";
			if (toDate != null) {
				if (fromDate != null)
					s += "and ";
				s += "e.dateCreated <= :toDate ";
			}
			hql += s;
		}
		
		hql += "group by ";
		if (groupBy.length() > 0)
			hql += groupBy + " ";
		hql += "e." + encounterColumn + ", e.encounterType, e.form ";
		Query q = sessionFactory.getCurrentSession().createQuery(hql);
		if (fromDate != null)
			q.setParameter("fromDate", fromDate);
		if (toDate != null)
			q.setParameter("toDate", toDate);
		List<Object[]> l = q.list();
		for (Object[] holder : l) {
			DataEntryStatistic s = new DataEntryStatistic();
			int offset = 0;
			if (groupBy.length() > 0) {
				s.setGroupBy(holder[0]);
				offset = 1;
			}
			
			s.setUser((User) holder[0 + offset]);
			EncounterType encType = ((EncounterType) holder[1 + offset]);
			Form form = ((Form) holder[2 + offset]);
			s.setEntryType(form != null ? form.getName() : (encType != null ? encType.getName() : "null"));
			int numEncounters = ((Number) holder[3 + offset]).intValue();
			int numObs = ((Number) holder[4 + offset]).intValue();
			s.setNumberOfEntries(numEncounters); // not sure why this comes out as a Long instead of an Integer
			log.debug("NEW Num encounters is " + numEncounters);
			s.setNumberOfObs(numObs);
			log.debug("NEW Num obs is " + numObs);
			ret.add(s);
		}
		
		// default userColumn to creator
		if (orderColumn == null)
			orderColumn = "creator";
		orderColumn = orderColumn.toLowerCase();
		
		// for orders, count how many were created. (should eventually count something with voided/changed)
		hql = "select o." + orderColumn + ", o.orderType.name, count(*) " + "from Order o ";
		if (fromDate != null || toDate != null) {
			String s = "where ";
			if (fromDate != null)
				s += "o.dateCreated >= :fromDate ";
			if (toDate != null) {
				if (fromDate != null)
					s += "and ";
				s += "o.dateCreated <= :toDate ";
			}
			hql += s;
		}
		hql += "group by o." + orderColumn + ", o.orderType.name ";
		q = sessionFactory.getCurrentSession().createQuery(hql);
		if (fromDate != null)
			q.setParameter("fromDate", fromDate);
		if (toDate != null)
			q.setParameter("toDate", toDate);
		l = q.list();
		for (Object[] holder : l) {
			DataEntryStatistic s = new DataEntryStatistic();
			s.setUser((User) holder[0]);
			s.setEntryType((String) holder[1]);
			s.setNumberOfEntries(((Number) holder[2]).intValue()); // not sure why this comes out as a Long instead of an Integer
			s.setNumberOfObs(0);
			ret.add(s);
		}
		
		return ret;
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#executeSQL(java.lang.String, boolean)
	 */
	public List<List<Object>> executeSQL(String sql, boolean selectOnly) throws DAOException {
		sql = sql.trim();
		boolean dataManipulation = false;
		
		String sqlLower = sql.toLowerCase();
		if (sqlLower.startsWith("insert") || sqlLower.startsWith("update") || sqlLower.startsWith("delete")
		        || sqlLower.startsWith("alter") || sqlLower.startsWith("drop") || sqlLower.startsWith("create")
		        || sqlLower.startsWith("rename")) {
			dataManipulation = true;
		}
		
		if (selectOnly && dataManipulation)
			throw new DAOException("Illegal command(s) found in query string");
		
		// (solution for junit tests that usually use hsql
		// hsql does not like the backtick.  Replace the backtick with the hsql
		// escape character: the double quote (or nothing).
		if (HibernateUtil.isHSQLDialect(sessionFactory))
			sql = sql.replace("`", "");
		
		Connection conn = sessionFactory.getCurrentSession().connection();
		PreparedStatement ps = null;
		List<List<Object>> results = new Vector<List<Object>>();
		
		try {
			ps = conn.prepareStatement(sql);
			
			if (dataManipulation == true) {
				Integer i = ps.executeUpdate();
				List<Object> row = new Vector<Object>();
				row.add(i);
				results.add(row);
			} else {
				ResultSet resultSet = ps.executeQuery();
				
				ResultSetMetaData rmd = resultSet.getMetaData();
				int columnCount = rmd.getColumnCount();
				
				while (resultSet.next()) {
					List<Object> rowObjects = new Vector<Object>();
					for (int x = 1; x <= columnCount; x++) {
						rowObjects.add(resultSet.getObject(x));
					}
					results.add(rowObjects);
				}
			}
		}
		catch (Exception e) {
			log.error("Error while running sql: " + sql, e);
			throw new DAOException("Error while running sql: " + sql + " . Message: " + e.getMessage(), e);
		}
		
		return results;
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#getImplementationId()
	 */
	public ImplementationId getImplementationId() {
		
		String property = getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_IMPLEMENTATION_ID);
		
		// fail early if no gp has been defined yet
		if (property == null)
			return null;
		
		try {
			ImplementationId implId = OpenmrsUtil.getSerializer().read(ImplementationId.class, property);
			
			return implId;
		}
		catch (Throwable t) {
			log.debug("Error while getting implementation id", t);
		}
		
		return null;
		
	}
	
}
