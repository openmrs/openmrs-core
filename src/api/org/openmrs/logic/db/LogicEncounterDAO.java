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
package org.openmrs.logic.db;

import java.util.List;

import org.openmrs.Cohort;
import org.openmrs.Encounter;
import org.openmrs.logic.LogicCriteria;

/**
 *
 */
public interface LogicEncounterDAO {
	
	/**
	 * @see org.openmrs.api.db.Hibernate.HibernateObsDAO#getObservations(org.openmrs.Cohort,
	 *      org.openmrs.logic.LogicCriteria)
	 */
	public List<Encounter> getEncounters(Cohort who, LogicCriteria logicCriteria);
	
}
