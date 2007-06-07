package org.openmrs.web.taglib.fieldgen;

import org.openmrs.Location;

public class LocationHandler extends AbstractFieldGenHandler implements FieldGenHandler {

	private String defaultUrl = "location.field";
	
	public void run() {
		setUrl(defaultUrl);

		if ( fieldGenTag != null ) {
			String initialValue = "";
			checkEmptyVal((Location)null);
			Location l = (Location)this.fieldGenTag.getVal();
			if ( l != null ) if ( l.getLocationId() != null ) initialValue = l.getLocationId().toString();
			String optionHeader = "";
			if ( this.fieldGenTag.getParameterMap() != null ) {
				optionHeader = (String) this.fieldGenTag.getParameterMap().get("optionHeader");
			}
			if ( optionHeader == null ) optionHeader = "";

			setParameter("initialValue", initialValue);
			setParameter("optionHeader", optionHeader);
		}
	}
}
