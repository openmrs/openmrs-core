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
package org.openmrs.arden;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * 
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
	
	public void setKey(String key, String keyList) {
		this.key = key;
		this.keyList = keyList;
	}

	public void setOperator(Integer operator) {
		this.operator = operator;
	}

	public void addAnswerToList(Object answer) {
		if(answerList == null) {
			answerList = new ArrayList<Object> ();
		}
		this.answerList.add(answer);
	}
	
	public ArrayList<Object> getAnswerList() {
		return this.answerList;
	}
	public String getCompOpCode(MLMObjectElement objElement) throws Exception {
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
		if(this.answer instanceof Boolean&&objElement==null){
			retStr += "userVarMap.containsKey(\""+key+"\")";
			
			if (this.answer.toString().equalsIgnoreCase("false")) {
				return "!" + retStr;
			}
			return retStr;
		}

		retStr += "(";
		if (operator != null) {

			if (this.answer != null || (answerList!=null&&!answerList.isEmpty())) {
				retStr += "!" + key + ".isNull()&&";

				switch (operator) {
				case IN:
					if(keyList != null){
						retStr += key + ".contains(getResultList_" + keyList + "())";
					}
					else
					{
						retStr += key + ".contains(getResultList_" + key + "())";
					}
					break;
				case EQUALS:

					if (this.answer instanceof Integer
					        || this.answer instanceof Double
					        || this.answer instanceof Float) {
						retStr += key + ".toNumber() ==  " + this.answer;
					} else {
						retStr += key + ".toString().equalsIgnoreCase(\"" + this.answer
						        + "\")";
					}

					break;
				case GTE:
					if (this.answer instanceof Integer
					        || this.answer instanceof Double
					        || this.answer instanceof Float) {
						retStr += key + ".toNumber() >= "+ this.answer;
					}
					break;
				case GT:
					if (this.answer instanceof Integer
					        || this.answer instanceof Double
					        || this.answer instanceof Float) {
						retStr += key + ".toNumber() > "+ this.answer;
					}
					break;
				case LT:
					if (this.answer instanceof Integer
					        || this.answer instanceof Double
					        || this.answer instanceof Float) {
						retStr += key + ".toNumber() < "+ this.answer;
					} 
					break;
				case LTE:
					if (this.answer instanceof Integer
					        || this.answer instanceof Double
					        || this.answer instanceof Float) {
						retStr += key + ".toNumber() <= "+ this.answer;
					}
					break;

				default:
					break;
				}
			} else {
				switch (operator) {
				case EQUALS:

					retStr+= key + ".isNull()";

					break;

				default:
					break;
				}
			}

		}
		retStr += ")";
		return retStr;
	}
	
	public void write(Writer w,MLMObjectElement objElement){
		try {
		    String comparisonString = getCompOpCode(objElement);
	        if (comparisonString != null
	                && comparisonString.length() > 0) {
	        	w.append(comparisonString);
	        }
        } catch (Exception e) {
	       
        }
	}
	
	public void writeComparisonList(Writer w) {
		try {
			String retStr = "";
			if(this.operator != null && this.operator == org.openmrs.arden.ArdenBaseParserTokenTypes.IN)
			{
				if(answerList == null){
					return;
				}
				Iterator<Object> itr = answerList.iterator();
				Object answer = null;
				// The first one in the list
				if(itr.hasNext())
				{
					if(keyList != null){
						retStr = "\n\tprivate Result getResultList_" + this.keyList + "(){";
					}
					else
					{
						retStr = "\n\tprivate Result getResultList_" + this.key + "(){";
					}
														
					while(itr.hasNext())
					{
					    answer = itr.next();
						if ( answer instanceof Integer
						        || answer instanceof Double
						        || answer instanceof Float) {
							retStr += "\n\t\tResult aList = new Result();";
							retStr += "\n\t\taList.put(new Result(" + answer + "));";
						}
						else {
							retStr += "\n\t\tConceptService conceptService = Context.getConceptService();";
							retStr += "\n\t\tResult aList = new Result(conceptService.getConcept(\"" + answer + "\"));";
						}
					}
					
					retStr += "\n\t\treturn aList;\n\t}\n";
				}
			}
			w.append(retStr);
			
		} catch (Exception e) {
			
		}
		
	}
}
