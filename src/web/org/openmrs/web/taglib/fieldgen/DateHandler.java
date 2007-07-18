package org.openmrs.web.taglib.fieldgen;

public class DateHandler extends AbstractFieldGenHandler implements FieldGenHandler {

	private String defaultUrl = "date.field";
	
	public void run() {
		setUrl(defaultUrl);
		
		String needScript = "true";
		
		if ( getRequest().getAttribute("org.openmrs.widget.dateField.needScript") != null ) {
			needScript = "false";
		} else {
			getRequest().setAttribute("org.openmrs.widget.dateField.needScript", "false");
		}
		
		setParameter("needScript", needScript);
	}
}
