package org.openmrs.arden;


import java.io.Writer;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.Set;
import java.util.LinkedList;

import org.openmrs.Concept;
import org.openmrs.api.context.Context;



/*
 * @@ This class represents a statement in Data slot - read 
 */


public class MLMObjectElement implements ArdenBaseTreeParserTokenTypes {

	private boolean dbAccessRequired;
	private String conceptName;
	private Concept concept;
	private Concept answerConcept;
	private String readType;    // Exist, Last, First etc
	private int howMany;	// how many to read
	private boolean hasWhere;
	private String whereType;
	private String durationType;
	private String durationVal;
	private String durationOp; // TODO
	
	private LinkedList<String> answerStr;
	private ListIterator<String> iterAnswerStr;
	
	private LinkedList<Integer> answerInt;
	private ListIterator<Integer> iterAnswerInt;
	
	private boolean answerBool;
	private LinkedList<Integer> compOp;
	private ListIterator<Integer> iterCompOp;
	
	private boolean hasConclude;
	private boolean concludeVal;
	private HashMap<String, LinkedList<String>> userVarMap ;
	private String error;
	private boolean conceptEvalWritten;    // if the compiler has written a method as XYZ() already for this concept XYZ
	
	public MLMObjectElement(String s, String t, int n,  String d) {
		conceptName = s;
		readType = t;
		howMany = n;
		durationOp = d;
//		evaluated = false;
//		isEvaluated = false;
		userVarMap = new HashMap <String, LinkedList<String>>();
		dbAccessRequired = true;  // by default assume that we have to make an API call to get data
		conceptEvalWritten = false;
		answerStr = new LinkedList<String> ();
		answerInt = new LinkedList<Integer> ();
		compOp = new LinkedList<Integer> ();
		
		iterAnswerStr = answerStr.listIterator(0);
		iterAnswerInt = answerInt.listIterator(0);
		iterCompOp = compOp.listIterator(0);
	}
	
	public void setAnswer (String s){
		answerStr.add(s);
	}
	public void setAnswer (Integer i){
		answerInt.add(i);
	}
	public void setAnswer (boolean b){
		answerBool = b;
	}
	public void setCompOp (Integer op){
		compOp.add(op);
	}
	public void setConcludeVal (boolean val) {
		hasConclude = true;
		concludeVal = val;
	}
	public void setWhere (String type){
		hasWhere = true;
		whereType = type;
	}
	
	public void setDuration (String type, String val, String op){
		durationType = type;
		durationVal = val;
		if(op.toUpperCase().startsWith("MONTH")){
			durationOp = "months";	
		}
		else if(op.toUpperCase().startsWith("YEAR")){
			durationOp = "years";
		}
		else if(op.toUpperCase().startsWith("DAY")){
			durationOp = "days";
		}
	}
	
	public void setWhere(boolean val){
		hasWhere = val;
	}
	
	public void addUserVarVal(String var, String val) {
		if(!userVarMap.containsKey(var)) {
			LinkedList<String> thisList = new LinkedList<String> ();
			thisList.add(val);
			userVarMap.put(var, thisList);
		}
		else
		{
			userVarMap.get(var).add(val);
		}
		
	}
	private String getConcept(){
		   String  cn;
		   int len;
			int index;
			
			index = conceptName.indexOf("from");	// First substring
			if(index != -1) {
					cn = conceptName.substring(1,index);
				}
			else {
				len = conceptName.length();
				if(conceptName.contains("{")){
					cn = conceptName.substring(1,len-1);
				}
				else {
					cn = conceptName.substring(0,len);
				}
			}	
			return cn;
	   }
  
	private String getReadType() {
		String retVal = "";		
		if(readType != null){
		if (!readType.equals("") ) {
			retVal = "." + readType + "(";
			if(howMany > 0){
				retVal += howMany;
			}
			retVal += ")";
		}
		}
		else {
			retVal = ".last()";   // TODO: for now default
		}
	
		return retVal;
	}
	
   public boolean writeAction(String key, Writer w) throws Exception {
	   boolean retVal = false;
		/*if(!hasConclude)*/ {	// no conclude
				String var = "", val = "";
			    Iterator iter = iterator();
				while(iter.hasNext()) {
					var = (String) iter.next();
					val = getUserVarVal(var);
					w.append("\t\t//"+var+ " = " + val + "\n"); // write as comment
				//	w.append("\t\t\tif(!userVarMap.containsKey("+ var + ")) {\n\t\t\t\tuserVarMap.put(\"" + var + "\", \""+ val + "\");\n\t\t\t}\n");
				//	w.append("\t\t\telse {\n");
					w.append("\t\t\tuserVarMap.put(\"" + var + "\", \""+ val + "\");\n");
				//	w.append("\t\t\t}");
				//	w.append("\t\tuserVarMap.put(\"" + var + "\", " + val + ");\n");
				//	w.append("\t\tdssObj.addObs(\"" + getConcept().trim() + "\", obs);\n");	// changed to have the key such as last_pb than BLOOD_LEAD_LEVEL as the key to the obsMap see below
				//	w.append("\t\tvalueMap.put(\"" + key + "\", val);\n");
				}
				retVal = true;
			}
			/* else */
			if(hasConclude)	{ // has conclude
				if(concludeVal == true) {
					w.append("\t\tretVal = true;\n");
					
					if(!key.startsWith("Conclude") &&  !key.startsWith("ELSE") &&  !key.startsWith("ENDIF")
							   && !key.equals("AND")){
						//w.append("\t\tdssObj.addObs(\"" + getConcept().trim() + "\", obs);\n"); // changed to have the key such as last_pb than BLOOD_LEAD_LEVEL as the key to the obsMap see below
						//w.append("\t\tvalueMap.put(\"" + key + "\", val);\n");
					}
				}
				else if(concludeVal == false){
					w.append("\t\tretVal = false;\n");
					
					if(!key.startsWith("Conclude") &&  !key.startsWith("ELSE") &&  !key.startsWith("ENDIF")
							   && !key.equals("AND")){
						//w.append("\t\tdssObj.addObs(\"" + getConcept().trim() + "\", obs);\n"); // changed to have the key such as last_pb than BLOOD_LEAD_LEVEL as the key to the obsMap see below
						//w.append("\t\tvalueMap.put(\"" + key + "\", val);\n");
					}
				
				}
				else {	// TODO error
					
				}
				w.append("\t\treturn retVal;\n");
				retVal = true;
			}
			

			return retVal;
   }
  
   
public boolean writeEvaluate(String key, Writer w) throws Exception{
	   boolean retVal = true;
	 
	   if(conceptEvalWritten){
		   return true;         // we have a function that reads this concept as per READ statement
	   }
	   
	   if(!key.startsWith("Conclude") &&  !key.startsWith("ELSE") &&  !key.startsWith("ENDIF")
			   && !key.equals("AND")){
		   String cn = getConcept();
		   concept = Context.getConceptService().getConceptByName(cn);
		   
		   if(dbAccessRequired){
			   if(concept != null){
			   w.append("private Result " + key + "(){\n");
			   w.append("\tConcept c = new Concept();\n");
			   w.append("\tc.setConceptId(" + Integer.toString(concept.getConceptId()) + "); // " + cn + "\n");
		       
			 //  if(readType != null && !readType.equals("")  ){
				   if(hasWhere){
					   w.append("\t return dataSource.eval(patient, Aggregation" +  getReadType() + ", c, DateConstraint." + whereType 
							   + "(Duration."  /* +  durationType + "(" */ +  durationOp + "(" +  durationVal + ")) );\n");
					   
					   //w.append("\treturn dataSource.eval(patient, ardenClause.concept(c)." + readType + "(" + howMany + ")." + whereType + "()." + durationType + "()." + durationOp + "(" + durationVal + "));\n");
				    }
				   else {
					   w.append("\t return dataSource.eval(patient, Aggregation" +  getReadType() + ", c, null);\n");
					   
					   //w.append("\treturn dataSource.eval(patient, ardenClause.concept(c)." + readType+ "(" + howMany + "));\n");
				   }
			   //  }
			     w.append("}\n\n");
			   }
			   else {
				 System.out.println("Compiler error - No concept found in the dictionary: " + cn );
				 error = "Compiler error - No concept found in the dictionary: " + cn;
				 retVal = false;
				// w.append("private ArdenValue " + key + "(){\n");
				// w.append("\treturn null;\n");
				// w.append("}\n\n");  
			   }
			   conceptEvalWritten = true;		// Finished writing
			   
		   }  // end of DB access required
		   else {  // No DB access, simply conclude or else conclude
			   if(readType != null && readType.equals("call")) {
				   //w.append("private Result " + "call_" +cn + "(){\n");
				   //w.append("\tResult ardenValue;\n");
			       //w.append("\tRule mlm;\n\n");
				   
			       //w.append("mlm = new " + cn + "(patient, dataSource);\n");
			       //w.append("if(mlm != null) {\n\t\tardenValue = mlm.eval(patient, dataSource);\n\t\t return ardenValue;\n}\nelse {return null;}");
			   }
			   else {  
			   	w.append("\t\tString val = userVarMap.get( \"" + key + "\");\n");
			   	w.append("\t\tif(val == \"false\") {retVal = false;}\n");
			   	w.append("\t\tif(val == \"true\") {retVal = true;}\n");
			   }
			   	
		   }
		} 
       
	   return retVal;
   }

	public Iterator <String> iterator(){
		return userVarMap.keySet().iterator();
	}
	
	public String getConceptName(){
		
		return conceptName;
	}
	
	private String getAnswerStr() {
		String retVal;
		if(iterAnswerStr.hasNext()) {
			retVal =  answerStr.remove();
			return retVal;
		}
		else {
			return null;
		}
	}
	
	private Integer getAnswerInt() {
		Integer intVal;
		if(iterAnswerInt.hasNext()) {
		   intVal =  answerInt.remove();
		   return intVal;
		}
		else {
			return null;
		}
	}
	private Integer getCompOp(){
		Integer intVal;
		if(iterCompOp.hasNext()) {
			   intVal =  compOp.remove();
			   return intVal;
			}
			else {
				return null;
			}
	}
	
	public String getConcludeVal(){
		String retVal;
		if(hasConclude){
			if(concludeVal == true) 
				retVal="true"; 
			else
				retVal="false";
		}
		else {
			retVal = "unknown";
		}
		return retVal;
	}
	public boolean hasError() {
		if(error.equals("")){
			return false;
		}
		else {
			return true;
		}
	}
	
	public boolean isConclude() {
		return hasConclude;
	}
	
	public boolean conclude(){
		return concludeVal;
	}
	
	public String getUserVarVal() {
		String s = "";
		if(!userVarMap.isEmpty()) {
			Set<String> keys = userVarMap.keySet();
			for(String key : keys) {
			     s += key + " = " + userVarMap.get(key)+ "\n";
			}
		}
		return s;
	}
	
	public String getAnswers() {
		String s = "";
		Integer i;
		
		ListIterator<String> iter = answerStr.listIterator(0);
		while (iter.hasNext()){
		     s += iter.next() + " , ";
		}

		ListIterator<Integer> iterI = answerInt.listIterator(0);
		while (iterI.hasNext()){
			i = iterI.next();
			s += i + " , ";
		}
		
		return s;
	}
	
	public String getCompOps() {
		String s = "";
		Integer i;
		
		ListIterator<Integer> iterI = compOp.listIterator(0);
		while (iterI.hasNext()){
			i = iterI.next();
			
			s += i + " , ";
		}
		
		return s;
	}
	
	public String getUserVarVal(String var){
		String val = "";
		LinkedList<String> thisList;
		if(!userVarMap.isEmpty()) {
			thisList = userVarMap.get(var);
			if(thisList != null) {
				val = thisList.remove();
			}
		}
		return val;
	}
	
	public void setDBAccessRequired(boolean val){
		dbAccessRequired = val;
	}
	
	public boolean getDBAccessRequired(){
		return dbAccessRequired;
	}
	
	
	/*****************************************/
	public String getCompOpCode(String key, int type) throws Exception {
		String retStr = "";
		String answer;
		Integer i;
		
		if (compOp != null){
			   switch(getCompOp()) {
			   		case IN:
			   			if(concept != null && type == MLMObject.NOLIST) {	
				   			if (concept.isNumeric()) {
				   				retStr += "val.toNumber() == " + getAnswerInt() ;
			   				}
				   			else if (concept.getDatatype().getName().equals("Coded")) {
				   				answer = getAnswerStr();
				   				answerConcept = Context.getConceptService().getConceptByName(answer);   
				   				if(answerConcept != null){
				   					retStr += "val.containsConcept(" + Integer.toString(answerConcept.getConceptId()) + ")\t //" + answer + "\n";
				   				}
			   				}	
				   		}
			   			else if(concept != null && type == MLMObject.LIST) {	
				   			if (concept.isNumeric()) {
				   				retStr += "val.toNumber() == " + getAnswerInt() ;
				   				while((i = getAnswerInt()) != null){
				   					retStr += "\n\t || val.toNumber() == " + i ;
				   				}
			   				}
				   			else if (concept.getDatatype().getName().equals("Coded")) {
				   				answer = getAnswerStr();
				   				answerConcept = Context.getConceptService().getConceptByName(answer);   
				   				if(answerConcept != null){
				   					retStr += "val.containsConcept(" + Integer.toString(answerConcept.getConceptId()) + ")\t //" + answer + "\n";
				   				}
				   				else {
				   					Exception e = new Exception("Concept not found in openmrs dictionary: " + answer);
				   					
				   					throw(e);
				   				}
				   					
				   				while((answer = getAnswerStr()) != null){
				   					answerConcept = Context.getConceptService().getConceptByName(answer);   
					   				if(answerConcept != null){
					   					retStr += "\n\t || val.containsConcept(" + Integer.toString(answerConcept.getConceptId()) + ")\t //" + answer + "\n";
					   				}
					   				else {
					   					Exception e = new Exception("Concept not found in openmrs dictionary: " + answer);
					   					
					   					throw(e);
					   				}
					   				
				   				}
				   				
			   				}	
				   		} 
			   		break;
			   		case EQUALS:
			    		if(concept != null) {	
			   			if (concept.isNumeric()) {
			   				retStr += "val.toNumber() == " + getAnswerInt() ;
		   				}
			   			else if (concept.getDatatype().getName().equals("Coded")) {
			   				answer = getAnswerStr();
			   				answerConcept = Context.getConceptService().getConceptByName(answer);   
			   				if(answerConcept != null){
			   					//retStr += "\n\t //" + answer + "\n\tif (val.getValueCoded() == " + Integer.toString(answerConcept.getConceptId()) ;
			   					retStr += "val.containsConcept(" + Integer.toString(answerConcept.getConceptId()) + ")\t //" + answer + "\n";
			   				}
			   				
		   				}	
			   		}
			   		break;
			   		case GTE:
			   			if (concept != null && concept.isNumeric()) {
			   				retStr += "val.toNumber() >= " + getAnswerInt() ;
		   				}
		   			break;
			   		case GT:
			   			if (concept != null && concept.isNumeric()) {
			   				retStr += "val.toNumber() > " + getAnswerInt() ;
		   				}
		   			break;
			   		case LT:
			   			if (concept != null && concept.isNumeric()) {
			   				retStr += "val.toNumber() < " + getAnswerInt() ;
		   				}
		   			break;
		   			
		   			
		   			
			   		default:
			   			break;
			   	}
			   
			   }  
			
		   
		  
		   return retStr;
	}
}
