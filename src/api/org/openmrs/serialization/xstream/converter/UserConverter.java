package org.openmrs.serialization.xstream.converter;

import org.openmrs.User;
import org.openmrs.api.context.Context;

import com.thoughtworks.xstream.converters.SingleValueConverter;

public class UserConverter implements SingleValueConverter {
	
	public boolean canConvert(Class c) {
		return User.class.isAssignableFrom(c);
	}
	
	public Object fromString(String s) {
		return Context.getUserService().getUserByUsername(s);
	}
	
	public String toString(Object o) {
		return ((User) o).getUsername();
	}
	
}
