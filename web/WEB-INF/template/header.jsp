<!DOCTYPE html PUBLIC “-//W3C//DTD XHTML 1.0 Strict//EN” “http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd”>

<html>
	<head>
	<link href="/openmrs/openmrs.css" type="text/css" rel="stylesheet" />
	</head>

<body>

	<div id="userBar">
		<openmrs:isAuthenticated>
			Currently logged in as ${authenticatedUser.username} | <a href='/openmrs/logout'>Log out</a>
		</openmrs:isAuthenticated>
		<openmrs:isAuthenticated converse="true">
			Not logged in | <a href='/openmrs/login.jsp'>Log in</a>
		</openmrs:isAuthenticated>
		| <a href='/openmrs/help.jsp'>Help</a>
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
			<td valign="top">

			<%
				if (session.getAttribute("openmrs_msg") != null) {
					out.print("<div id='openmrs_msg'>");
					out.print(session.getAttribute("openmrs_msg"));
					out.print("</div>");
					session.removeAttribute("openmrs_msg");
				}
				if (session.getAttribute("openmrs_error") != null) {
					out.print("<div id='openmrs_error'>");
					out.print(session.getAttribute("openmrs_error"));
					out.print("</div>");
					session.removeAttribute("openmrs_error");
				}
			%>
			