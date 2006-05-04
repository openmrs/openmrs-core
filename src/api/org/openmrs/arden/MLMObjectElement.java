package org.openmrs.arden;


import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Locale;
import java.util.Iterator;
import org.openmrs.Concept;
import org.openmrs.ConceptWord;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.Patient;






public class MLMObjectElement implements ArdenBaseTreeParserTokenTypes {

	private boolean dbAccessRequired;
	private String conceptName;
	private String type;    // Exist, Last, First etc
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
	
	public MLMObjectElement(String s, String t, String d) {
		conceptName = s;
		isObsAvailable = false;
		type = t;
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
		int index;
		Concept concept;
		Set <Obs> MyObs ;
		Obs obs;
		List <Concept>  concepts;
		
		
//		 TODO: Need a better method to find a concept
		
		index = conceptName.indexOf("from");	// First substring
		cn = conceptName.substring(1,index);
		concepts = cs.getConceptByName(cn);  
		if (!concepts.isEmpty()) {
			    concept = concepts.get(0); 
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
}
