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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.WeakHashMap;
import java.util.function.Function;

import jakarta.annotation.PostConstruct;

import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.CollectionEntry;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.engine.spi.TransactionCompletionCallbacks;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.AutoFlushEvent;
import org.hibernate.event.spi.AutoFlushEventListener;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.FlushEvent;
import org.hibernate.event.spi.FlushEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.internal.SessionFactoryImpl;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.context.Daemon;
import org.openmrs.api.db.StoredRole;
import org.openmrs.api.impl.RoleAssignments;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.util.RoleConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Checks every change to a user's roles, or to a role's privileges or inherited roles, before the
 * session flushes it. Hibernate's dirty checking writes in-memory changes to any loaded user or
 * role, whether or not they went through {@link org.openmrs.api.UserService}, so the check has to
 * happen here. The authenticated user must hold, through their roles rather than proxy privileges,
 * {@link PrivilegeConstants#EDIT_USERS} (or {@link PrivilegeConstants#ADD_USERS} for a user
 * inserted in the current transaction) to change a user's roles, or
 * {@link PrivilegeConstants#MANAGE_ROLES} to change a role, and must be able to hand out whatever
 * is added (see {@link RoleAssignments}). Changing or deleting a user, or changing a role, that, as
 * stored, is or inherits {@link RoleConstants#SUPERUSER} also requires
 * {@link PrivilegeConstants#ASSIGN_SYSTEM_DEVELOPER_ROLE}. A role may not inherit itself. A
 * rejected change throws, failing the flush.
 * <p>
 * A change is the difference between the collection a managed user or role holds and its loaded
 * snapshot or, for a collection put in place of the loaded one, what is stored. Nothing is flushed
 * while the check runs, and the flush fails if code run by the check changes anything.
 *
 * @since 2.8.11
 */
@Component
public class RoleWriteGuardListener implements FlushEventListener, AutoFlushEventListener, PostInsertEventListener {

	private static final String USER_ROLES = User.class.getName() + ".roles";

	private static final String ROLE_PRIVILEGES = Role.class.getName() + ".privileges";

	private static final String ROLE_PARENTS = Role.class.getName() + ".inheritedRoles";

	/** Not a collection role: marks the deletion of a user, whose role assignments go with it */
	private static final String USER_DELETION = User.class.getName() + "#deletion";

	/**
	 * Ids of the users each session has inserted in its current transaction. Recorded by
	 * {@link #onPostInsert}, since by the time the guard runs an inserted user is indistinguishable
	 * from a loaded one. Weak, so a session that never completes a transaction doesn't leak.
	 */
	private final Map<Object, Set<Object>> insertedUsers = Collections.synchronizedMap(new WeakHashMap<>());

	@Autowired
	private SessionFactory sessionFactory;

	@PostConstruct
	public void registerListener() {
		EventListenerRegistry registry = ((SessionFactoryImpl) sessionFactory).getServiceRegistry()
		        .getService(EventListenerRegistry.class);
		registry.getEventListenerGroup(EventType.FLUSH).prependListener(this);
		registry.getEventListenerGroup(EventType.AUTO_FLUSH).prependListener(this);
		registry.getEventListenerGroup(EventType.POST_INSERT).appendListener(this);
	}

	/**
	 * Records an inserted user, until the transaction completes, so Add Users suffices to give them
	 * roles.
	 */
	@Override
	public void onPostInsert(PostInsertEvent event) {
		if (!(event.getEntity() instanceof User)) {
			return;
		}
		SharedSessionContractImplementor session = event.getSession();
		insertedUsers.computeIfAbsent(session, s -> {
			session.getTransactionCompletionCallbacks()
			        .registerCallback((TransactionCompletionCallbacks.AfterCompletionCallback) (success,
			                completed) -> insertedUsers.remove(completed));
			return Collections.synchronizedSet(new HashSet<>());
		}).add(event.getId());
	}

	private boolean isInsertedInTransaction(EventSource session, Object userId) {
		Set<Object> inserted = insertedUsers.get(session);
		return userId != null && inserted != null && inserted.contains(userId);
	}

	/** Set while checking, since reading roles for the check runs queries, which fire this again */
	private static final ThreadLocal<Boolean> CHECKING = new ThreadLocal<>();

	/**
	 * @throws APIException if code run by the check, such as a privilege listener, flushes the session,
	 *             since that flush would write changes before they're checked
	 */
	@Override
	public void onFlush(FlushEvent event) {
		requireNotChecking();
		check(event.getSession());
	}

	/**
	 * Hibernate fires this before every query, so it only checks when the session would really flush.
	 *
	 * @throws APIException if code run by the check runs a query that would flush the session
	 */
	@Override
	public void onAutoFlush(AutoFlushEvent event) {
		FlushMode flushMode = event.getSession().getHibernateFlushMode();
		if (flushMode == FlushMode.AUTO || flushMode == FlushMode.ALWAYS) {
			requireNotChecking();
			check(event.getSession());
		}
	}

	private static void requireNotChecking() {
		if (CHECKING.get() != null && !Daemon.isDaemonThread()) {
			throw new APIException("The session can't be flushed while role changes are being checked");
		}
	}

	private void check(EventSource session) {
		if (Daemon.isDaemonThread() || CHECKING.get() != null) {
			return;
		}
		CHECKING.set(Boolean.TRUE);
		FlushMode flushMode = session.getHibernateFlushMode();
		session.setHibernateFlushMode(FlushMode.MANUAL);
		try {
			checkChanges(session);
		} finally {
			session.setHibernateFlushMode(flushMode);
			CHECKING.remove();
		}
	}

	private void checkChanges(EventSource session) {
		// collected first, since checking can load more into the session
		List<Change> changes = collectChanges(session);
		if (changes.isEmpty()) {
			return;
		}
		Function<String, StoredRole> storedNow = name -> StoredRoleReader.read(session, name);
		Function<String, StoredRole> stored = afterFlush(session, changes, storedNow);
		for (Change change : changes) {
			if (USER_DELETION.equals(change.role)) {
				// whatever else deleting a user requires is the service's to check
				RoleAssignments.requireCanChangeSuperuser("User.superuser.privilegeRequired",
				    StoredRoleReader.readUserRoleNames(session, (Integer) change.ownerId), storedNow);
			} else if (USER_ROLES.equals(change.role)) {
				Set<String> storedRoles = change.ownerId == null ? new HashSet<>()
				        : StoredRoleReader.readUserRoleNames(session, (Integer) change.ownerId);
				requirePrivilege(isInsertedInTransaction(session, change.ownerId)
				        ? new String[] { PrivilegeConstants.EDIT_USERS, PrivilegeConstants.ADD_USERS }
				        : new String[] { PrivilegeConstants.EDIT_USERS });
				// what is stored, not the pending changes, decides whether superuser status may be taken away
				RoleAssignments.requireCanChangeSuperuser("User.superuser.privilegeRequired", storedRoles, storedNow);
				requireNotMissing("User.you.must.have.privileges", RoleAssignments.missingPrivileges(change.added, stored));
			} else {
				requirePrivilege(PrivilegeConstants.MANAGE_ROLES);
				if (change.ownerId != null) {
					RoleAssignments.requireCanChangeSuperuser("Role.superuser.privilegeRequired",
					    Collections.singleton((String) change.ownerId), storedNow);
				}
				if (ROLE_PRIVILEGES.equals(change.role)) {
					requireNotMissing("Role.you.must.have.privileges", RoleAssignments.notHeld(change.added));
				} else {
					RoleAssignments.requireNoInheritanceCycle((String) change.ownerId, change.after, stored);
					requireNotMissing("Role.you.must.have.privileges",
					    RoleAssignments.missingPrivileges(change.added, stored));
				}
			}
		}
		// the checks run code, such as privilege listeners, that could change the session's users and roles
		if (!new HashSet<>(changes).equals(new HashSet<>(collectChanges(session)))) {
			throw new APIException("Roles or privileges were changed while role changes were being checked");
		}
	}

	/**
	 * Finds the changes to the guarded collections of the users and roles the session manages, keyed by
	 * the entity holding the collection, which is the owner Hibernate writes it for: a collection's own
	 * record of its owner can be stale, for example when it was moved from another entity.
	 */
	private static List<Change> collectChanges(EventSource session) {
		PersistenceContext persistenceContext = session.getPersistenceContextInternal();
		List<Change> changes = new ArrayList<>();
		for (Map.Entry<Object, EntityEntry> managed : persistenceContext.reentrantSafeEntityEntries()) {
			Object entity = managed.getKey();
			EntityEntry entry = managed.getValue();
			// by the id the row is deleted under, which, unlike the user's own, can't be changed in memory
			if (entity instanceof User && entry.getStatus() == Status.DELETED && entry.getId() != null) {
				changes.add(new Change(USER_DELETION, entry.getId(), new HashSet<>(), new HashSet<>()));
				continue;
			}
			// a read-only entity's collections are still written
			if (entry.getStatus() != Status.MANAGED && entry.getStatus() != Status.READ_ONLY) {
				continue;
			}
			if (entity instanceof User) {
				collectChange(session, changes, entry, entity, "roles", USER_ROLES);
			} else if (entity instanceof Role) {
				collectChange(session, changes, entry, entity, "privileges", ROLE_PRIVILEGES);
				collectChange(session, changes, entry, entity, "inheritedRoles", ROLE_PARENTS);
			}
		}
		return changes;
	}

	private static void collectChange(EventSource session, List<Change> changes, EntityEntry entry, Object entity,
	        String property, String role) {
		Object value = entry.getPersister().getPropertyValue(entity, property);
		Object ownerId = entry.getId();
		if (value instanceof PersistentCollection<?> collection) {
			CollectionEntry collectionEntry = session.getPersistenceContextInternal().getCollectionEntry(collection);
			boolean loadedForOwner = collectionEntry != null && collectionEntry.getLoadedPersister() != null
			        && collection.getOwner() == entity && Objects.equals(collectionEntry.getLoadedKey(), ownerId);
			if (loadedForOwner) {
				if (collection.wasInitialized()) {
					addIfChanged(changes, role, ownerId, names(session, role, collection.getStoredSnapshot()),
					    names(session, role, collection));
				}
				return;
			}
		}
		// a null collection is written as an empty one
		addIfChanged(changes, role, ownerId, stored(session, role, ownerId), names(session, role, value));
	}

	/**
	 * For a collection the session didn't load, Hibernate replaces whatever is stored for its owner, so
	 * the change is from what is stored, never from the snapshot of a collection it replaces, which may
	 * be stale.
	 */
	private static Set<String> stored(EventSource session, String role, Object ownerId) {
		if (ownerId == null) {
			return new HashSet<>();
		}
		if (USER_ROLES.equals(role)) {
			return StoredRoleReader.readUserRoleNames(session, (Integer) ownerId);
		}
		StoredRole stored = StoredRoleReader.read(session, (String) ownerId);
		if (stored == null) {
			return new HashSet<>();
		}
		return new HashSet<>(ROLE_PRIVILEGES.equals(role) ? stored.getPrivileges() : stored.getParentRoles());
	}

	private static void addIfChanged(List<Change> changes, String role, Object ownerId, Set<String> before,
	        Set<String> after) {
		if (!before.equals(after)) {
			Set<String> added = new HashSet<>(after);
			added.removeAll(before);
			changes.add(new Change(role, ownerId, added, after));
		}
	}

	/**
	 * Returns a reader that sees roles as they will be after this flush: roles being inserted, and the
	 * given changes to roles' privileges and inherited roles, override what is stored. This lets a role
	 * created in the flush be handed out in it, and catches a cycle formed across several roles at
	 * once.
	 */
	private static Function<String, StoredRole> afterFlush(EventSource session, List<Change> changes,
	        Function<String, StoredRole> stored) {
		Map<String, StoredRole> pending = new HashMap<>();
		for (Map.Entry<Object, EntityEntry> managed : session.getPersistenceContextInternal().reentrantSafeEntityEntries()) {
			EntityEntry entry = managed.getValue();
			if (managed.getKey() instanceof Role && entry.getStatus() == Status.MANAGED && !entry.isExistsInDatabase()) {
				Role role = (Role) managed.getKey();
				pending.put(role.getRole(),
				    new StoredRole(role.getRole(),
				            names(session, ROLE_PRIVILEGES, entry.getPersister().getPropertyValue(role, "privileges")),
				            names(session, ROLE_PARENTS, entry.getPersister().getPropertyValue(role, "inheritedRoles"))));
			}
		}
		for (Change change : changes) {
			if ((ROLE_PRIVILEGES.equals(change.role) || ROLE_PARENTS.equals(change.role)) && change.ownerId != null) {
				String name = (String) change.ownerId;
				StoredRole base = pending.containsKey(name) ? pending.get(name) : stored.apply(name);
				Set<String> privileges = base != null ? base.getPrivileges() : new HashSet<>();
				Set<String> parents = base != null ? base.getParentRoles() : new HashSet<>();
				String baseName = base != null ? base.getName() : name;
				pending.put(baseName, ROLE_PRIVILEGES.equals(change.role) ? new StoredRole(baseName, change.after, parents)
				        : new StoredRole(baseName, privileges, change.after));
			}
		}
		return name -> {
			if (pending.containsKey(name)) {
				return pending.get(name);
			}
			StoredRole role = stored.apply(name);
			return role != null && pending.containsKey(role.getName()) ? pending.get(role.getName()) : role;
		};
	}

	/** Requires one of the given privileges, held through the user's roles */
	private static void requirePrivilege(String... privileges) {
		for (String privilege : privileges) {
			if (RoleAssignments.holds(privilege)) {
				return;
			}
		}
		throw new ContextAuthenticationException(Context.getMessageSourceService().getMessage("error.privilegesRequired",
		    new Object[] { String.join(", ", privileges) }, Locale.getDefault()));
	}

	private static void requireNotMissing(String messageKey, SortedSet<String> missing) {
		if (!missing.isEmpty()) {
			throw new APIException(messageKey, new Object[] { String.join(", ", missing) });
		}
	}

	/**
	 * Names of the roles or privileges in a collection or its stored snapshot, which may be a map. A
	 * snapshot holds the element objects, not their names, so a renamed element would misreport what is
	 * written. Each element must therefore be managed under its name as id, which Hibernate refuses to
	 * change on flush.
	 *
	 * @param role the collection role, which decides whether its elements must be roles or privileges
	 * @throws APIException if an element isn't a managed role or privilege, as the collection holds, or
	 *             its name isn't its id
	 */
	private static Set<String> names(EventSource session, String role, Object elements) {
		// Hibernate writes any managed entity's id as the element's key, whatever the entity's type
		Class<?> elementType = ROLE_PRIVILEGES.equals(role) ? Privilege.class : Role.class;
		Set<String> names = new HashSet<>();
		if (elements != null) {
			PersistenceContext persistenceContext = session.getPersistenceContextInternal();
			Collection<?> values = elements instanceof Map ? ((Map<?, ?>) elements).keySet() : (Collection<?>) elements;
			for (Object element : values) {
				Object instance = persistenceContext.unproxy(element);
				if (instance == null) {
					continue;
				}
				String name;
				if (instance instanceof Role && elementType == Role.class) {
					name = ((Role) instance).getRole();
				} else if (instance instanceof Privilege && elementType == Privilege.class) {
					name = ((Privilege) instance).getPrivilege();
				} else {
					throw new APIException("Users' roles and roles' inherited roles may only hold roles, and roles' "
					        + "privileges only privileges: " + instance);
				}
				// an entry rather than Session#contains, which is false for an entity being deleted
				EntityEntry entry = persistenceContext.getEntry(instance);
				if (entry == null || name == null || !name.equals(entry.getId())) {
					throw new APIException("Users' roles and roles' privileges and inherited roles may only hold roles "
					        + "and privileges loaded in the current session, under their stored names: " + name);
				}
				names.add(name);
			}
		}
		return names;
	}

	/** A change to one of the guarded collections that the flush is about to write */
	private static final class Change {

		private final String role;

		/** The owning user's id or role's name */
		private final Object ownerId;

		private final Set<String> added;

		private final Set<String> after;

		private Change(String role, Object ownerId, Set<String> added, Set<String> after) {
			this.role = role;
			this.ownerId = ownerId;
			this.added = added;
			this.after = after;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Change)) {
				return false;
			}
			Change other = (Change) o;
			return role.equals(other.role) && Objects.equals(ownerId, other.ownerId) && added.equals(other.added)
			        && after.equals(other.after);
		}

		@Override
		public int hashCode() {
			return Objects.hash(role, ownerId, added, after);
		}
	}
}
