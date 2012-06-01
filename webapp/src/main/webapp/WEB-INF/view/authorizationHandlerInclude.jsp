<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="org.openmrs.web.WebConstants"%>
<%@page import="org.openmrs.api.APIAuthenticationException"%>
<%@page import="org.openmrs.api.context.ContextAuthenticationException"%>
<%@page import="org.apache.commons.logging.Log"%>
<%@page import="org.openmrs.api.context.Context"%>
<%@page import="org.apache.commons.logging.LogFactory"%>

<%
	if (ContextAuthenticationException.class.equals(exception.getClass())
	        || APIAuthenticationException.class.equals(exception.getClass())) {
		
		Log log = LogFactory.getLog(this.getClass().getName());
		if (Context.getAuthenticatedUser() != null) {
			log.error("Exception was thrown by user with id=" + Context.getAuthenticatedUser().getUserId(),
			    exception);
			
			session.setAttribute(WebConstants.FOUND_MISSING_PRIVILEGES, true);
			String errorCodeOrMsg = exception.getMessage();
			if (StringUtils.isBlank(errorCodeOrMsg))
				errorCodeOrMsg = "require.unauthorized";
			else
				session.setAttribute(WebConstants.UNCAUGHT_EXCEPTION_MESSAGE, errorCodeOrMsg);
			
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, errorCodeOrMsg);
			
			Object requestUrl = request.getAttribute("javax.servlet.error.request_uri");
			if (requestUrl != null) {
				String uri = (String) request.getAttribute("javax.servlet.error.request_uri");
				if (StringUtils.isNotBlank(uri))
					session.setAttribute(WebConstants.DENIED_PAGE, uri);
				
				if (request.getQueryString() != null)
					uri = uri + "?" + request.getQueryString();
				
				session.setAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR, uri);
			}
			if (request.getQueryString() != null) {

			}
			
			response.sendRedirect(request.getContextPath() + "/login.htm");
		} else {
			log.error("Exception was thrown by not authenticated user", exception);
			response.sendRedirect(request.getContextPath() + "/login.htm");
		}
	}
%>