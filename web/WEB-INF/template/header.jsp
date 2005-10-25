<!DOCTYPE html PUBLIC “-//W3C//DTD XHTML 1.0 Strict//EN
  http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd>

<%@ page import="org.openmrs.web.Constants" %>

<html>
	<head>
		<link href="<%= request.getContextPath() %>/openmrs.css" type="text/css" rel="stylesheet" />
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

	<table border="0" width="100%" cellspacing="0" cellpadding="0">
		<col width="200" /><col width="5" /><col width="*" />
		<tr>
			<td colspan="3" valign="top">
				<%@ include file="/WEB-INF/template/banner.jsp" %>
			</td>
		</tr>
		<tr>
			<td valign="top">
				<%@ include file="/WEB-INF/template/gutter.jsp" %>
			</td>
			<td></td>
			<td valign="top" id="content">
			

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

					