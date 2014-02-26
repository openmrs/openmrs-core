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
package org.openmrs.xml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.simpleframework.xml.graph.ReferenceLogic;

/**
 * This reference logic class allows openmrs to call serialize multiple times on separate objects
 * and carry the reference ids over
 * 
 * @deprecated - Use OpenmrsSerializer from Context.getSerializationService.getDefaultSerializer()
 */
@Deprecated
public class OpenmrsReferenceLogic implements ReferenceLogic {
	
	private static Log log = LogFactory.getLog(OpenmrsReferenceLogic.class);
	
	private long key = 0L;
	
	/**
	 * @see org.simpleframework.xml.graph.ReferenceLogic#getReferenceKey(java.lang.Object)
	 */
	public String getReferenceKey(Object arg0) {
		if (++key >= Long.MAX_VALUE) {
			log.warn("We reached the max long value.");
			key = 0L;
		}
		
		return Long.toString(key);
	}
	
}
