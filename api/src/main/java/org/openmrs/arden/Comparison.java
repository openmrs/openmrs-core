/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.arden;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class translates comparisons within MLM if statements into java
 */
public class Comparison implements ArdenBaseTreeParserTokenTypes {
	
	private String key = null;
	
	private String keyList = null;
	
	private Integer operator = null;
	
	private Object answer = null;
	
	private ArrayList<Object> answerList = null;
	
	public Comparison(String key, Integer operator) {
		this.key = key;
		this.operator = operator;
	}
	
	public void setAnswer(Object answer) {
		this.answer = answer;
	}
	
	public Object getAnswer() {
		return answer;
	}
	
	public void setKey(String key, String keyList) {
		this.key = key;
		this.keyList = keyList;
	}
	
	public void setOperator(Integer operator) {
		this.operator = operator;
	}
	
	public void addAnswerToList(Object answer) {
		if (answerList == null) {
			answerList = new ArrayList<Object>();
		}
		this.answerList.add(answer);
	}
	
	public ArrayList<Object> getAnswerList() {
		return this.answerList;
	}
	
	public String getCompOpCode(MLMObjectElement objElement, boolean isAnswerVar) throws Exception {
		String retStr = "";
		
		if (objElement != null) {
			String readType = objElement.getReadType();
			if (readType != null && readType.equalsIgnoreCase("Exist")) {
				
				retStr += key + ".exists()";
				
				if (this.answer.toString().equalsIgnoreCase("false")) {
					return "!" + retStr;
				}
				return retStr;
			}
		}
		
		//there is no read value for this comparison so try
		//reading from the userVarMap
		if (this.answer instanceof Boolean && objElement == null) {
			retStr += "userVarMap.containsKey(\"" + key + "\")";
			
			if (this.answer.toString().equalsIgnoreCase("false")) {
				return "!" + retStr;
			}
			return retStr;
		}
		
		retStr += "(";
		if (operator != null) {
			
			if (this.answer != null || (answerList != null && !answerList.isEmpty())) {
				retStr += "!" + key + ".isNull()&&";
				if (isAnswerVar) {
					retStr += "!" + this.answer + ".isNull()&&";
				}
				
				switch (operator) {
					case IN:
						if (keyList != null) {
							retStr += "containsIgnoreCase(getResultList_" + keyList + "()," + key + ")";
						} else {
							retStr += "containsIgnoreCase(getResultList_" + key + "()," + key + ")";
						}
						break;
					case EQUALS:

						if (this.answer instanceof Integer || this.answer instanceof Double || this.answer instanceof Float) {
							retStr += "(" + key + ".toNumber()!= null&&" + key + ".toNumber() ==  " + this.answer + ")";
						} else {
							retStr += key + ".toString().equalsIgnoreCase(\"" + this.answer + "\")";
						}
						
						break;
					case GTE:
					case GT:
					case LTE:
					case LT:
						String comparator = null;
						switch (operator) {
							case GTE:
								comparator = ">=";
								break;
							case GT:
								comparator = ">";
								break;
							case LTE:
								comparator = "<=";
								break;
							case LT:
								comparator = "<";
								break;
							default:
								assert false; // execution should not reach this point
								break;
						}
						if (this.answer instanceof Integer || this.answer instanceof Double || this.answer instanceof Float) {
							retStr += "(" + key + ".toNumber()!= null&&" + key + ".toNumber() " + comparator + " "
							        + this.answer + ")";
						} else if (isAnswerVar) {
							retStr += "(" + key + ".toNumber()!= null&&" + this.answer + ".toNumber()!=null&&" + key
							        + ".toNumber() " + comparator + "  " + this.answer + ".toNumber())";
						}
						break;
					
					default:
						break;
				}
			} else {
				switch (operator) {
					case EQUALS:

						retStr += key + ".isNull()";
						
						break;
					
					default:
						break;
				}
			}
			
		}
		retStr += ")";
		return retStr;
	}
	
	public void write(Writer w, MLMObjectElement objElement, boolean isAnswerVar) {
		try {
			String comparisonString = getCompOpCode(objElement, isAnswerVar);
			if (comparisonString != null && comparisonString.length() > 0) {
				w.append(comparisonString);
			}
		}
		catch (Exception e) {

		}
	}
	
	public void writeComparisonList(Writer w) {
		try {
			StringBuilder retStr = new StringBuilder("");
			if (this.operator != null && this.operator == org.openmrs.arden.ArdenBaseParserTokenTypes.IN) {
				if (answerList == null) {
					return;
				}
				Iterator<Object> itr = answerList.iterator();
				Object answer = null;
				// The first one in the list
				if (itr.hasNext()) {
					if (keyList != null) {
						retStr.append("\n\tprivate Result getResultList_").append(this.keyList).append("(){");
					} else {
						retStr.append("\n\tprivate Result getResultList_").append(this.key).append("(){");
					}
					
					while (itr.hasNext()) {
						answer = itr.next();
						if (answer instanceof Integer || answer instanceof Double || answer instanceof Float) {
							retStr.append("\n\t\tResult aList = new Result();").append(
							    "\n\t\taList.add(new Result(" + answer + "));");
						} else {
							retStr.append("\n\t\tResult aList = new Result();").append(
							    "\n\t\tConcept concept = new Concept();").append(
							    "\n\t\tConceptName conceptName = new ConceptName(\"").append(answer).append(
							    "\", Context.getLocale());").append("concept.addName(conceptName);").append(
							    "\n\t\taList.add(new Result(concept));");
						}
					}
					
					retStr.append("\n\t\treturn aList;\n\t}\n");
				}
			}
			w.append(retStr);
			
		}
		catch (Exception e) {

		}
		
	}
}
