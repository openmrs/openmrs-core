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
package org.openmrs.logic;

import org.openmrs.Concept;
import org.openmrs.Patient;

public interface LogicDataSource {
	
	public Result eval(Patient patient, String token);
	
	public Result eval(Patient patient, String token, Object[] args);

	public Result eval(Patient patient, Rule rule, Object[] args);
	
	public Result eval(Patient patient, Aggregation aggregation, Concept concept, Constraint constraint);
	
	public Result eval(Patient patient, Aggregation aggregation, String token, Constraint constraint, Object[] args);

	public Result eval(Patient patient, Aggregation aggregation, Rule rule, Constraint constraint, Object[] args);
	
}
