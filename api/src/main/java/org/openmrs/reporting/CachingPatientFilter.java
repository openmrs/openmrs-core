/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.reporting;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.report.EvaluationContext;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public abstract class CachingPatientFilter extends AbstractPatientFilter implements PatientFilter {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Subclasses should implement PatientFilter.filter("all patients", evalContext) in this method
	 *
	 * @param context
	 * @return TODO
	 */
	public abstract Cohort filterImpl(EvaluationContext context);
	
	/**
	 * @return The key under which this object, with its current parameter values, will store
	 *         results in a cache. Changing properties of this object will typically change the
	 *         cache key returned.
	 */
	public abstract String getCacheKey();
	
	/**
	 * TODO Auto generated method comment
	 *
	 * @param context
	 * @return
	 */
	private Cohort getAndMaybeCache(EvaluationContext context) {
		if (context == null) {
			return filterImpl(null);
		} else {
			String key = getCacheKey();
			Cohort cached = (Cohort) context.getFromCache(key);
			if (cached == null) {
				cached = filterImpl(context);
				context.addToCache(key, cached);
			}
			return cached;
		}
	}
	
	/**
	 * @see org.openmrs.reporting.PatientFilter#filter(org.openmrs.Cohort,
	 *      org.openmrs.report.EvaluationContext)
	 */
	public Cohort filter(Cohort input, EvaluationContext context) {
		Cohort cached = getAndMaybeCache(context);
		if (input == null) {
			if (context != null) {
				input = context.getBaseCohort();
			} else {
				input = Context.getPatientSetService().getAllPatients();
			}
		}
		return Cohort.intersect(input, cached);
	}
	
	/**
	 * @see org.openmrs.reporting.PatientFilter#filterInverse(org.openmrs.Cohort,
	 *      org.openmrs.report.EvaluationContext)
	 */
	public Cohort filterInverse(Cohort input, EvaluationContext context) {
		Cohort cached = getAndMaybeCache(context);
		if (input == null) {
			if (context != null) {
				input = context.getBaseCohort();
			} else {
				input = Context.getPatientSetService().getAllPatients();
			}
		}
		return Cohort.subtract(input, cached);
	}
	
	/**
	 * @see org.openmrs.reporting.PatientFilter#isReadyToRun()
	 */
	public abstract boolean isReadyToRun();
	
}
