<%@ page isErrorPage="true" import="java.io.*" %>

<%-- Exception Handler --%>
<font color="red">
<h2>An error has occurred!</h2>
The following error happened somewhere on this page:<br/>
<%= exception.toString() %>

<br/><br/>
(The full error stack trace output is in the source of this page.)
</font>

<%
org.apache.commons.logging.LogFactory.getLog(getClass()).error("Error on page " + request.getRequestURI(), exception);

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
%>
