package org.openmrs.logic;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;

public class Result { // TODO: should implement List interface as well

	private Concept concept;
	private Date datetime;
	private int datatype;
	private static final int NUMERIC = 1;
	private static final int DATE = 2;
	private static final int CODED = 3;
	private static final int TEXT = 4;
	private static final int BOOLEAN = 5;
	private Double valueNumeric;
	private Date valueDate;
	private Concept valueCoded;
	private String valueText;
	private Boolean valueBoolean;

	private List<Result> valueList = null;

	public static final Result NULL_RESULT = new Result(null, null, null, null,
			null, null, null);

	public Result(Obs o) {
		init(o);
	}

	public Result(Result result) {
		init(result);
	}

	public Result(Concept concept, Date timeStamp, Double valueNumeric,
			Date valueDate, Concept valueCoded, String valueText,
			Boolean valueBoolean) {
		init(concept, timeStamp, valueNumeric, valueDate, valueCoded,
				valueText, valueBoolean);
	}

	public Result(List<Obs> obsList) {
		if (obsList == null || obsList.size() < 1)
			init(NULL_RESULT);
		else if (obsList.size() < 2)
			init(obsList.get(0));
		else {
			valueList = new Vector<Result>();
			for (Obs o : obsList)
				valueList.add(new Result((o)));
		}
	}
	
	/**
	 * Constructs a result from a list of results.  A second parameter is needed 
	 * because Java does not allow two constructors that take only a single List&lt;?&gt;
	 * parameter with different types.  The 2nd parameter serves only to make this
	 * constructor's signature unique and is ignored.
	 * 
	 * @param resultList list of results to be bound into a single result
	 * @param javaBug ignored
	 */
	public Result(List<Result> resultList, boolean javaBug) {
		if (resultList == null || resultList.size() < 1)
			init(NULL_RESULT);
		else if (resultList.size() < 2)
			init(resultList.get(0));
		else {
			valueList = new Vector<Result>();
			for (Result r : resultList)
				valueList.add(r);
		}
	}

	public Result(String valueText) {
		init(null, new Date(), null, null, null, valueText, null);
		datatype = TEXT;
	}

	public Result(Date valueDate) {
		init(null, new Date(), null, valueDate, null, null, null);
		datatype = DATE;
	}

	public Result(Boolean valueBoolean) {
		init(null, null, null, null, null, null, true);
		datatype = BOOLEAN;
	}

	private void init(Obs o) {
		init(o.getConcept(), o.getObsDatetime(), o.getValueNumeric(), o
				.getValueDatetime(), o.getValueCoded(), o.getValueText(), o
				.getValueAsBoolean());
	}

	private void init(Result result) {
		init(result.concept, result.datetime, result.valueNumeric,
				result.valueDate, result.valueCoded, result.valueText,
				result.valueBoolean);
	}

	private void init(Concept concept, Date timeStamp, Double valueNumeric,
			Date valueDate, Concept valueCoded, String valueText,
			Boolean valueBoolean) {
		this.concept = concept;
		if (concept != null) {
			ConceptDatatype conceptDatatype = concept.getDatatype();
			if (conceptDatatype == null) {
				concept = Context.getConceptService().getConcept(
						concept.getConceptId());
				conceptDatatype = concept.getDatatype();
			}
			if (conceptDatatype.isCoded())
				datatype = CODED;
			else if (conceptDatatype.isNumeric())
				datatype = NUMERIC;
			else if (conceptDatatype.isDate())
				datatype = DATE;
			else if (conceptDatatype.isText())
				datatype = TEXT;
			else if (conceptDatatype.isBoolean())
				datatype = BOOLEAN;
		} else {
			datatype = TEXT; // TODO: need to allow datatype to be set
			// manually
		}
		this.datetime = timeStamp;
		this.valueNumeric = valueNumeric;
		this.valueDate = valueDate;
		this.valueCoded = valueCoded;
		this.valueText = valueText;
		this.valueBoolean = valueBoolean;
	}

	public void add(Result result) {
		if (valueList == null) {
			valueList = new Vector<Result>();
			valueList.add(new Result(this));
		}
		valueList.add(result);
	}

	public boolean remove(Result result) {
		if (valueList != null)
			return valueList.remove(result);
		return false;
	}

	public String toString() {
		if (valueList != null) {
			StringBuffer s = new StringBuffer();
			for (Result r : valueList) {
				if (s.length() > 0)
					s.append(",");
				s.append(r.toString());
			}
			return s.toString();
		}
		switch (datatype) {
		case BOOLEAN:
			return (valueBoolean == null ? "false" : valueBoolean.toString());
		case NUMERIC:
			return (valueNumeric == null ? "" : String.valueOf(valueNumeric));
		case CODED:
			return (valueCoded == null ? "" : valueCoded.getName(
					Context.getLocale()).getName());
		case DATE:
			return (valueDate == null ? "" : DateFormat.getDateInstance(
					DateFormat.LONG, Context.getLocale()).format(valueDate));
		}
		return (valueText == null ? "" : valueText);
	}

	public Boolean toBoolean() {
		if (valueList != null) {
			Boolean result = true;
			for (Result r : valueList) {
				if (!r.toBoolean()) {
					result = false;
					break;
				}
			}
			return result;
		}
		switch (datatype) {
		case BOOLEAN:
			return (valueBoolean == null ? false : valueBoolean);
		case NUMERIC:
			return valueNumeric != 0;
		case CODED:
			return getConceptTrue().equals(valueCoded);
		case DATE:
			return valueDate != null;
		}
		return false;
	}

	private static Concept CONCEPT_TRUE = null;

	private Concept getConceptTrue() {
		if (CONCEPT_TRUE == null) {
			String globalConceptTrue = Context.getAdministrationService()
					.getGlobalProperty("concept.true");
			CONCEPT_TRUE = (globalConceptTrue == null ? new Concept() : Context
					.getConceptService().getConcept(
							Integer.valueOf(globalConceptTrue)));
		}
		return CONCEPT_TRUE;
	}

	public Double toNumber() {
		if (valueList != null) {
			if (valueList.size() < 1)
				return 0D;
			Double sum = 0D;
			for (Result r : valueList)
				sum += r.toNumber();
			return sum / valueList.size();
		}
		if (valueNumeric != null)
			return valueNumeric;
		switch (datatype) {
		case NUMERIC:
			return 0D;
		case CODED:
			return 0D;
		case BOOLEAN:
			if (valueBoolean != null && valueBoolean)
				return 1D;
			else
				return 0D;
		case DATE:
			if (valueDate != null)
				return Double.valueOf(valueDate.getTime());
			else
				return 0D;
		case TEXT:
			if (valueText != null)
				return Double.valueOf(valueText);
		}
		return 0D;
	}

	public Date toDate() {
		if (valueList != null) {
			return null;
		}
		if (valueDate != null)
			return valueDate;
		return null;
	}

	public int size() {
		if (valueList != null)
			return valueList.size();
		return (isNull() ? 0 : 1);
	}

	public boolean exists() {
		if (valueList != null) {
			if (valueList.size() < 1)
				return false;
			for (Result r : valueList) {
				if (r.exists())
					return true;
			}
			return false;
		}
		return !isNull();
	}

	public boolean isNull() {
		return (this == NULL_RESULT || (valueNumeric == null
				&& valueDate == null && valueCoded == null && valueText == null && valueBoolean == null));
	}

	public boolean contains(Concept concept) {
		return containsConcept(concept.getConceptId());
	}
	
	public boolean containsConcept(Integer conceptId) {
		if (valueList != null) {
			for (Result r : valueList) {
				if (r.containsConcept(conceptId))
					return true;
			}
			return false;
		}
		if (valueCoded == null)
			return false;
		return (valueCoded.getConceptId().equals(conceptId));
	}

	public boolean contains(Boolean valueBoolean) {
		if (valueList != null) {
			for (Result r : valueList) {
				if (r.contains(valueBoolean))
					return true;
			}
			return false;
		}
		if (this.valueBoolean == null)
			return false;
		return (this.valueBoolean.equals(valueBoolean));
	}

	public int indexOf(Result r) {
		if (valueList != null) {
			for (int i = 0; i < valueList.size(); i++)
				if (r == null ? valueList.get(i) == null : r.equals(valueList
						.get(i)))
					return i;
		}
		if (equals(r))
			return 0;
		else
			return -1;
	}

	public Result unique() {
		if (valueList == null)
			return this;
		Integer dummy = new Integer(1);
		HashMap<Result, Integer> map = new HashMap<Result, Integer>();
		List<Result> uniqueList = new Vector<Result>();
		for (Result r : valueList) {
			if (!map.containsKey(r)) {
				map.put(r, dummy);
				uniqueList.add(r);
			}
		}
		Result result = new Result(NULL_RESULT);
		result.valueList = uniqueList;
		return result;
	}
	
	public int hashCode() {
		int hashCode = 49867; // some random number
		switch (datatype) {
		case NUMERIC:
			return (valueNumeric == null ? hashCode+datatype : hashCode+valueNumeric.hashCode());
		case DATE:
			return (valueDate == null ? hashCode+datatype : hashCode+valueDate.hashCode());
		case CODED:
			return (valueCoded == null ? hashCode+datatype : hashCode+valueCoded.hashCode());
		case TEXT:
			return (valueText == null ? hashCode+datatype : hashCode+valueText.hashCode());
		case BOOLEAN:
			return (valueBoolean == null ? hashCode+datatype : hashCode+valueBoolean.hashCode());
		}
		return hashCode;
	}

	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Result))
			return false;
		Result r = (Result) obj;
		if (valueList != null) {
			if (r.valueList == null)
				return false;
			if (valueList.size() != r.valueList.size())
				return false;
			for (int i = 0; i < valueList.size(); i++) {
				if (!valueList.get(i).equals(r.valueList.get(i)))
					return false;
			}
			return true;
		}
		switch (datatype) {
		case NUMERIC:
			if (valueNumeric == null)
				return (r.valueNumeric == null);
			return (valueNumeric.equals(r.valueNumeric));
		case DATE:
			if (valueDate == null)
				return (r.valueDate == null);
			return (valueDate.equals(r.valueDate));
		case CODED:
			if (valueCoded == null)
				return (r.valueCoded == null);
			return (valueCoded.equals(r.valueCoded));
		case TEXT:
			if (valueText == null)
				return (r.valueText == null);
			return (valueText.equals(r.valueText));
		case BOOLEAN:
			if (valueBoolean == null)
				return (r.valueBoolean == null);
			return (valueBoolean.equals(r.valueBoolean));
		}
		return this == r;
	}

	public Result get(int index) {
		if (valueList != null) {
			if (index < valueList.size())
				return valueList.get(index);
			else
				return NULL_RESULT;
		}
		if (valueList == null && index == 0)
			return this;
		return NULL_RESULT;
	}

	public List<Result> getResultList() {
		if (valueList != null)
			return valueList;
		Vector<Result> list = new Vector<Result>();
		if (!isNull())
			list.add(this);
		return list;
	}

	public Date getDate() {
		if (valueList != null && valueList.size() > 0) {
			return valueList.get(0).getDate();
		}
		return datetime;
	}
	
	public Concept getConcept() {
		if (valueList != null && valueList.size() > 0)
			return valueList.get(0).getConcept();
		return valueCoded;
	}
	
	public void setValueNumeric(Integer valueNumeric) {
		this.valueNumeric = new Double(valueNumeric);
	}
	
	public void setValueNumeric(Double valueNumeric) {
		this.valueNumeric = valueNumeric;
	}
	
	public void setValueText(String valueText) {
		this.valueText = valueText;
	}
	
	public void debug() {
		debug(0);
	}
	
	private static final String[] datatypeNames = new String[] {"NULL", "NUMERIC", "DATE", "CODED", "TEXT", "BOOLEAN"};
	public void debug(int level) {
		String indent = "";
		for (int i=0; i < level; i++)
			indent += " ";
		if (valueList != null) {
			System.out.println(indent + "*** RESULT LIST ***");
			for (Result r : valueList)
				r.debug(level+2);
		}
		if (isNull()) {
			System.out.println(indent + "*** NULL RESULT ***");
			return;
		}
		System.out.println(indent + "*** SINGLE RESULT ***");
		System.out.println(indent + "  concept = " + (concept == null ? "null" : concept.getName(Context.getLocale()) + " (" + concept.getConceptId() + ")"));
		System.out.println(indent + "  datetime = " + datetime);
		System.out.println(indent + "  datatype = " + datatypeNames[datatype]);
		System.out.println(indent + "  valueNumeric = " + valueNumeric);
		System.out.println(indent + "  valueDate = " + valueDate);
		System.out.println(indent + "  valueCoded = " + (valueCoded == null ? "null" : valueCoded.getName(Context.getLocale()) + " (" + valueCoded.getConceptId() + ")"));
		System.out.println(indent + "  valueText = " + valueText);
		System.out.println(indent + "  valueBoolean = " + valueBoolean);
	}
}