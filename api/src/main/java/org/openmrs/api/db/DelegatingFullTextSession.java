/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * 
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import java.io.Serializable;

import org.apache.lucene.search.Query;
import org.hibernate.engine.spi.SessionDelegatorBaseImpl;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.FullTextSharedSessionBuilder;
import org.hibernate.search.MassIndexer;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.query.engine.spi.QueryDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Custom implementation of the {@link FullTextSession} interface that acts a wrapper around a
 * target FullTextSession instance, it actually delegates all the method calls directly to the
 * target except for the {@link FullTextSession#createFullTextQuery(Query, Class[])} method where it
 * first notifies registered listeners of the creation event before returning the newly created
 * {@link FullTextQuery} object. The newly created query object and entity type are passed to the
 * listeners wrapped in a {@link FullTextQueryAndEntityClass} object. <br>
 * <br>
 * An example use case is that a listener can enable/disable filters on the newly created query
 * object.
 */
public class DelegatingFullTextSession extends SessionDelegatorBaseImpl implements FullTextSession {
	
	private static final Logger log = LoggerFactory.getLogger(DelegatingFullTextSession.class);
	
	private FullTextSession delegate;
	
	private ApplicationEventPublisher eventPublisher;
	
	public DelegatingFullTextSession(FullTextSession delegate, ApplicationEventPublisher eventPublisher) {
		super((SessionImplementor) delegate, delegate);
		this.delegate = delegate;
		this.eventPublisher = eventPublisher;
	}
	
	/**
	 * @see FullTextSession#createFullTextQuery(Query, Class[])
	 */
	@Override
	public FullTextQuery createFullTextQuery(Query luceneQuery, Class<?>... entities) {
		if (entities.length > 1) {
			throw new DAOException("Can't create FullTextQuery for multiple persistent classes");
		}
		
		log.debug("Creating new FullTextQuery instance");
		
		Class<?> entityClass = entities[0];
		FullTextQuery query = delegate.createFullTextQuery(luceneQuery, entityClass);
		
		log.debug("Notifying FullTextQueryCreated listeners...");
		
		//Notify listeners, note that we intentionally don't catch any exception from a listener
		//so that failure should just halt the entire creation operation, this is possible because 
		//the default ApplicationEventMulticaster in spring fires events serially in the same thread
		//but has the downside of where a rogue listener can block the entire application.
		FullTextQueryAndEntityClass queryAndClass = new FullTextQueryAndEntityClass(query, entityClass);
		eventPublisher.publishEvent(new FullTextQueryCreatedEvent(queryAndClass));
		
		return query;
	}
	
	@Override
	public FullTextQuery createFullTextQuery(QueryDescriptor descriptor, Class<?>... entities) {
		if (entities.length > 1) {
			throw new DAOException("Can't create FullTextQuery for multiple persistent classes");
		}

		log.debug("Creating new FullTextQuery instance");

		Class<?> entityClass = entities[0];
		FullTextQuery query = delegate.createFullTextQuery(descriptor, entityClass);

		log.debug("Notifying FullTextQueryCreated listeners...");

		//Notify listeners, note that we intentionally don't catch any exception from a listener
		//so that failure should just halt the entire creation operation, this is possible because 
		//the default ApplicationEventMulticaster in spring fires events serially in the same thread
		//but has the downside of where a rogue listener can block the entire application.
		FullTextQueryAndEntityClass queryAndClass = new FullTextQueryAndEntityClass(query, entityClass);
		eventPublisher.publishEvent(new FullTextQueryCreatedEvent(queryAndClass));

		return query;
	}
	
	/**
	 * @see FullTextSession#index(Object)
	 */
	@Override
	public <T> void index(T entity) {
		delegate.index(entity);
	}
	
	/**
	 * @see FullTextSession#getSearchFactory()
	 */
	@Override
	public SearchFactory getSearchFactory() {
		return delegate.getSearchFactory();
	}
	
	/**
	 * @see FullTextSession#purge(Class, Serializable)
	 */
	@Override
	public <T> void purge(Class<T> entityType, Serializable id) {
		delegate.purge(entityType, id);
	}
	
	/**
	 * @see FullTextSession#purgeAll(Class)
	 */
	@Override
	public <T> void purgeAll(Class<T> entityType) {
		delegate.purgeAll(entityType);
	}
	
	/**
	 * @see FullTextSession#flushToIndexes()
	 */
	@Override
	public void flushToIndexes() {
		delegate.flushToIndexes();
	}
	
	/**
	 * @see FullTextSession#createIndexer(Class[])
	 */
	@Override
	public MassIndexer createIndexer(Class<?>... types) {
		return delegate.createIndexer(types);
	}
	
	/**
	 * @see FullTextSession#sessionWithOptions()
	 */
	@Override
	public FullTextSharedSessionBuilder sessionWithOptions() {
		return delegate.sessionWithOptions();
	}
}
