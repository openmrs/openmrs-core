package org.openmrs.arden;

public interface ArdenRule {

	public boolean evaluate();
	public ArdenRule getChildren();
	public String action();
}
