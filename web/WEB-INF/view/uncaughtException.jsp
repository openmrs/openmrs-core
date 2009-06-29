<%@page isErrorPage="true" %>
<%@ page import="org.openmrs.web.WebUtil" %>
<%@ page import="org.openmrs.web.WebConstants" %>
<%@ page import="org.openmrs.api.context.UserContext" %>
<%@ page import="org.openmrs.util.OpenmrsConstants" %>
<%@ page import="org.openmrs.api.APIAuthenticationException" %>
<%@ page import="org.springframework.transaction.UnexpectedRollbackException" %>
<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/headerMinimal.jsp" %>

&nbsp;<br />

<h2>An Internal Error has Occurred</h2>

<script>
	function showOrHide() {
		var link = document.getElementById("toggleLink");
		var trace = document.getElementById("stackTrace");
		if (link.innerHTML == "Show stack trace") {
			link.innerHTML = "Hide stack trace";
			trace.style.display = "block";
		}
		else {
			link.innerHTML = "Show stack trace";
			trace.style.display = "none";
		}
	}
</script>	

<% 
	// MSR/ERROR Session attributes are removed after being displayed
	// If they weren't displayed/removed because of this error, remove them
	session.removeAttribute(WebConstants.OPENMRS_MSG_ATTR);
	session.removeAttribute(WebConstants.OPENMRS_ERROR_ATTR); 
	
try {
	// The Servlet spec guarantees this attribute will be available
	//Throwable exception = (Throwable) request.getAttribute("javax.servlet.error.exception"); 

	if (exception != null) {
		out.println("<b>" + exception.getClass().getName() + "</b>");
		if (exception.getMessage() != null)
			out.println("<pre id='exceptionMessage'>" + WebUtil.escapeHTML(exception.getMessage()) + "</pre>");
		
		if (UnexpectedRollbackException.class.equals(exception.getClass())) {
			out.println("<br/><b>Possible cause</b>: A programmer has made an error and forgotten to include a @Transaction(readOnly=true) annotation on a method.<br/>");
		}
	}
	%>
	
	<br /><br />
	Consult the <a href="<%= request.getContextPath() %>/help.htm">help document</a>. <br />
	Contact your friendly neighborhood administrator if it cannot be resolved.
	
	<br /><br />
	
	<a href="#" onclick="showOrHide()" id="toggleLink" style="font-size: 12px;">Show stack trace</a>
	<br />
	<div id="stackTrace">
	<%
	// check to see if the current user is authenticated
	// this logic copied from the OpenmrsFilter because this
	// page isn't passed through that filter like all other pages
	UserContext userContext = (UserContext) session.getAttribute(WebConstants.OPENMRS_USER_CONTEXT_HTTPSESSION_ATTR);
	if (exception != null) {
		if (exception instanceof APIAuthenticationException) {
			// If they are not authorized to use a function
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, exception.getMessage());
			String uri = (String)request.getAttribute("javax.servlet.error.request_uri");
			if (request.getQueryString() != null) {
				uri = uri + "?" + request.getQueryString();
			}
			session.setAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR, uri);
			response.sendRedirect(request.getContextPath() + "/login.htm");
		}
		else if (userContext == null || userContext.getAuthenticatedUser() == null) {
			out.println("You must be logged in to view the stack trace");
			// print the stack trace to the servlet container's error logs
			exception.printStackTrace();
		}
		else {
			java.lang.StackTraceElement[] elements;
			
			if (exception instanceof ServletException) {
				// It's a ServletException: we should extract the root cause
				ServletException sEx = (ServletException) exception;
				Throwable rootCause = sEx.getRootCause();
				if (rootCause == null)
					rootCause = sEx;
				out.println("<br/><br/>** Root cause is: "+ rootCause.getMessage());
				elements = rootCause.getStackTrace();
			}
			else {
				// It's not a ServletException, so we'll just show it
				elements = exception.getStackTrace(); 
			}
			for (StackTraceElement element : elements) {
				if (element.getClassName().contains("openmrs"))
					out.println("<b>" + element + "</b><br/>");
				else
					out.println(element + "<br/>");
			}
		}
	} 
	else  {
    	out.println("<br>No error information available");
	}
	
	// Display current version
	out.println("<br/><br/>OpenMRS Version: " + OpenmrsConstants.OPENMRS_VERSION);
	    
} catch (Exception ex) { 
	ex.printStackTrace(new java.io.PrintWriter(out));
}
%>
	</div> <!-- close stack trace box -->
	
<openmrs:extensionPoint pointId="org.openmrs.uncaughtException" type="html" />

<%@ include file="/WEB-INF/template/footerMinimal.jsp" %>