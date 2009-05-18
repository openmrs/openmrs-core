package org.openmrs.serialization.xstream.converter;

import org.openmrs.Location;
import org.openmrs.api.context.Context;

import com.thoughtworks.xstream.converters.SingleValueConverter;

public class LocationConverter implements SingleValueConverter {
	
	public boolean canConvert(Class c) {
		return Location.class.isAssignableFrom(c);
	}
	
	public Object fromString(String s) {
		if (s != null) {
			return Context.getLocationService().getLocation(Integer.parseInt(s));
		}
		return null;
	}
	
	public String toString(Object o) {
		return ((Location) o).getLocationId().toString();
	}
	
}
