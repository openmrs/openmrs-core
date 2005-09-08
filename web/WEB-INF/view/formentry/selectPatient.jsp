<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.context.ContextFactory" %>
<openmrs:require privilege="FormEntry User" otherwise="access-denied.html" />
<html>
<head>
<title>FormEntry</title>
<openmrs:css />
</head>
<body>
<%@ include file="/WEB-INF/template/banner.jsp" %>

<center>

<h2>Select a patient</h2>

<form method="POST">
	<table border="0" width="400">
		<tr>
			<td>

got here

			</td>
		</tr>
	</table>
</form>

</center>

</body>
</html>