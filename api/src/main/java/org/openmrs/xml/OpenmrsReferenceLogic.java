/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
