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

import java.util.function.Function;

import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

/**
 * Allows to set final predicate for a query. It can be used for example to apply filters.
 * <p>
 * When setting a final predicate, the code should first check if {@link #getFinalPredicate()} is not null and include it in 
 * the new final predicate.
 * <p>
 * See {@link SearchCreatedEvent}.
 * 
 * @since 2.8.0
 */
public interface SearchQueryContributor {

	void setFinalPredicate(Function<SearchPredicateFactory, SearchPredicate> finalPredicate);

	Function<SearchPredicateFactory, SearchPredicate> getFinalPredicate();
}
