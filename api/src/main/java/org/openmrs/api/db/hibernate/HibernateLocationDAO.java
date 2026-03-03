/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.LocationTag;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.LocationDAO;

/**
 * Hibernate location-related database functions
 */
public class HibernateLocationDAO implements LocationDAO {
	
	private SessionFactory sessionFactory;
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#setSessionFactory(org.hibernate.SessionFactory)
	 */
	@Override
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#saveLocation(org.openmrs.Location)
	 */
	@Override
	public Location saveLocation(Location location) {
		if (location.getChildLocations() != null && location.getLocationId() != null) {
			// hibernate has a problem updating child collections
			// if the parent object was already saved so we do it
			// explicitly here
			for (Location child : location.getChildLocations()) {
				if (child.getLocationId() == null) {
					saveLocation(child);
				}
			}
		}
		
		sessionFactory.getCurrentSession().saveOrUpdate(location);
		return location;
	}
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#getLocation(java.lang.Integer)
	 */
	@Override
	public Location getLocation(Integer locationId) {
		return sessionFactory.getCurrentSession().get(Location.class, locationId);
	}
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#getLocation(java.lang.String)
	 */
	@Override
	public Location getLocation(String name) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Location> cq = cb.createQuery(Location.class);
		Root<Location> locationRoot = cq.from(Location.class);

		cq.where(cb.equal(locationRoot.get("name"), name));

		List<Location> locations = session.createQuery(cq).getResultList();
		if (null == locations || locations.isEmpty()) {
			return null;
		}
		return locations.get(0);
	}

	/**
	 * @see org.openmrs.api.db.LocationDAO#getAllLocations(boolean)
	 */
	@Override
	public List<Location> getAllLocations(boolean includeRetired) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Location> cq = cb.createQuery(Location.class);
		Root<Location> locationRoot = cq.from(Location.class);

		List<Order> orderList = new ArrayList<>();
		if (!includeRetired) {
			cq.where(cb.isFalse(locationRoot.get("retired")));
		} else {
			orderList.add(cb.asc(locationRoot.get("retired")));
		}
		orderList.add(cb.asc(locationRoot.get("name")));

		cq.orderBy(orderList);

		return session.createQuery(cq).getResultList();
	}
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#deleteLocation(org.openmrs.Location)
	 */
	@Override
	public void deleteLocation(Location location) {
		sessionFactory.getCurrentSession().delete(location);
	}
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#saveLocation(org.openmrs.Location)
	 */
	@Override
	public LocationTag saveLocationTag(LocationTag tag) {
		sessionFactory.getCurrentSession().saveOrUpdate(tag);
		return tag;
	}
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#getLocationTag(java.lang.Integer)
	 */
	@Override
	public LocationTag getLocationTag(Integer locationTagId) {
		return sessionFactory.getCurrentSession().get(LocationTag.class, locationTagId);
	}

	/**
	 * @see org.openmrs.api.db.LocationDAO#getLocationTagByName(java.lang.String)
	 */
	@Override
	public LocationTag getLocationTagByName(String tag) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<LocationTag> cq = cb.createQuery(LocationTag.class);
		Root<LocationTag> root = cq.from(LocationTag.class);

		cq.where(cb.equal(root.get("name"), tag));

		List<LocationTag> tags = session.createQuery(cq).getResultList();
		if (null == tags || tags.isEmpty()) {
			return null;
		}
		return tags.get(0);
	}

	/**
	 * @see org.openmrs.api.db.LocationDAO#getAllLocationTags(boolean)
	 */
	@Override
	public List<LocationTag> getAllLocationTags(boolean includeRetired) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<LocationTag> cq = cb.createQuery(LocationTag.class);
		Root<LocationTag> root = cq.from(LocationTag.class);

		if (!includeRetired) {
			cq.where(cb.isFalse(root.get("retired")));
		}
		cq.orderBy(cb.asc(root.get("name")));

		return session.createQuery(cq).getResultList();
	}

	/**
	 * @see org.openmrs.api.db.LocationDAO#getLocationTags(String)
	 */
	@Override
	public List<LocationTag> getLocationTags(String search) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<LocationTag> cq = cb.createQuery(LocationTag.class);
		Root<LocationTag> root = cq.from(LocationTag.class);

		// 'ilike' case insensitive search
		cq.where(cb.like(cb.lower(root.get("name")), MatchMode.START.toLowerCasePattern(search)));
		cq.orderBy(cb.asc(root.get("name")));

		return session.createQuery(cq).getResultList();
	}
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#deleteLocationTag(org.openmrs.LocationTag)
	 */
	@Override
	public void deleteLocationTag(LocationTag tag) {
		sessionFactory.getCurrentSession().delete(tag);
	}
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#getLocationByUuid(java.lang.String)
	 */
	@Override
	public Location getLocationByUuid(String uuid) {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, Location.class, uuid);
	}
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#getLocationTagByUuid(java.lang.String)
	 */
	@Override
	public LocationTag getLocationTagByUuid(String uuid) {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, LocationTag.class, uuid);
	}

	/**
	 * @see org.openmrs.api.db.LocationDAO#getCountOfLocations(String, Boolean)
	 */
	@Override
	public Long getCountOfLocations(String nameFragment, Boolean includeRetired) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<Location> root = cq.from(Location.class);

		cq.select(cb.count(root));

		List<Predicate> predicates = new ArrayList<>();

		if (!includeRetired) {
			predicates.add(cb.isFalse(root.get("retired")));
		}

		if (StringUtils.isNotBlank(nameFragment)) {
			predicates.add(cb.like(cb.lower(root.get("name")), MatchMode.START.toLowerCasePattern(nameFragment)));
		}

		cq.where(cb.and(predicates.toArray(new Predicate[]{})));

		return session.createQuery(cq).getSingleResult();
	}

	/**
	 * @see LocationDAO#getLocations(String, org.openmrs.Location, java.util.Map, boolean, Integer, Integer)
	 */
	@Override
	public List<Location> getLocations(String nameFragment, Location parent,
	        Map<LocationAttributeType, String> serializedAttributeValues, boolean includeRetired, Integer start,
	        Integer length) {

		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Location> cq = cb.createQuery(Location.class);
		Root<Location> locationRoot = cq.from(Location.class);

		List<Predicate> predicates = new ArrayList<>();

		if (StringUtils.isNotBlank(nameFragment)) {
			predicates.add(cb.like(cb.lower(locationRoot.get("name")), MatchMode.START.toLowerCasePattern(nameFragment)));
		}

		if (parent != null) {
			predicates.add(cb.equal(locationRoot.get("parentLocation"), parent));
		}

		if (serializedAttributeValues != null) {
			predicates.addAll(HibernateUtil.getAttributePredicate(cb, locationRoot, serializedAttributeValues));
		}

		if (!includeRetired) {
			predicates.add(cb.isFalse(locationRoot.get("retired")));
		}

		cq.where(cb.and(predicates.toArray(new Predicate[]{})));
		cq.orderBy(cb.asc(locationRoot.get("name")));

		TypedQuery<Location> query = session.createQuery(cq);

		if (start != null) {
			query.setFirstResult(start);
		}
		if (length != null && length > 0) {
			query.setMaxResults(length);
		}

		return query.getResultList();
	}

	/**
	 * @see LocationDAO#getRootLocations(boolean)
	 */
	@Override
	public List<Location> getRootLocations(boolean includeRetired) throws DAOException {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Location> cq = cb.createQuery(Location.class);
		Root<Location> locationRoot = cq.from(Location.class);

		List<Predicate> predicates = new ArrayList<>();

		if (!includeRetired) {
			predicates.add(cb.isFalse(locationRoot.get("retired")));
		}

		predicates.add(cb.isNull(locationRoot.get("parentLocation")));

		cq.where(predicates.toArray(new Predicate[]{}));
		cq.orderBy(cb.asc(locationRoot.get("name")));

		return session.createQuery(cq).getResultList();
	}

	/**
	 * @see org.openmrs.api.db.LocationDAO#getAllLocationAttributeTypes()
	 */
	@Override
	public List<LocationAttributeType> getAllLocationAttributeTypes() {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<LocationAttributeType> cq = cb.createQuery(LocationAttributeType.class);
		cq.from(LocationAttributeType.class);
		
		return session.createQuery(cq).getResultList();
	}
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#getLocationAttributeType(java.lang.Integer)
	 */
	@Override
	public LocationAttributeType getLocationAttributeType(Integer id) {
		return sessionFactory.getCurrentSession().get(LocationAttributeType.class, id);
	}
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#getLocationAttributeTypeByUuid(java.lang.String)
	 */
	@Override
	public LocationAttributeType getLocationAttributeTypeByUuid(String uuid) {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, LocationAttributeType.class, uuid);
	}
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#saveLocationAttributeType(org.openmrs.LocationAttributeType)
	 */
	@Override
	public LocationAttributeType saveLocationAttributeType(LocationAttributeType locationAttributeType) {
		sessionFactory.getCurrentSession().saveOrUpdate(locationAttributeType);
		return locationAttributeType;
	}
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#deleteLocationAttributeType(org.openmrs.LocationAttributeType)
	 */
	@Override
	public void deleteLocationAttributeType(LocationAttributeType locationAttributeType) {
		sessionFactory.getCurrentSession().delete(locationAttributeType);
	}
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#getLocationAttributeByUuid(java.lang.String)
	 */
	@Override
	public LocationAttribute getLocationAttributeByUuid(String uuid) {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, LocationAttribute.class, uuid);
	}

	/**
	 * @see org.openmrs.api.db.LocationDAO#getLocationAttributeTypeByName(java.lang.String)
	 */
	@Override
	public LocationAttributeType getLocationAttributeTypeByName(String name) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<LocationAttributeType> cq = cb.createQuery(LocationAttributeType.class);
		Root<LocationAttributeType> root = cq.from(LocationAttributeType.class);

		cq.where(cb.equal(root.get("name"), name));

		return session.createQuery(cq).uniqueResult();
	}

	/**
	 * @see org.openmrs.api.db.LocationDAO#getLocationsHavingAllTags(java.util.List)
	 */
	@Override
	public List<Location> getLocationsHavingAllTags(List<LocationTag> tags) {
		tags.removeAll(Collections.singleton(null));

		List<Integer> tagIds = getLocationTagIds(tags);

		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();

		CriteriaQuery<Location> mainQuery = cb.createQuery(Location.class);
		Root<Location> locationRoot = mainQuery.from(Location.class);

		// Create a subquery to count matching tags
		Subquery<Long> tagCountSubquery = mainQuery.subquery(Long.class);
		Root<Location> subRoot = tagCountSubquery.from(Location.class);
		Join<Location, LocationTag> tagsJoin = subRoot.join("tags");

		tagCountSubquery.select(cb.count(subRoot))
			.where(cb.and(
				tagsJoin.get("locationTagId").in(tagIds),
				cb.equal(subRoot.get("locationId"), locationRoot.get("locationId"))
			));

		mainQuery.select(locationRoot)
			.where(cb.and(
				cb.isFalse(locationRoot.get("retired")),
				cb.equal(cb.literal((long) tags.size()), tagCountSubquery)
			));

		return session.createQuery(mainQuery).getResultList();
	}
	
	/**
	 * Extract locationTagIds from the list of LocationTag objects provided.
	 *
	 * @param tags A list of LocationTag objects from which to extract the location tag IDs.
	 *             This list should not be null.
	 * @return A List of Integer representing the IDs of the provided LocationTag objects.
	 *         Returns an empty list if the input list is empty.
	 */
	private List<Integer> getLocationTagIds(List<LocationTag> tags) {
		List<Integer> locationTagIds = new ArrayList<>();
		for (LocationTag tag : tags) {
			locationTagIds.add(tag.getLocationTagId());
		}
		return locationTagIds;
	}
}
