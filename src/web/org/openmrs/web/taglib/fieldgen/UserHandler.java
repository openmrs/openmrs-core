package org.openmrs.web.taglib.fieldgen;

import org.openmrs.User;


public class UserHandler extends AbstractFieldGenHandler implements FieldGenHandler {

	private String defaultUrl = "user.field";
	
	public void run() {
		setUrl(defaultUrl);
		checkEmptyVal((User)null);
		String initialValue = "";
		User u = (User)this.fieldGenTag.getVal();
		if ( u != null ) {
			Integer userId = u.getUserId();
			initialValue = userId.toString();
		}
		setParameter("initialValue", initialValue);
	}
}
