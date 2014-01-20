<%@page isErrorPage="true" %>
<%@ page import="org.openmrs.web.WebUtil" %>
<%@page import="org.openmrs.web.WebConstants"%>
<%@ page import="org.openmrs.api.context.UserContext" %>
<%@ page import="org.openmrs.util.OpenmrsConstants" %>
<%@page import="org.openmrs.api.APIAuthenticationException"%>
<%@page import="org.openmrs.api.context.ContextAuthenticationException"%>
<%@ page import="org.springframework.transaction.UnexpectedRollbackException" %>
<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/headerMinimal.jsp" %>

&nbsp;
<%@page import="org.openmrs.util.OpenmrsUtil"%>
<%@page import="org.openmrs.api.context.Context"%>
<%@page import="org.openmrs.module.ModuleFactory"%>
<%@page import="org.openmrs.module.Module"%>
<%@page import="org.openmrs.ImplementationId" %>

<br />

<h2><openmrs:message code="uncaughtException.title" /></h2>

<script>
	function showOrHide() {
		var link = document.getElementById("toggleLink");
		var trace = document.getElementById("stackTrace");
		if (link.innerHTML == "<openmrs:message code='uncaughtException.showStackTrace'/>") {
			link.innerHTML = "<openmrs:message code='uncaughtException.hideStackTrace'/>";
			trace.style.display = "block";
		}
		else {
			link.innerHTML = "<openmrs:message code='uncaughtException.showStackTrace'/>";
			trace.style.display = "none";
		}
	}

    function submitErrorReport() {
        var issue_subject = document.getElementById("issue_subject").value;
        var submitter_name = document.getElementById("submitter_name").value;
        var submitter_email = document.getElementById("submitter_email").value;
        var recent_steps = document.getElementById("recent_steps").value;

        if(issue_subject === ""){
            alert("<openmrs:message code='uncaughtException.subject.mandatory'/>");
            return false;
        }

        //Assign subject to errorMessageElement since that is the field that JIRA picks up for Subject & Summary
        document.getElementById("errorMessageElement").value = issue_subject;

        //Construct a bulk string with remaining user data
        var user_inputted_data = "";
        user_inputted_data += "Name: "+ submitter_name + "\n";
        user_inputted_data += "Email: "+ submitter_email + "\n";
        user_inputted_data += "Recent Steps:"+ "\n"+ recent_steps + "\n";

        //Attach all user data to the start of the stack-trace so that it shows up in Description Section of JIRA
        var stackTraceElement = document.getElementById("stackTraceElement");
        var fullDescription = user_inputted_data + "\n\n"+ stackTraceElement.value;
        stackTraceElement.value = fullDescription;

        document.forms["errorSubmitForm"].submit();

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
%>

<%@ include file="/WEB-INF/view/authorizationHandlerInclude.jsp" %>

<%       
		out.println("<b>" + exception.getClass().getName() + "</b>");
		if (exception.getMessage() != null)
			out.println("<pre id='exceptionMessage'>" + WebUtil.escapeHTML(exception.getMessage()) + "</pre>");
		
		if (UnexpectedRollbackException.class.equals(exception.getClass())) {
			out.println("<br/><b><openmrs:message code='uncaughtException.possibleCause' /></b><openmrs:message code='uncaughtException.programmerError'/><br/>");
		}
	}
%>
	
	<br /><br />
	<openmrs:message code="uncaughtException.help.text1" /><a href="<%= request.getContextPath() %>/help.htm"><openmrs:message code="uncaughtException.help.text2" /></a><br />
	<openmrs:message code="uncaughtException.contact" />
	
	<br /><br />
	
	<a href="#" onclick="showOrHide()" id="toggleLink" style="font-size: 12px;"><openmrs:message code="uncaughtException.showStackTrace" /></a>
	<br />
	<div id="stackTrace">
	<%
	// check to see if the current user is authenticated
	// this logic copied from the OpenmrsFilter because this
	// page isn't passed through that filter like all other pages	
	UserContext userContext = (UserContext) session.getAttribute(WebConstants.OPENMRS_USER_CONTEXT_HTTPSESSION_ATTR);
	if (exception != null) {
		if (userContext == null || userContext.getAuthenticatedUser() == null) {
			out.println("<openmrs:message code='uncaughtException.logged.out'/>");
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
            ImplementationId id = Context.getAdministrationService().getImplementationId();
            String implementationId = ""; 
            if (id != null) {
            	implementationId = id.getImplementationId();
            	implementationId += " = " + id.getName();
            }
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
    	out.println("<br><openmrs:message code='uncaughtException.noErrorInfo'/>");
	}
	
	// Display current version
	out.println("<br/><br/>OpenMRS<openmrs:message code='general.version'/>" + OpenmrsConstants.OPENMRS_VERSION);
	    
} catch (Exception ex) { 
	ex.printStackTrace(new java.io.PrintWriter(out));
}
%>
	</div> <!-- close stack trace box -->

<br/>
<openmrs:extensionPoint pointId="org.openmrs.uncaughtException" type="html" />


<br/>
 <b> <openmrs:message code="uncaughtException.bugFound" /></b>
<br/> <br/>
<table bgcolor="#fafad2" width="70%">
    <tr>
        <td width="10%" align="right"> <openmrs:message code="uncaughtException.subject" />  </td>
        <td><input type="text" id="issue_subject" size="40" /> </td>
    </tr>
    <tr>
        <td align="right"><openmrs:message code="uncaughtException.name" /></td>
        <td><input type="text" id="submitter_name" size="40"/> </td>
    </tr>
    <tr>
        <td align="right"><openmrs:message code="uncaughtException.email" /></td>
        <td><input type="text" id="submitter_email" size="40"/></td>
    </tr>
    <tr>
        <td align="right" valign="top"><openmrs:message code="uncaughtException.doing" /></td>
        <td><textarea rows="10" cols="80" id="recent_steps"></textarea> </td>
    </tr>

</table>

<br/> <br/>

<div>
<openmrs:message code="uncaughtException.additionalInfo" />
<ul>
<li><openmrs:message code="uncaughtException.additionalInfo.msg" /></li>
<li><openmrs:message code="uncaughtException.additionalInfo.version" /></li>
<li><openmrs:message code="uncaughtException.additionalInfo.appNameAndVersion" /></li>
<li><openmrs:message code="uncaughtException.additionalInfo.username" /></li>
<li><openmrs:message code="uncaughtException.additionalInfo.implementationId" /></li>
<li><openmrs:message code="uncaughtException.additionalInfo.modules" /></li>
</ul>
</div>

<div>

<form action="${reportBugUrl}" target="_blank" method="POST" name="errorSubmitForm">
	<input type="hidden" name="openmrs_version" value="${openmrs_version}" />
	<input type="hidden" name="server_info" value="${server_info}" />
	<input type="hidden" name="username" value="${username}" />
	<input type="hidden" name="implementationId" value="${implementationId}" />
	<input type="hidden" name="startedModules" value="${startedModules}" />
	<input type="hidden" name="errorMessage" id="errorMessageElement" value="${errorMessage}" />
	<input type="hidden" name="stackTrace" id="stackTraceElement" value="${stackTrace}" />

    <br/>

    <input type="button" onclick="javascript:submitErrorReport()" value="Report Problem">
</form>

</div>
	


<%@ include file="/WEB-INF/template/footerMinimal.jsp" %>
