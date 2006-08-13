package org.openmrs.web.taglib.fieldgen;

import org.openmrs.User;


public class UserHandler extends AbstractFieldGenHandler implements FieldGenHandler {

	private String defaultUrl = "user.field";
	
	public void run() {
		htmlInclude("/scripts/dojoConfig.js");
		htmlInclude("/scripts/dojo/dojo.js");
		htmlInclude("/scripts/dojoUserSearchIncludes.js");
		
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
