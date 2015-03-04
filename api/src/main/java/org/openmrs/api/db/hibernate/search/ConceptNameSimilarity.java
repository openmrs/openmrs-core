/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
