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
	private Integer compOp;
	private boolean hasConclude;
	private boolean concludeVal;
	private HashMap<String, String> userVarMap ;
	private String error;
	private boolean conceptEvalWritten;    // if the compiler has written a method as XYZ() already for this concept XYZ
	
	public MLMObjectElement(String s, String t, int n,  String d) {
		conceptName = s;
		readType = t;
		howMany = n;
		durationOp = d;
//		evaluated = false;
//		isEvaluated = false;
		userVarMap = new HashMap <String, String>();
		dbAccessRequired = true;  // by default assume that we have to make an API call to get data
		conceptEvalWritten = false;
		answerStr = new LinkedList<String> ();
		answerInt = new LinkedList<Integer> ();
		iterAnswerStr = answerStr.listIterator(0);
		iterAnswerInt = answerInt.listIterator(0);
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
		compOp = op;
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
		durationOp = op;
	}
	
	public void setWhere(boolean val){
		hasWhere = val;
	}
	
	public void addUserVarVal(String var, String val) {
		if(!userVarMap.containsKey(var)) {
			userVarMap.put(var, val);
		}
		else
		{
			//TODO either an error or overwrite previous one
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
		if (!readType.equals("") ) {
			retVal = "." + readType + "(";
			if(howMany > 0){
				retVal += howMany;
			}
			retVal += ")";
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
				//	w.append("\t\t\t\tuserVarMap.put(\"" + var + "\", \""+ val + "\");\n");
				//	w.append("\t\t\t}");
					w.append("\t\tuserVarMap.put(\"" + var + "\", " + val + ");\n");
				//	w.append("\t\tdssObj.addObs(\"" + getConcept().trim() + "\", obs);\n");	// changed to have the key such as last_pb than BLOOD_LEAD_LEVEL as the key to the obsMap see below
					w.append("\t\tvalueMap.put(\"" + key + "\", val);\n");
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
						w.append("\t\tvalueMap.put(\"" + key + "\", val);\n");
					}
				}
				else if(concludeVal == false){
					w.append("\t\tretVal = false;\n");
					
					if(!key.startsWith("Conclude") &&  !key.startsWith("ELSE") &&  !key.startsWith("ENDIF")
							   && !key.equals("AND")){
						//w.append("\t\tdssObj.addObs(\"" + getConcept().trim() + "\", obs);\n"); // changed to have the key such as last_pb than BLOOD_LEAD_LEVEL as the key to the obsMap see below
						w.append("\t\tvalueMap.put(\"" + key + "\", val);\n");
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
			   w.append("private ArdenValue " + key + "(){\n");
			   w.append("\tConcept c = new Concept();\n");
			   w.append("\tc.setConceptId(" + Integer.toString(concept.getConceptId()) + "); // " + cn + "\n");
		       
			   //w.append("\tconcept = Context.getConceptService().getConceptByName(\"" + cn.trim() + "\");\n");
			   w.append("\t //return dataSource.eval(patient, Aggregation" +  getReadType() + ", c, DateCriteria." + whereType 
					   + "(Duration." + durationType + "("  +  durationOp + "(" +  durationVal + "))) );\n");
			   
			   if(!readType.equals("")){
				   if(hasWhere){
					   w.append("\treturn dataSource.eval(patient, ardenClause.concept(c)." + readType + "(" + howMany + ")." + whereType + "()." + durationType + "()." + durationOp + "(" + durationVal + "));\n");
				    }
				   else {
					   w.append("\treturn dataSource.eval(patient, ardenClause.concept(c)." + readType+ "(" + howMany + "));\n");
				   }
			   }
			   else { 
				   w.append("\treturn dataSource.eval(patient, ardenClause.concept(c));\n");
			   }
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
			   if(readType.equals("call")) {
				   w.append("private ArdenValue " + "call_" +cn + "(){\n");
				   w.append("\tArdenValue ardenValue;\n");
			       w.append("\tArdenRule mlm;\n\n");
				   
			       w.append("mlm = new " + cn + "(patient, dataSource);\n");
			       w.append("if(mlm != null) {\n\t\tardenValue = mlm.evaluate();\n\t\t return ardenValue;\n}\nelse {return null;}");
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
	public String getCompOp(){
		String s =  Integer.toString(compOp);
		System.err.println(s);
		return s;
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
	
	public String getUserVarVal(String var){
		String val = "";
		if(!userVarMap.isEmpty()) {
			val = userVarMap.get(var);
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
	public String getCompOpCode(String key) throws Exception {
		String retStr = "";
		String answer;
		if (compOp != null){
			   switch(compOp) {
			   		case EQUALS:
			 /*  		{switch(compOpType){
		  			case 3: // boolean
		  				retStr += "\tif (val.getValueAsBoolean() == " + Boolean.toString(answerBool) ;
		  				break;
		  			case 2: // integer
		  				retStr += "\tif (val.getValueNumeric() == " + Integer.toString(answerInt) ;
		  				break;
		  			case 1: // String
		  				retStr += "\tif (val.getValueText() != null && val.getValueText().equals(\"" + answerStr + "\")";
		  				break;
				   }
			   	*/
			   		if(concept != null) {	
			   			if (concept.isNumeric()) {
			   				retStr += "\tif (val.getValueNumeric() == " + getAnswerInt() ;
		   				}
			   			else if (concept.getDatatype().getName().equals("Coded")) {
			   				answer = getAnswerStr();
			   				answerConcept = Context.getConceptService().getConceptByName(answer);   
			   				if(answerConcept != null){
			   					retStr += "\n\t //" + answer + "\n\tif (val.getValueCoded() == " + Integer.toString(answerConcept.getConceptId()) ;
			   				}
		   				}	
			   		}
			   		break;
			   		case GTE:
			   			if (concept != null && concept.isNumeric()) {
			   				retStr += "\tif (val.getValueNumeric() >= " + getAnswerInt() ;
		   				}
		   					
			   		
			   	/*	{switch(compOpType){
		  			case 3: // boolean
		  				
		  				break;
		  			case 2: // integer
		  				retStr += "\tif (val.getValueNumeric() >= " + Integer.toString(answerInt) ;
		  				break;
		  			case 1: // String
		  				
		  				break;
				   }
		   			
			   		}
			   	*/	break;
			   		default:
			   			break;
			   	}
			   
			   }  
			
		   
		  
		   return retStr;
	}
}
