package org.openmrs.arden;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.Locale;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import org.openmrs.ConceptWord;
import org.openmrs.api.context.Context;
import org.openmrs.Patient;
import org.openmrs.arden.*;


public class MLMObject {
	
	private HashMap<String, MLMObjectElement> conceptMap ;
	private String ConceptVar;
	private boolean IsVarAdded;
	private Context context;
	private Locale locale;
	private Patient patient;
	private LinkedList<String> ifList;
	

//	private Iterator<String> iter; 
	
	// default constructor
	public MLMObject(){
		conceptMap = new HashMap <String, MLMObjectElement>();
		IsVarAdded = false;
	}
	
	public MLMObject(Context c, Locale l, Patient p)
	{
		conceptMap = new HashMap <String, MLMObjectElement>();
		IsVarAdded = false;
		context = c;
		locale = l;
		patient = p;
		ifList = new LinkedList <String>();
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
	
	public void PrintConceptMap()
	{
		System.out.println("Concepts are - ");
		Set<String> keys = conceptMap.keySet();
		for(String key : keys) {
		     System.out.println(key);
		}
	     Collection<MLMObjectElement> collection = conceptMap.values();
	     for(MLMObjectElement mo : collection) {
	       System.out.println(mo.getConceptName() + " = " + mo.getObsVal(locale) + " Answer = " + mo.getAnswer() + " Operator = " + mo.getCompOp() );
	       
	     }
	}
	
	public void PrintEvaluateList(){
		System.out.println("\n Evaluate order list is  - ");
		ListIterator<String> thisList = ifList.listIterator(0);
		while (thisList.hasNext()){
		     System.out.println(thisList.next());
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
	public void InitForIf() {
		ResetConceptVar();
	}
	public boolean RetrieveConcept(String key) {
		
		//TODO check to see if user authenticated
		boolean retVal = false;
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null){
			mObjElem.setServicesContext(context.getConceptService(), context.getObsService());
			mObjElem.getConceptForPatient(locale, patient);
			retVal = mObjElem.evaluateEquals(true);
		}
		return retVal;
	}
	
	public boolean EvaluateConcept(String key, boolean val) {
		boolean retVal = false;
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null){
			retVal = mObjElem.evaluateEquals(val);
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
		ifList.add(key);
		SetConceptVar(key);
	}
	
	public void SetCompOperator(String op, String key) {
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
	public void SetBooleanVal (boolean val){
		
	}
}
