package org.openmrs.util;

import org.openmrs.ConceptMapType;

import java.util.Comparator;

public class ConceptMapTypeComparator implements Comparator<ConceptMapType> {
	
	@Override
	public int compare(ConceptMapType conceptMapType, ConceptMapType conceptMapType2) {
		int firstWeight = getConceptMapTypeSortWeight(conceptMapType);
		int secondWeight = getConceptMapTypeSortWeight(conceptMapType2);
		
		return (firstWeight < secondWeight) ? -1 : (firstWeight == secondWeight) ? 0 : 1;
	}
	
	public static int getConceptMapTypeSortWeight(ConceptMapType conceptMapType) {
		return ((conceptMapType.isRetired() ? 1 : 0) + (conceptMapType.isHidden() ? 2 : 0));
	}
}
