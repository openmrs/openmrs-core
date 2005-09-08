<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:require privilege="FormEntry User" otherwise="access-denied.html" />
<html>
<head>
<title>FormEntry</title>
<openmrs:css />
</head>
<body>
<%@ include file="/WEB-INF/template/banner.jsp" %>

<center>

<h2>FormEntry</h2>

<form method="POST">
	<table border="0" width="400">
		<tr>
			<td colspan="2">
				<em>Search by <b>name</b> or <b>identifier</b></em>
			</td>
		</tr>
		<tr>
			<td></td>
			<td>
				<spring:bind path="command.query">
					<input name="query" type="text" size="30"
						value="<c:out value="${status.value}" />"/>
					<spring:hasBindErrors name="query">
						<font color="red">
							<br/><c:out value="${status.errorMessage}" />
						</font>
					</spring:hasBindErrors>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td></td>
			<td><input type="submit" value="Search" /></td>
		</tr>
	</table>
</form>

<form method="POST" action="newPatient.jsp">
	<table border="0" width="400">
		<tr>
			<td>
				<em>Create a <b>new</b> patient</em>
			</td>
		</tr>
		<tr>
			<td><input type="submit" value="New Patient" /></td>
		</tr>
	</table>
</form>

</center>

</body>
</html>