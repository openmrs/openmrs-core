package org.openmrs.arden;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;

import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;

public class DSSObject {
	private HashMap<String, Obs> obsMap ;
	private boolean concludeVal;
	private String printStr;
	private Context context;
	private Locale locale;
	private Patient patient;
	
	//default constructor
	public DSSObject(){
		obsMap = new HashMap <String, Obs>();
		concludeVal = false;
		
	}
	
	public DSSObject(Context c, Locale l, Patient p)
	{
		obsMap = new HashMap <String, Obs>();
		context = c;
		locale = l;
		patient = p;
	}
	
	public void addObs (String s, Obs obs){
		if(!obsMap.containsKey(s)) {
			obsMap.put(s, obs);
		}
	}
	
	
	// Accessors for properties
	/**
	 * @return Returns the concludeVal.
	 */
	public boolean getConcludeVal() {
		return concludeVal;
	}
	
	public void setConcludeVal(boolean val) {
		concludeVal = val;
	}
	
	/**
	 * @return Returns the printStr.
	 */
	public String getPrintString(){
		return printStr;
	}
	
	public void setPrintString(String str) {
		printStr = str;
	}
	
	/**
	 * @return Returns the locale.
	 */
	public Locale getLocale() {
		return locale;
	}
	
	/**
	 * @return Returns the context.
	 */
	public Context getContext() {
		return context;
	}
	
	public Patient getPatient() {
		return patient;
	}
	
	public void PrintObsMap()
	{
		
		System.out.println("\nDecision Analysis:\n________________________");
	     Collection<Obs> collection = obsMap.values();
	     for(Obs mo : collection) {
	       System.out.println(mo.getConcept().getName(locale) + " = " + mo.getValueAsString(locale)
	    		   + " on date: " +  mo.getDateCreated().toString());
	    System.out.println("__________________________________");
		    
	       
	     }
	}
}
