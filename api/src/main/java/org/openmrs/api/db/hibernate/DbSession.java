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

import java.io.Serializable;
import java.sql.Connection;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Filter;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.IdentifierLoadAccess;
import org.hibernate.LobHelper;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.NaturalIdLoadAccess;
import org.hibernate.Query;
import org.hibernate.ReplicationMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Session.LockRequest;
import org.hibernate.SessionEventListener;
import org.hibernate.SessionFactory;
import org.hibernate.SharedSessionBuilder;
import org.hibernate.SimpleNaturalIdLoadAccess;
import org.hibernate.Transaction;
import org.hibernate.TransientObjectException;
import org.hibernate.TypeHelper;
import org.hibernate.UnknownProfileException;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;
import org.hibernate.procedure.ProcedureCall;
import org.hibernate.stat.SessionStatistics;

/**
 * This class has been created to provide backwards compatibility for modules, which need to support
 * OpenMRS 1.12 and before. It is because org.hibernate.classic.Session has been removed in
 * Hibernate 4 (used in OpenMRS 1.12) and sessionFactory.getCurrentSession() has been changed to
 * return org.hiberante.Session. It wraps SessionFactory so that any calls to getCurrentSession()
 * are directed to the correct Session class.
 * 
 * @since 1.12, 1.11.3, 1.10.2, 1.9.9
 */
public class DbSession {
	
	private SessionFactory sessionFactory;
	
	public DbSession(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	private Session getSession() {
		return sessionFactory.getCurrentSession();
	}
	
	/**
	 * Obtain the tenant identifier associated with this session.
	 *
	 * @return The tenant identifier associated with this session, or {@code null}
	 */
	public String getTenantIdentifier() {
		return getSession().getTenantIdentifier();
	}
	
	/**
	 * Begin a unit of work and return the associated {@link Transaction} object. If a new
	 * underlying transaction is required, begin the transaction. Otherwise continue the new work in
	 * the context of the existing underlying transaction.
	 *
	 * @return a Transaction instance
	 * @see #getTransaction
	 */
	public Transaction beginTransaction() {
		return getSession().beginTransaction();
	}
	
	/**
	 * Get the {@link Transaction} instance associated with this session. The concrete type of the
	 * returned {@link Transaction} object is determined by the
	 * {@code hibernate.transaction_factory} property.
	 *
	 * @return a Transaction instance
	 */
	public Transaction getTransaction() {
		return getSession().getTransaction();
	}
	
	/**
	 * Create a {@link Query} instance for the named query string defined in the metadata.
	 *
	 * @param queryName the name of a query defined externally
	 * @return The query instance for manipulation and execution
	 */
	public Query getNamedQuery(String queryName) {
		return getSession().getNamedQuery(queryName);
	}
	
	/**
	 * Create a {@link Query} instance for the given HQL query string.
	 *
	 * @param queryString The HQL query
	 * @return The query instance for manipulation and execution
	 */
	public Query createQuery(String queryString) {
		return getSession().createQuery(queryString);
	}
	
	/**
	 * Create a {@link SQLQuery} instance for the given SQL query string.
	 *
	 * @param queryString The SQL query
	 * @return The query instance for manipulation and execution
	 */
	public SQLQuery createSQLQuery(String queryString) {
		return getSession().createSQLQuery(queryString);
	}
	
	/**
	 * Gets a ProcedureCall based on a named template
	 *
	 * @param name The name given to the template
	 * @return The ProcedureCall
	 * @see javax.persistence.NamedStoredProcedureQuery
	 */
	public ProcedureCall getNamedProcedureCall(String name) {
		return getSession().getNamedProcedureCall(name);
	}
	
	/**
	 * Creates a call to a stored procedure.
	 *
	 * @param procedureName The name of the procedure.
	 * @return The representation of the procedure call.
	 */
	public ProcedureCall createStoredProcedureCall(String procedureName) {
		return getSession().createStoredProcedureCall(procedureName);
	}
	
	/**
	 * Creates a call to a stored procedure with specific result set entity mappings. Each class
	 * named is considered a "root return".
	 *
	 * @param procedureName The name of the procedure.
	 * @param resultClasses The entity(s) to map the result on to.
	 * @return The representation of the procedure call.
	 */
	public ProcedureCall createStoredProcedureCall(String procedureName, Class... resultClasses) {
		return getSession().createStoredProcedureCall(procedureName, resultClasses);
	}
	
	/**
	 * Creates a call to a stored procedure with specific result set entity mappings.
	 *
	 * @param procedureName The name of the procedure.
	 * @param resultSetMappings The explicit result set mapping(s) to use for mapping the results
	 * @return The representation of the procedure call.
	 */
	public ProcedureCall createStoredProcedureCall(String procedureName, String... resultSetMappings) {
		return getSession().createStoredProcedureCall(procedureName, resultSetMappings);
	}
	
	/**
	 * Create {@link Criteria} instance for the given class (entity or subclasses/implementors).
	 *
	 * @param persistentClass The class, which is an entity, or has entity subclasses/implementors
	 * @return The criteria instance for manipulation and execution
	 */
	public Criteria createCriteria(Class persistentClass) {
		return getSession().createCriteria(persistentClass);
	}
	
	/**
	 * Create {@link Criteria} instance for the given class (entity or subclasses/implementors),
	 * using a specific alias.
	 *
	 * @param persistentClass The class, which is an entity, or has entity subclasses/implementors
	 * @param alias The alias to use
	 * @return The criteria instance for manipulation and execution
	 */
	public Criteria createCriteria(Class persistentClass, String alias) {
		return getSession().createCriteria(persistentClass, alias);
	}
	
	/**
	 * Create {@link Criteria} instance for the given entity name.
	 *
	 * @param entityName The entity name @return The criteria instance for manipulation and
	 *            execution
	 */
	public Criteria createCriteria(String entityName) {
		return getSession().createCriteria(entityName);
	}
	
	/**
	 * Create {@link Criteria} instance for the given entity name, using a specific alias.
	 *
	 * @param entityName The entity name
	 * @param alias The alias to use
	 * @return The criteria instance for manipulation and execution
	 */
	public Criteria createCriteria(String entityName, String alias) {
		return getSession().createCriteria(entityName, alias);
	}
	
	/**
	 * Obtain a {@link Session} builder with the ability to grab certain information from this
	 * session.
	 *
	 * @return The session builder
	 */
	public SharedSessionBuilder sessionWithOptions() {
		return getSession().sessionWithOptions();
	}
	
	/**
	 * Force this session to flush. Must be called at the end of a unit of work, before committing
	 * the transaction and closing the session (depending on {@link #setFlushMode(FlushMode)},
	 * {@link Transaction#commit()} calls this method).
	 * <p/>
	 * <i>Flushing</i> is the process of synchronizing the underlying persistent store with
	 * persistable state held in memory.
	 *
	 * @throws HibernateException Indicates problems flushing the session or talking to the
	 *             database.
	 */
	public void flush() throws HibernateException {
		getSession().flush();
	}
	
	/**
	 * Set the flush mode for this session.
	 * <p/>
	 * The flush mode determines the points at which the session is flushed. <i>Flushing</i> is the
	 * process of synchronizing the underlying persistent store with persistable state held in
	 * memory.
	 * <p/>
	 * For a logically "read only" session, it is reasonable to set the session's flush mode to
	 * {@link FlushMode#MANUAL} at the start of the session (in order to achieve some extra
	 * performance).
	 *
	 * @param flushMode the new flush mode
	 * @see FlushMode
	 */
	public void setFlushMode(FlushMode flushMode) {
		getSession().setFlushMode(flushMode);
	}
	
	/**
	 * Get the current flush mode for this session.
	 *
	 * @return The flush mode
	 */
	public FlushMode getFlushMode() {
		return getSession().getFlushMode();
	}
	
	/**
	 * Set the cache mode.
	 * <p/>
	 * Cache mode determines the manner in which this session can interact with the second level
	 * cache.
	 *
	 * @param cacheMode The new cache mode.
	 */
	public void setCacheMode(CacheMode cacheMode) {
		getSession().setCacheMode(cacheMode);
	}
	
	/**
	 * Get the current cache mode.
	 *
	 * @return The current cache mode.
	 */
	public CacheMode getCacheMode() {
		return getSession().getCacheMode();
	}
	
	/**
	 * Get the session factory which created this session.
	 *
	 * @return The session factory.
	 * @see SessionFactory
	 */
	public SessionFactory getSessionFactory() {
		return getSession().getSessionFactory();
	}
	
	/**
	 * End the session by releasing the JDBC connection and cleaning up. It is not strictly
	 * necessary to close the session but you must at least {@link #disconnect()} it.
	 *
	 * @return the connection provided by the application or null.
	 * @throws HibernateException Indicates problems cleaning up.
	 */
	public Connection close() throws HibernateException {
		return getSession().close();
	}
	
	/**
	 * Cancel the execution of the current query.
	 * <p/>
	 * This is the sole method on session which may be safely called from another thread.
	 *
	 * @throws HibernateException There was a problem canceling the query
	 */
	public void cancelQuery() throws HibernateException {
		getSession().cancelQuery();
	}
	
	/**
	 * Check if the session is still open.
	 *
	 * @return boolean
	 */
	public boolean isOpen() {
		return getSession().isOpen();
	}
	
	/**
	 * Check if the session is currently connected.
	 *
	 * @return boolean
	 */
	public boolean isConnected() {
		return getSession().isConnected();
	}
	
	/**
	 * Does this session contain any changes which must be synchronized with the database? In other
	 * words, would any DML operations be executed if we flushed this session?
	 *
	 * @return True if the session contains pending changes; false otherwise.
	 * @throws HibernateException could not perform dirtying checking
	 */
	public boolean isDirty() throws HibernateException {
		return getSession().isDirty();
	}
	
	/**
	 * Will entities and proxies that are loaded into this session be made read-only by default? To
	 * determine the read-only/modifiable setting for a particular entity or proxy:
	 * 
	 * @see Session#isReadOnly(Object)
	 * @return true, loaded entities/proxies will be made read-only by default; false, loaded
	 *         entities/proxies will be made modifiable by default.
	 */
	public boolean isDefaultReadOnly() {
		return getSession().isDefaultReadOnly();
	}
	
	/**
	 * Change the default for entities and proxies loaded into this session from modifiable to
	 * read-only mode, or from modifiable to read-only mode. Read-only entities are not
	 * dirty-checked and snapshots of persistent state are not maintained. Read-only entities can be
	 * modified, but changes are not persisted. When a proxy is initialized, the loaded entity will
	 * have the same read-only/modifiable setting as the uninitialized proxy has, regardless of the
	 * session's current setting. To change the read-only/modifiable setting for a particular entity
	 * or proxy that is already in this session:
	 * 
	 * @see Session#setReadOnly(Object,boolean) To override this session's read-only/modifiable
	 *      setting for entities and proxies loaded by a Query:
	 * @see Query#setReadOnly(boolean)
	 * @param readOnly true, the default for loaded entities/proxies is read-only; false, the
	 *            default for loaded entities/proxies is modifiable
	 */
	public void setDefaultReadOnly(boolean readOnly) {
		getSession().setDefaultReadOnly(readOnly);
	}
	
	/**
	 * Return the identifier value of the given entity as associated with this session. An exception
	 * is thrown if the given entity instance is transient or detached in relation to this session.
	 *
	 * @param object a persistent instance
	 * @return the identifier
	 * @throws TransientObjectException if the instance is transient or associated with a different
	 *             session
	 */
	public Serializable getIdentifier(Object object) {
		return getSession().getIdentifier(object);
	}
	
	/**
	 * Check if this instance is associated with this <tt>Session</tt>.
	 *
	 * @param object an instance of a persistent class
	 * @return true if the given instance is associated with this <tt>Session</tt>
	 */
	public boolean contains(Object object) {
		return getSession().contains(object);
	}
	
	/**
	 * Remove this instance from the session cache. Changes to the instance will not be synchronized
	 * with the database. This operation cascades to associated instances if the association is
	 * mapped with <tt>cascade="evict"</tt>.
	 *
	 * @param object The entity to evict
	 * @throws NullPointerException if the passed object is {@code null}
	 * @throws IllegalArgumentException if the passed object is not defined as an entity
	 */
	public void evict(Object object) {
		getSession().evict(object);
	}
	
	/**
	 * Return the persistent instance of the given entity class with the given identifier, obtaining
	 * the specified lock mode, assuming the instance exists.
	 *
	 * @param theClass a persistent class
	 * @param id a valid identifier of an existing persistent instance of the class
	 * @param lockMode the lock level
	 * @return the persistent instance or proxy
	 * @deprecated LockMode parameter should be replaced with LockOptions
	 */
	@Deprecated
	public Object load(Class theClass, Serializable id, LockMode lockMode) {
		return getSession().load(theClass, id, lockMode);
	}
	
	/**
	 * Return the persistent instance of the given entity class with the given identifier, obtaining
	 * the specified lock mode, assuming the instance exists.
	 *
	 * @param theClass a persistent class
	 * @param id a valid identifier of an existing persistent instance of the class
	 * @param lockOptions contains the lock level
	 * @return the persistent instance or proxy
	 */
	public Object load(Class theClass, Serializable id, LockOptions lockOptions) {
		return getSession().load(theClass, id, lockOptions);
	}
	
	/**
	 * Return the persistent instance of the given entity class with the given identifier, obtaining
	 * the specified lock mode, assuming the instance exists.
	 *
	 * @param entityName a persistent class
	 * @param id a valid identifier of an existing persistent instance of the class
	 * @param lockMode the lock level
	 * @return the persistent instance or proxy
	 * @deprecated LockMode parameter should be replaced with LockOptions
	 */
	@Deprecated
	public Object load(String entityName, Serializable id, LockMode lockMode) {
		return getSession().load(entityName, id, lockMode);
	}
	
	/**
	 * Return the persistent instance of the given entity class with the given identifier, obtaining
	 * the specified lock mode, assuming the instance exists.
	 *
	 * @param entityName a persistent class
	 * @param id a valid identifier of an existing persistent instance of the class
	 * @param lockOptions contains the lock level
	 * @return the persistent instance or proxy
	 */
	public Object load(String entityName, Serializable id, LockOptions lockOptions) {
		return getSession().load(entityName, id, lockOptions);
	}
	
	/**
	 * Return the persistent instance of the given entity class with the given identifier, assuming
	 * that the instance exists. This method might return a proxied instance that is initialized
	 * on-demand, when a non-identifier method is accessed. <br>
	 * <br>
	 * You should not use this method to determine if an instance exists (use <tt>get()</tt>
	 * instead). Use this only to retrieve an instance that you assume exists, where non-existence
	 * would be an actual error.
	 *
	 * @param theClass a persistent class
	 * @param id a valid identifier of an existing persistent instance of the class
	 * @return the persistent instance or proxy
	 */
	public Object load(Class theClass, Serializable id) {
		return getSession().load(theClass, id);
	}
	
	/**
	 * Return the persistent instance of the given entity class with the given identifier, assuming
	 * that the instance exists. This method might return a proxied instance that is initialized
	 * on-demand, when a non-identifier method is accessed. <br>
	 * <br>
	 * You should not use this method to determine if an instance exists (use <tt>get()</tt>
	 * instead). Use this only to retrieve an instance that you assume exists, where non-existence
	 * would be an actual error.
	 *
	 * @param entityName a persistent class
	 * @param id a valid identifier of an existing persistent instance of the class
	 * @return the persistent instance or proxy
	 */
	public Object load(String entityName, Serializable id) {
		return getSession().load(entityName, id);
	}
	
	/**
	 * Read the persistent state associated with the given identifier into the given transient
	 * instance.
	 *
	 * @param object an "empty" instance of the persistent class
	 * @param id a valid identifier of an existing persistent instance of the class
	 */
	public void load(Object object, Serializable id) {
		getSession().load(object, id);
	}
	
	/**
	 * Persist the state of the given detached instance, reusing the current identifier value. This
	 * operation cascades to associated instances if the association is mapped with
	 * {@code cascade="replicate"}
	 *
	 * @param object a detached instance of a persistent class
	 * @param replicationMode The replication mode to use
	 */
	public void replicate(Object object, ReplicationMode replicationMode) {
		getSession().replicate(object, replicationMode);
	}
	
	/**
	 * Persist the state of the given detached instance, reusing the current identifier value. This
	 * operation cascades to associated instances if the association is mapped with
	 * {@code cascade="replicate"}
	 *
	 * @param entityName The entity name
	 * @param object a detached instance of a persistent class
	 * @param replicationMode The replication mode to use
	 */
	public void replicate(String entityName, Object object, ReplicationMode replicationMode) {
		getSession().replicate(entityName, object, replicationMode);
	}
	
	/**
	 * Persist the given transient instance, first assigning a generated identifier. (Or using the
	 * current value of the identifier property if the <tt>assigned</tt> generator is used.) This
	 * operation cascades to associated instances if the association is mapped with
	 * {@code cascade="save-update"}
	 *
	 * @param object a transient instance of a persistent class
	 * @return the generated identifier
	 */
	public Serializable save(Object object) {
		return getSession().save(object);
	}
	
	/**
	 * Persist the given transient instance, first assigning a generated identifier. (Or using the
	 * current value of the identifier property if the <tt>assigned</tt> generator is used.) This
	 * operation cascades to associated instances if the association is mapped with
	 * {@code cascade="save-update"}
	 *
	 * @param entityName The entity name
	 * @param object a transient instance of a persistent class
	 * @return the generated identifier
	 */
	public Serializable save(String entityName, Object object) {
		return getSession().save(entityName, object);
	}
	
	/**
	 * Either {@link #save(Object)} or {@link #update(Object)} the given instance, depending upon
	 * resolution of the unsaved-value checks (see the manual for discussion of unsaved-value
	 * checking).
	 * <p/>
	 * This operation cascades to associated instances if the association is mapped with
	 * {@code cascade="save-update"}
	 *
	 * @param object a transient or detached instance containing new or updated state
	 * @see Session#save(java.lang.Object)
	 * @see Session#update(Object object)
	 */
	public void saveOrUpdate(Object object) {
		getSession().saveOrUpdate(object);
	}
	
	/**
	 * Either {@link #save(String, Object)} or {@link #update(String, Object)} the given instance,
	 * depending upon resolution of the unsaved-value checks (see the manual for discussion of
	 * unsaved-value checking).
	 * <p/>
	 * This operation cascades to associated instances if the association is mapped with
	 * {@code cascade="save-update"}
	 *
	 * @param entityName The entity name
	 * @param object a transient or detached instance containing new or updated state
	 * @see Session#save(String,Object)
	 * @see Session#update(String,Object)
	 */
	public void saveOrUpdate(String entityName, Object object) {
		getSession().saveOrUpdate(entityName, object);
	}
	
	/**
	 * Update the persistent instance with the identifier of the given detached instance. If there
	 * is a persistent instance with the same identifier, an exception is thrown. This operation
	 * cascades to associated instances if the association is mapped with
	 * {@code cascade="save-update"}
	 *
	 * @param object a detached instance containing updated state
	 */
	public void update(Object object) {
		getSession().update(object);
	}
	
	/**
	 * Update the persistent instance with the identifier of the given detached instance. If there
	 * is a persistent instance with the same identifier, an exception is thrown. This operation
	 * cascades to associated instances if the association is mapped with
	 * {@code cascade="save-update"}
	 *
	 * @param entityName The entity name
	 * @param object a detached instance containing updated state
	 */
	public void update(String entityName, Object object) {
		getSession().update(entityName, object);
	}
	
	/**
	 * Copy the state of the given object onto the persistent object with the same identifier. If
	 * there is no persistent instance currently associated with the session, it will be loaded.
	 * Return the persistent instance. If the given instance is unsaved, save a copy of and return
	 * it as a newly persistent instance. The given instance does not become associated with the
	 * session. This operation cascades to associated instances if the association is mapped with
	 * {@code cascade="merge"}
	 * <p/>
	 * The semantics of this method are defined by JSR-220.
	 *
	 * @param object a detached instance with state to be copied
	 * @return an updated persistent instance
	 */
	public Object merge(Object object) {
		return getSession().merge(object);
	}
	
	/**
	 * Copy the state of the given object onto the persistent object with the same identifier. If
	 * there is no persistent instance currently associated with the session, it will be loaded.
	 * Return the persistent instance. If the given instance is unsaved, save a copy of and return
	 * it as a newly persistent instance. The given instance does not become associated with the
	 * session. This operation cascades to associated instances if the association is mapped with
	 * {@code cascade="merge"}
	 * <p/>
	 * The semantics of this method are defined by JSR-220.
	 *
	 * @param entityName The entity name
	 * @param object a detached instance with state to be copied
	 * @return an updated persistent instance
	 */
	public Object merge(String entityName, Object object) {
		return getSession().merge(entityName, object);
	}
	
	/**
	 * Make a transient instance persistent. This operation cascades to associated instances if the
	 * association is mapped with {@code cascade="persist"}
	 * <p/>
	 * The semantics of this method are defined by JSR-220.
	 *
	 * @param object a transient instance to be made persistent
	 */
	public void persist(Object object) {
		getSession().persist(object);
	}
	
	/**
	 * Make a transient instance persistent. This operation cascades to associated instances if the
	 * association is mapped with {@code cascade="persist"}
	 * <p/>
	 * The semantics of this method are defined by JSR-220.
	 *
	 * @param entityName The entity name
	 * @param object a transient instance to be made persistent
	 */
	public void persist(String entityName, Object object) {
		getSession().persist(entityName, object);
	}
	
	/**
	 * Remove a persistent instance from the datastore. The argument may be an instance associated
	 * with the receiving <tt>Session</tt> or a transient instance with an identifier associated
	 * with existing persistent state. This operation cascades to associated instances if the
	 * association is mapped with {@code cascade="delete"}
	 *
	 * @param object the instance to be removed
	 */
	public void delete(Object object) {
		getSession().delete(object);
	}
	
	/**
	 * Remove a persistent instance from the datastore. The <b>object</b> argument may be an
	 * instance associated with the receiving <tt>Session</tt> or a transient instance with an
	 * identifier associated with existing persistent state. This operation cascades to associated
	 * instances if the association is mapped with {@code cascade="delete"}
	 *
	 * @param entityName The entity name for the instance to be removed.
	 * @param object the instance to be removed
	 */
	public void delete(String entityName, Object object) {
		getSession().delete(entityName, object);
	}
	
	/**
	 * Obtain the specified lock level upon the given object. This may be used to perform a version
	 * check (<tt>LockMode.READ</tt>), to upgrade to a pessimistic lock (
	 * <tt>LockMode.PESSIMISTIC_WRITE</tt>), or to simply reassociate a transient instance with a
	 * session (<tt>LockMode.NONE</tt>). This operation cascades to associated instances if the
	 * association is mapped with <tt>cascade="lock"</tt>.
	 *
	 * @param object a persistent or transient instance
	 * @param lockMode the lock level
	 * @deprecated instead call buildLockRequest(LockMode).lock(object)
	 */
	@Deprecated
	public void lock(Object object, LockMode lockMode) {
		getSession().lock(object, lockMode);
	}
	
	/**
	 * Obtain the specified lock level upon the given object. This may be used to perform a version
	 * check (<tt>LockMode.OPTIMISTIC</tt>), to upgrade to a pessimistic lock (
	 * <tt>LockMode.PESSIMISTIC_WRITE</tt>), or to simply reassociate a transient instance with a
	 * session (<tt>LockMode.NONE</tt>). This operation cascades to associated instances if the
	 * association is mapped with <tt>cascade="lock"</tt>.
	 *
	 * @param entityName The name of the entity
	 * @param object a persistent or transient instance
	 * @param lockMode the lock level
	 * @deprecated instead call buildLockRequest(LockMode).lock(entityName, object)
	 */
	@SuppressWarnings({ "JavaDoc" })
	@Deprecated
	public void lock(String entityName, Object object, LockMode lockMode) {
		getSession().lock(entityName, object, lockMode);
	}
	
	/**
	 * Build a LockRequest that specifies the LockMode, pessimistic lock timeout and lock scope.
	 * timeout and scope is ignored for optimistic locking. After building the LockRequest, call
	 * LockRequest.lock to perform the requested locking.
	 * <p/>
	 * Example usage:
	 * {@code session.buildLockRequest().setLockMode(LockMode.PESSIMISTIC_WRITE).setTimeOut(60000).lock(entity);}
	 *
	 * @param lockOptions contains the lock level
	 * @return a lockRequest that can be used to lock the passed object.
	 */
	public LockRequest buildLockRequest(LockOptions lockOptions) {
		return getSession().buildLockRequest(lockOptions);
	}
	
	/**
	 * Re-read the state of the given instance from the underlying database. It is inadvisable to
	 * use this to implement long-running sessions that span many business tasks. This method is,
	 * however, useful in certain special circumstances. For example
	 * <ul>
	 * <li>where a database trigger alters the object state upon insert or update
	 * <li>after executing direct SQL (eg. a mass update) in the same session
	 * <li>after inserting a <tt>Blob</tt> or <tt>Clob</tt>
	 * </ul>
	 *
	 * @param object a persistent or detached instance
	 */
	public void refresh(Object object) {
		getSession().refresh(object);
	}
	
	/**
	 * Re-read the state of the given instance from the underlying database. It is inadvisable to
	 * use this to implement long-running sessions that span many business tasks. This method is,
	 * however, useful in certain special circumstances. For example
	 * <ul>
	 * <li>where a database trigger alters the object state upon insert or update
	 * <li>after executing direct SQL (eg. a mass update) in the same session
	 * <li>after inserting a <tt>Blob</tt> or <tt>Clob</tt>
	 * </ul>
	 *
	 * @param entityName a persistent class
	 * @param object a persistent or detached instance
	 */
	public void refresh(String entityName, Object object) {
		getSession().refresh(entityName, object);
	}
	
	/**
	 * Re-read the state of the given instance from the underlying database, with the given
	 * <tt>LockMode</tt>. It is inadvisable to use this to implement long-running sessions that span
	 * many business tasks. This method is, however, useful in certain special circumstances.
	 *
	 * @param object a persistent or detached instance
	 * @param lockMode the lock mode to use
	 * @deprecated LockMode parameter should be replaced with LockOptions
	 */
	@Deprecated
	public void refresh(Object object, LockMode lockMode) {
		getSession().refresh(object, lockMode);
	}
	
	/**
	 * Re-read the state of the given instance from the underlying database, with the given
	 * <tt>LockMode</tt>. It is inadvisable to use this to implement long-running sessions that span
	 * many business tasks. This method is, however, useful in certain special circumstances.
	 *
	 * @param object a persistent or detached instance
	 * @param lockOptions contains the lock mode to use
	 */
	public void refresh(Object object, LockOptions lockOptions) {
		getSession().refresh(object, lockOptions);
	}
	
	/**
	 * Re-read the state of the given instance from the underlying database, with the given
	 * <tt>LockMode</tt>. It is inadvisable to use this to implement long-running sessions that span
	 * many business tasks. This method is, however, useful in certain special circumstances.
	 *
	 * @param entityName a persistent class
	 * @param object a persistent or detached instance
	 * @param lockOptions contains the lock mode to use
	 */
	public void refresh(String entityName, Object object, LockOptions lockOptions) {
		getSession().refresh(entityName, object, lockOptions);
	}
	
	/**
	 * Determine the current lock mode of the given object.
	 *
	 * @param object a persistent instance
	 * @return the current lock mode
	 */
	public LockMode getCurrentLockMode(Object object) {
		return getSession().getCurrentLockMode(object);
	}
	
	/**
	 * Create a {@link Query} instance for the given collection and filter string. Contains an
	 * implicit {@code FROM} element named {@code this} which refers to the defined table for the
	 * collection elements, as well as an implicit {@code WHERE} restriction for this particular
	 * collection instance's key value.
	 *
	 * @param collection a persistent collection
	 * @param queryString a Hibernate query fragment.
	 * @return The query instance for manipulation and execution
	 */
	public Query createFilter(Object collection, String queryString) {
		return getSession().createFilter(collection, queryString);
	}
	
	/**
	 * Completely clear the session. Evict all loaded instances and cancel all pending saves,
	 * updates and deletions. Do not close open iterators or instances of <tt>ScrollableResults</tt>
	 * .
	 */
	public void clear() {
		getSession().clear();
	}
	
	/**
	 * Return the persistent instance of the given entity class with the given identifier, or null
	 * if there is no such persistent instance. (If the instance is already associated with the
	 * session, return that instance. This method never returns an uninitialized instance.)
	 *
	 * @param clazz a persistent class
	 * @param id an identifier
	 * @return a persistent instance or null
	 */
	public Object get(Class clazz, Serializable id) {
		return getSession().get(clazz, id);
	}
	
	/**
	 * Return the persistent instance of the given entity class with the given identifier, or null
	 * if there is no such persistent instance. (If the instance is already associated with the
	 * session, return that instance. This method never returns an uninitialized instance.) Obtain
	 * the specified lock mode if the instance exists.
	 *
	 * @param clazz a persistent class
	 * @param id an identifier
	 * @param lockMode the lock mode
	 * @return a persistent instance or null
	 * @deprecated LockMode parameter should be replaced with LockOptions
	 */
	@Deprecated
	public Object get(Class clazz, Serializable id, LockMode lockMode) {
		return getSession().get(clazz, id, lockMode);
	}
	
	/**
	 * Return the persistent instance of the given entity class with the given identifier, or null
	 * if there is no such persistent instance. (If the instance is already associated with the
	 * session, return that instance. This method never returns an uninitialized instance.) Obtain
	 * the specified lock mode if the instance exists.
	 *
	 * @param clazz a persistent class
	 * @param id an identifier
	 * @param lockOptions the lock mode
	 * @return a persistent instance or null
	 */
	public Object get(Class clazz, Serializable id, LockOptions lockOptions) {
		return getSession().get(clazz, id, lockOptions);
	}
	
	/**
	 * Return the persistent instance of the given named entity with the given identifier, or null
	 * if there is no such persistent instance. (If the instance is already associated with the
	 * session, return that instance. This method never returns an uninitialized instance.)
	 *
	 * @param entityName the entity name
	 * @param id an identifier
	 * @return a persistent instance or null
	 */
	public Object get(String entityName, Serializable id) {
		return getSession().get(entityName, id);
	}
	
	/**
	 * Return the persistent instance of the given entity class with the given identifier, or null
	 * if there is no such persistent instance. (If the instance is already associated with the
	 * session, return that instance. This method never returns an uninitialized instance.) Obtain
	 * the specified lock mode if the instance exists.
	 *
	 * @param entityName the entity name
	 * @param id an identifier
	 * @param lockMode the lock mode
	 * @return a persistent instance or null
	 * @deprecated LockMode parameter should be replaced with LockOptions
	 */
	@Deprecated
	public Object get(String entityName, Serializable id, LockMode lockMode) {
		return getSession().get(entityName, id, lockMode);
	}
	
	/**
	 * Return the persistent instance of the given entity class with the given identifier, or null
	 * if there is no such persistent instance. (If the instance is already associated with the
	 * session, return that instance. This method never returns an uninitialized instance.) Obtain
	 * the specified lock mode if the instance exists.
	 *
	 * @param entityName the entity name
	 * @param id an identifier
	 * @param lockOptions contains the lock mode
	 * @return a persistent instance or null
	 */
	public Object get(String entityName, Serializable id, LockOptions lockOptions) {
		return getSession().get(entityName, id, lockOptions);
	}
	
	/**
	 * Return the entity name for a persistent entity.
	 * 
	 * @param object a persistent entity
	 * @return the entity name
	 */
	public String getEntityName(Object object) {
		return getSession().getEntityName(object);
	}
	
	/**
	 * Create an {@link IdentifierLoadAccess} instance to retrieve the specified entity type by
	 * primary key.
	 * 
	 * @param entityName The entity name of the entity type to be retrieved
	 * @return load delegate for loading the specified entity type by primary key
	 * @throws HibernateException If the specified entity name cannot be resolved as an entity name
	 */
	public IdentifierLoadAccess byId(String entityName) {
		return getSession().byId(entityName);
	}
	
	/**
	 * Create an {@link IdentifierLoadAccess} instance to retrieve the specified entity by primary
	 * key.
	 *
	 * @param entityClass The entity type to be retrieved
	 * @return load delegate for loading the specified entity type by primary key
	 * @throws HibernateException If the specified Class cannot be resolved as a mapped entity
	 */
	public IdentifierLoadAccess byId(Class entityClass) {
		return getSession().byId(entityClass);
	}
	
	/**
	 * Create an {@link NaturalIdLoadAccess} instance to retrieve the specified entity by its
	 * natural id.
	 * 
	 * @param entityName The entity name of the entity type to be retrieved
	 * @return load delegate for loading the specified entity type by natural id
	 * @throws HibernateException If the specified entity name cannot be resolved as an entity name
	 */
	public NaturalIdLoadAccess byNaturalId(String entityName) {
		return getSession().byNaturalId(entityName);
	}
	
	/**
	 * Create an {@link NaturalIdLoadAccess} instance to retrieve the specified entity by its
	 * natural id.
	 * 
	 * @param entityClass The entity type to be retrieved
	 * @return load delegate for loading the specified entity type by natural id
	 * @throws HibernateException If the specified Class cannot be resolved as a mapped entity
	 */
	public NaturalIdLoadAccess byNaturalId(Class entityClass) {
		return getSession().byNaturalId(entityClass);
	}
	
	/**
	 * Create an {@link SimpleNaturalIdLoadAccess} instance to retrieve the specified entity by its
	 * natural id.
	 *
	 * @param entityName The entity name of the entity type to be retrieved
	 * @return load delegate for loading the specified entity type by natural id
	 * @throws HibernateException If the specified entityClass cannot be resolved as a mapped
	 *             entity, or if the entity does not define a natural-id or if its natural-id is
	 *             made up of multiple attributes.
	 */
	public SimpleNaturalIdLoadAccess bySimpleNaturalId(String entityName) {
		return getSession().bySimpleNaturalId(entityName);
	}
	
	/**
	 * Create an {@link SimpleNaturalIdLoadAccess} instance to retrieve the specified entity by its
	 * simple (single attribute) natural id.
	 *
	 * @param entityClass The entity type to be retrieved
	 * @return load delegate for loading the specified entity type by natural id
	 * @throws HibernateException If the specified entityClass cannot be resolved as a mapped
	 *             entity, or if the entity does not define a natural-id or if its natural-id is
	 *             made up of multiple attributes.
	 */
	public SimpleNaturalIdLoadAccess bySimpleNaturalId(Class entityClass) {
		return getSession().bySimpleNaturalId(entityClass);
	}
	
	/**
	 * Enable the named filter for this current session.
	 *
	 * @param filterName The name of the filter to be enabled.
	 * @return The Filter instance representing the enabled filter.
	 */
	public Filter enableFilter(String filterName) {
		return getSession().enableFilter(filterName);
	}
	
	/**
	 * Retrieve a currently enabled filter by name.
	 *
	 * @param filterName The name of the filter to be retrieved.
	 * @return The Filter instance representing the enabled filter.
	 */
	public Filter getEnabledFilter(String filterName) {
		return getSession().getEnabledFilter(filterName);
	}
	
	/**
	 * Disable the named filter for the current session.
	 *
	 * @param filterName The name of the filter to be disabled.
	 */
	public void disableFilter(String filterName) {
		getSession().disableFilter(filterName);
	}
	
	/**
	 * Get the statistics for this session.
	 *
	 * @return The session statistics being collected for this session
	 */
	public SessionStatistics getStatistics() {
		return getSession().getStatistics();
	}
	
	/**
	 * Is the specified entity or proxy read-only? To get the default read-only/modifiable setting
	 * used for entities and proxies that are loaded into the session:
	 * 
	 * @see org.hibernate.Session#isDefaultReadOnly()
	 * @param entityOrProxy an entity or HibernateProxy
	 * @return {@code true} if the entity or proxy is read-only, {@code false} if the entity or
	 *         proxy is modifiable.
	 */
	public boolean isReadOnly(Object entityOrProxy) {
		return getSession().isReadOnly(entityOrProxy);
	}
	
	/**
	 * Set an unmodified persistent object to read-only mode, or a read-only object to modifiable
	 * mode. In read-only mode, no snapshot is maintained, the instance is never dirty checked, and
	 * changes are not persisted. If the entity or proxy already has the specified
	 * read-only/modifiable setting, then this method does nothing. To set the default
	 * read-only/modifiable setting used for entities and proxies that are loaded into the session:
	 * 
	 * @see org.hibernate.Session#setDefaultReadOnly(boolean) To override this session's
	 *      read-only/modifiable setting for entities and proxies loaded by a Query:
	 * @see Query#setReadOnly(boolean)
	 * @param entityOrProxy an entity or HibernateProxy
	 * @param readOnly {@code true} if the entity or proxy should be made read-only; {@code false}
	 *            if the entity or proxy should be made modifiable
	 */
	public void setReadOnly(Object entityOrProxy, boolean readOnly) {
		getSession().setReadOnly(entityOrProxy, readOnly);
	}
	
	/**
	 * Controller for allowing users to perform JDBC related work using the Connection managed by
	 * this Session.
	 *
	 * @param work The work to be performed.
	 * @throws HibernateException Generally indicates wrapped {@link java.sql.SQLException}
	 */
	public void doWork(Work work) throws HibernateException {
		getSession().doWork(work);
	}
	
	/**
	 * Controller for allowing users to perform JDBC related work using the Connection managed by
	 * this Session. After execution returns the result of the {@link ReturningWork#execute} call.
	 *
	 * @param work The work to be performed.
	 * @param <T> The type of the result returned from the work
	 * @return the result from calling {@link ReturningWork#execute}.
	 * @throws HibernateException Generally indicates wrapped {@link java.sql.SQLException}
	 */
	public <T> T doReturningWork(ReturningWork<T> work) throws HibernateException {
		return getSession().doReturningWork(work);
	}
	
	/**
	 * Disconnect the session from its underlying JDBC connection. This is intended for use in cases
	 * where the application has supplied the JDBC connection to the session and which require
	 * long-sessions (aka, conversations).
	 * <p/>
	 * It is considered an error to call this method on a session which was not opened by supplying
	 * the JDBC connection and an exception will be thrown.
	 * <p/>
	 * For non-user-supplied scenarios, normal transaction management already handles disconnection
	 * and reconnection automatically.
	 *
	 * @return the application-supplied connection or {@code null}
	 * @see #reconnect(Connection)
	 */
	Connection disconnect() {
		return getSession().disconnect();
	}
	
	/**
	 * Reconnect to the given JDBC connection.
	 *
	 * @param connection a JDBC connection
	 * @see #disconnect()
	 */
	void reconnect(Connection connection) {
		getSession().reconnect(connection);
	}
	
	/**
	 * Is a particular fetch profile enabled on this session?
	 *
	 * @param name The name of the profile to be checked.
	 * @return True if fetch profile is enabled; false if not.
	 * @throws UnknownProfileException Indicates that the given name does not match any known
	 *             profile names
	 * @see org.hibernate.engine.profile.FetchProfile for discussion of this feature
	 */
	public boolean isFetchProfileEnabled(String name) throws UnknownProfileException {
		return getSession().isFetchProfileEnabled(name);
	}
	
	/**
	 * Enable a particular fetch profile on this session. No-op if requested profile is already
	 * enabled.
	 *
	 * @param name The name of the fetch profile to be enabled.
	 * @throws UnknownProfileException Indicates that the given name does not match any known
	 *             profile names
	 * @see org.hibernate.engine.profile.FetchProfile for discussion of this feature
	 */
	public void enableFetchProfile(String name) throws UnknownProfileException {
		getSession().enableFetchProfile(name);
	}
	
	/**
	 * Disable a particular fetch profile on this session. No-op if requested profile is already
	 * disabled.
	 *
	 * @param name The name of the fetch profile to be disabled.
	 * @throws UnknownProfileException Indicates that the given name does not match any known
	 *             profile names
	 * @see org.hibernate.engine.profile.FetchProfile for discussion of this feature
	 */
	public void disableFetchProfile(String name) throws UnknownProfileException {
		getSession().disableFetchProfile(name);
	}
	
	/**
	 * Convenience access to the {@link TypeHelper} associated with this session's
	 * {@link SessionFactory}.
	 * <p/>
	 * Equivalent to calling {@link #getSessionFactory()}.{@link SessionFactory#getTypeHelper
	 * getTypeHelper()}
	 *
	 * @return The {@link TypeHelper} associated with this session's {@link SessionFactory}
	 */
	public TypeHelper getTypeHelper() {
		return getSession().getTypeHelper();
	}
	
	/**
	 * Retrieve this session's helper/delegate for creating LOB instances.
	 *
	 * @return This session's LOB helper
	 */
	public LobHelper getLobHelper() {
		return getSession().getLobHelper();
	}
	
	/**
	 * Add one or more listeners to the Session
	 *
	 * @param listeners The listener(s) to add
	 */
	public void addEventListeners(SessionEventListener... listeners) {
		getSession().addEventListeners(listeners);
	}
}
