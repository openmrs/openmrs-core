<%@page isErrorPage="true" %>
<%@ page import="org.openmrs.web.WebUtil" %>
<%@ page import="org.openmrs.web.WebConstants" %>
<%@ page import="org.openmrs.api.context.UserContext" %>
<%@ page import="org.openmrs.util.OpenmrsConstants" %>
<%@ page import="org.openmrs.api.APIAuthenticationException" %>
<%@ page import="org.springframework.transaction.UnexpectedRollbackException" %>
<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/headerMinimal.jsp" %>
<openmrs:htmlInclude file="/scripts/jquery/jquery.client.js"/>

&nbsp;
<%@page import="org.openmrs.util.OpenmrsUtil"%>
<%@page import="org.openmrs.api.context.Context"%>
<%@page import="org.openmrs.module.ModuleFactory"%>
<%@page import="org.openmrs.module.Module"%><br />

<h2>An Internal Error has Occurred</h2>

<script>
	var MAXIMUM_LENGTH = 1434;
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
	
	/** 
	 * This function is called upon clicking the 'reportBug' button and trims short the
	 * stacktrace in case it is an old version of IE so that it doesn't exceed the 
	 * maximum supported length
	 */
	function preProcess(){
		var stackTrace = document.reportBugForm.stackTrace.value;		
		if(stackTrace){
			try{
				if($j.client.os == 'Windows' && $j.trim(stackTrace).length > MAXIMUM_LENGTH){
					//reduce the length the length y an extra 3 characters so that we add 3 dots
					//to indicate that there was extra text that was trimmed off
					stackTrace = $j.trim(stackTrace).substr(0, (MAXIMUM_LENGTH - 3));					
					document.reportBugForm.stackTrace.value = stackTrace+"...";					
				}
			}catch(e){
				//If there was an error, just trim the size to ensure that the submission is successful,
				//this can be helpful in case users tweaked their browser identity data wrongly.
				stackTrace = $j.trim(stackTrace).substr(0, (MAXIMUM_LENGTH - 3));
				document.reportBugForm.stackTrace.value = stackTrace+"...";
			}
		}
		return true;
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

            // Collect stack trace for reporting bug description
            StringBuilder description = new StringBuilder("Stack trace:\n");
			for (StackTraceElement element : elements) {
                description.append(element + "\n");
				if (element.getClassName().contains("openmrs"))
					out.println("<b>" + element + "</b><br/>");
				else
					out.println(element + "<br/>");
			}
			
			pageContext.setAttribute("reportBugUrl", Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_REPORT_BUG_URL)); 
            pageContext.setAttribute("stackTrace", OpenmrsUtil.shortenedStackTrace(description.toString()));
            pageContext.setAttribute("errorMessage", exception.toString());
            pageContext.setAttribute("openmrs_version", OpenmrsConstants.OPENMRS_VERSION);
            pageContext.setAttribute("server_info", session.getServletContext().getServerInfo());
            String username = Context.getAuthenticatedUser().getUsername();
            if (username == null || username.length() == 0)
            	username = Context.getAuthenticatedUser().getSystemId();
            pageContext.setAttribute("username", username);
            String implementationId = Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_IMPLEMENTATION_ID);
            pageContext.setAttribute("implementationId", (implementationId != null) ? implementationId : "");
            StringBuilder sb = new StringBuilder();
            boolean isFirst = true;
            for(Module module : ModuleFactory.getStartedModules()){
            	if(isFirst){
            		sb.append(module.getModuleId())
            		  .append(" v")
            		  .append(module.getVersion());
            		isFirst = false;
            	}
            	else
            		sb.append(", ")
            		  .append(module.getModuleId())
            		  .append(" v")
            		  .append(module.getVersion());
            }
            pageContext.setAttribute("startedModules", sb.toString());            
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

<br/>
<openmrs:extensionPoint pointId="org.openmrs.uncaughtException" type="html" />

<div>
The following data will be submitted with the report to enable the team to resolve the problem.
<ul>
<li>The error message and stack trace</li>
<li>OpenMRS version</li>
<li>Application server name and version</li>
<li>Username of the user currently logged in</li>
<li>The implementation id of this installation (if defined)</li>
<li>Names and versions of all installed modules</li>
</ul>
</div>

<div>

<form name="reportBugForm" action="${reportBugUrl}" target="_blank" method="POST" onsubmit="return preProcess()">
	<input type="hidden" name="openmrs_version" value="${openmrs_version}" />
	<input type="hidden" name="server_info" value="${server_info}" />
	<input type="hidden" name="username" value="${username}" />
	<input type="hidden" name="implementationId" value="${implementationId}" />
	<input type="hidden" name="startedModules" value="${startedModules}" />
	<input type="hidden" name="errorMessage" value="${errorMessage}" />
	<input type="hidden" name="stackTrace" value="${stackTrace}" />
	<br/>
	<input type="submit" value="Report Problem">
</form>

</div>
	


<%@ include file="/WEB-INF/template/footerMinimal.jsp" %>