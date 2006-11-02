package org.openmrs.arden;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.Locale;
import java.util.Set;
import java.util.List;
import java.util.Map.Entry;

import org.openmrs.Obs;
import org.openmrs.Patient;

public class ArdenValue {
	private Vector<Obs> obs ;
//	private boolean concludeVal;
//	private Vector<String> printStr;
	private Patient patient;
	private Locale locale;
	
	//default constructor
	public ArdenValue(){
		obs = new Vector<Obs>();
	}
	
	public ArdenValue(Patient p, Locale l)
	{
		obs = new Vector<Obs>();
		patient = p;
		locale = l;
	}
	
	public void addObs (List<Obs> o){
		obs.addAll(o);
	}
	
	/**
	 * @return Returns the valueNumeric. Returning the last elements value for now // TODO
	 */
	public Double getValueNumeric() {
		if(!obs.isEmpty())
			return obs.lastElement().getValueNumeric();
		else
			return null;
	}

	/**
	 * This converts the value_numeric to a value_boolean, essentially
	 * @return Boolean of the obs value
	 */
	public Boolean getValueAsBoolean() {
		return (getValueNumeric() == null ? false : getValueNumeric() != 0);
	}
	
	/**
	 * @return Returns the valueText. Returning the last elements value for now // TODO
	 */
	public String getValueText() {
		if(!obs.isEmpty())
			return obs.lastElement().getValueText();
		else
			return null;
	}
	
	public Integer getValueCoded() {
		if(!obs.isEmpty())
			return obs.lastElement().getValueCoded().getConceptId();
		else
			return null;
	}
	
	public Patient getPatient() {
		return patient;
	}
	
	public void PrintObsMap(){
	 Iterator<Obs> iterator = obs.iterator();
		 Obs obs;
		 
		 while(iterator.hasNext()) {
			obs = iterator.next();
			System.out.println(obs.getValueAsString(locale)+ "---" + obs.getDateCreated());
		    
		 }
		
	}
}
