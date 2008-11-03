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

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.openmrs.Location;
import org.openmrs.api.db.LocationDAO;

/**
 * Hibernate location-related database functions
 */
public class HibernateLocationDAO implements LocationDAO {

	private SessionFactory sessionFactory;

	/**
	 * @see org.openmrs.api.db.LocationDAO#setSessionFactory(org.hibernate.SessionFactory)
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * @see org.openmrs.api.db.LocationDAO#saveLocation(org.openmrs.Location)
	 */
	public Location saveLocation(Location location) {
		sessionFactory.getCurrentSession().saveOrUpdate(location);
		return location;
	}

	/**
	 * @see org.openmrs.api.db.LocationDAO#getLocation(java.lang.Integer)
	 */
	public Location getLocation(Integer locationId) {
		return (Location) sessionFactory.getCurrentSession()
		                                .get(Location.class, locationId);
	}

	/**
	 * @see org.openmrs.api.db.LocationDAO#getLocation(java.lang.String)
	 */
	public Location getLocation(String name) {
    	Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Location.class)
    	.add(Expression.eq("name", name));
    	
    	List<Location> locations = criteria.list();
    	if (null == locations || locations.isEmpty()) {
    		return null;
    	}
    	return locations.get(0);
    }

	/**
	 * @see org.openmrs.api.db.LocationDAO#getAllLocations(boolean)
	 */
	@SuppressWarnings("unchecked")
    public List<Location> getAllLocations(boolean includeRetired) {
		Criteria criteria =  sessionFactory.getCurrentSession().createCriteria(Location.class);
		if (!includeRetired) {
            criteria.add(Expression.like("retired", false));
		}
		criteria.addOrder(Order.asc("name"));
		return criteria.list();
	}

	/**
	 * @see org.openmrs.api.db.LocationDAO#getLocations(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
    public List<Location> getLocations(String search) {
		if (search == null || search.equals(""))
			return getAllLocations(true);

		return sessionFactory.getCurrentSession()
		                     .createCriteria(Location.class)
		                     // 'ilike' case insensitive search
		                     .add(Expression.ilike("name",
		                                           search,
		                                           MatchMode.START))
		                     .addOrder(Order.asc("name"))
		                     .list();
	}

	/**
	 * @see org.openmrs.api.db.LocationDAO#deleteLocation(org.openmrs.Location)
	 */
	public void deleteLocation(Location location) {
		sessionFactory.getCurrentSession().delete(location);
	}

}
