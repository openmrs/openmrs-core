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
package org.openmrs.api.db.hibernate.search;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.search.DefaultSimilarity;

/**
 * It modifies default scoring algorithm so that the shorter the matching name is the better.
 */
public class ConceptNameSimilarity extends DefaultSimilarity {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public float computeNorm(String field, FieldInvertState state) {
		final int numTerms;
		if (discountOverlaps)
			numTerms = state.getLength() - state.getNumOverlap();
		else
			numTerms = state.getLength();
		
		//Score longer documents much lower
		return state.getBoost() * ((float) (1.0 / numTerms / numTerms / numTerms / numTerms));
	}
}
