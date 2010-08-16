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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.GlobalProperty;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.api.db.DAOException;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.Report;
import org.openmrs.reporting.ReportObjectWrapper;
import org.openmrs.util.OpenmrsConstants;

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
	 * @see org.openmrs.api.AdministrationService#createReport(org.openmrs.reporting.Report)
	 * @deprecated see reportingcompatibility module
	 */
	@Deprecated
	public void createReport(Report r) throws DAOException {
		r.setCreator(Context.getAuthenticatedUser());
		r.setDateCreated(new Date());
		sessionFactory.getCurrentSession().save(r);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#updateReport(org.openmrs.reporting.Report)
	 * @deprecated see reportingcompatibility module
	 */
	@Deprecated
	public void updateReport(Report r) throws DAOException {
		if (r.getReportId() == null)
			createReport(r);
		else {
			sessionFactory.getCurrentSession().saveOrUpdate(r);
		}
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#deleteReport(org.openmrs.reporting.Report)
	 * @deprecated see reportingcompatibility module
	 */
	@Deprecated
	public void deleteReport(Report r) throws DAOException {
		sessionFactory.getCurrentSession().delete(r);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#mrnGeneratorLog(java.lang.String,java.lang.Integer,java.lang.Integer)
	 */
	public void mrnGeneratorLog(String site, Integer start, Integer count) {
		PreparedStatement ps = null;
		try {
			String sql = "insert into `";
			sql += OpenmrsConstants.DATABASE_BUSINESS_NAME + "`.ext_mrn_log ";
			sql += "(date_generated, generated_by, site, mrn_first, mrn_count) values (?, ?, ?, ?, ?)";
			
			ps = sessionFactory.getCurrentSession().connection().prepareStatement(sql);
			
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
		finally {
			if (ps != null) {
				try {
					ps.close();
				}
				catch (SQLException e) {
					log.error("Error generated while closing statement", e);
				}
			}
		}
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#getMRNGeneratorLog()
	 */
	public Collection getMRNGeneratorLog() {
		Collection<Map<String, Object>> logs = new Vector<Map<String, Object>>();
		
		PreparedStatement ps = null;
		try {
			Map<String, Object> row;
			
			String sql = "select * from `";
			sql += OpenmrsConstants.DATABASE_BUSINESS_NAME + "`.ext_mrn_log ";
			sql += "order by mrn_log_id desc";
			
			ps = sessionFactory.getCurrentSession().connection().prepareStatement(sql);
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				row = new HashMap<String, Object>();
				row.put("date", rs.getTimestamp("date_generated"));
				row.put("user", rs.getString("generated_by"));
				row.put("site", rs.getString("site"));
				row.put("first", rs.getInt("mrn_first"));
				row.put("count", rs.getInt("mrn_count"));
				logs.add(row);
			}
		}
		catch (Exception e) {
			throw new DAOException("Error getting mrn log", e);
		}
		finally {
			if (ps != null) {
				try {
					ps.close();
				}
				catch (SQLException e) {
					log.error("Error generated while closing statement", e);
				}
			}
		}
		
		return logs;
	}
	
	/**
	 * @deprecated see reportingcompatibility module
	 */
	@Deprecated
	public void createReportObject(AbstractReportObject ro) throws DAOException {
		
		ReportObjectWrapper wrappedReportObject = new ReportObjectWrapper(ro);
		User user = Context.getAuthenticatedUser();
		Date now = new Date();
		wrappedReportObject.setCreator(user);
		wrappedReportObject.setDateCreated(now);
		wrappedReportObject.setVoided(false);
		sessionFactory.getCurrentSession().save(wrappedReportObject);
	}
	
	/**
	 * @deprecated see reportingcompatibility module
	 */
	@Deprecated
	public void updateReportObject(AbstractReportObject ro) throws DAOException {
		if (ro.getReportObjectId() == null)
			createReportObject(ro);
		else {
			sessionFactory.getCurrentSession().clear();
			ReportObjectWrapper wrappedReportObject = new ReportObjectWrapper(ro);
			User user = Context.getAuthenticatedUser();
			Date now = new Date();
			wrappedReportObject.setChangedBy(user);
			wrappedReportObject.setDateChanged(now);
			
			sessionFactory.getCurrentSession().saveOrUpdate(wrappedReportObject);
		}
	}
	
	/**
	 * @deprecated see reportingcompatibility module
	 */
	@Deprecated
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
		GlobalProperty gp = getGlobalPropertyObject(propertyName);
		
		// if no gp exists, return a null value
		if (gp == null)
			return null;
		
		return gp.getPropertyValue();
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#getGlobalPropertyObject(java.lang.String)
	 */
	public GlobalProperty getGlobalPropertyObject(String propertyName) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(GlobalProperty.class);
		GlobalProperty gp = (GlobalProperty) criteria.add(Restrictions.eq("property", propertyName)).uniqueResult();

		// if no gp exists, hibernate returns a null value
		
		return gp;
	}
	
	public GlobalProperty getGlobalPropertyByUuid(String uuid) throws DAOException {
		GlobalProperty gp = (GlobalProperty) sessionFactory.getCurrentSession().createQuery(
		    "from GlobalProperty t where t.uuid = :uuid").setString("uuid", uuid).uniqueResult();
		
		return gp;
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#getAllGlobalProperties()
	 */
	@SuppressWarnings("unchecked")
	public List<GlobalProperty> getAllGlobalProperties() throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(GlobalProperty.class);
		return criteria.addOrder(Order.asc("property")).list();
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#getGlobalPropertiesByPrefix(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<GlobalProperty> getGlobalPropertiesByPrefix(String prefix) {
		return sessionFactory.getCurrentSession().createCriteria(GlobalProperty.class).add(
		    Restrictions.like("property", prefix, MatchMode.START)).list();
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#getGlobalPropertiesBySuffix(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<GlobalProperty> getGlobalPropertiesBySuffix(String suffix) {
		return sessionFactory.getCurrentSession().createCriteria(GlobalProperty.class).add(
		    Restrictions.like("property", suffix, MatchMode.END)).list();
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
			log.debug("Error while running sql: " + sql, e);
			throw new DAOException("Error while running sql: " + sql + " . Message: " + e.getMessage(), e);
		}
		finally {
			if (ps != null) {
				try {
					ps.close();
				}
				catch (SQLException e) {
					log.error("Error generated while closing statement", e);
				}
			}
		}
		
		return results;
	}
	
}
