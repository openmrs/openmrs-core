package org.openmrs.api.impl;

import org.openmrs.GlobalProperty;
import org.openmrs.PersonName;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.util.OpenmrsConstants;

public class PersonNameGlobalPropertyListener implements GlobalPropertyListener {
	
	@Override
	public boolean supportsPropertyName(String propertyName) {
		// TODO Auto-generated method stub
		return OpenmrsConstants.GLOBAL_PROPERTY_LAYOUT_NAME_FORMAT.equals(propertyName);
	}
	
	@Override
	public void globalPropertyChanged(GlobalProperty newValue) {
		// TODO Auto-generated method stub
		PersonName.setFormat(newValue.getPropertyValue());
		
	}
	
	@Override
	public void globalPropertyDeleted(String propertyName) {
		// TODO Auto-generated method stub
		
	}
	
}
