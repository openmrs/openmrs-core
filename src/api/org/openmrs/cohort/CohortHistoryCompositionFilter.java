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
package org.openmrs.cohort;

import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.PatientSetService.BooleanOperator;
import org.openmrs.reporting.AbstractPatientFilter;
import org.openmrs.reporting.CompoundPatientFilter;
import org.openmrs.reporting.InversePatientFilter;
import org.openmrs.reporting.PatientFilter;
import org.openmrs.reporting.PatientSet;
import org.openmrs.util.OpenmrsUtil;

public class CohortHistoryCompositionFilter extends AbstractPatientFilter
		implements PatientFilter {
	
	protected final static Log log = LogFactory.getLog(CohortHistoryCompositionFilter.class);
	
	private CohortSearchHistory history;
	private List<Object> parsedCompositionString;
	
	public CohortHistoryCompositionFilter() { }

	public CohortSearchHistory getHistory() {
		return history;
	}

	public void setHistory(CohortSearchHistory history) {
		this.history = history;
	}

	public List<Object> getParsedCompositionString() {
		return parsedCompositionString;
	}

	public void setParsedCompositionString(List<Object> parsedCompositionString) {
		this.parsedCompositionString = parsedCompositionString;
	}
	
	public String getName() {
		return nameHelper(parsedCompositionString);
	}
	
	public void setName(String name) { }
	
	private String nameHelper(List list) {
		StringBuilder ret = new StringBuilder();
		for (Object o : list) {
			if (ret.length() > 0)
				ret.append(" ");
			if (o instanceof List)
				ret.append("(" + nameHelper((List) o) + ")");
			else
				ret.append(o);
		}
		return ret.toString();
	}
	
	/**
	 * Call this to notify this composition filter that the _i_th element of the search history
	 * has been removed, and it potentially needs to renumber its constituent parts
	 * @return whether or not this filter itself should be removed (because it directly references the removed history element
	 *//*
	public boolean removeFromHistoryNotify(int i) {
		return removeHelper(parsedCompositionString, i);
	}
	
	private boolean removeHelper(List<Object> list, int i) {
		boolean ret = false;
		for (ListIterator<Object> iter = list.listIterator(); iter.hasNext(); ) {
			Object o = iter.next();
			if (o instanceof List)
				ret |= removeHelper((List) o, i);
			else if (o instanceof Integer) {
				Integer ref = (Integer) o;
				if (ref == i) {
					ret = true;
					iter.set("-1");
				} else if (ref < i)
					iter.set(ref - 1);
			}
		}
		return ret;
	}
	*/
	
	private PatientFilter toPatientFilter(List<Object> phrase) {
		// Recursive step:
		// * if anything in this list is a list, then recurse on that
		// * if anything in this list is a number, replace it with the relevant filter from the history
		log.debug("Starting with " + phrase);
		List<Object> use = new ArrayList<Object>();
		for (ListIterator<Object> i = phrase.listIterator(); i.hasNext(); ) {
			Object o = i.next();
			if (o instanceof List)
				use.add(toPatientFilter((List<Object>) o));
			else if (o instanceof Integer)
				use.add(getHistory().getSearchHistory().get((Integer) o - 1));
			else
				use.add(o);
		}
		
		// base case. All elements are PatientFilter or BooleanOperator.
		log.debug("Base case with " + use);
		
		// first, replace all [..., NOT, PatientFilter, ...] with [ ..., InvertedPatientFilter, ...]
		boolean invertTheNext = false;
		for (ListIterator<Object> i = use.listIterator(); i.hasNext(); ) {
			Object o = i.next();
			if (o instanceof BooleanOperator) {
				if ((BooleanOperator) o == BooleanOperator.NOT) {
					i.remove();
					invertTheNext = !invertTheNext;
				} else {
					if (invertTheNext)
						throw new RuntimeException("Can't have NOT AND. Test() should have failed");
				}
			} else {
				if (invertTheNext) {
					i.set(new InversePatientFilter((PatientFilter) o));
					invertTheNext = false;
				}
			}
		}
		
		log.debug("Finished with NOTs: " + use);
		
		// Now all we have left are PatientFilter, AND, OR
		// eventually go with left-to-right precedence, and we can combine runs of the same operator into a single one
		//     1 AND 2 AND 3 -> AND(1, 2, 3)
		//     1 AND 2 OR 3 -> OR(AND(1, 2), 3)
		// for now a hack so we take the last operator in the run, and apply that to all filters
		//     for example 1 AND 2 OR 3 -> OR(1, 2, 3)
		if (use.size() == 1) {
			return (PatientFilter) use.get(0);
		}
		BooleanOperator bo = BooleanOperator.AND;
		List<PatientFilter> args = new ArrayList<PatientFilter>();
		for (Object o : use)
			if (o instanceof BooleanOperator)
				bo = (BooleanOperator) o;
			else
				args.add((PatientFilter) o);
		
		return new CompoundPatientFilter(bo, args);
	}
	
	public PatientFilter toCohortDefinition() {
		return toPatientFilter(getParsedCompositionString());
	}

	public PatientSet filter(PatientSet input) {
		PatientFilter pf = toPatientFilter(getParsedCompositionString());
		return pf.filter(input);
	}

	public PatientSet filterInverse(PatientSet input) {
		PatientFilter pf = toPatientFilter(getParsedCompositionString());
		return pf.filterInverse(input);
	}
	
	public boolean isReadyToRun() {
		return true;
	}

}
