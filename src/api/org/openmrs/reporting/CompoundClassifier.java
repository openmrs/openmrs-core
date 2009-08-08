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

import java.util.ArrayList;
import java.util.List;

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
