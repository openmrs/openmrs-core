package org.openmrs.arden;


public interface ArdenRule {

	public DSSObject evaluate();
	public ArdenRule getChildren();
	public ArdenRule getInstance();
}
