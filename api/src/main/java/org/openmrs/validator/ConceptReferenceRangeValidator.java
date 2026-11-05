/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.validator;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jspecify.annotations.NonNull;
import org.openmrs.ConceptReferenceRange;
import org.openmrs.annotation.Handler;
import org.openmrs.util.ConceptReferenceRangeUtility;
import org.springframework.expression.ParseException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validates {@link ConceptReferenceRange} objects. Criteria are parsed with the same SpEL parser
 * used at evaluation time so that unparseable criteria are rejected when the range is saved rather
 * than failing every subsequent obs save for the concept.
 *
 * @since 3.0.0
 */
@Handler(supports = { ConceptReferenceRange.class }, order = 50)
public class ConceptReferenceRangeValidator implements Validator {

	/**
	 * Determines if the command object being submitted is a valid type
	 *
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> c) {
		return ConceptReferenceRange.class.isAssignableFrom(c);
	}

	/**
	 * Checks that a given concept reference range object is valid.
	 * <p>
	 * <strong>Should</strong> fail if the concept reference range object is null<br/>
	 * <strong>Should</strong> fail if the criteria does not parse<br/>
	 * <strong>Should</strong> fail if the criteria calls a disallowed method<br/>
	 * <strong>Should</strong> fail if the criteria assigns a value<br/>
	 * <strong>Should</strong> pass if the criteria parses<br/>
	 * <strong>Should</strong> pass if the criteria is html escaped and parses once unescaped<br/>
	 * <strong>Should</strong> pass if the criteria is null<br/>
	 *
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 */
	@Override
	public void validate(@NonNull Object obj, @NonNull Errors errors) {
		if (!(obj instanceof ConceptReferenceRange referenceRange)) {
			throw new IllegalArgumentException(
			        "The parameter obj should not be null and must be of type " + ConceptReferenceRange.class);
		}

		String criteria = referenceRange.getCriteria();
		if (StringUtils.isBlank(criteria)) {
			return;
		}

		try {
			List<String> disallowed = ConceptReferenceRangeUtility
			        .findDisallowedConstructs(StringEscapeUtils.unescapeHtml4(criteria));
			if (!disallowed.isEmpty()) {
				String constructs = String.join(", ", disallowed);
				errors.rejectValue("criteria", "ConceptReferenceRange.error.criteria.disallowed",
				    new Object[] { constructs }, "Criteria contains expressions that are not allowed: " + constructs);
			}
		} catch (ParseException e) {
			errors.rejectValue("criteria", "ConceptReferenceRange.error.criteria.invalid",
			    new Object[] { e.getSimpleMessage() }, "Criteria is not a valid expression: " + e.getSimpleMessage());
		}
	}
}
