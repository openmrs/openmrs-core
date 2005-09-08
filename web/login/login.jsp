<%@ include file="/WEB-INF/template/include.jsp" %>
<html>
<head>
<title>OpenMRS Login</title>
<link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/openmrs.css"></link>
<style type="text/css">
<!--
#errorMessage {
	color: red;
	font-weight: bold;
	border: thin red solid;
	width: 400px;
	padding: 0.2em;
}
-->
</style>	
</head>
<body>

<center>

<img src="/openmrs/images/openmrs_logo.gif" alt="OpenMRS Logo" /><br />
&nbsp;<br />
<img src="/openmrs/images/gradient_bar.gif" />

<p>&nbsp;</p>

<%
/* Display error message if present */
String msg = request.getParameter("msg");
if (msg != null) {
	out.println("<p id=\"errorMessage\">" + msg + "</p>");
}
%>

<form method="POST" action="loginServlet">
<table border="0" width="400">
	<tr>
		<td align="right">Username</td>
		<td><input name="username" type="text" size="15" /></td>
	</tr>
	<tr>
		<td align="right">Password</td>
		<td><input name="password" type="password" size="15" /></td>
	</tr>
	<tr>
		<td></td>
		<td><input type="submit" value="Login" /></td>
	</tr>
</table>

</center>

</form>