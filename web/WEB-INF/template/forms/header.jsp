<!DOCTYPE html PUBLIC “-//W3C//DTD XHTML 1.0 Strict//EN
  http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd>

<html>
	<head>
		<link href="<%= request.getContextPath() %>/openmrs.css" type="text/css" rel="stylesheet" />
		<link href="/WEB-INF/template/forms/forms.css" type="text/css" rel="stylesheet" />
		<script language="JavaScript" src="/WEB-INF/template/forms/forms.js"></script>
	</head>

<body>
	<div id="userBar">
		<openmrs:isAuthenticated converse="false">
			Currently logged in as ${authenticatedUser} | <a href='<%= request.getContextPath() %>/logout'>Log out</a>
		</openmrs:isAuthenticated>
		<openmrs:isAuthenticated converse="true">
			Not logged in | <a href='<%= request.getContextPath() %>/login.jsp'>Log in</a>
		</openmrs:isAuthenticated>
		| <a href='<%= request.getContextPath() %>/help.jsp'>Help</a>
	</div>

	<%
		if (session.getAttribute(Constants.OPENMRS_MSG_ATTR) != null) {
			out.print("<div id='openmrs_msg'>");
			out.print(session.getAttribute(Constants.OPENMRS_MSG_ATTR));
			out.print("</div>");
			session.removeAttribute(Constants.OPENMRS_MSG_ATTR);
		}
		if (session.getAttribute(Constants.OPENMRS_ERROR_ATTR) != null) {
			out.print("<div id='openmrs_error'>");
			out.print(session.getAttribute(Constants.OPENMRS_ERROR_ATTR));
			out.print("</div>");
			session.removeAttribute(Constants.OPENMRS_ERROR_ATTR);
		}
	%>

	<br />
				