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
package org.openmrs.logic.util;

import java.util.Map;
import java.util.Set;

import org.openmrs.Cohort;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.LogicTransform;
import org.openmrs.logic.op.Operator;
import org.openmrs.logic.result.EmptyResult;
import org.openmrs.logic.result.Result;

/**
 * 
 */
public class Util {

	/**
	 * Programmatically applies aggregators like COUNT, AVERAGE, etc
	 * 
	 * @param finalResult result map of patient id to result list
	 * @param criteria provides type of transform
	 */
	public static void applyAggregators(Map<Integer, Result> finalResult,
			LogicCriteria criteria,Cohort patients) {
		Set<Integer> personIds = finalResult.keySet();
		LogicTransform transform = criteria.getExpression().getTransform();
		
		// finalResult is empty so populate it with empty counts/averages
		if (personIds.size() == 0) {

			for (Integer personId : patients.getMemberIds()) {
				if (transform != null
				        && (transform.getTransformOperator() == Operator.COUNT || transform.getTransformOperator() == Operator.AVERAGE)) {
					Result newResult = new Result();
					newResult.setValueNumeric(0);
					finalResult.put(personId, newResult);
				}
			}
			return;
		}
		
		for (Integer personId : personIds) {
			// if this was a count, then return the actual count of results
			// instead of the objects
			
			Result r = finalResult.get(personId);
			if (transform != null
			        && transform.getTransformOperator() == Operator.COUNT) {
				Result newResult = new Result();
				newResult.setValueNumeric(r.size());
				finalResult.put(personId, newResult);
			} else if (transform != null
			        && transform.getTransformOperator() == Operator.AVERAGE) {

				int count = 0;
				double sum = 0;
				for (Result currResult : r) {
					if (!(currResult instanceof EmptyResult)) {
						count++;
						sum += currResult.toNumber();
					}
				}
				double average = 0;
				if (count > 0 && sum > 0) {
					average = sum / count;
				}
				Result newResult = new Result();
				newResult.setValueNumeric(average);
				finalResult.put(personId, newResult);
			}
		}
	}
}
