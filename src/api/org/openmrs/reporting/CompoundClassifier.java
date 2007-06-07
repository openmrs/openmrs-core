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
