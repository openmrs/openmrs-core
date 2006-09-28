package org.openmrs.arden;

import org.openmrs.api.context.*;
import org.openmrs.Patient;

public interface ArdenRule {

	public DSSObject evaluate();
	public ArdenRule getChildren();
	public ArdenRule getInstance();
}
