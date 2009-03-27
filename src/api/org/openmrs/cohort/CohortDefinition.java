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

import org.openmrs.report.Parameterizable;
import org.simpleframework.xml.Root;

/**
 * Represents a search strategy for arriving at a cohort. This interface is intentionally empty. You
 * evaluate a CohortDefinition using CohortService
 * 
 * @see org.openmrs.api.CohortService#evaluate(CohortDefinition,
 *      org.openmrs.report.EvaluationContext)
 */
@Root(strict = false)
public interface CohortDefinition extends Parameterizable {

}
