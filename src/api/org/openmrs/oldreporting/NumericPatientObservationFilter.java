package org.openmrs.oldreporting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.AbstractReportObject;

public class NumericPatientObservationFilter extends AbstractReportObject implements DataFilter<Patient> {

	/**
	 * ANY means any observation matches
	 * ALL means all the observations match
	 * FIRST means the first observation matches (i.e. not the same as "first matching observation")
	 * LAST means the last observation matches (i.e. not the same as "last matching observation")
	 */
	public enum Method { ANY, ALL, FIRST, LAST }
	public enum Modifier { LESS, LESS_EQUAL, GREATER, GREATER_EQUAL, EQUAL, NOT_EQUAL }
	
	private Context context;
	private Concept concept;
	private Modifier modifier;
	private Number number;
	private Method method;
	
	/**
	 * @see org.openmrs.oldreporting.DataFilter#filter(org.openmrs.oldreporting.DataSet)
	 */
	public <U extends Patient> DataSet<Patient> filter(DataSet<U> input) {
		// TODO Auto-generated method stub
		return null;
	}

	public NumericPatientObservationFilter(Concept concept, Modifier modifier, Number number, Method method) {
		this.concept = concept;
		this.modifier = modifier;
		this.number = number;
		this.method = method;
		// TODO Make sure concept is a numeric one 
	}
	
	/**
	 * @return Returns the context.
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * @param context The context to set.
	 */
	public void setContext(Context context) {
		this.context = context;
	}

	public <P extends Patient> DataSet<P> fildter(DataSet<P> input) {
		ObsService os = context.getObsService();
		DataSet<P> ret = null; //new SimpleDataSet<P>();
		for (P p : input.getRowKeys()) {
			Set<Obs> set =  os.getObservations(p, concept);
			if (setHelper(set)) {
				ret.setRow(p, input.getRow(p));
			}
		}
		return ret;
	}

	private boolean setHelper(Set<Obs> set) {
		if (set == null || set.size() == 0) {
			return false;
		}
		List<Obs> list = new ArrayList<Obs>(set);
		java.util.Collections.sort(list, new Comparator<Obs>() {
				public int compare(Obs left, Obs right) {
					int temp = left.getObsDatetime().compareTo(right.getObsDatetime());
					if (temp == 0) {
						temp = left.getObsId().compareTo(right.getObsId());
					}
					return temp;
				}
			});
		if (method == Method.FIRST) {
			return obsHelper(list.get(0));
		} else if (method == Method.LAST) {
			return obsHelper(list.get(list.size() - 1));
		} else if (method == Method.ALL) {
			for (Obs o : list) {
				if (!obsHelper(o)) {
					return false;
				}
			}
			return true;
		} else {// ANY {
			for (Obs o : list) {
				if (obsHelper(o)) {
					return true;
				}
			}
			return false;
		}
	}

	private boolean obsHelper(Obs o) {
		double val = o.getValueNumeric().doubleValue();
		double comp = number.doubleValue();
		if (modifier == Modifier.EQUAL) {
			return val == comp;
		} else if (modifier == Modifier.NOT_EQUAL) {
			return val != comp;
		} else if (modifier == Modifier.GREATER) {
			return val > comp;
		} else if (modifier == Modifier.GREATER_EQUAL) {
			return val >= comp;
		} else if (modifier == Modifier.LESS) {
			return val < comp;
		} else if (modifier == Modifier.LESS_EQUAL) {
			return val <= comp;
		} else {
			return false;
		}
	}
	
	public String getDescription() {
		return method + " of " + concept + " " + modifier + " " + number;
	}
}
