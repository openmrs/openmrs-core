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

import java.util.HashMap;

import org.openmrs.Concept;
import org.openmrs.Patient;

public interface LogicService {
	
	public Rule getRule(String token);

	public void addToken(String token, Class clazz) throws LogicException;
	
	public void addToken(String token, Concept concept) throws LogicException;
	
	public void removeToken(String token);
	
	public Result eval(Patient who, String token);

	public Result eval(Patient who, String token, Object[] args);

	public Result eval(Patient who, Aggregation aggregation, String token,
			DateConstraint constraint, Object[] args);

	public Result eval(Patient who, Concept concept);

	public Result eval(Patient who, Aggregation aggregation, Concept concept,
			DateConstraint constraint);

	public HashMap<Patient, HashMap<String, Result>> eval(PatientCohort cohort,
			String[] tokenList, Object[] args);

}