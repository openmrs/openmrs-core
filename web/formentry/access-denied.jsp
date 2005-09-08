<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.context.ContextFactory" %>
<html>
<head>
<title>FormEntry</title>
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

<h1>Access Denied</h1>

<p>
You do not have access rights to FormEntry.
</p>

<p>
<a href="../login/login.jsp">Click here</a> to login as a different user.
</p>

</body>
</html>