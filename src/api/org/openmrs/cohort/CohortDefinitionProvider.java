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
package org.openmrs.cohort;

import java.util.List;

import org.openmrs.Cohort;
import org.openmrs.cohort.impl.StaticCohortDefinitionProvider;
import org.openmrs.report.EvaluationContext;

/**
 * This interfaces provides the functionality to evaluate a CohortDefinition (of a particular class)
 * and return a Cohort It also handles persistence of CohortDefinition classes Historical note: it
 * exists because in order to keep our application layers separate, we need to be able to call
 * CohortService.evaluate(CohortDefinition, EvaluationContext) -> Cohort instead of
 * CohortDefinition.evaluate(EvaluationContext) -> Cohort
 */
public interface CohortDefinitionProvider {
	
	/**
	 * TODO: this is potentially not necessary because its set via spring's application context
	 * 
	 * @return all the classes that this provider is capable of evaluating or persisting
	 */
	public Class<? extends CohortDefinition> getClassHandled();
	
	/**
	 * @return All cohort definitions whose persistence is managed by this provider
	 */
	public List<CohortDefinitionItemHolder> getAllCohortDefinitions();
	
	/**
	 * @param id
	 * @return the cohort definition with the given id, of the type whose persistence is managed by
	 *         this class
	 */
	public CohortDefinition getCohortDefinition(Integer id);
	
	/**
	 * Creates or updates a CohortDefinition in the database. CohortService ensures that this method
	 * is only called with a CohortDefinition of a class this provider is registered for.
	 * 
	 * @param cohortDefinition
	 * @return the CohortDefinition that was passed in
	 */
	public CohortDefinition saveCohortDefinition(CohortDefinition cohortDefinition);
	
	/**
	 * Deletes a cohort definition from the database. CohortService ensures that this method is only
	 * called with a CohortDefinition of a class this provider is registered for.
	 * 
	 * @param cohortDefinition
	 */
	public void purgeCohortDefinition(CohortDefinition cohortDefinition);
	
	/**
	 * Computes the list of patients who currently meet the given definition CohortService ensures
	 * that this method is only called with a CohortDefinition of a class this provider is
	 * registered for. The Cohort that is returned will generally be populated with links back to
	 * the CohortDefinition and EvaluationContext passed into this method, although this is
	 * occasionally not the case, for example in {@link StaticCohortDefinitionProvider}
	 * 
	 * @param cohortDefinition CohortDefinition to evaluate
	 * @param evaluationContext context to use during evaluation
	 * @return the cohort of all patients who meet the definition now
	 */
	public Cohort evaluate(CohortDefinition cohortDefinition, EvaluationContext evaluationContext);
	
}
