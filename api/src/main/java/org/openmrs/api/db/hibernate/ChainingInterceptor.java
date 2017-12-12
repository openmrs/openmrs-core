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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.CallbackException;
import org.hibernate.EntityMode;
import org.hibernate.Interceptor;
import org.hibernate.Transaction;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used by the {@link HibernateSessionFactoryBean} to keep track of multiple interceptors <br>
 * Each of the methods in {@link Interceptor} are called for each interceptor that is added to this
 * class
 * 
 * @since 1.9
 */
public class ChainingInterceptor implements Interceptor {
	
	private static final Logger log = LoggerFactory.getLogger(ChainingInterceptor.class);
	
	// using a linkedhashset to preserve insert order and maintain a list of unique objects
	public Collection<Interceptor> interceptors = new LinkedHashSet<>();
	
	/**
	 * Adds the given interceptor to the list of interceptors to be applied to hibernate sessions.
	 * Interceptors are called in the added order, with core interceptors being called first
	 * 
	 * @param interceptor the interceptor to add to the queue
	 */
	public void addInterceptor(Interceptor interceptor) {
		// do nothing if adding ourself to the list. This would cause infinite looping
		if (interceptor == this) {
			log.error("Attempting to add self to chain.  This would result in epic failures.");
			return;
		}
		
		log.debug("Adding " + interceptor + " to interceptor chain");
		
		if (interceptors == null) {
			interceptors = new LinkedHashSet<>();
		}
		
		interceptors.add(interceptor);
	}
	
	@Override
	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		for (Interceptor i : interceptors) {
			i.onDelete(entity, id, state, propertyNames, types);
		}
	}
	
	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
	        String[] propertyNames, Type[] types) {
		boolean objectChanged = false;
		
		for (Interceptor i : interceptors) {
			// must be in this order so that java doesn't skip the method call for optimizations
			objectChanged = i.onFlushDirty(entity, id, currentState, previousState, propertyNames, types) || objectChanged;
		}
		
		return objectChanged;
	}
	
	@Override
	public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		boolean objectChanged = false;
		
		for (Interceptor i : interceptors) {
			// must be in this order so that java doesn't skip the method call for optimizations
			objectChanged = i.onLoad(entity, id, state, propertyNames, types) || objectChanged;
		}
		
		return objectChanged;
	}
	
	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		boolean objectChanged = false;
		
		for (Interceptor i : interceptors) {
			// must be in this order so that java doesn't skip the method call for optimizations
			objectChanged = i.onSave(entity, id, state, propertyNames, types) || objectChanged;
		}
		
		return objectChanged;
	}
	
	@Override
	public void postFlush(Iterator entities) {
		for (Interceptor i : interceptors) {
			i.postFlush(entities);
		}
	}
	
	@Override
	public void preFlush(Iterator entities) {
		for (Interceptor i : interceptors) {
			i.preFlush(entities);
		}
	}
	
	@Override
	public Boolean isTransient(Object entity) {
		// by default let hibernate figure it out
		Boolean returnValue = null;
		
		for (Interceptor i : interceptors) {
			Boolean tmpReturnValue = i.isTransient(entity);
			
			// 
			if (tmpReturnValue != null) {
				if (returnValue == null) {
					returnValue = tmpReturnValue;
				} else {
					returnValue = returnValue && tmpReturnValue;
				}
			}
		}
		
		return returnValue;
	}
	
	// returns the first non-null response from all interceptors
	@Override
	public Object instantiate(String entityName, EntityMode entityMode, Serializable id) {
		for (Interceptor i : interceptors) {
			Object o = i.instantiate(entityName, entityMode, id);
			if (o != null) {
				return o;
			}
		}
		
		return null;
	}
	
	@Override
	public int[] findDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
	        String[] propertyNames, Type[] types) {
		
		List<Integer> uniqueIndices = new LinkedList<>();
		
		for (Interceptor i : interceptors) {
			int[] indices = i.findDirty(entity, id, currentState, previousState, propertyNames, types);
			if (indices != null) {
				for (int index : indices) {
					if (!uniqueIndices.contains(index)) {
						uniqueIndices.add(index);
					}
				}
			}
		}
		
		if (uniqueIndices.isEmpty()) {
			return null;
		}
		
		// turn it back into an array and return it
		
		int[] uniquePrimitiveIndices = new int[uniqueIndices.size()];
		
		for (int x = 0; x < uniqueIndices.size(); x++) {
			uniquePrimitiveIndices[x] = uniqueIndices.get(x);
		}
		
		return uniquePrimitiveIndices;
	}
	
	// returns the first non-null name from the interceptors
	@Override
	public String getEntityName(Object object) {
		for (Interceptor i : interceptors) {
			String name = i.getEntityName(object);
			if (name != null) {
				return name;
			}
		}
		
		return null;
	}
	
	@Override
	public Object getEntity(String entityName, Serializable id) {
		for (Interceptor i : interceptors) {
			Object o = i.getEntity(entityName, id);
			if (o != null) {
				return o;
			}
		}
		
		return null;
	}
	
	@Override
	public void afterTransactionBegin(Transaction tx) {
		for (Interceptor i : interceptors) {
			i.afterTransactionBegin(tx);
		}
	}
	
	@Override
	public void afterTransactionCompletion(Transaction tx) {
		for (Interceptor i : interceptors) {
			i.afterTransactionCompletion(tx);
		}
	}
	
	@Override
	public void beforeTransactionCompletion(Transaction tx) {
		for (Interceptor i : interceptors) {
			i.beforeTransactionCompletion(tx);
		}
	}
	
	// passes the sql returned from each previous onPrepareStatement onto the next
	@Override
	public String onPrepareStatement(String sql) {
		for (Interceptor i : interceptors) {
			sql = i.onPrepareStatement(sql);
		}
		
		return sql;
	}
	
	@Override
	public void onCollectionRemove(Object collection, Serializable key) throws CallbackException {
		for (Interceptor i : interceptors) {
			i.onCollectionRemove(collection, key);
		}
	}
	
	@Override
	public void onCollectionRecreate(Object collection, Serializable key) throws CallbackException {
		for (Interceptor i : interceptors) {
			i.onCollectionRecreate(collection, key);
		}
	}
	
	@Override
	public void onCollectionUpdate(Object collection, Serializable key) throws CallbackException {
		for (Interceptor i : interceptors) {
			i.onCollectionUpdate(collection, key);
		}
	}
	
}
