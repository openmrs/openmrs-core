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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.hibernate.SessionFactory;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.AbstractCollectionEvent;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PreCollectionRecreateEvent;
import org.hibernate.event.spi.PreCollectionRecreateEventListener;
import org.hibernate.event.spi.PreCollectionRemoveEvent;
import org.hibernate.event.spi.PreCollectionRemoveEventListener;
import org.hibernate.event.spi.PreCollectionUpdateEvent;
import org.hibernate.event.spi.PreCollectionUpdateEventListener;
import org.hibernate.event.spi.PreDeleteEvent;
import org.hibernate.event.spi.PreDeleteEventListener;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.hibernate.internal.SessionFactoryImpl;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.UserServiceImpl.RoleWriteGuard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Rejects any write to a {@link Role} or {@link Privilege} that didn't go through
 * {@link org.openmrs.api.UserService}'s role and privilege methods in the current transaction (see
 * {@link RoleWriteGuard}). Without this, Hibernate's dirty checking writes any change made in
 * memory to a loaded role whenever the session flushes, whatever call triggered the flush.
 * <p>
 * A rejected write throws, so the flush fails and the transaction rolls back.
 *
 * @since 2.8.11
 */
@Component
public class RoleWriteGuardListener implements PreInsertEventListener, PreUpdateEventListener, PreDeleteEventListener, PreCollectionRecreateEventListener, PreCollectionUpdateEventListener, PreCollectionRemoveEventListener {

	@Autowired
	private SessionFactory sessionFactory;

	@PostConstruct
	public void registerListener() {
		EventListenerRegistry registry = ((SessionFactoryImpl) sessionFactory).getServiceRegistry()
		        .getService(EventListenerRegistry.class);
		registry.getEventListenerGroup(EventType.PRE_INSERT).appendListener(this);
		registry.getEventListenerGroup(EventType.PRE_UPDATE).appendListener(this);
		registry.getEventListenerGroup(EventType.PRE_DELETE).appendListener(this);
		registry.getEventListenerGroup(EventType.PRE_COLLECTION_RECREATE).appendListener(this);
		registry.getEventListenerGroup(EventType.PRE_COLLECTION_UPDATE).appendListener(this);
		registry.getEventListenerGroup(EventType.PRE_COLLECTION_REMOVE).appendListener(this);
	}

	@Override
	public boolean onPreInsert(PreInsertEvent event) {
		requirePermitted(event.getEntity(), event.getId());
		return false;
	}

	@Override
	public boolean onPreUpdate(PreUpdateEvent event) {
		requirePermitted(event.getEntity(), event.getId());
		return false;
	}

	@Override
	public boolean onPreDelete(PreDeleteEvent event) {
		requirePermitted(event.getEntity(), event.getId());
		return false;
	}

	@Override
	public void onPreRecreateCollection(PreCollectionRecreateEvent event) {
		requirePermittedOwner(event);
	}

	/**
	 * A merge marks a collection modified even when it copies identical contents onto it, so only a
	 * real change to the contents is rejected.
	 */
	@Override
	public void onPreUpdateCollection(PreCollectionUpdateEvent event) {
		if (isGuarded(event) && !ids(event.getCollection().getStoredSnapshot()).equals(ids(event.getCollection()))) {
			requirePermittedOwner(event);
		}
	}

	@Override
	public void onPreRemoveCollection(PreCollectionRemoveEvent event) {
		requirePermittedOwner(event);
	}

	/** Inverse collections, like {@code Role.childRoles}, are never written, so they're ignored */
	private static void requirePermittedOwner(AbstractCollectionEvent event) {
		if (isGuarded(event)) {
			requirePermitted(event.getAffectedOwnerOrNull(), event.getAffectedOwnerIdOrNull());
		}
	}

	/**
	 * Whether the collection belongs to a role or privilege. Checked before touching the collection's
	 * contents, since reading another entity's collection during a flush can load data mid-flush.
	 */
	private static boolean isGuarded(AbstractCollectionEvent event) {
		Object owner = event.getAffectedOwnerOrNull();
		return (owner instanceof Role || owner instanceof Privilege) && !event.getCollectionPersister().isInverse();
	}

	private static void requirePermitted(Object entity, Object id) {
		Class<?> type = entity instanceof Role ? Role.class : entity instanceof Privilege ? Privilege.class : null;
		if (type != null && !RoleWriteGuard.isPermitted(type, id)) {
			throw new APIException(type.getSimpleName() + " '" + id
			        + "' was changed outside UserService; roles and privileges can only be written by saving or purging them through UserService");
		}
	}

	/**
	 * Names of the roles or privileges in a collection or in its stored snapshot, which may be a map
	 */
	private static Set<String> ids(Object elements) {
		if (elements == null) {
			return Set.of();
		}
		Collection<?> values = elements instanceof Map<?, ?> map ? map.keySet() : (Collection<?>) elements;
		return values.stream().map(RoleWriteGuardListener::id).collect(Collectors.toSet());
	}

	private static String id(Object element) {
		if (element instanceof Role role) {
			return role.getRole();
		}
		if (element instanceof Privilege privilege) {
			return privilege.getPrivilege();
		}
		return String.valueOf(element);
	}
}
