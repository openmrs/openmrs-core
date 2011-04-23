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
package org.openmrs.attribute;

import java.util.List;
import java.util.Set;

/**
 * Marker interface for classes that hold attributes, e.g. Visit has VisitAttributes, so it implements
 * AttributeHolder<VisitAttribute>
 * @param <AttrType> the type of attribute held
 * @since 1.9
 */
public interface AttributeHolder<AttrType> {
	
	/**
	 * @return all attributes (including voided ones)
	 */
	Set<AttrType> getAttributes();
	
	/**
	 * @return non-voided attributes
	 */
	List<AttrType> getActiveAttributes();
	
}
