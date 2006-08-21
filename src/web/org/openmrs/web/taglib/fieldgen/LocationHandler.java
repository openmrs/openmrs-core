package org.openmrs.web.taglib.fieldgen;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Location;
import org.openmrs.OrderType;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.openmrs.web.taglib.FieldGenTag;

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
