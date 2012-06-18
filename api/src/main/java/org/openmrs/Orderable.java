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
 * Marker interface for things that a User can order. Generally this interface is meant to drive a UI where the
 * User types a few letters and is presented with options matching that search that can be ordered.
 * 
 * @param <T> the specific type of {@link Order} that will ultimately be created from this Orderable.
 */
public interface Orderable<T extends Order> {
	
	/**
	 * The unique identifier for this Orderable.
	 * 
	 * @return the orderable's unique identifier.
	 */
	public String getUniqueIdentifier();
	
	/**
	 * The concept of the order that will ultimately be created from this Orderable.
	 * 
	 * @return the order's concept
	 */
	public Concept getConcept();
	
	/**
	 * The display name of this Orderable, possibly inherited from the concept. Intended as the title of this
	 * orderable when presenting it in a UI.
	 * 
	 * @return name of this Orderable element
	 */
	public String getName();
	
	/**
	 * A description of this orderable, possibly inherited from the concept. Intended as a further description
	 * of this orderable in a UI, in case the name is not sufficient. 
	 * 
	 * @return description of this Orderable
	 */
	public String getDescription();
}
