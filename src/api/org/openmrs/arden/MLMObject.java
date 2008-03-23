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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;

/*
 *  This class represents the complete mlm sections
 */

public class MLMObject {
	
	public static int NOLIST = 0;
	public static int LIST = 1;
	
	private HashMap<String, MLMObjectElement> conceptMap ;
	private String ConceptVar;   // These 3 variables are used when parsing. Cache to keep the concept name.
	private String readType;
	private int howMany;
	
	private boolean IsVarAdded;
	private int InNestedIf ;       // counting semaphore
	private boolean IsComplexIf;
	private LinkedList<MLMEvaluateElement> evaluateList;
	private HashMap<String, String> userVarMapFinal ;
	private String className;
	
	// default constructor
	public MLMObject(){
		conceptMap = new HashMap <String, MLMObjectElement>();
		IsVarAdded = false;
		InNestedIf = 0;
		IsComplexIf = false;
		userVarMapFinal = new HashMap <String, String>();
	}
	
	public MLMObject(Locale l, Patient p)
	{
		conceptMap = new HashMap <String, MLMObjectElement>();
		IsVarAdded = false;
		evaluateList = new LinkedList <MLMEvaluateElement>();
		userVarMapFinal = new HashMap <String, String>();
	}

	public void AddConcept(String s)
	{
		int index = 0, nindex = 0, endindex = 0, startindex = 0, index2=0, nindex2=0, endindex2 = 0, startindex2 = 0;
		String tempstr, variable, varVal;
		String inStr = ConceptVar;

		tempstr = inStr;

		index = tempstr.indexOf(",", nindex);
		index2 = s.indexOf(",", nindex);
		if(index != -1 && index2 != -1) {
			while(index > 0 && index2 > 0){
				if(nindex == 0 && nindex2 == 0){ // Are we starting now
					startindex = nindex;
					endindex = index;
					startindex2 = nindex2;
					endindex2 = index2;
					variable = tempstr.substring(startindex, endindex);
					varVal = s.substring(startindex2, endindex2);
					ConceptVar = variable;
					IsVarAdded = true; // so that the following statement completes
					CreateElement(varVal, readType, howMany);
				}
				else {
					startindex = nindex + 1;
					startindex2 = nindex2 + 1;
					endindex = index;
					endindex2 = index2;
					variable = tempstr.substring(startindex, endindex);
					varVal = s.substring(startindex2, endindex2);
					ConceptVar = variable;
					IsVarAdded = true; // so that the following statement completes
					CreateElement(varVal, readType, howMany);
				}
				nindex = index;
				nindex2 = index2;
				index = tempstr.indexOf(",", nindex+1);
				index2 = s.indexOf(",", nindex2+1);
			}
			
		}
		else {
			CreateElement(s, readType, howMany);
		}
		
	}
	
	private void CreateElement (String s, String readType, Integer howMany){
		int n = 1;
		
		if(IsVarAdded == true && !conceptMap.containsKey(ConceptVar)) {
			if(s == "false" || s == "true") {
				MLMObjectElement  mObjElem = new MLMObjectElement("", readType, n, "");
				conceptMap.put(ConceptVar, mObjElem);
				mObjElem.addUserVarVal(ConceptVar,s);
				
			}
			else {
				if(howMany != null){
					n = howMany.intValue(); 
				}
				
				conceptMap.put(ConceptVar, new MLMObjectElement(s, readType, n, ""));
			}
			IsVarAdded = false;    // for next time
			ConceptVar = "";
		}
	}
	
	
	public void SetConceptVar(String s)
	{
		ConceptVar = s;
		IsVarAdded = true;
	}
	
	public void ResetConceptVar()
	{
		ConceptVar = "";
		howMany = 0;
		readType = "";
		IsVarAdded = false;
	}
	
	public void setReadType(String s){
		readType = s;
	}
	
	public void setHowMany(String s){
		howMany = Integer.valueOf(s).intValue();
	}
	
	public void PrintConceptMap()
	{
		System.out.println("__________________________________");
	     Collection<MLMObjectElement> collection = conceptMap.values();
	     for(MLMObjectElement mo : collection) {
	       System.out.println(mo.getConceptName()  + 
	    		   "\n Answer = " + mo.getAnswers() + 
	    		   "\n Operator = " + mo.getCompOps() +
	    		   "\n Conclude Val = " + mo.getConcludeVal() +
	    		   "\n User Vars = " + mo.getUserVarVal()
	    		   );
	    System.out.println("__________________________________");
	     }
	}
	
	public void PrintEvaluateList(){
		System.out.println("\n Evaluate order list is  - ");
		ListIterator<MLMEvaluateElement> thisList = evaluateList.listIterator(0);
		while (thisList.hasNext()){
		     thisList.next().printThisList();
		}
	}
	
/*	public boolean Evaluate(){
		boolean retVal = false;
		String key;
		ListIterator<MLMEvaluateElement> thisList = evaluateList.listIterator(0);
		while (thisList.hasNext()){
			Iterator iter = thisList.next().iterator();
			while (iter.hasNext()) {
			    key = (String) iter.next();
				if(RetrieveConcept(key)){
					PrintConcept(key);
					if(EvaluateConcept(key)){ 
						if(isConclude(key)) {
						  retVal = conclude(key);	
						  break;  // concluded true or false
						}
						else {
								// set all the user defined variables
							addUserVarValFinal(key);
						}
					}
				}
			}
		}
		return retVal;
	}
*/	
	public void WriteAction(String str, Writer w) throws Exception {
		try{
			 w.write("public void initAction() {\n");
		     w.append("\t\tuserVarMap.put(\"ActionStr\", \"" +  str + "\");\n");
				   
		     w.write("}\n\n");	// End of this function
		     w.flush();
		
			 w.write("public String doAction() {\n");
		     w.write("\tint index = 0, nindex = 0, endindex = 0, startindex = 0;\n");
		     w.write("\tString tempstr, variable, outStr = \"\";\n");
		     w.write("\tString inStr = userVarMap.get(\"ActionStr\");\n\n");
		     w.write("\ttempstr = inStr;\n\n");
		     w.write("\tindex = tempstr.indexOf(\"||\", nindex);\n");
		     w.write("\tif(index != -1) {\n");
		     w.write("\t\tif(index == 0) { // At the beginning\n");
		     w.write("\t\t\tnindex = tempstr.indexOf(\"||\", index+1);\n");
		     w.write("\t\t\tstartindex = index + 2;\n");
		     w.write("\t\t\tendindex = nindex;\n");
		     w.write("\t\t\tvariable = inStr.substring(startindex, endindex).trim();\n");
		     w.write("\t\t\toutStr += userVarMap.get(variable);\n");
		     w.write("\t\t\tindex = tempstr.indexOf(\"||\", nindex+2);\n");
		     w.write("\t\t}\n");
		     
		     w.write("\t\twhile(index > 0){\n");
		     w.write("\t\t\tif(nindex == 0){ // Are we starting now\n");
		     w.write("\t\t\t\tstartindex = nindex;\n");
		     w.write("\t\t\t\tendindex = index;\n");
		     w.write("\t\t\t\toutStr += tempstr.substring(startindex, endindex);\n");
		     w.write("\t\t\t}\n");
		     
		     w.write("\t\t\telse {\n");
		     w.write("\t\t\t\tstartindex = nindex + 2;\n");
		     w.write("\t\t\t\tendindex = index;\n");
		     w.write("\t\t\t\toutStr += tempstr.substring(startindex, endindex);\n");
		     w.write("\t\t\t}\n");
		     
		     
		     w.write("\t\t\tnindex = tempstr.indexOf(\"||\", index+2);\n");
		     w.write("\t\t\tstartindex = index + 2;\n");
		     w.write("\t\t\tendindex = nindex;\n");
		     w.write("\t\t\tvariable = inStr.substring(startindex, endindex).trim();\n");
		     w.write("\t\t\toutStr += userVarMap.get(variable);\n");
		     w.write("\t\t\tindex = tempstr.indexOf(\"||\", nindex+2);\n");
		     w.write("\t\t}\n");
		     w.write("\t\toutStr += tempstr.substring(nindex+2);\n");
		     w.write("\t}\n");
		     
		     w.write("\telse {\n");
		     w.write("\t\toutStr += tempstr;\n");
		     w.write("\t}\n");
		     
		     w.write("\treturn outStr;\n");
		     w.write("}\n");
		     
		    
		     w.flush();
		}
		catch (Exception e) {
		      System.err.println("Write Action: "+e);
		      e.printStackTrace();   // so we can get stack trace		
		    }
	}
	public boolean WriteEvaluate(Writer w, String classname) throws Exception {
	boolean retValEval = true, retVal = true;
	try{
		String key;
		ListIterator<MLMEvaluateElement> thisList;
		thisList = evaluateList.listIterator(0);
		
		
		while (thisList.hasNext()){		// Writes individual evaluate functions
			Iterator iter1 = thisList.next().iterator();
			while (iter1.hasNext()) {
			    key = (String) iter1.next();	// else if
			    retVal = writeEvaluateConcept(key, w);
			    if(retVal == false){
			    	retValEval = false;	   // Atleast 1 error
			    }
			}
		}
		if(retValEval == false) {
			return false;
		}
			
		 w.append("\n@Override\npublic Result eval(LogicDataSource d, Patient p, Object[] args) {\n");
		 w.append("\n\tpatient = p;\n\tdataSource = d;\n");
		 
		 w.append("\tuserVarMap = new HashMap <String, String>();\n");
		 w.append("\tfirstname = patient.getPersonName().getGivenName();\n");
		 w.append("\tuserVarMap.put(\"firstname\", firstname);\n");
		 w.append("\tinitAction();\n");		     
		 
		 w.append("\tResult ruleResult = new Result(\"Evaluating Rule - " + classname + "...\");\n");	
		 w.append("\tString actionStr = \"\";\n\n");	
		 w.append("\tif(evaluate_logic(ruleResult)){\n");	
		 w.append("\t\tactionStr = doAction();\n");
		 w.append("\t\truleResult.setValueText(\"Evaluating Rule - " + classname + "...............*****CONCLUDED TRUE****\");\n");
			
		 w.append("\t\t//ruleResult.debug(0);\n");
		 w.append("\t\treturn new Result(actionStr);\n");
		 w.append("\n\t}\n\n");
		
		 w.append("\truleResult.setValueText(\"Evaluating Rule - " + classname + "...............*****CONCLUDED FALSE****\");\n");
		 w.append("\treturn ruleResult;\n");	
		 w.append("\n}\n");
		
		 w.append("\n");
	     w.append("private boolean evaluate_logic(Result valueMap) {\n");

	     w.append("\tboolean retVal = false;\n");
	     w.append("\tResult val;\n");
	 
	     thisList = evaluateList.listIterator(0);   // Start the Big Evaluate()
	     while (thisList.hasNext()){
	    	 Iterator iter = thisList.next().iterator();
	    	 retVal = WriteLogic(iter, w);
	    	 w.flush();
	     }
	    if(retVal){				// The WriteLogic function did not find a standalone conclude
	    	w.append("\t\treturn retVal;\n");
	    }
	    w.append("\n\t}");
	 	w.append("\n");
	}
	catch (Exception e) {
	      System.err.println("Write Evaluate: "+e);
	      e.printStackTrace();   // so we can get stack trace		
	    }
	return retValEval;
    }
	
/*	
	private void WriteIf (Iterator iter, Writer w) {
	  
		try {
		
		String key = "", nextKey = "", tmpStr = "";
		
		boolean startIterFlag = false;
		boolean boolFlag = false;
		
				
			if(iter.hasNext()) {		// IF 
					key = (String) iter.next();
					if(key.equals("tmp_conclude")){
				    	w.append("\n //conclude here\n");
				    	writeActionConcept(key, w);
					//	w.append("\n\t}");
				    }
				    else if(key.equals("ELSE")){
				    	w.append("\n\telse {\n");
				    	writeActionConcept(key, w);
						w.append("\n\t}");
				    }
				    else if(key.equals("IF")){ 
				    	//tmpStr = "\n\tif(evaluate_" + key + "()";
				    	if(iter.hasNext()) {
				    		startIterFlag = true;
				           	while(iter.hasNext()) {		// IF  followed by else if, else, etc
								nextKey = (String) iter.next();
								if(nextKey.equals("THEN")) {
									tmpStr += " ) {\n\t\t";
									w.append(tmpStr);
									InNestedIf++;
									WriteIf(iter, w);   // recurse, because If then if ...
									// reset the following as starting fresh
									InNestedIf--;
									startIterFlag = false; 
									boolFlag = false;
									tmpStr = "";
							    }
								else if(nextKey.equals("AND")){
									tmpStr += " && ";
									boolFlag = true;
									
								}
								else if(boolFlag) {
									tmpStr += complexBool(nextKey);
									boolFlag = false;
								}
								else if(startIterFlag && !boolFlag){
									tmpStr += " ) {\n";
									w.append(tmpStr);
									startIterFlag = false;
									writeActionConcept(key, w);
									w.append("\n\t}");
									tmpStr = complexIf(nextKey);
									key = nextKey;					// store the previous key
									nextKey = "";
									w.append(tmpStr);
								}
								else {
									writeActionConcept(key, w);
									if(InNestedIf == 0) {
										w.append("\n\t}");
									}
									tmpStr = complexIf(nextKey);
									key = nextKey;					// store the previous key
									nextKey = "";
									w.append(tmpStr);
								}
								
					       	}
				           	if(nextKey.equals("") && !key.equals("")) {       // end of loop
				           		writeActionConcept(key, w);
				           		w.append("\n\t}");
				           	}
					    }	
					    else {      // If we do not have a complex if
					       	tmpStr += " ) {\n";
						    w.append(tmpStr);
							writeActionConcept(key, w);
							w.append("\n\t}");
					    }
				    }
			}
			
		
		
		
   //	w.append("}\n");	// End of this function
		w.append("\n");
		}
		catch (Exception e) {
		      System.err.println("Write Evaluate: "+e);
		      e.printStackTrace();   // so we can get stack trace		
		    }
	}
*/	
	
	private boolean WriteLogic (Iterator iter, Writer w) {
		boolean retVal = true;   // Have the calling function add the statement - return retVal;  
		try {
		
		String key = "", nextKey = "", tmpStr = "";
		
		boolean startIterFlag = false;
		boolean boolFlag = false;
		boolean funcFlag = false;
		MLMObjectElement mObjElem;
				
			if(iter.hasNext()) {		// IF 
			key = (String) iter.next();
			if(key.equals("IF")){ 
		    	tmpStr = complexIf(key,null);
		    	w.append (tmpStr);
		    	
		    	       	while(iter.hasNext()) {		// IF  followed by else if, else, etc
		    	       	w.flush();
		    	       	tmpStr = "";
		           		nextKey = (String) iter.next();
		           		if(nextKey.equals("THEN")) {
							tmpStr += " ) {\n\t";
							tmpStr += "\tvalueMap.add(val);\n";
							w.append(tmpStr);
							
							
		           		}
		           		else if (nextKey.equals("IF")) {
							InNestedIf++;
							WriteLogic(iter, w);   // recurse, because If then if ...
							// reset the following as starting fresh
							InNestedIf--;
							startIterFlag = false; 
							boolFlag = false;
							tmpStr = "";
					    }
						else if(nextKey.equals("AND")){
							tmpStr += " && ";
							w.append(tmpStr);
							w.flush();
							boolFlag = true;
							
						}
						else if(nextKey.equals("OR")){
							tmpStr += " || ";
							w.append(tmpStr);
							w.flush();
							boolFlag = true;
							
						}
						else if(boolFlag) {
						//	tmpStr += complexBool(nextKey);
							mObjElem = GetMLMObjectElement(nextKey);
							tmpStr += complexBool(nextKey, mObjElem);
							w.append(tmpStr);
							boolFlag = false;
						}
						else if(startIterFlag && !boolFlag && !funcFlag){
							tmpStr += complexIf(nextKey, null);
							w.append(tmpStr);
							startIterFlag = false;
							writeActionConcept(key, w);
							key = nextKey;					// store the previous key
							nextKey = "";
							
						}
						else if (nextKey.equals("Logic_Assignment")) {
							writeActionConcept(key, w);
							if(InNestedIf == 0 && IsComplexIf) {
								IsComplexIf = false;
								w.append("\n\t}\n\t}\n");
							}
							
							nextKey = "";
						}
						else if (nextKey.equals("Conclude")){
							w.append("\n\t //conclude here\n");
					    	writeActionConcept(key, w);
					    	if(InNestedIf == 0) {
					    		if(!IsComplexIf) {
					    			w.append("\n\t}\n");
					    			
					    		}
					    		else {
					    			w.append("\n\t}\n\t}\n");
					    			IsComplexIf = false;
					    		}
					    		
					    		if(key.startsWith("ELSE_"))
					    		{
					    			retVal = false;	// Since we are conclude unconditionally, we do not want calling function to add the return statement
					    		}
							}
							
							nextKey = "";
						}
						
						else if(nextKey.equals("EXIST") || (nextKey.equals("ANY"))){
		    		    	funcFlag = true;
		    		    	key = nextKey;
		           		}
						else if(funcFlag){
							mObjElem = GetMLMObjectElement(nextKey);
							tmpStr += complexFunc(nextKey, mObjElem,key);
							w.append(tmpStr);
							key = nextKey;
							funcFlag = false;
						}
						else {
							mObjElem = GetMLMObjectElement(nextKey);
							tmpStr += complexIf(nextKey, mObjElem);
							w.append(tmpStr);
							key = nextKey;  // store the current key
						}
						
			       	}
		         
			    }
			else if(key.startsWith("Conclude")) { // Conclude by itself
				w.append("\n\t //conclude here\n");
		    	writeActionConcept(key, w);
		    	retVal = false;	// Since we are conclude unconditionally, we do not want calling function to add the return statement
		   	  }
			    
		    }
			w.append("\n");
		}
		catch (Exception e) {
		      System.err.println("Write Evaluate: "+e);
		      e.printStackTrace();   // so we can get stack trace		
		    }
		return retVal;
	}	
	
	private String complexIf(String nextKey, MLMObjectElement mObjElem) {
		String tmpStr = "";
	
   try{
	   if(nextKey.equals("ELSE") || nextKey.startsWith("ELSE_")){
	    	if(InNestedIf > 0) {
				tmpStr = "\n\t}\n\telse {\n";
			}
	    	else {
	    		tmpStr = "\n\telse {\n";
	    	}
	    }
	    else if(nextKey.equals("ELSEIF")){
	    	if(InNestedIf > 0) {
				tmpStr = "\n\t}\n\telse if (";
			}
	    	else {
	    		tmpStr = "\n\telse if (";
	    	}
	        
	    }
	    else if(nextKey.equals("IF")){
	    	if(InNestedIf > 0) {
				tmpStr = "\n\t}\n\tif (";
			}
	    	else {
	    		tmpStr = "\n\tif (";
	    	}
	        
	    }
	   else if(!nextKey.equals("ENDIF")){
			tmpStr = " (val = " + nextKey + "()) != null ) {\n\t";
			IsComplexIf = true;
			if(mObjElem != null ){
				tmpStr += "\tif (";
				tmpStr += mObjElem.getCompOpCode(nextKey,NOLIST);
				
			}
			 
		}
   	} catch (Exception e){
	      System.err.println("ComplexIf: "+e);
	      e.printStackTrace();   // so we can get stack trace		
	    }
		return tmpStr;
	}
	
	private String complexFunc(String nextKey, MLMObjectElement mObjElem, String Key) {
		String tmpStr = "";
     try {		
		if(Key.equals("EXIST") && !nextKey.equals("")){       // Not blank
		    	tmpStr = "userVarMap.containsKey(\""+ nextKey+ "\") && !userVarMap.get(\"" + nextKey + "\").equals(\"\")  ";
		}
		else if(Key.equals("ANY") && !nextKey.equals("")){
			tmpStr = " (val = " + nextKey + "()) != null ) {\n\t";
			IsComplexIf = true;
			if(mObjElem != null ){
				tmpStr += "\tif (";
				tmpStr += mObjElem.getCompOpCode(nextKey, LIST);
			}
				
		}
		else {
			if(mObjElem != null ){
				tmpStr += mObjElem.getCompOpCode(nextKey, NOLIST);
				
			}
		}
     } catch (Exception e){
	      System.err.println("ComplexFunc: "+e);
	      e.printStackTrace();   // so we can get stack trace		
	    }
		return tmpStr;
	}
	
	
	private String complexBool(String nextKey, MLMObjectElement mObjElem) {
		String tmpStr = "";
     try {		
		if(nextKey.equals("tmp_conclude")){
	    	//TODO error
	    }
	    else if(nextKey.equals("tmp_01")){
	    	//TODO error
	    }
		else {
			if(mObjElem != null ){
				tmpStr += mObjElem.getCompOpCode(nextKey, NOLIST);
				
			}
		}
     } catch (Exception e){
	      System.err.println("ComplexBool: "+e);
	      e.printStackTrace();   // so we can get stack trace		
	    }
		return tmpStr;
	}
	
	
	public int GetSize(){
		return conceptMap.size();
	}
	//public void InitIterator() {
	//	iter = conceptMap.keySet().iterator();
	//}
	//public String GetNextConceptVar(){
	//	if(iter.hasNext()) { 
	//		return iter.next();
	//	}
	//	else {
	//		return null;
	//	}
	//}
	
	public String GetConceptName(String key){
		if(conceptMap.containsKey(key)) {
			return conceptMap.get(key).getConceptName();
		}
		else {
			return null;
		}
				
	}
	
	public MLMObjectElement GetMLMObjectElement(String key) {
		if(conceptMap.containsKey(key)) {
			return conceptMap.get(key);
		}
		else {
			return null;
		}
				
	}
	public void InitEvaluateList() {
	//	ResetConceptVar();
		if(!evaluateList.isEmpty()){
			MLMEvaluateElement mEvalElem = evaluateList.getLast();
			if(mEvalElem != null && mEvalElem.getLast().equals("ELSEIF")){
				// Nested if
				return;
			}
			else if (mEvalElem != null && mEvalElem.getLast().equals("ELSE")){
				// Nested if
				return;
			} 
			else {
				MLMEvaluateElement mEvalElemNew = new MLMEvaluateElement();
				evaluateList.add(mEvalElemNew);
			}
		}
		else {
			MLMEvaluateElement mEvalElemNew = new MLMEvaluateElement();
			evaluateList.add(mEvalElemNew);
		}
		
	}
/*	public boolean RetrieveConcept(String key) {
		
		//TODO check to see if user authenticated
		boolean retVal = false;
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null ){
			mObjElem.setServicesContext(Context.getConceptService(), Context.getObsService());
			if(mObjElem.getDBAccessRequired()){
				retVal = mObjElem.getConceptForPatient(locale, patient);
			}
			else {
				retVal = true; // No DB access required like else or simply conclude
			}
		}
		return retVal;
	}
	
		
	public boolean EvaluateConcept(String key) {
		boolean retVal = false;
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null && !mObjElem.isElementEvaluated()){
			retVal = mObjElem.evaluate();
		}
		return retVal;
	}
*/	
	public boolean writeEvaluateConcept(String key, Writer w) throws Exception{
		boolean retVal = true;
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null ){
			retVal = mObjElem.writeEvaluate(key, w);
			w.flush();
		}
		return retVal;
	}

	public boolean writeActionConcept(String key, Writer w) throws Exception{
		boolean retVal = false;
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null ){
			retVal = mObjElem.writeAction(key, w);
			w.flush();
		}
		else {
			// Must be conclude not attached to a read statement variable, and attached to a function like EXIST
			mObjElem = GetMLMObjectElement("Func_1");
			retVal = mObjElem.writeAction(key, w);
			w.flush();
		}
		return retVal;
	}
	
	
/*	public boolean Evaluated(String key){
		boolean retVal = false;
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null){
			retVal = mObjElem.getEvaluated();
		}
		return retVal;
	}
	
	public Iterator <String> iterator(){
		Iterator iter;
		return iter = conceptMap.keySet().iterator();
	}
*/	
	public void AddToEvaluateList(String key){

		MLMEvaluateElement mEvalElem = evaluateList.getLast();
		if(mEvalElem != null){
			mEvalElem.add(key);
		}
	//	SetConceptVar(key);
	}
	
	public void SetCompOperator(Integer op, String key) {
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null){
			mObjElem.setCompOp(op);
		}
	}
	
	public void SetAnswer (String val, String key) {
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null){
			mObjElem.setAnswer(val);
		}
	}
	
	public void SetAnswer (int val, String key) {
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null){
			mObjElem.setAnswer(val);
		}
	}
	public void SetAnswer (boolean val, String key) {
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null){
			mObjElem.setAnswer(val);
		}
	}
	
	public void SetConcludeVal (boolean val, String key){
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null){
			mObjElem.setConcludeVal(val);
		}
//		 remove it as no nested IFs anymore
//		if(!evaluateList.isEmpty()){
//			MLMEvaluateElement mEvalElem = evaluateList.getLast();
//			if(mEvalElem != null && mEvalElem.getLast().equals("THEN")){
//				// Nested if
//				mEvalElem.removeThen();
//			}
//		}
		
	}
		
	public void SetUserVarVal (String var, String val, String key) {
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null){
			mObjElem.addUserVarVal(var, val);
		}
	
	}
	
	public void SetDBAccess(boolean val, String key ) {
		MLMObjectElement mObjElem;
		if(key.equals("")) {
				mObjElem = GetMLMObjectElement(ConceptVar);
		}
		else {	
				mObjElem = GetMLMObjectElement(key);
		}
		if(mObjElem != null){
			mObjElem.setDBAccessRequired(val);
		}
	}
	
	public boolean GetDBAccess(String key ) {
		boolean retVal = false;
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null){
			retVal = mObjElem.getDBAccessRequired();
		}
		return retVal;
	}
	
	public void addUserVarValFinal(String key) {
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null){
			String var = "", val = "";
		    Iterator iter = mObjElem.iterator();
			while(iter.hasNext()) {
				var = (String) iter.next();
				val = mObjElem.getUserVarVal(var);
				if(!userVarMapFinal.containsKey(var)) {
					userVarMapFinal.put(var, val);
				}
				else
				{
					//TODO either an error or overwrite previous one
				}
			}
		}
	}
	public boolean isConclude(String key) {
		boolean retVal = false;
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null){
			retVal = mObjElem.isConclude();
		}
		return retVal;
	}
	
	public boolean conclude(String key) {
		boolean retVal = false;
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null){
			retVal = mObjElem.conclude();
		}
		return retVal;
	}
	
	
/*	public String getUserVarVal(String key) {
		String retVal = "";
		if(userVarMapFinal.containsKey(key)) {
			retVal = userVarMapFinal.get(key);
		}
		else if(key.equals("firstname")) {
			retVal = patient.getPersonName().getGivenName();
		}
		return retVal;
	}
*/	
	public void setWhere(String type, String key) {
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null){
			mObjElem.setWhere(type);
		}
	}
	
	public void setDuration(String type, String val, String op, String key) {
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null){
			mObjElem.setDuration(type, val,op);
		}
	}
	
	public void setClassName(String name) {
		if(name.endsWith(".mlm")){
			className = name.substring(0,name.indexOf(".mlm"));
		}
		else {
			className = name.trim();
		}
	}
	public String getClassName() {
		return className.trim();
	}
}
