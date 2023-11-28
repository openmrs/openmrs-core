/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

public enum MatchMode {
	START,
	END,
	ANYWHERE,
	EXACT;

	public String toCaseSensitivePattern(String str) {
		return toPatternInternal(str, false);
	}

	public String toLowerCasePattern(String str) {
		return toPatternInternal(str, true);
	}

	private String toPatternInternal(String str, boolean caseInsensitive) {
		if (str == null) {
			return null;
		}
		String processedStr = caseInsensitive ? str.toLowerCase() : str;
		switch (this) {
			case START:
				return processedStr + "%";
			case END:
				return "%" + processedStr;
			case ANYWHERE:
				return "%" + processedStr + "%";
			case EXACT:
			default:
				return processedStr;
		}
	}
}
