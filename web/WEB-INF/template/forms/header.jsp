<!DOCTYPE html PUBLIC “-//W3C//DTD XHTML 1.0 Strict//EN
  http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd>

<%@ page import="org.openmrs.web.Constants" %>

<html>
	<head>
		<link href="<%= request.getContextPath() %>/openmrs.css" type="text/css" rel="stylesheet" />
		<link href="taskpane.css" type="text/css" rel="stylesheet" />
		<script language="JavaScript" src="taskpane.js"></script>
	</head>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
	<div id="userBar">
		<openmrs:isAuthenticated converse="false">
			Currently logged in as ${authenticatedUser} | <a href='<%= request.getContextPath() %>/logout'>Log out</a>
		</openmrs:isAuthenticated>
		<openmrs:isAuthenticated converse="true">
			Not logged in | <a href='<%= request.getContextPath() %>/login.jsp'>Log in</a>
		</openmrs:isAuthenticated>
		| <a href='<%= request.getContextPath() %>/help.jsp'>Help</a>
	</div>

	<div id="content">
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
	<br />
				