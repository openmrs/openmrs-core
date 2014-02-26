<%@ page isErrorPage="true" import="java.io.*" %>

<%@ taglib uri="/openmrs" prefix="openmrs" %>
<%-- 
	Exceptions thrown from within jsps e.g those with scripts calling code that requires authentication, are
	forwarded here so we need to be able to handle them too, but by the time we are on this JSP, the actual  
	thrown exception has been wrapped into a javax.servlet.jsp.JspException, therefore we need to check the 
	root cause exception to see if it is an authentication related exception and handle it appropriately.
--%>
<%
if (exception.getCause() != null && (ContextAuthenticationException.class.equals(exception.getCause().getClass())
		        || APIAuthenticationException.class.equals(exception.getCause().getClass()))) {
	//convert it back to the actual exception that was thrown
	exception = exception.getCause();
%>

<%@ include file="/WEB-INF/view/authorizationHandlerInclude.jsp" %>

<%
}else{
%>

<%-- Otherwise the Exception Handler retains control --%>
<font color="red">
<h2>An error has occurred!</h2>
The following error happened somewhere on this page:<br/>
<%= exception.toString() %>

<br/><br/>
(The full error stack trace output is in the source of this page.)
</font>

<openmrs:extensionPoint pointId="org.openmrs.errorHandler" type="html" />
<%
org.apache.commons.logging.LogFactory.getLog(getClass()).error("Error on page " + request.getRequestURI(), exception);

org.openmrs.api.context.Context.openSession();

if (org.openmrs.api.context.Context.isAuthenticated() == false) {
	out.println("<!-- There is no stack trace here because you are not authenticated -->");
}
else {
	out.println("<!--");
	StringWriter sw = new StringWriter();
	PrintWriter pw = new PrintWriter(sw);
	exception.printStackTrace(pw);
	out.print(sw);
	sw.close();
	pw.close();
	out.println("-->");
}
org.openmrs.api.context.Context.closeSession();

}//end else%>
