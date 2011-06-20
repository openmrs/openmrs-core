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
package org.openmrs;

/**
 * Base implementation of the {@link Orderable} interface, with concept, name, and description properties.
 * The name and description will be inherited from the concept if they're not explicitly specified.
 * 
 * @param <T> the specific type of {@link Order} that will ultimately be created from this Orderable.
 */
public abstract class BaseOrderable<T extends Order> implements Orderable<T> {
	
	protected Concept concept;
	
	protected String name;
	
	protected String description;
	
	/**
	 * @see org.openmrs.Orderable#getConcept()
	 */
	@Override
	public Concept getConcept() {
		return concept;
	}
	
	/**
	 * Returns the name property, if explicitly specified. Otherwise returns the preferred name of the concept
	 * in the authenticated user's locale. 
	 * 
	 * @see org.openmrs.Orderable#getName()
	 */
	@Override
	public String getName() {
		if (name != null)
			return name;
		else if (concept != null)
			return concept.getName().getName();
		else
			return null;
	}
	
	/**
	 * Returns the description property, if explicitly specified. Otherwise returns the description of the concept
	 * in the authenticated user's locale.
	 * 
	 * @see org.openmrs.Orderable#getDescription()
	 */
	@Override
	public String getDescription() {
		if (name != null)
			return name;
		else if (concept != null)
			return concept.getDescription().getDescription();
		else
			return null;
	}
}
