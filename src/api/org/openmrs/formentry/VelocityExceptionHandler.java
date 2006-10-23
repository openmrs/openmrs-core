package org.openmrs.formentry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.event.MethodExceptionEventHandler;

public class VelocityExceptionHandler implements MethodExceptionEventHandler {

	private Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * When a user-supplied method throws an exception, the MethodExceptionEventHandler 
	 * is invoked with the Class, method name and thrown Exception. The handler can 
	 * either return a valid Object to be used as the return value of the method call, 
	 * or throw the passed-in or new Exception, which will be wrapped and propogated to 
	 * the user as a MethodInvocationException
	 * 
	 * @see org.apache.velocity.app.event.MethodExceptionEventHandler#methodException(java.lang.Class, java.lang.String, java.lang.Exception)
	 */
	public Object methodException(Class claz, String method, Exception e) throws Exception {
		
		log.debug("Claz: " + claz.getName() + " method: " + method, e);
		
		// if formatting a date (and probably getting an "IllegalArguementException")
		if ("format".equals(method))
			return null;
		
		// keep the default behaviour
		throw e;
	}

}
