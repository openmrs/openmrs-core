package org.openmrs.reporting;

import java.beans.ExceptionListener;

public class ReportObjectWrapperExceptionListener implements ExceptionListener {

	public ReportObjectWrapperExceptionListener() {
		// do nothing
	}
	
	public void exceptionThrown(Exception e) {
		//throw new APIException("Exception thrown while converting ReportObject into XML");
	}

}
