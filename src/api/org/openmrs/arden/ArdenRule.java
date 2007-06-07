package org.openmrs.arden;

import java.util.Set;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;

public interface ArdenRule {
/*
public abstract class ArdenRule {

	private Context context;
	private Patient patient;
	private ArdenDataSource dataSource;
	
	public ArdenRule() {
		
	}
	
	public ArdenRule(Context context, Patient patient, ArdenDataSource dataSource) {
		setContext(context);
		setPatient(patient);
		setDataSource(dataSource);
	}
	
	public void setContext(Context context) {
		this.context = context;
	}
	
	public Context getContext() {
		return context;
	}
	
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	public Patient getPatient() {
		return this.patient;
	}
	
	public void setDataSource(ArdenDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public ArdenDataSource getDataSource() {
		return dataSource;
	}
	
	public abstract ArdenValue evaluate();
	
	public abstract Set<ArdenRule> getChildren();
*/
	public boolean evaluate();
	public String doAction();
	public ArdenRule getChildren();
	public ArdenRule getInstance();
	public void printDebug();
}