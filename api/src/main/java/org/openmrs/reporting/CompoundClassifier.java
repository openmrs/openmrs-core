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

import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class CompoundClassifier implements TableRowClassifier {
	
	private String separator;
	
	private List<TableRowClassifier> classifiers;
	
	public CompoundClassifier(String separator) {
		this.separator = separator;
		classifiers = new ArrayList<TableRowClassifier>();
	}
	
	public void addClassifiers(TableRowClassifier... args) {
		for (TableRowClassifier c : args)
			classifiers.add(c);
	}
	
	public String classify(TableRow row) {
		StringBuilder sb = new StringBuilder();
		for (TableRowClassifier classifier : classifiers) {
			if (sb.length() > 0)
				sb.append(separator);
			sb.append(classifier.classify(row));
		}
		
		return sb.toString();
	}
	
}
