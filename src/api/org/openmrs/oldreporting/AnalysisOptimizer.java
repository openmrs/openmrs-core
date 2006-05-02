package org.openmrs.oldreporting;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openmrs.Patient;

public class AnalysisOptimizer {

	private Map<Object, Analysis> analyses;
	
	public AnalysisOptimizer() {
		analyses = new HashMap<Object, Analysis>();
	}
	
	public void addAnalysis(Object key, Analysis a) {
		analyses.put(key, a);
	}
	
	public Analysis getAnalysis(Object key) {
		return analyses.get(key);
	}
	
	public void runAll() { }
	
	public DataTable getResult(Set<Patient> input, Object key) {
		return analyses.get(key).run(input);
	}
	
}
