package org.openmrs.arden;


import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;






public class MLMObjectElement implements ArdenBaseTreeParserTokenTypes {

	private boolean dbAccessRequired;
	private String conceptName;
	private String readType;    // Exist, Last, First etc
	private int howMany;	// how many to read
	private String duration; // TODO
	private Concept conceptObj;	
	private Obs obsObj;
	private boolean isObsAvailable;
	private ConceptService cs;
	private ObsService os;
	private boolean evaluated;
	private boolean isEvaluated;
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
		isObsAvailable = false;
		readType = t;
		howMany = n;
		duration = d;
		evaluated = false;
		isEvaluated = false;
		userVarMap = new HashMap <String, String>();
		dbAccessRequired = true;  // by default assume that we have to make an API call to get data
	}
	
	private void setObs(Obs o) {
		obsObj = o;
		isObsAvailable = true;
	}
	
	public void setServicesContext(ConceptService conceptService, ObsService obsService){
		cs = conceptService;
		os = obsService;	
	}
	
	private void setConcept(Concept c){
		conceptObj = c;
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
	
	public void addUserVarVal(String var, String val) {
		if(!userVarMap.containsKey(var)) {
			userVarMap.put(var, val);
		}
		else
		{
			//TODO either an error or overwrite previous one
		}
		
	}
	
	public boolean getConceptForPatient(Locale locale, Patient patient ) {
		
		boolean retVal = false;
		String  cn;
		int index, len;
		Concept concept;
		Set <Obs> MyObs ;
		Obs obs;
		
		
		
//		 TODO: Need a better method to find a concept
		
		index = conceptName.indexOf("from");	// First substring
		if(index != -1) {
			cn = conceptName.substring(1,index);
		}
		else {
//			cn = conceptName;
			len = conceptName.length();
			cn = conceptName.substring(1,len-1);

		}
		concept = cs.getConceptByName(cn);  
		//TODO: Check if concept populated
		{
			    setConcept(concept);
			    // Now get observations
			    MyObs = os.getObservations(patient, conceptObj);
				Iterator iter = MyObs.iterator();
				while(iter.hasNext())	{ // For now get the first
				  obs = (Obs) iter.next();
			 	  setObs(obs);		      
			      System.out.println(obsObj.getValueAsString(locale));
			      retVal = true;
				}
		}		
		return retVal;
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
  
   public String getObsVal(Locale locale){
	   String val = null;
	   if(isObsAvailable){
		   val = obsObj.getValueAsString(locale);
	   }
	   return val;
   }
   
   public boolean evaluate(){
	   boolean retVal = false;
	   if (isEvaluated){
	      retVal = evaluated;
	   }
	   else if(hasConclude) {
		   retVal = concludeVal;
	   }
	   else if (compOp != null){
	   switch(compOp) {
	   		case EQUALS:
	   		{switch(compOpType){
	   			case 3: // boolean
	   				retVal = evaluateEquals(answerBool);
	   				break;
	   			case 2: // integer
	   				retVal = evaluateEquals(answerInt);
	   				break;
	   			case 1: // String
	   				retVal = evaluateEquals(answerStr);
	   				break;
	   		}
	   			
	   		}
	   		break;
	   		case GTE:
	   		{switch(compOpType){
	   			case 3: // boolean
	   				retVal = evaluateEquals(answerBool); //TODO ERROR
	   				break;
	   			case 2: // integer
	   				retVal = evaluateGTE(answerInt);
	   				break;
	   			case 1: // String
	   				retVal = evaluateEquals(answerStr); //TODO 
	   				break;
		   	}
   			
	   		}
	   		break;
	   		default:
	   			break;
	   	}
	   
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
					w.append("\t\t//"+var+ " = \"" + val +"\"\n"); // write as comment
				//	w.append("\t\t\tif(!userVarMap.containsKey("+ var + ")) {\n\t\t\t\tuserVarMap.put(\"" + var + "\", \""+ val + "\");\n\t\t\t}\n");
				//	w.append("\t\t\telse {\n");
				//	w.append("\t\t\t\tuserVarMap.put(\"" + var + "\", \""+ val + "\");\n");
				//	w.append("\t\t\t}");
					w.append("\t\tuserVarMap.put(\"" + var + "\", \""+ val + "\");\n");
				//	w.append("\t\tdssObj.addObs(\"" + getConcept().trim() + "\", obs);\n");	// changed to have the key such as last_pb than BLOOD_LEAD_LEVEL as the key to the obsMap see below
					w.append("\t\tdssObj.addObs(\"" + key + "\", obs);\n");
				}
				retVal = true;
			}
			/* else */
			if(hasConclude)	{ // has conclude
				if(concludeVal == true) {
					w.append("\t\tretVal = true;\n");
					w.append("\t\tdssObj.setConcludeVal(true);\n");
					if(!key.startsWith("Conclude") &&  !key.startsWith("ELSE") &&  !key.startsWith("ENDIF")
							   && !key.equals("AND")){
						//w.append("\t\tdssObj.addObs(\"" + getConcept().trim() + "\", obs);\n"); // changed to have the key such as last_pb than BLOOD_LEAD_LEVEL as the key to the obsMap see below
						w.append("\t\tdssObj.addObs(\"" + key + "\", obs);\n");
					}
				}
				else if(concludeVal == false){
					w.append("\t\tretVal = false;\n");
					w.append("\t\tdssObj.setConcludeVal(false);\n");
					if(!key.startsWith("Conclude") &&  !key.startsWith("ELSE") &&  !key.startsWith("ENDIF")
							   && !key.equals("AND")){
						//w.append("\t\tdssObj.addObs(\"" + getConcept().trim() + "\", obs);\n"); // changed to have the key such as last_pb than BLOOD_LEAD_LEVEL as the key to the obsMap see below
						w.append("\t\tdssObj.addObs(\"" + key + "\", obs);\n");
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
			   w.append("private Obs " + key + "(){\n");
			   w.append("\tConcept concept;\n");
		       w.append("\tObs obs;\n\n");
			   
			   w.append("\tconcept = Context.getConceptService().getConceptByName(\"" + cn.trim() + "\");\n");
			   if(readType.equals("last")){
			   w.append("\tobs = dataSource.getLastPatientObsForConcept(concept, patient, " +  howMany + ");\n\n");
			   }
			   else { 
				   w.append("\tobs = dataSource.getPatientObsForConcept(concept, patient);\n\n");
			   }
			   w.append("\treturn obs;\n");
			   w.append("}\n\n");
			   
		   }  // end of DB access required
		   else {  // No DB access, simply conclude or else conclude
			   if(readType.equals("call")) {
				   w.append("private DSSObject " + "call_" +cn + "(){\n");
				   w.append("\tDSSObject dssObj;\n");
			       w.append("\tArdenRule mlm;\n\n");
				   
			       w.append("mlm = new " + cn + "(patient, dataSource);\n");
			       w.append("if(mlm != null) {\n\t\tdssObj = mlm.evaluate();\n\t\t return dssObj;\n}\nelse {return null;}");
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
   /*
   public boolean writeEvaluate(String key, Writer w) throws Exception{
	   boolean retVal = false;
	   if(!key.startsWith("Conclude") &&  !key.startsWith("ELSE") &&  !key.startsWith("ENDIF")
			   && !key.equals("AND")){
		   String cn = getConcept();
		   
		   w.append("private Obs " + key + "(){\n");
		  		
		   if(dbAccessRequired){
		   w.append("\tConcept concept;\n");
	       w.append("\tObs obs;\n\n");
		   
		   w.append("\tconcept = Context.getConceptService().getConceptByName(\"" + cn.trim() + "\");\n");
		   if(readType.equals("last")){
			   w.append("\tobs = dataSource.getLastPatientObsForConcept(concept, patient, " +  howMany + ");\n\n");
		   }
		   else { 
			   w.append("\tobs = dataSource.getPatientObsForConcept(concept, patient);\n\n");
		   }
		   w.append("\treturn obs;\n");
		   w.append("}\n\n");
		   
		   
		   }  // end of DB access required
		   else {  // No DB access, simply conclude or else conclude
			   	w.write("\t\tString val = userVarMap.get( \"" + key + "\");\n");
			   	w.write("\t\tif(val == \"false\") {retVal = false;}\n");
			   	w.write("\t\tif(val == \"true\") {retVal = true;}\n");
			   	
		   }
		 
	   }	   
	   return retVal;
   }
   */
   public boolean evaluateEquals(boolean RHS) {
	   boolean retVal = false;
	   
	   if(isObsAvailable && RHS == true){
		   retVal = true;
	   }
	   else if (isObsAvailable && RHS == false) {
	   	   retVal = true;
	   }
	   evaluated = retVal;
	   isEvaluated = true;
	   return retVal;
   }
   
   public boolean evaluateEquals(String RHS) {
	   boolean retVal = false;
	   
	   if(isObsAvailable){
		   String val = obsObj.getValueText();
		   if(val.equals(RHS)){
			   retVal = true;  
		   }
	   }
	   else {
	   	   retVal = false;
	   }
	   evaluated = retVal;
	   isEvaluated = true;
	   return retVal;
   }
	
   public boolean evaluateEquals(Integer RHS) {
	   boolean retVal = false;
	   
	   if(isObsAvailable){
		   double val = obsObj.getValueNumeric();
		   if(val == RHS){
			   retVal = true;  
		   }
	   }
	   else {
	   	   retVal = false;
	   }
	   evaluated = retVal;
	   isEvaluated = true;
	   return retVal;
   }
   
   public boolean evaluateGTE(Integer RHS) {
	   boolean retVal = false;
	   
	   if(isObsAvailable){
		   double val = obsObj.getValueNumeric();
		   if(val >= RHS){
			   retVal = true;  
		   }
	   }
	   else {
	   	   retVal = false;
	   }
	   evaluated = retVal;
	   isEvaluated = true;
	   return retVal;
   }
   public boolean getEvaluated(){
	      return evaluated;
   }

   public boolean isElementEvaluated(){
	   return isEvaluated;
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
	
	public Iterator <String> iterator(){
		Iterator iter;
		return iter = userVarMap.keySet().iterator();
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
		  				retStr += "\tif (obs.getValueAsBoolean() == " + Boolean.toString(answerBool) ;
		  				break;
		  			case 2: // integer
		  				retStr += "\tif (obs.getValueNumeric() == " + Integer.toString(answerInt) ;
		  				break;
		  			case 1: // String
		  				retStr += "\tif (obs.getValueText().equals(\"" + answerStr + "\")";
		  				break;
				   }
			   			
			   		}
			   		break;
			   		case GTE:
			   		{switch(compOpType){
		  			case 3: // boolean
		  				
		  				break;
		  			case 2: // integer
		  				retStr += "\tif (obs.getValueNumeric() >= " + Integer.toString(answerInt) ;
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
