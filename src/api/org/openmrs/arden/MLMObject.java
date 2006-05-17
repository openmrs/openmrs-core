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


public class MLMObject {
	
	private HashMap<String, MLMObjectElement> conceptMap ;
	private String ConceptVar;
	private boolean IsVarAdded;
	private Context context;
	private Locale locale;
	private Patient patient;
	//private LinkedList<String> evaluateList;
	private LinkedList<MLMEvaluateElement> evaluateList;
	private HashMap<String, String> userVarMapFinal ;
	private String className;

//	private Iterator<String> iter; 
	
	// default constructor
	public MLMObject(){
		conceptMap = new HashMap <String, MLMObjectElement>();
		IsVarAdded = false;
		userVarMapFinal = new HashMap <String, String>();
	}
	
	public MLMObject(Context c, Locale l, Patient p)
	{
		conceptMap = new HashMap <String, MLMObjectElement>();
		IsVarAdded = false;
		context = c;
		locale = l;
		patient = p;
	//	evaluateList = new LinkedList <String>();
		evaluateList = new LinkedList <MLMEvaluateElement>();
		userVarMapFinal = new HashMap <String, String>();
	}

	public void SetContext(Context c) {
		context = c;
	}

	public void SetLocale(Locale l) {
		locale = l;
	}

	public void SetPatient(Patient p) {
		patient = p;
	}

	public void AddConcept(String s)
	{
		if(IsVarAdded == true && !conceptMap.containsKey(ConceptVar)) {
			conceptMap.put(ConceptVar, new MLMObjectElement(s, "", ""));
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
		IsVarAdded = false;
	}
	
	public void PrintConcept(String key)
	{
		System.out.println("__________________________________");
	     MLMObjectElement mo = conceptMap.get(key);
	     {
	       System.out.println(mo.getConceptName() + " = " + mo.getObsVal(locale) + 
	    		   "\n Answer = " + mo.getAnswer() + 
	    		   //"\n Operator = " + mo.getCompOp() +
	    		   "\n Conclude Val = " + mo.getConcludeVal() +
	    		   "\n User Vars = " + mo.getUserVarVal()
	       	);
	    System.out.println("__________________________________");
		    
	       
	     }
	}
	
	public void PrintConceptMap()
	{
	//	System.out.println("Concepts are - ");
	//	Set<String> keys = conceptMap.keySet();
	//	for(String key : keys) {
	//	     System.out.println(key);
	//	}
		System.out.println("__________________________________");
	     Collection<MLMObjectElement> collection = conceptMap.values();
	     for(MLMObjectElement mo : collection) {
	       System.out.println(mo.getConceptName() + " = " + mo.getObsVal(locale) + 
	    		   "\n Answer = " + mo.getAnswer() + 
	    		   //"\n Operator = " + mo.getCompOp() +
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
	
	public boolean Evaluate(){
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
	
	public void WriteAction(String str, Writer w) throws Exception {
		
		try{
			 w.write("public void initAction() {\n");
		     w.append("\t\tuserVarMap.put(\"ActionStr\", \"" +  str + "\");\n");
		     w.write("}\n\n");	// End of this function
		     w.flush();
		
			 w.write("public String action() {\n");
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
		     w.write("\t\t\tvariable = inStr.substring(startindex, endindex);\n");
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
		     w.write("\t\t\tvariable = inStr.substring(startindex, endindex);\n");
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
	public void WriteEvaluate(Writer w) throws Exception {
	
	try{
		String key;
		ListIterator<MLMEvaluateElement> thisList;
		thisList = evaluateList.listIterator(0);

		while (thisList.hasNext()){		// Writes individual evaluate functions
			Iterator iter = thisList.next().iterator();
			while (iter.hasNext()) {
			    key = (String) iter.next();	// else if
			    writeEvaluateConcept(key, w);
			}
		}
		
		 w.append("\n");
	     w.append("public boolean evaluate() {\n");
	     w.append("\tConcept concept;\n");
	     w.append("\tboolean retVal = false;\n");
	     w.append("\tObs obs;\n\n");
	     
	    thisList = evaluateList.listIterator(0); 
		while (thisList.hasNext()){		// Now write the big evaluate function
			Iterator iter = thisList.next().iterator();
			if(iter.hasNext()) {		// IF 
				key = (String) iter.next();
				if(key.equals("tmp_conclude")){
			    	w.append("\n //conclude here\n");
			    }
			    else if(key.equals("tmp_01")){
			    	w.append("\n\telse {");
			    }
			    else {
			    	w.append("\n\tif(evaluate_" + key + "()) {\n");
			    }
				writeActionConcept(key, w);
				w.append("\n\t}");
			}
			
			while (iter.hasNext()) {
			    key = (String) iter.next();	// else if
			    if(key.equals("tmp_conclude")){
			    	w.append("\n\t//conclude here");
			    }
			    else if(key.equals("tmp_01")){
			    	w.append("\n\telse {\n");
			    }
			    else {
			    	w.append("\n\telse if(evaluate_" + key + "()) {\n");
			    }
				writeActionConcept(key, w);
				w.append("\n\t}");
			}
		}
		
		
		
	//w.append("}\n");	// End of this function
		w.append("\n");
		}
		catch (Exception e) {
		      System.err.println("Write Evaluate: "+e);
		      e.printStackTrace();   // so we can get stack trace		
		    }
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
	
	private MLMObjectElement GetMLMObjectElement(String key) {
		if(conceptMap.containsKey(key)) {
			return conceptMap.get(key);
		}
		else {
			return null;
		}
				
	}
	public void InitEvaluateList() {
	//	ResetConceptVar();
		MLMEvaluateElement mEvalElem = new MLMEvaluateElement();
		evaluateList.add(mEvalElem);
		
	}
	public boolean RetrieveConcept(String key) {
		
		//TODO check to see if user authenticated
		boolean retVal = false;
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null ){
			mObjElem.setServicesContext(context.getConceptService(), context.getObsService());
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
	
	public boolean writeEvaluateConcept(String key, Writer w) throws Exception{
		boolean retVal = false;
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
		return retVal;
	}
	
	
	public boolean Evaluated(String key){
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
	}
		
	public void SetUserVarVal (String var, String val, String key) {
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null){
			mObjElem.addUserVarVal(var, val);
		}
	}
	
	public void SetDBAccess(boolean val, String key ) {
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
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
	
	public String getUserVarVal(String key) {
		String retVal = "";
		if(userVarMapFinal.containsKey(key)) {
			retVal = userVarMapFinal.get(key);
		}
		else if(key.equals("firstname")) {
			retVal = patient.getPatientName().getGivenName();
		}
		return retVal;
	}
	
	public void setClassName(String name) {
		className = name;
	}
	public String getClassName() {
		return className;
	}
}
