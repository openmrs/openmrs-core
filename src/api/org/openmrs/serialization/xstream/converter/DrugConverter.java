package org.openmrs.serialization.xstream.converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Drug;
import org.openmrs.api.context.Context;

import com.thoughtworks.xstream.converters.SingleValueConverter;

public class DrugConverter implements SingleValueConverter {
	
	protected Log log = LogFactory.getLog(this.getClass());
	
	public boolean canConvert(Class c) {
		return Drug.class.isAssignableFrom(c);
	}
	
	public Object fromString(String s) {
		Drug drug = Context.getConceptService().getDrug(s);
		if (drug == null && s.length() > 0)
			log.error("Could not find drug: " + s);
		return drug;
	}
	
	public String toString(Object o) {
		return ((Drug) o).getName();
	}
	
}
