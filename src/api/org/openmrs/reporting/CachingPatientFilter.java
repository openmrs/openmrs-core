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
package org.openmrs.reporting;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.report.EvaluationContext;

/**
 *
 */
public abstract class CachingPatientFilter extends AbstractPatientFilter implements PatientFilter {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Subclasses should implement PatientFilter.filter("all patients", evalContext) in this method
	 * 
	 * @param context
	 * @return
	 */
	public abstract Cohort filterImpl(EvaluationContext context);
	
	/**
	 * @return The key under which this object, with its current parameter values, will store
	 *         results in a cache. Changing properties of this object will typically change the
	 *         cache key returned.
	 */
	public abstract String getCacheKey();
	
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
	
	public Cohort filter(Cohort input, EvaluationContext context) {
		Cohort cached = getAndMaybeCache(context);
		if (input == null) {
			if (context != null)
				input = context.getBaseCohort();
			else
				input = Context.getPatientSetService().getAllPatients();
		}
		return Cohort.intersect(input, cached);
	}
	
	public Cohort filterInverse(Cohort input, EvaluationContext context) {
		Cohort cached = getAndMaybeCache(context);
		if (input == null) {
			if (context != null)
				input = context.getBaseCohort();
			else
				input = Context.getPatientSetService().getAllPatients();
		}
		return Cohort.subtract(input, cached);
	}
	
	public abstract boolean isReadyToRun();
	
}
