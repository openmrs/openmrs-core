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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.LocationTag;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.LocationDAO;
import org.openmrs.parameter.LocationSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
	 * @return A List of Integer representing the IDs of the provided LocationTag objects, one per entry and in
	 *         the same order, null where the entry or its id was null.
	 *         Returns an empty list if the input list is empty.
	 */
	private List<Integer> getLocationTagIds(List<LocationTag> tags) {
		List<Integer> locationTagIds = new ArrayList<>();
		for (LocationTag tag : tags) {
			// a null entry maps to a null id rather than being dropped, so that callers counting the ids back
			// against the tags they asked for still see one entry per tag
			locationTagIds.add(tag == null ? null : tag.getLocationTagId());
		}
		return locationTagIds;
	}

	/**
	 * @see LocationDAO#getLocations(LocationSearchCriteria)
	 */
	@Override
	public List<Location> getLocations(LocationSearchCriteria criteria) {
		Session session = sessionFactory.getCurrentSession();

		// a non-null but empty id list means a descendant filter was asked for and matched nothing, which no
		// other predicate can widen again
		List<Integer> descendantIds = getDescendantIds(session, criteria);
		if (descendantIds != null && descendantIds.isEmpty()) {
			return Collections.emptyList();
		}

		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Location> cq = cb.createQuery(Location.class);
		Root<Location> root = cq.from(Location.class);

		List<Predicate> predicates = buildPredicates(cb, cq, root, criteria, descendantIds);

		cq.where(cb.and(predicates.toArray(new Predicate[0])));
		cq.orderBy(cb.asc(root.get("name")), cb.asc(root.get("locationId")));

		Query<Location> query = session.createQuery(cq);
		applyPagination(query, criteria);

		return query.getResultList();
	}

	/**
	 * Resolves the ids of every location beneath {@code criteria.getDescendantOfLocation()} by walking the
	 * hierarchy one level at a time. Each level issues a single query filtered to the current level's children,
	 * so only the relevant subtree is touched rather than the whole location table. Avoiding a recursive CTE keeps
	 * this compatible with databases that lack {@code WITH RECURSIVE} support (MySQL versions earlier than 8.0).
	 * <p>
	 * When retired locations are excluded, a retired location prunes its whole subtree; the ancestor is no
	 * exception, so a retired ancestor yields no descendants at all.
	 *
	 * @return {@code null} when no descendant filter is requested; otherwise the descendant location ids, which is
	 *         empty when the ancestor has no (matching) descendants
	 */
	private List<Integer> getDescendantIds(Session session, LocationSearchCriteria criteria) {
		if (criteria.getDescendantOfLocation() == null) {
			return null;
		}

		Integer ancestorId = criteria.getDescendantOfLocation().getLocationId();
		if (ancestorId == null) {
			return Collections.emptyList();
		}

		// A retired location prunes its whole subtree, and that applies to the ancestor the walk starts from:
		// the in-memory isDescendantOf() this replaced walked upwards from each candidate and rejected it as
		// soon as any node on the path - the ancestor included - was retired. So if the ancestor is filtered
		// out the walk never starts.
		if (!ancestorPassesRetiredFilter(session, criteria, ancestorId)) {
			return Collections.emptyList();
		}

		// Track every id already queued so a pathological parent cycle can neither loop forever nor pull the
		// starting location back in as its own descendant. Seeding the ancestor keeps it out of the results.
		Set<Integer> seen = new LinkedHashSet<>();
		seen.add(ancestorId);
		List<Integer> descendantIds = new ArrayList<>();
		List<Integer> currentLevel = Collections.singletonList(ancestorId);

		while (!currentLevel.isEmpty()) {
			List<Integer> nextLevel = new ArrayList<>();
			for (Integer childId : getChildIds(session, criteria, currentLevel)) {
				if (seen.add(childId)) {
					descendantIds.add(childId);
					nextLevel.add(childId);
				}
			}
			currentLevel = nextLevel;
		}

		return descendantIds;
	}

	/**
	 * Reports whether the ancestor a descendant search starts from survives the criteria's retired filter. The
	 * flag is read back from the database rather than off {@code criteria.getDescendantOfLocation()}, so that the
	 * ancestor's current state is used even when the caller handed in a stale instance, and so that changes still
	 * pending in the session - a location retired earlier in the same transaction, say - are seen. No query is
	 * issued when retired locations are included, since then nothing can be pruned.
	 */
	private boolean ancestorPassesRetiredFilter(Session session, LocationSearchCriteria criteria, Integer ancestorId) {
		if (criteria.getIncludeRetired()) {
			return true;
		}

		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Integer> cq = cb.createQuery(Integer.class);
		Root<Location> root = cq.from(Location.class);
		cq.select(root.get("locationId"));
		cq.where(cb.and(cb.equal(root.get("locationId"), ancestorId), cb.isFalse(root.get("retired"))));

		return !session.createQuery(cq).setMaxResults(1).getResultList().isEmpty();
	}

	/**
	 * Fetches the ids of the direct children of {@code parentIds}, honouring the criteria's retired filter so
	 * that a retired location is not descended into.
	 */
	private List<Integer> getChildIds(Session session, LocationSearchCriteria criteria, List<Integer> parentIds) {
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Integer> cq = cb.createQuery(Integer.class);
		Root<Location> root = cq.from(Location.class);
		cq.select(root.get("locationId"));

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(root.get("parentLocation").get("locationId").in(parentIds));
		if (!criteria.getIncludeRetired()) {
			predicates.add(cb.isFalse(root.get("retired")));
		}
		cq.where(cb.and(predicates.toArray(new Predicate[0])));

		return session.createQuery(cq).getResultList();
	}

	private List<Predicate> buildPredicates(CriteriaBuilder cb, CriteriaQuery<Location> cq, Root<Location> root,
	        LocationSearchCriteria criteria, List<Integer> descendantIds) {
		List<Predicate> predicates = new ArrayList<>();

		if (!criteria.getIncludeRetired()) {
			predicates.add(cb.isFalse(root.get("retired")));
		}

		if (descendantIds != null) {
			predicates.add(root.get("locationId").in(descendantIds));
		}

		addNameFragmentPredicate(cb, root, criteria, predicates);
		addTagPredicates(cb, cq, root, criteria, predicates);

		return predicates;
	}

	private void addNameFragmentPredicate(CriteriaBuilder cb, Root<Location> root, LocationSearchCriteria criteria,
	        List<Predicate> predicates) {
		if (StringUtils.isNotBlank(criteria.getNameFragment())) {
			// The fragment is a literal prefix, so escape LIKE wildcards ('%', '_') in it; the trailing '%' that
			// MatchMode.START appends stays a wildcard and is applied against the already-escaped fragment.
			String pattern = MatchMode.START.toLowerCasePattern(escapeLikeWildcards(criteria.getNameFragment()));
			predicates.add(cb.like(cb.lower(root.get("name")), pattern, '\\'));
		}
	}

	/**
	 * Escapes the LIKE wildcards in a value bound as a query parameter, for use with an explicit
	 * {@code ESCAPE '\'} clause. {@link HibernateUtil#escapeSqlWildcards(String, SessionFactory)} does not fit
	 * here: it doubles quotes and escapes {@code '*'}, neither of which applies to a bound parameter, and it
	 * leaves a backslash already present in the value unescaped.
	 */
	private String escapeLikeWildcards(String value) {
		// the escape character has to go first, or the backslashes added below would be escaped in turn
		return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}

	private void addTagPredicates(CriteriaBuilder cb, CriteriaQuery<Location> cq, Root<Location> root,
	        LocationSearchCriteria criteria, List<Predicate> predicates) {
		if (criteria.getLocationTags() == null || criteria.getLocationTags().isEmpty()) {
			return;
		}

		List<Integer> requestedTagIds = getLocationTagIds(new ArrayList<>(criteria.getLocationTags()));
		List<Integer> matchableTagIds = new ArrayList<>();
		for (Integer tagId : requestedTagIds) {
			if (tagId != null) {
				matchableTagIds.add(tagId);
			}
		}

		if (matchableTagIds.isEmpty()) {
			// nothing is left to match against, and an empty IN list is not valid SQL
			predicates.add(cb.disjunction());
			return;
		}

		// The matching tags are counted in a correlated subquery rather than joined into the outer query. A join
		// multiplies the location rows and needs a GROUP BY to collapse them again, which would leave
		// setFirstResult/setMaxResults paging over grouped rows and, on MySQL, depend on how the dialect renders
		// a GROUP BY over a whole entity under ONLY_FULL_GROUP_BY. This is the same shape as
		// getLocationsHavingAllTags and keeps the outer query at one row per location.
		Subquery<Long> matchedTagCount = cq.subquery(Long.class);
		Root<Location> subRoot = matchedTagCount.from(Location.class);
		Join<Location, LocationTag> tagsJoin = subRoot.join("tags");
		matchedTagCount.select(cb.count(tagsJoin)).where(cb.and(tagsJoin.get("locationTagId").in(matchableTagIds),
		    cb.equal(subRoot.get("locationId"), root.get("locationId"))));

		if (criteria.getTagMatchMode() == LocationSearchCriteria.TagMatchMode.ALL) {
			// location_tag_map holds each pairing once, so the count is the number of distinct tags matched
			predicates.add(cb.equal(matchedTagCount, (long) requestedTagIds.size()));
		} else {
			predicates.add(cb.greaterThan(matchedTagCount, 0L));
		}
	}

	private void applyPagination(Query<Location> query, LocationSearchCriteria criteria) {
		// Mirror the bounds handling of getLocations(String, ...): a non-positive maxResults means "no limit"
		// rather than an empty result, and a negative startIndex is ignored rather than passed to the driver.
		if (criteria.getStartIndex() != null && criteria.getStartIndex() >= 0) {
			query.setFirstResult(criteria.getStartIndex());
		}
		if (criteria.getMaxResults() != null && criteria.getMaxResults() > 0) {
			query.setMaxResults(criteria.getMaxResults());
		}
	}
}
