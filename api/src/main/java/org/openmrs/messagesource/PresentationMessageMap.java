/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.messagesource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * A mapped collection of PresentationMessages, all of which are enforced to be in the same locale.
 */
public class PresentationMessageMap implements Map<String, PresentationMessage> {
	
	private Locale locale;
	
	private Map<String, PresentationMessage> internalMap = new HashMap<>();
	
	/**
	 * Create a new PresentationMessageMap for the given locale.
	 * 
	 * @param locale
	 */
	public PresentationMessageMap(Locale locale) {
		this.locale = locale;
	}
	
	/**
	 * @see java.util.Map#clear()
	 */
	@Override
	public void clear() {
		internalMap.clear();
	}
	
	/**
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey(Object key) {
		return internalMap.containsKey(key);
	}
	
	/**
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	@Override
	public boolean containsValue(Object value) {
		return internalMap.containsValue(value);
	}
	
	/**
	 * @see java.util.Map#entrySet()
	 */
	@Override
	public Set<Entry<String, PresentationMessage>> entrySet() {
		return internalMap.entrySet();
	}
	
	/**
	 * @see java.util.Map#get(java.lang.Object)
	 */
	@Override
	public PresentationMessage get(Object key) {
		return internalMap.get(key);
	}
	
	/**
	 * @see java.util.Map#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return internalMap.isEmpty();
	}
	
	/**
	 * @see java.util.Map#keySet()
	 */
	@Override
	public Set<String> keySet() {
		return internalMap.keySet();
	}
	
	/**
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 * @should should ignore non matching locale messages
	 */
	@Override
	public PresentationMessage put(String key, PresentationMessage value) {
		PresentationMessage putValue = null;
		if (value.getLocale().equals(locale)) {
			putValue = internalMap.put(key, value);
		}
		return putValue;
	}
	
	/**
	 * Adds all entries from an input Map which have PresentationMessages from the same locale.
	 * 
	 * @see java.util.Map#putAll(java.util.Map)
	 * @should filter out non matching locale messages from batch add
	 */
	@Override
	public void putAll(Map<? extends String, ? extends PresentationMessage> t) {
		for (Entry<? extends String, ? extends PresentationMessage> entry : t.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	@Override
	public PresentationMessage remove(Object key) {
		return internalMap.remove(key);
	}
	
	/**
	 * @see java.util.Map#size()
	 */
	@Override
	public int size() {
		return internalMap.size();
	}
	
	/**
	 * @see java.util.Map#values()
	 */
	@Override
	public Collection<PresentationMessage> values() {
		return internalMap.values();
	}
	
}
