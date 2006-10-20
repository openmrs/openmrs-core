package org.openmrs.arden;


import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;



/*
 * @@ This class represents a statement in Data slot - read 
 */


public class MLMObjectElement implements ArdenBaseTreeParserTokenTypes {

	private boolean dbAccessRequired;
	private String conceptName;
	private String readType;    // Exist, Last, First etc
	private int howMany;	// how many to read
	private boolean hasWhere;
	private String whereType;
	private String durationType;
	private String durationVal;
	private String durationOp; // TODO
	private String answerStr;
	private Integer answerInt;
	private boolean answerBool;
	private Integer compOpType; // 0 = none, 1= Str, 2= Integer, 3 = Boolean
	private Integer compOp;
	private boolean hasConclude;
	private boolean concludeVal;
	private HashMap<String, String> userVarMap ;
	
	public MLMObjectElement(String s, String t, int n,  String d) {
		conceptName = s;
		readType = t;
		howMany = n;
		durationOp = d;
//		evaluated = false;
//		isEvaluated = false;
		userVarMap = new HashMap <String, String>();
		dbAccessRequired = true;  // by default assume that we have to make an API call to get data
	}
	
	public void setAnswer (String s){
		answerStr = s;
		compOpType = 1;  //TODO define a better RHS object
	}
	public void setAnswer (Integer i){
		answerInt = i;
		compOpType = 2;
	}
	public void setAnswer (boolean b){
		answerBool = b;
		compOpType = 3;
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
	public String getConcept(){
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
  
   public boolean writeAction(String key, Writer w) throws Exception {
	   boolean retVal = false;
		/*if(!hasConclude)*/ {	// no conclude
				String var = "", val = "";
			    Iterator iter = iterator();
				while(iter.hasNext()) {
					var = (String) iter.next();
					val = getUserVarVal(var);
					w.append("\t\t//"+var+ " = \"" + val +"\"\n"); // write as comment
				//	w.append("\t\t\tif(!userVarMap.containsKey("+ var + ")) {\n\t\t\t\tuserVarMap.put(\"" + var + "\", \""+ val + "\");\n\t\t\t}\n");
				//	w.append("\t\t\telse {\n");
				//	w.append("\t\t\t\tuserVarMap.put(\"" + var + "\", \""+ val + "\");\n");
				//	w.append("\t\t\t}");
					w.append("\t\tuserVarMap.put(\"" + var + "\", \""+ val + "\");\n");
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
	   boolean retVal = false;
	   if(!key.startsWith("Conclude") &&  !key.startsWith("ELSE") &&  !key.startsWith("ENDIF")
			   && !key.equals("AND")){
		   String cn = getConcept();
		   
		   if(dbAccessRequired){
			   w.append("private ArdenValue " + key + "(){\n");
			   w.append("\tConcept concept;\n");
		       
			   w.append("\tconcept = Context.getConceptService().getConceptByName(\"" + cn.trim() + "\");\n");
			   if(readType.equals("last")){
				   if(hasWhere){
					   w.append("\treturn dataSource.eval(patient, ardenClause.concept(concept).last(" + howMany + ")." + whereType + "()." + durationType + "()." + durationOp + "(" + durationVal + "));\n");
				    }
				   else {
					   w.append("\treturn dataSource.eval(patient, ardenClause.concept(concept).last(" + howMany + "));\n");
				   }
			   }
			   else { 
				   w.append("\treturn dataSource.eval(patient, ardenClause.concept(concept));\n");
			   }
			   w.append("}\n\n");
			   
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
	
	public String getAnswer() {
		String retVal = "";
		if(answerInt != null)
			retVal = Integer.toString(answerInt);
		else
			retVal = answerStr;
		return retVal;
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
		if (compOp != null){
			   switch(compOp) {
			   		case EQUALS:
			   		{switch(compOpType){
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
			   			
			   		}
			   		break;
			   		case GTE:
			   		{switch(compOpType){
		  			case 3: // boolean
		  				
		  				break;
		  			case 2: // integer
		  				retStr += "\tif (val.getValueNumeric() >= " + Integer.toString(answerInt) ;
		  				break;
		  			case 1: // String
		  				
		  				break;
				   }
		   			
			   		}
			   		break;
			   		default:
			   			break;
			   	}
			   
			   }  
			
		   
		  
		   return retStr;
	}
}
