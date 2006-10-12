package org.openmrs.arden;


public interface ArdenRule {

	public boolean evaluate();
	public String doAction();
	public ArdenRule getChildren();
	public ArdenRule getInstance();
	public void printDebug();
}
