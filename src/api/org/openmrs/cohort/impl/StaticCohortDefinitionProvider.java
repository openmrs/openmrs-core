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
package org.openmrs.cohort.impl;

import java.util.List;
import java.util.Vector;

import org.openmrs.Cohort;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.cohort.CohortDefinition;
import org.openmrs.cohort.CohortDefinitionItemHolder;
import org.openmrs.cohort.CohortDefinitionProvider;
import org.openmrs.cohort.StaticCohortDefinition;
import org.openmrs.report.EvaluationContext;

/**
 * This class provides access to {@link org.openmrs.Cohort} objects that are saved in the cohort
 * table, but exposes them as {@link CohortDefinition} The
 * {@link #evaluate(CohortDefinition, EvaluationContext)} method does not set links to the
 * CohortDefinition or EvaluationContext in the cohort that it returns. Those are not meaningful in
 * the context of a static cohort, and alse because we want to preserve the original
 * CohortDefinition that produced the Cohort that is being wrapped by a StaticCohortDefinition.
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class StaticCohortDefinitionProvider implements CohortDefinitionProvider {
	
	/**
	 * @see org.openmrs.cohort.CohortDefinitionProvider#getClassHandled()
	 */
	public Class<? extends CohortDefinition> getClassHandled() {
		return StaticCohortDefinition.class;
	}
	
	/**
	 * Note that this method does *not* set a CohortDefinition or EvaluationContext in the Cohort
	 * that it returns, although that Cohort may have had those properties set when it was
	 * originally evaluated.
	 * 
	 * @see org.openmrs.cohort.CohortDefinitionProvider#evaluate(org.openmrs.cohort.CohortDefinition,
	 *      org.openmrs.report.EvaluationContext)
	 */
	public Cohort evaluate(CohortDefinition cohortDefinition, EvaluationContext evaluationContext) {
		StaticCohortDefinition def = (StaticCohortDefinition) cohortDefinition;
		return def.getCohort();
	}
	
	/**
	 * @see org.openmrs.cohort.CohortDefinitionProvider#getAllCohortDefinitions()
	 */
	public List<CohortDefinitionItemHolder> getAllCohortDefinitions() {
		List<CohortDefinitionItemHolder> ret = new Vector<CohortDefinitionItemHolder>();
		for (Cohort cohort : Context.getCohortService().getAllCohorts()) {
			CohortDefinitionItemHolder item = new CohortDefinitionItemHolder();
			CohortDefinition cohortDefinition = new StaticCohortDefinition(cohort);
			item.setKey(cohort.getCohortId() + ":" + cohortDefinition.getClass().getCanonicalName());
			item.setName(cohort.getName());
			item.setCohortDefinition(cohortDefinition);
			ret.add(item);
		}
		return ret;
	}
	
	/**
	 * @see org.openmrs.cohort.CohortDefinitionProvider#getCohortDefinition(java.lang.Integer)
	 */
	public CohortDefinition getCohortDefinition(Integer id) {
		return new StaticCohortDefinition(Context.getCohortService().getCohort(id));
	}
	
	/**
	 * @see org.openmrs.cohort.CohortDefinitionProvider#purgeCohortDefinition(org.openmrs.cohort.CohortDefinition)
	 */
	public void purgeCohortDefinition(CohortDefinition cohortDefinition) {
		throw new APIException("Not Yet Implemented");
	}
	
	/**
	 * @see org.openmrs.cohort.CohortDefinitionProvider#saveCohortDefinition(org.openmrs.cohort.CohortDefinition)
	 */
	public CohortDefinition saveCohortDefinition(CohortDefinition cohortDefinition) {
		StaticCohortDefinition def = (StaticCohortDefinition) cohortDefinition;
		Cohort c = def.getCohort();
		Context.getCohortService().saveCohort(c);
		return def;
	}
	
}
