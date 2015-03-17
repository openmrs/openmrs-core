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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.openmrs.Patient;

/*
 *  This class represents the complete mlm sections
 */

public class MLMObject {
	
	public static int NOLIST = 0;
	
	public static int LIST = 1;
	
	//metadata
	private String className;
	
	private String title;
	
	private String author;
	
	private String institution;
	
	private String data;
	
	private String logic;
	
	private String action;
	
	private String purpose;
	
	private String explanation;
	
	private String keywords;
	
	private String citations;
	
	private String links;
	
	private String date;
	
	private String specialist;
	
	private Integer priority;
	
	private Double version;
	
	private String type;
	
	private Integer ageMax;
	
	private Integer ageMin;
	
	private String ageMaxUnits;
	
	private String ageMinUnits;
	
	private HashMap<String, MLMObjectElement> conceptMap; //maps logic variables to query that assigned them
	
	private HashMap<String, LinkedList<MLMEvaluateElement>> evaluateList; //tokens to evaluate
	
	private ArrayList<Action> actions; //print action strings
	
	private ArrayList<LogicAssignment> logicAssignments = null;
	
	private LinkedHashMap<String, LinkedHashMap<String, Comparison>> comparisons = null;
	
	private ArrayList<Conclude> concludes = null;
	
	private ArrayList<MLMObjectElement> objElements = null;
	
	private HashMap<String, ArrayList<Call>> calls = null;
	
	static int keyId = 1;
	
	static int compKeyId = 1;
	
	static boolean compKeyIdUsed = false;
	
	// default constructor
	public MLMObject() {
		conceptMap = new HashMap<String, MLMObjectElement>();
		actions = new ArrayList<Action>();
		evaluateList = new HashMap<String, LinkedList<MLMEvaluateElement>>();
		logicAssignments = new ArrayList<LogicAssignment>();
		comparisons = new LinkedHashMap<String, LinkedHashMap<String, Comparison>>();
		concludes = new ArrayList<Conclude>();
		objElements = new ArrayList<MLMObjectElement>();
		calls = new HashMap<String, ArrayList<Call>>();
	}
	
	public MLMObject(Locale l, Patient p) {
		this();
	}
	
	public void addLogicAssignment(String variableName, String variableValue) {
		LogicAssignment logicAssignment = new LogicAssignment(variableName, variableValue);
		this.logicAssignments.add(logicAssignment);
	}
	
	public void AddConcept(String s) {
		this.objElements.get(this.objElements.size() - 1).setConceptName(s);
	}
	
	public void SetConceptVar(String s) {
		MLMObjectElement objElement = new MLMObjectElement();
		
		if (!conceptMap.containsKey(s)) {
			
			conceptMap.put(s, objElement);
		}
		
		this.objElements.add(objElement);
	}
	
	public void setReadType(String readType) {
		this.objElements.get(this.objElements.size() - 1).setReadType(readType);
	}
	
	public void setHowMany(String s) {
		int howMany = Integer.valueOf(s).intValue();
		this.objElements.get(this.objElements.size() - 1).setHowMany(howMany);
	}
	
	public void PrintEvaluateList(String section) {
		System.out.println("\n Evaluate order list is  - ");
		LinkedList<MLMEvaluateElement> evalListBySection = evaluateList.get(section);
		if (evalListBySection == null) {
			evalListBySection = new LinkedList<MLMEvaluateElement>();
			this.evaluateList.put(section, evalListBySection);
		}
		ListIterator<MLMEvaluateElement> thisList = evalListBySection.listIterator(0);
		while (thisList.hasNext()) {
			thisList.next().printThisList();
		}
	}
	
	public void WriteAction(Writer w) throws Exception {
		try {
			w.append("\tpublic ArrayList<String> initAction() {\n");
			w.append("\t\tArrayList<String> actions = new ArrayList<String>();\n");
			
			int pos = 1;
			for (Action action : this.actions) {
				w.append("\t\tactions.add(\"" + action.getActionString());
				if (action.getAtVar() != null) {
					w.append("@" + action.getAtVar());
				}
				w.append("\");\n");
				pos++;
			}
			
			w.append("\n\n\t\treturn actions;\n");
			w.append("\t}\n\n"); // End of this function
			w.flush();
			
			w
			        .append("\tprivate String substituteString(String variable,String outStr, HashMap<String, String> userVarMap, HashMap<String, Result> resultLookup){\n");
			w.append("\t\t//see if the variable is in the user map\n");
			w.append("\t\tString value = userVarMap.get(variable);\n");
			w.append("\t\tif (value != null)\n");
			w.append("\t\t{\n");
			w.append("\t\t}\n");
			w.append("\t\t// It must be a result value or date\n");
			w.append("\t\telse if (variable.contains(\"_value\"))\n");
			w.append("\t\t{\n");
			w.append("\t\t\tvariable = variable.replace(\"_value\",\"\").trim();\n");
			w.append("\t\t\tif(resultLookup.get(variable) != null){\n");
			w.append("\t\t\t\tvalue = resultLookup.get(variable).toString();\n");
			w.append("\t\t\t}\n");
			w.append("\t\t}\n");
			w.append("\t\t// It must be a result date\n");
			w.append("\t\telse if (variable.contains(\"_date\"))\n");
			w.append("\t\t{\n");
			w.append("\t\t\tString pattern = \"MM/dd/yy\";\n");
			w.append("\t\t\tSimpleDateFormat dateForm = new SimpleDateFormat(pattern);\n");
			w.append("\t\t\tvariable = variable.replace(\"_date\",\"\").trim();\n");
			w.append("\t\t\tif(resultLookup.get(variable) != null){\n");
			w.append("\t\t\t\tvalue = dateForm.format(resultLookup.get(variable).getResultDate());\n");
			w.append("\t\t\t}\n");
			w.append("\t\t}\n");
			w.append("\t\telse\n");
			w.append("\t\t{\n");
			w.append("\t\t\tif(resultLookup.get(variable) != null){\n");
			w.append("\t\t\t\tvalue = resultLookup.get(variable).toString();\n");
			w.append("\t\t\t}\n");
			w.append("\t\t}\n");
			
			w.append("\t\tif (value != null)\n");
			w.append("\t\t{\n");
			w.append("\t\t\toutStr += value;\n");
			w.append("\t\t}\n");
			w.append("\t\treturn outStr;\n");
			w.append("\t}\n");
			
			w
			        .append("\tpublic String doAction(String inStr, HashMap<String, String> userVarMap, HashMap<String, Result> resultLookup)\n");
			w.append("\t{\n");
			w.append("\t\tint startindex = -1;\n");
			w.append("\t\tint endindex = -1;\n");
			w.append("\t\tint index = -1;\n");
			w.append("\t\tString outStr = \"\";\n");
			
			w.append("\t\twhile((index = inStr.indexOf(\"||\"))>-1)\n");
			w.append("\t\t{\n");
			w.append("\t\t\tif(startindex == -1){\n");
			w.append("\t\t\t\tstartindex = 0;\n");
			w.append("\t\t\t\toutStr+=inStr.substring(0,index);\n");
			w.append("\t\t\t}else if(endindex == -1){\n");
			w.append("\t\t\t\tendindex = index-1;\n");
			w.append("\t\t\t\tString variable = inStr.substring(startindex, endindex).trim();\n");
			w.append("\t\t\t\toutStr = substituteString(variable,outStr,userVarMap,resultLookup);\n");
			
			w.append("\t\t\t\tstartindex = -1;\n");
			w.append("\t\t\t\tendindex = -1;\n");
			w.append("\t\t\t}\n");
			w.append("\t\t\tinStr = inStr.substring(index+2);\n");
			w.append("\t\t}\n");
			w.append("\t\toutStr+=inStr;\n");
			w.append("\t\treturn outStr;\n");
			w.append("\t}\n");
			
			w.flush();
		}
		catch (Exception e) {
			System.err.println("Write Action: " + e);
			e.printStackTrace(); // so we can get stack trace
		}
	}
	
	public boolean WriteEvaluate(Writer w, String classname) throws Exception {
		boolean retValEval = true, retVal = true;
		try {
			String key;
			ListIterator<MLMEvaluateElement> thisList;
			LinkedList<MLMEvaluateElement> evalListBySection = evaluateList.get("data");
			if (evalListBySection == null) {
				evalListBySection = new LinkedList<MLMEvaluateElement>();
				this.evaluateList.put("data", evalListBySection);
			}
			thisList = evalListBySection.listIterator(0);
			
			if (retValEval == false) {
				return false;
			}
			
			w.append("\n\tpublic Result eval(LogicContext context, Integer patientId,\n"
			        + "\t\t\tMap<String, Object> parameters) throws LogicException {\n\n");
			w.append("\t\tString actionStr = \"\";\n");
			w.append("\t\tPatientService patientService = Context.getPatientService();\n");
			w.append("\t\tPatient patient = patientService.getPatient(patientId);\n");
			w.append("\t\tHashMap<String, Result> resultLookup = new HashMap <String, Result>();\n");
			w.append("\t\tBoolean ageOK = null;\n\n\t\ttry {\n");
			
			w.append("\t\t\tRuleProvider ruleProvider = (RuleProvider)parameters.get(\"ruleProvider\");\n");
			
			w.append("\t\t\tHashMap<String, String> userVarMap = new HashMap <String, String>();\n");
			w.append("\t\t\tString firstname = patient.getPersonName().getGivenName();\n");
			w.append("\t\t\tuserVarMap.put(\"firstname\", toProperCase(firstname));\n");
			w.append("\t\t\tString lastName = patient.getFamilyName();\n");
			w.append("\t\t\tuserVarMap.put(\"lastName\", lastName);\n");
			w.append("\t\t\tString gender = patient.getGender();\n");
			w.append("\t\t\tuserVarMap.put(\"Gender\", gender);\n");
			w.append("\t\t\tif(gender.equalsIgnoreCase(\"M\")){\n");
			w.append("\t\t\t\tuserVarMap.put(\"gender\",\"his\");\n");
			w.append("\t\t\t\tuserVarMap.put(\"hisher\",\"his\");\n");
			w.append("\t\t\t}else{\n");
			w.append("\t\t\t\tuserVarMap.put(\"gender\",\"her\");\n");
			w.append("\t\t\t\tuserVarMap.put(\"hisher\",\"her\");\n");
			w.append("\t\t\t}\n");
			
			w.append("\t\t\tArrayList<String> actions = initAction();\n");
			/***************************************************************************************
			 * Do the LogicCriteria here
			 */
			
			if (this.calls.get("data") != null) {
				for (Call currCall : this.calls.get("data")) {
					currCall.write(w);
				}
			}
			Iterator<Map.Entry<String, Comparison>> comparisonIterator = null;
			if (this.comparisons.get("data") != null) {
				comparisonIterator = this.comparisons.get("data").entrySet().iterator();
			}
			while (thisList.hasNext()) {
				
				WriteData(thisList.next(), w, comparisonIterator);
				w.flush();
				
			}
			//get all the distinct keys
			Set<String> uniqueKeys = this.conceptMap.keySet();
			uniqueKeys.remove("Gender");
			
			/** ******************************************************************************************** */
			
			w
			        .append("\n\n\t\t\tif(evaluate_logic(parameters, context, ruleProvider, patient, userVarMap, resultLookup)){\n");
			w.append("\t\t\t\tResult ruleResult = new Result();\n");
			
			/*******************
			 * Code for implementing If() then in Action
			 *
			 */
			for (String uniqueKey : uniqueKeys) {
				if (uniqueKey.startsWith("Box") || uniqueKey.startsWith("mode")) // Needs improvement - for now we allow if(variable not like Box1,...
				{	

				} else {
					w.append("\t\tResult " + uniqueKey + " = (Result) resultLookup.get(\"" + uniqueKey + "\");\n");
				}
			}
			w.append("\n");
			
			Iterator<Map.Entry<String, Comparison>> comparisonIteratorAction = null;
			if (this.comparisons.get("action") != null) {
				comparisonIteratorAction = this.comparisons.get("action").entrySet().iterator();
			}
			evalListBySection = evaluateList.get("action");
			if (evalListBySection == null) {
				evalListBySection = new LinkedList<MLMEvaluateElement>();
				this.evaluateList.put("action", evalListBySection);
			}
			thisList = evalListBySection.listIterator(0);
			ArrayList<Call> callBySectionAction = this.calls.get("action");
			Iterator<Call> callIteratorAction = null;
			
			if (callBySectionAction != null) {
				callIteratorAction = callBySectionAction.iterator();
				w.append("\t\t\t\tObject value = null;\n");
				w.append("\t\t\t\tString variable = null;\n");
				w.append("\t\t\t\tint varLen = 0;\n");
			}
			
			while (thisList.hasNext()) {
				
				WriteAction(thisList.next(), w, comparisonIteratorAction, callIteratorAction);
				w.append("\n");
				w.flush();
				
			}
			
			/*****
			 *
			 * End of code for implementing If() then in Action
			 */
			
			/*********** DO NOT NEED THIS because the above code does it
			 if (this.calls.get("action") != null) {
			 w.append("\t\t\t\tString value = null;\n");
			 w.append("\t\t\t\tString variable = null;\n");
			 w.append("\t\t\t\tint varLen = 0;\n");
			 for (Call currCall : this.calls.get("action")) {
			 currCall.write(w);
			 }
			 }
			 **************/
			
			w.append("\t\t\t\tfor(String currAction:actions){\n");
			w.append("\t\t\t\t\tcurrAction = doAction(currAction, userVarMap, resultLookup);\n");
			w.append("\t\t\t\t\truleResult.add(new Result(currAction));\n");
			w.append("\t\t\t\t}\n");
			
			w.append("\t\t\t\treturn ruleResult;\n");
			w.append("\t\t\t}\n");
			
			w.append("\t\t} catch (Exception e) {\n");
			w.append("\t\t\tlog.error(e.getMessage(),e);\n");
			w.append("\t\t\treturn Result.emptyResult();");
			w.append("\n\t\t}\n\t\treturn Result.emptyResult();\n\t}\n\n");
			
			/**
			 * *********************************************************Added to write List forming
			 * private methods **********************
			 */
			LinkedHashMap<String, Comparison> compListBySection = comparisons.get("logic");
			Iterator<Map.Entry<String, Comparison>> comparisonIteratorLogic = null;
			if (compListBySection != null) {
				comparisonIteratorLogic = compListBySection.entrySet().iterator();
				
				while (comparisonIteratorLogic.hasNext()) {
					Comparison comparison = comparisonIteratorLogic.next().getValue();
					comparison.writeComparisonList(w); // write a list helper method only if the operator is IN
				}
			}
			/** *************************************************************************************************************************** */
			
			w
			        .append("\tprivate boolean evaluate_logic(Map<String, Object> parameters, LogicContext context, RuleProvider ruleProvider, Patient patient, HashMap<String, String> userVarMap, HashMap<String, Result> resultLookup) throws LogicException {\n\n");
			evalListBySection = evaluateList.get("logic");
			if (evalListBySection == null) {
				evalListBySection = new LinkedList<MLMEvaluateElement>();
				this.evaluateList.put("logic", evalListBySection);
			}
			thisList = evalListBySection.listIterator(0); // Start the Big
			
			w.append("\t\tResult Gender = new Result(userVarMap.get(\"Gender\"));\n");
			for (String uniqueKey : uniqueKeys) {
				w.append("\t\tResult " + uniqueKey + " = (Result) resultLookup.get(\"" + uniqueKey + "\");\n");
			}
			w.append("\n");
			// Evaluate()
			boolean skipReturn = false;
			Iterator<LogicAssignment> logicIterator = this.logicAssignments.iterator();
			
			if (compListBySection == null) {
				compListBySection = new LinkedHashMap<String, Comparison>();
				this.comparisons.put("logic", compListBySection);
			}
			
			ArrayList<Call> callBySection = this.calls.get("logic");
			Iterator<Call> callIterator = null;
			
			if (callBySection != null) {
				callIterator = callBySection.iterator();
				w.append("\t\tObject value = null;\n");
				w.append("\t\t\t\tString variable = null;\n");
				w.append("\t\t\t\tint varLen = 0;\n");
			}
			
			Iterator<Conclude> concludeIterator = this.concludes.iterator();
			comparisonIteratorLogic = compListBySection.entrySet().iterator();
			
			while (thisList.hasNext()) {
				skipReturn = WriteLogic(thisList.next(), w, logicIterator, comparisonIteratorLogic, concludeIterator,
				    callIterator);
				w.flush();
				
			}
			if (!skipReturn) {
				w.append("\t\treturn false;\n");
			}
			
			w.append("\t}");
			w.append("\n\n");
		}
		catch (Exception e) {
			System.err.println("Write Evaluate: " + e);
			e.printStackTrace(); // so we can get stack trace
		}
		return retValEval;
	}
	
	private void WriteData(MLMEvaluateElement el, Writer w,
	//		Comparison comparison
	        Iterator<Map.Entry<String, Comparison>> comparisonIterator) {
		
		LinkedList<Integer> openParens = new LinkedList<Integer>();
		LinkedList<Integer> openBrackets = new LinkedList<Integer>();
		Integer openParen = 0;
		Integer openBracket = 0;
		
		try {
			
			String key = "";
			Iterator iter = el.iterator();
			
			while (iter.hasNext()) { // IF
				key = (String) iter.next();
				if (openParens.size() > 0) {
					openParen = openParens.getLast();
				}
				if (openBrackets.size() > 0) {
					openBracket = openBrackets.getLast();
					if (!(key.equalsIgnoreCase("ELSEIF") || key.startsWith("ELSE"))) {
						if (openBracket == 0) {
							openBrackets.removeLast();
							if (openBrackets.size() > 0) {
								openBracket = openBrackets.getLast();
							}
						}
					}
				}
				if (key.equalsIgnoreCase("IF")) {
					w.append("\t\tif(");
					openParen = 1;
					openParens.add(openParen);
				} else if (key.equalsIgnoreCase("ELSEIF")) {
					
					while (openBracket > 0) {
						w.append("}");
						openBracket--;
					}
					if (openBrackets.size() > 0) {
						openBrackets.removeLast();
					}
					w.append("\t\telse if(");
					openParen = 1;
					openParens.add(openParen);
				} else if (key.startsWith("ENDIF")) {
					while (openBracket > 0) {
						w.append("}");
						openBracket--;
					}
					if (openBrackets.size() > 0) {
						openBrackets.removeLast();
					}
				} else if (key.startsWith("ELSE")) {
					
					while (openBracket > 0) {
						w.append("}");
						openBracket--;
					}
					if (openBrackets.size() > 0) {
						openBrackets.removeLast();
					}
					openBracket = 1;
					openBrackets.add(openBracket);
					w.append("\t\telse{\n");
					
				} else if (key.equalsIgnoreCase("THEN")) {
					while (openParen > 0) {
						w.append(")");
						openParen--;
					}
					if (openParens.size() > 0) {
						openParens.removeLast();
					}
					w.append("{\n");
					openBracket = 1;
					openBrackets.add(openBracket);
				} else if (key.equalsIgnoreCase("AND")) {
					w.append("&&\n\t\t\t");
				} else if (key.equalsIgnoreCase("OR")) {
					w.append("||\n\t\t\t");
				} else if (key.equalsIgnoreCase("NOT")) {
					w.append("!");
				} else {
					
					MLMObjectElement objElement = this.conceptMap.get(key);
					if (openParen > 0) {
						if (comparisonIterator != null && comparisonIterator.hasNext()) {
							Comparison comparison = comparisonIterator.next().getValue();
							;
							if (comparison != null) {
								comparison.write(w, objElement, this.isVarCallorDataRead((String) comparison.getAnswer()));
							}
						}
					} else {
						if (!key.equalsIgnoreCase("gender")) {
							writeEvaluateConcept(key, w);
						}
					}
				}
			}
			while (openBrackets.size() > 0 && openBrackets.getLast() != null) {
				openBracket = openBrackets.removeLast();
				while (openBracket > 0) {
					w.append("}");
					openBracket--;
				}
			}
		}
		catch (Exception e) {
			System.err.println("Write Evaluate: " + e);
			e.printStackTrace(); // so we can get stack trace
		}
	}
	
	private boolean WriteLogic(MLMEvaluateElement el, Writer w, Iterator<LogicAssignment> logicIterator,
	        //Comparison comparison,
	        Iterator<Map.Entry<String, Comparison>> comparisonIterator, Iterator<Conclude> concludeIterator,
	        Iterator<Call> callIterator) {
		
		boolean skipReturn = false;
		LinkedList<Integer> openParens = new LinkedList<Integer>();
		LinkedList<Integer> openBrackets = new LinkedList<Integer>();
		Integer openParen = 0;
		Integer openBracket = 0;
		
		try {
			
			String key = "";
			Iterator iter = el.iterator();
			Comparison comparison;
			
			while (iter.hasNext()) { // IF
				key = (String) iter.next();
				
				if (openParens.size() > 0) {
					openParen = openParens.getLast();
				}
				if (openBrackets.size() > 0) {
					openBracket = openBrackets.getLast();
					if (!(key.equalsIgnoreCase("ELSEIF") || key.startsWith("ELSE"))) {
						if (openBracket == 0) {
							openBrackets.removeLast();
							if (openBrackets.size() > 0) {
								openBracket = openBrackets.getLast();
							}
						}
					}
				}
				
				if (key.equalsIgnoreCase("IF")) {
					w.append("\t\tif(");
					openParen = 1;
					openParens.add(openParen);
				} else if (key.equalsIgnoreCase("ELSEIF")) {
					
					while (openBracket > 0) {
						w.append("}");
						openBracket--;
					}
					if (openBrackets.size() > 0) {
						openBrackets.removeLast();
					}
					w.append("\t\telse if(");
					openParen = 1;
					openParens.add(openParen);
				} else if (key.startsWith("ENDIF")) {
					while (openBracket > 0) {
						w.append("}");
						openBracket--;
					}
					if (openBrackets.size() > 0) {
						openBrackets.removeLast();
					}
				} else if (key.startsWith("ELSE")) {
					
					while (openBracket > 0) {
						w.append("}");
						openBracket--;
					}
					if (openBrackets.size() > 0) {
						openBrackets.removeLast();
					}
					if (openBrackets.size() > 0) {
						openBracket = openBrackets.getLast();
						if (openBracket == 0) {
							skipReturn = true;
						}
					} else {
						skipReturn = true;
					}
					openBracket = 1;
					openBrackets.add(openBracket);
					w.append("\t\telse{\n");
					
				} else if (key.equalsIgnoreCase("THEN")) {
					while (openParen > 0) {
						w.append(")");
						openParen--;
					}
					if (openParens.size() > 0) {
						openParens.removeLast();
					}
					w.append("{\n");
					openBracket = 1;
					openBrackets.add(openBracket);
				} else if (key.equalsIgnoreCase("AND")) {
					w.append("&&\n\t\t\t");
				} else if (key.equalsIgnoreCase("OR")) {
					w.append("||\n\t\t\t");
				} else if (key.equalsIgnoreCase("NOT")) {
					w.append("!");
				} else if (key.equalsIgnoreCase("Logic_Assignment")) {
					if (logicIterator.hasNext()) {
						LogicAssignment logicAssignment = logicIterator.next();
						logicAssignment.write(w);
						
						//make sure to close the open bracket here
						//we do NOT want to conclude from a logic_assignment
						if (openBracket > 0) {
							w.append("\t\t}\n");
							if (openBrackets.size() > 0) {
								openBrackets.removeLast();
							}
							openBrackets.add(--openBracket);
						}
					}
				} else if (key.startsWith("Conclude")) {
					if (concludeIterator.hasNext()) {
						Conclude conclude = concludeIterator.next();
						conclude.write(w);
					}
					
					//if we conclude with no open brackets
					//then it is the final return of the
					//logic method
					if (openBracket == 0) {
						skipReturn = true;
					}
					
					if (openBracket > 0) {
						w.append("\t\t}\n");
						if (openBrackets.size() > 0) {
							openBrackets.removeLast();
						}
						openBrackets.add(--openBracket);
					}
					
				} else if (key.equalsIgnoreCase("Call")) {
					Call call = callIterator.next();
					call.write(w);
				} else {
					
					MLMObjectElement objElement = this.conceptMap.get(key);
					if (comparisonIterator.hasNext()) {
						comparison = comparisonIterator.next().getValue();
					} else {
						comparison = null;
					}
					if (comparison != null) {
						comparison.write(w, objElement, this.isVarCallorDataRead(comparison.getAnswer()));
					}
					
				}
			}
			
			while (openBrackets.size() > 0 && openBrackets.getLast() != null) {
				openBracket = openBrackets.removeLast();
				while (openBracket > 0) {
					w.append("}");
					openBracket--;
				}
			}
		}
		catch (Exception e) {
			System.err.println("Write Evaluate: " + e);
			e.printStackTrace(); // so we can get stack trace
		}
		
		return skipReturn;
	}
	
	private void WriteAction(MLMEvaluateElement el, Writer w,
	//		Comparison comparison
	        Iterator<Map.Entry<String, Comparison>> comparisonIterator, Iterator<Call> callIterator) {
		boolean skipReturn = false;
		LinkedList<Integer> openParens = new LinkedList<Integer>();
		LinkedList<Integer> openBrackets = new LinkedList<Integer>();
		Integer openParen = 0;
		Integer openBracket = 0;
		
		try {
			
			String key = "";
			Iterator iter = el.iterator();
			Comparison comparison;
			
			while (iter.hasNext()) { // IF
				key = (String) iter.next();
				
				if (openParens.size() > 0) {
					openParen = openParens.getLast();
				}
				if (openBrackets.size() > 0) {
					openBracket = openBrackets.getLast();
					if (!(key.equalsIgnoreCase("ELSEIF") || key.startsWith("ELSE"))) {
						if (openBracket == 0) {
							openBrackets.removeLast();
							if (openBrackets.size() > 0) {
								openBracket = openBrackets.getLast();
							}
						}
					}
				}
				
				if (key.equalsIgnoreCase("IF")) {
					w.append("\t\tif(");
					openParen = 1;
					openParens.add(openParen);
				} else if (key.equalsIgnoreCase("ELSEIF")) {
					
					while (openBracket > 0) {
						w.append("}");
						openBracket--;
					}
					if (openBrackets.size() > 0) {
						openBrackets.removeLast();
					}
					w.append("\t\telse if(");
					openParen = 1;
					openParens.add(openParen);
				} else if (key.startsWith("ENDIF")) {
					while (openBracket > 0) {
						w.append("}");
						openBracket--;
					}
					if (openBrackets.size() > 0) {
						openBrackets.removeLast();
					}
				} else if (key.startsWith("ELSE")) {
					
					while (openBracket > 0) {
						w.append("}");
						openBracket--;
					}
					if (openBrackets.size() > 0) {
						openBrackets.removeLast();
					}
					if (openBrackets.size() > 0) {
						openBracket = openBrackets.getLast();
						if (openBracket == 0) {
							skipReturn = true;
						}
					} else {
						skipReturn = true;
					}
					openBracket = 1;
					openBrackets.add(openBracket);
					w.append("\t\telse{\n");
					
				} else if (key.equalsIgnoreCase("THEN")) {
					while (openParen > 0) {
						w.append(")");
						openParen--;
					}
					if (openParens.size() > 0) {
						openParens.removeLast();
					}
					w.append("{\n");
					openBracket = 1;
					openBrackets.add(openBracket);
				} else if (key.equalsIgnoreCase("AND")) {
					w.append("&&\n\t\t\t");
				} else if (key.equalsIgnoreCase("OR")) {
					w.append("||\n\t\t\t");
				} else if (key.equalsIgnoreCase("NOT")) {
					w.append("!");
				} else if (key.equalsIgnoreCase("Call")) {
					Call call = callIterator.next();
					call.write(w);
				} else {
					
					MLMObjectElement objElement = this.conceptMap.get(key);
					if (comparisonIterator.hasNext()) {
						comparison = comparisonIterator.next().getValue();
					} else {
						comparison = null;
					}
					if (comparison != null) {
						comparison.write(w, objElement, this.isVarCallorDataRead(comparison.getAnswer()));
					}
					
				}
			}
			
			while (openBrackets.size() > 0 && openBrackets.getLast() != null) {
				openBracket = openBrackets.removeLast();
				while (openBracket > 0) {
					w.append("}");
					openBracket--;
				}
			}
		}
		catch (Exception e) {
			System.err.println("Write Evaluate: " + e);
			e.printStackTrace(); // so we can get stack trace
		}
		
	}
	
	public int GetSize() {
		return conceptMap.size();
	}
	
	public MLMObjectElement GetMLMObjectElement(String key) {
		if (conceptMap.containsKey(key)) {
			return conceptMap.get(key);
		} else {
			return null;
		}
		
	}
	
	public void InitEvaluateList(String section, String keyToAdd) {
		if (!evaluateList.isEmpty()) {
			LinkedList<MLMEvaluateElement> evalListBySection = evaluateList.get(section);
			MLMEvaluateElement mEvalElem = null;
			if (evalListBySection == null) {
				evalListBySection = new LinkedList<MLMEvaluateElement>();
				this.evaluateList.put(section, evalListBySection);
			} else {
				mEvalElem = evalListBySection.getLast();
			}
			if (mEvalElem != null && mEvalElem.getLast().equals("ELSEIF")) {
				// Nested if
				return;
			} else if (mEvalElem != null && mEvalElem.getLast().equals("ELSE")) {
				// Nested if
				return;
			} else {
				if (openIf(mEvalElem)) {
					return;
				} else {
					MLMEvaluateElement mEvalElemNew = new MLMEvaluateElement();
					evalListBySection.add(mEvalElemNew);
				}
			}
		} else {
			MLMEvaluateElement mEvalElemNew = new MLMEvaluateElement();
			LinkedList<MLMEvaluateElement> evalListBySection = new LinkedList<MLMEvaluateElement>();
			this.evaluateList.put(section, evalListBySection);
			evalListBySection.add(mEvalElemNew);
		}
		
	}
	
	private boolean openIf(MLMEvaluateElement mEvalElem) {
		
		if (mEvalElem == null) {
			return false;
		}
		Iterator iter = mEvalElem.iterator();
		int numOpen = 0;
		int numClosed = 0;
		
		while (iter.hasNext()) {
			String currKey = (String) iter.next();
			if (currKey.equalsIgnoreCase("If") || currKey.startsWith("Else")) {
				numOpen++;
			}
			
			if (currKey.startsWith("Conclude") || currKey.equalsIgnoreCase("endif")
			        || currKey.equalsIgnoreCase("logic_assignment")) {
				numClosed++;
			}
		}
		
		if (numOpen > numClosed) {
			return true;
		}
		return false;
	}
	
	public boolean writeEvaluateConcept(String key, Writer w) throws Exception {
		boolean retVal = true;
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if (mObjElem != null) {
			retVal = mObjElem.writeEvaluate(key, w);
			w.flush();
		}
		return retVal;
	}
	
	public void AddToEvaluateList(String section, String key) {
		LinkedList<MLMEvaluateElement> evalListBySection = evaluateList.get(section);
		if (evalListBySection == null) {
			evalListBySection = new LinkedList<MLMEvaluateElement>();
			this.evaluateList.put(section, evalListBySection);
		}
		MLMEvaluateElement mEvalElem = evalListBySection.getLast();
		if (mEvalElem != null) {
			mEvalElem.add(key);
		}
	}
	
	public void setWhere(String type, String key) {
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if (mObjElem != null) {
			mObjElem.setWhere(type);
		}
	}
	
	public void setDuration(String type, String val, String op, String key) {
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if (mObjElem != null) {
			mObjElem.setDuration(type, val, op);
		}
	}
	
	public void setClassName(String name) {
		if (name.endsWith(".mlm")) {
			className = name.substring(0, name.indexOf(".mlm"));
		} else {
			className = name.trim();
		}
	}
	
	public String getClassName() {
		return className.trim();
	}
	
	public void setTitle(String s) {
		title = s.trim();
		
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setAuthor(String s) {
		author = s.trim();
	}
	
	public String getAuthor() {
		return author;
	}
	
	public void setInstitution(String s) {
		institution = s.trim();
		
	}
	
	public String getInstitution() {
		return institution;
	}
	
	public void setPriority(String s) {
		priority = Integer.parseInt(s.trim());
	}
	
	public Integer getPriority() {
		return priority;
	}
	
	public void setPurpose(String s) {
		purpose = s.trim();
	}
	
	public String getPurpose() {
		return purpose;
	}
	
	public void setExplanation(String s) {
		explanation = s.trim();
	}
	
	public String getExplanation() {
		return explanation;
	}
	
	public void setKeywords(String s) {
		keywords = s.trim();
	}
	
	public String getKeywords() {
		return keywords;
	}
	
	public void setSpecialist(String s) {
		specialist = s.trim();
	}
	
	public String getSpecialist() {
		return specialist;
	}
	
	public void setLinks(String s) {
		links = s.trim();
	}
	
	public String getLinks() {
		return links;
	}
	
	public void setCitations(String s) {
		citations = s.trim();
	}
	
	public String getCitations() {
		return citations;
	}
	
	public void setAction(String s) {
		action = s.trim();
	}
	
	public String getAction() {
		return action;
	}
	
	public void setDate(String s) {
		date = s.trim();
	}
	
	public String getDate() {
		return date;
	}
	
	public void setData(String s) {
		data = s.trim();
	}
	
	public String getData() {
		return data;
	}
	
	public void setLogic(String s) {
		logic = s.trim();
	}
	
	public String getLogic() {
		return logic;
	}
	
	public void setVersion(String s) {
		version = Double.valueOf(s.trim());
	}
	
	public Double getVersion() {
		return version;
	}
	
	public void setType(String s) {
		type = s.trim();
	}
	
	public String getType() {
		return type;
	}
	
	public void addAction(String actionString) {
		this.actions.add(new Action(actionString.trim()));
	}
	
	public void addCall(String section, String var, String method) {
		ArrayList<Call> callsBySection = this.calls.get(section);
		if (callsBySection == null) {
			callsBySection = new ArrayList<Call>();
			this.calls.put(section, callsBySection);
		}
		callsBySection.add(new Call(var, method));
	}
	
	public void addParameter(String section, String parameter) {
		ArrayList<Call> callBySection = this.calls.get(section);
		
		if (callBySection == null) {
			return;
		}
		
		Call lastCall = callBySection.get(callBySection.size() - 1);
		lastCall.addParameter(parameter);
	}
	
	public static void setCompKeyIdUsed(boolean compKeyIdUsed) {
		MLMObject.compKeyIdUsed = compKeyIdUsed;
	}
	
	public static void setKeyId(int keyId) {
		MLMObject.keyId = keyId;
	}
	
	public static void setCompKeyId(int compKeyId) {
		MLMObject.compKeyId = compKeyId;
	}
	
	public void addCompOperator(String section, Integer operator, String key) {
		
		LinkedHashMap<String, Comparison> compBySection = this.comparisons.get(section);
		if (compBySection == null) {
			compBySection = new LinkedHashMap<String, Comparison>();
			this.comparisons.put(section, compBySection);
		}
		Comparison thisComparison = compBySection.get(key);
		if (thisComparison != null) {
			if (operator != null && operator == org.openmrs.arden.ArdenBaseParserTokenTypes.IN) {
				thisComparison.setOperator(operator);
				// Always make new Comparison object example -  if(key = A) OR (key > 2)
				// but if key exists, modify it with __number for the hashmap only
			} else {
				compBySection.put(key + "__" + compKeyId, new Comparison(key, operator));
				setCompKeyIdUsed(true);
			}
		} else {
			compBySection.put(key, new Comparison(key, operator));
		}
	}
	
	public void SetAnswer(String section, Object answer, String key) {
		HashMap<String, Comparison> compBySection = this.comparisons.get(section);
		
		if (compBySection == null) {
			return;
		}
		
		//Comparison lastComparison = compBySection.get(compBySection.size()-1);
		Comparison lastComparison;
		if (compKeyIdUsed == true) {
			lastComparison = compBySection.get(key + "__" + compKeyId);
			lastComparison.setAnswer(answer);
			setCompKeyIdUsed(false);
			setCompKeyId(compKeyId + 1);//for next use
		} else {
			lastComparison = compBySection.get(key);
			lastComparison.setAnswer(answer);
		}
	}
	
	public String SetAnswerList(String section, Object answer, String key) {
		String retStr = "";
		
		LinkedHashMap<String, Comparison> compBySection = this.comparisons.get(section);
		
		if (compBySection == null) {
			compBySection = new LinkedHashMap<String, Comparison>();
			this.comparisons.put(section, compBySection);
		}
		
		//Comparison lastComparison = compBySection.get(compBySection.size()-1);
		if (key.compareTo("") == 0) {
			Comparison thisComparison = compBySection.get("__Temp__" + keyId);
			if (thisComparison == null) {
				// Create a temp key
				Comparison c = new Comparison("__Temp__" + keyId, null);
				c.addAnswerToList(answer);
				compBySection.put("__Temp__" + keyId, c);
				retStr = "__Temp__" + keyId;
			} else {
				// Temp key found, add answers to it as no key known yet
				thisComparison.addAnswerToList(answer);
				retStr = key;
			}
		} else {
			Comparison lastComparison = compBySection.get(key);
			lastComparison.setAnswer(answer);
			retStr = key;
		}
		return retStr;
	}
	
	public boolean SetAnswerListKey(String section, String key) {
		LinkedHashMap<String, Comparison> compBySection = this.comparisons.get(section);
		
		boolean retVal = false; // indicates if several comparisons for the same key
		
		if (compBySection == null) {
			compBySection = new LinkedHashMap<String, Comparison>();
			this.comparisons.put(section, compBySection);
			
			// This should be an error
		}
		
		if (key.compareTo("") != 0) {
			Comparison keyComparison = compBySection.get(key);
			
			// get Last temp key if any
			Comparison thisComparison = compBySection.get("__Temp__" + keyId);
			
			if (keyComparison != null) {
				// Already a key with ID, we need to move all the TempKey into this key
				if (thisComparison != null) {
					//	Iterator<Object> iterator = thisComparison.getAnswerList().iterator();
					//	while (iterator.hasNext()) {
					//		keyComparison.addAnswerToList(iterator.next());
					//	}
					retVal = true;
					thisComparison.setKey(key, key + "__" + keyId);
					compBySection.put(key + "__" + keyId, thisComparison);
					
					compBySection.remove("__Temp__" + keyId);
					if (keyId > 100) {
						// At most 100 Temp Keys
						setKeyId(1);
					} else {
						setKeyId(keyId + 1);
					}
				}
			} else if (thisComparison != null) {
				
				// Temp key found, add answers to it as no key known yet
				thisComparison.setKey(key, null);
				
				compBySection.put(key, thisComparison);
				compBySection.remove("__Temp__" + keyId);
				retVal = false;
				if (keyId > 100) {
					// At most 100 Temp Keys
					setKeyId(1);
				} else {
					setKeyId(keyId + 1);
				}
				
			}
		}
		return retVal;
	}
	
	public void addConcludeVal(boolean concludeVal) {
		this.concludes.add(new Conclude(concludeVal));
	}
	
	public void setAt(String atVar) {
		Action lastAction = this.actions.get(actions.size() - 1);
		lastAction.setAtVar(atVar);
	}
	
	public void setAgeMax(String ageMax) {
		
		int unitIndex = ageMax.indexOf("days");
		if (unitIndex < 0) {
			unitIndex = ageMax.indexOf("weeks");
		}
		if (unitIndex < 0) {
			unitIndex = ageMax.indexOf("months");
		}
		if (unitIndex < 0) {
			unitIndex = ageMax.indexOf("years");
		}
		
		if (unitIndex > 0) {
			this.ageMaxUnits = ageMax.substring(unitIndex, ageMax.length());
			this.ageMax = Integer.parseInt(ageMax.substring(0, unitIndex));
		} else {
			this.ageMax = Integer.parseInt(ageMax);
		}
	}
	
	public void setAgeMin(String ageMin) {
		
		int unitIndex = ageMin.indexOf("days");
		if (unitIndex < 0) {
			unitIndex = ageMin.indexOf("weeks");
		}
		if (unitIndex < 0) {
			unitIndex = ageMin.indexOf("months");
		}
		if (unitIndex < 0) {
			unitIndex = ageMin.indexOf("years");
		}
		
		if (unitIndex > 0) {
			this.ageMinUnits = ageMin.substring(unitIndex, ageMin.length());
			this.ageMin = Integer.parseInt(ageMin.substring(0, unitIndex));
		} else {
			this.ageMin = Integer.parseInt(ageMin);
		}
		
	}
	
	public Integer getAgeMax() {
		return ageMax;
	}
	
	public Integer getAgeMin() {
		return ageMin;
	}
	
	public String getAgeMaxUnits() {
		return ageMaxUnits;
	}
	
	public String getAgeMinUnits() {
		return ageMinUnits;
	}
	
	public boolean isVarCallorDataRead(Object varObj) {
		// find if the Variable passed in assigned to a data read or result of call to another rule. If so, the variable
		// is treated as result for translation in comparison object
		boolean retVal = false;
		Iterator<String> callSectionIterator = this.calls.keySet().iterator();
		
		if (!(varObj instanceof String)) {
			return retVal;
		}
		String var = (String) varObj;
		
		if (this.conceptMap.containsKey(var)) {
			retVal = true;
		} else if (callSectionIterator != null) {
			while (callSectionIterator.hasNext()) {
				ArrayList<Call> callBySection = this.calls.get(callSectionIterator.next());
				Iterator<Call> callIterator = null;
				
				if (callBySection != null) {
					callIterator = callBySection.iterator();
					if (callIterator != null) {
						while (callIterator.hasNext()) {
							Call c = callIterator.next();
							if (var.equalsIgnoreCase(c.getCallVar())) {
								retVal = true;
								break;
							}
						}
					}
					
				}
			}
		}
		
		return retVal;
	}
}
