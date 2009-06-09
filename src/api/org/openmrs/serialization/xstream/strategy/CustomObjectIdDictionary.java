/*
 * Copyright (C) 2004 Joe Walnes.
 * Copyright (C) 2006, 2007, 2008 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 09. May 2004 by Joe Walnes
 */
package org.openmrs.serialization.xstream.strategy;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class adds the functionality to deal with EnhancerByCGLIB proxy in the course of judging if
 * it needs an "id" reference. If we lookup id for a EnhancerByCGLIB proxy, use its .equals()
 * method, not the "==" comparator.
 */
public class CustomObjectIdDictionary {
	
	/* 
	 * About the info of map
	 * Key: the object match one element in serialized xml string
	 * Value: the id of that match element
	 */
	@SuppressWarnings("unchecked")
	private final Map map = new LinkedHashMap();
	
	private static final String marker = new String("EnhancerByCGLIB");
	
	private volatile int counter;
	
	/**
	 * Auto generated method comment
	 * 
	 * @param obj
	 * @param id
	 */
	@SuppressWarnings("unchecked")
	public void associateId(Object obj, Object id) {
		map.put(obj, id);
		++counter;
		cleanup();
	}
	
	/**
	 * Look up id for current obj. If map already contains current obj, then return id of current
	 * obj. If map doesn't contain current obj, then return null so that xstream will add a new "id"
	 * attribute for this obj's match element. <br/>
	 * <br/>
	 * Note: the compare operation for current obj has two cases. If current obj is a
	 * EnhancerByCGLIB proxy, use its "equals()" to compare. If current obj is a common pojo, use
	 * "==" to compare
	 * 
	 * @param obj the obj need to judge if it's already in map
	 * @return if obj is already in map, return id of obj. if obj is not in map, return null
	 */
	@SuppressWarnings("unchecked")
	public Object lookupId(Object obj) {
		++counter;
		if (isCGlibEhanced(obj)) {
			Iterator it = map.keySet().iterator();
			while (it.hasNext()) {
				Object key = it.next();
				/*
				 * for cglib proxy, just use equals method to compare whether map already has a equal obj in it.
				 * if yes, return the id matching with that equal obj
				 * if no, return null
				 * 
				 * Here we don't use the "==" to compare for cglib proxy is that 
				 * even if a pojo and a cglib proxy are the same one, but the address of pojo and cglib proxy are not equal
				 * so we replace "==" with "equals" here just for cglib proxy.
				 * 
				 * Here we don't use "equals()" for pojo is that I find for those pojo which is a collection, 
				 * if we use "equals()" to compare whether map already has a same collection obj in it, maybe this manner
				 * will bring about some mistakes.
				 * For example, A is Set<PersonName> and B is Set<PersonAddress>, 
				 * but if A and B's size both are zero, then we will get the "true" result while we use "equals()" to compare these two collections
				 * you can refer to the jdk document, search "Set", and then look its "equals()" method's description
				 */
				if (obj.equals(key))
					return map.get(key);
			}
			return null;
		} else {
			Iterator it = map.keySet().iterator();
			while (it.hasNext()) {
				Object key = it.next();
				if (isCGlibEhanced(key)) {
					//for cglib proxy, we use "equals"
					if (obj.equals(key))
						return map.get(key);
				} else {
					//for pojo, we just use "=="
					if (obj == key)
						return map.get(key);
				}
			}
			return null;
		}
	}
	
	/**
	 * Judge whether map contains a key equals with item. <br/>
	 * <br/>
	 * Note: the compare operation for current obj has two cases. If current obj is a
	 * EnhancerByCGLIB proxy, use its equals to compare. If current obj is a common pojo, use == to
	 * compare
	 * 
	 * @param obj the obj need to judge if it's already in map
	 * @return whether item has already been in map
	 */
	@SuppressWarnings("unchecked")
	public boolean containsId(Object item) {
		boolean contain = false;
		++counter;
		if (isCGlibEhanced(item)) {
			//for EnhancerByCGLIB proxy, just use equals method to compare
			Iterator it = map.keySet().iterator();
			while (it.hasNext()) {
				Object key = it.next();
				//for cglib proxy just use equals method
				if (item.equals(key)) {
					contain = true;
					break;
				}
			}
		} else {
			//for common pojo, use == to compare
			Iterator it = map.keySet().iterator();
			while (it.hasNext()) {
				Object key = it.next();
				if (isCGlibEhanced(key)) {
					//if exist one EnhancerByCGLIB proxy in map, pass it into "item.equals(...)"
					if (item.equals(key)) {
						contain = true;
						break;
					}
				} else {
					if (item == key) {
						contain = true;
						break;
					}
				}
			}
		}
		return contain;
	}
	
	public void removeId(Object item) {
		map.remove(item);
		++counter;
		cleanup();
	}
	
	public int size() {
		return map.size();
	}
	
	private void cleanup() {
		if (counter > 10000) {
			counter = 0;
			// much more efficient to remove any orphaned wrappers at once
			for (final Iterator iterator = map.keySet().iterator(); iterator.hasNext();) {
				if (iterator.next() == null) {
					iterator.remove();
				}
			}
		}
	}
	
	/**
	 * judge whether a obj is a EnhancerByCGLIB proxy
	 * 
	 * @param obj the object need to be judged
	 * @return whether this obj is a EnhancerByCGLIB proxy
	 */
	private boolean isCGlibEhanced(Object obj) {
		String className = obj.getClass().getName();
		if (className.indexOf(marker) != -1)
			return true;
		else
			return false;
	}
	
}
