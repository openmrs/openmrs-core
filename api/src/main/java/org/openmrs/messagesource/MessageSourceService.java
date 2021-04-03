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

import java.util.Set;

/**
 * Interface for a service which expands on the basic capabilities of a message source.
 */
public interface MessageSourceService extends MutableMessageSource {
	
	/**
	 * Get the message with the given code from the current user's selected locale.
	 * 
	 * @see #getMessage(String arg0, Object[] arg1, java.util.Locale arg2)
	 * @param s message code to retrieve
	 * @return the translated message
	 */
	public String getMessage(String s);
	
	/**
	 * Gets the message source service which is currently providing services.
	 * 
	 * @return the activeMessageSource
	 */
	public MutableMessageSource getActiveMessageSource();
	
	/**
	 * Sets the message source service which will actually provide services.
	 * 
	 * @param activeMessageSource the <code>MutableMessageSource</code> to set
	 */
	public void setActiveMessageSource(MutableMessageSource activeMessageSource);
	
	/**
	 * @return the availableMessageSources
	 */
	public Set<MutableMessageSource> getMessageSources();
	
	/**
	 * @param availableMessageSources the availableMessageSources to set
	 */
	public void setMessageSources(Set<MutableMessageSource> availableMessageSources);
	
}
