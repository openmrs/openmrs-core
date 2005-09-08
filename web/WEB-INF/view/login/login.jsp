<%@ include file="/WEB-INF/template/include.jsp" %>
<html>
<head>
<title>OpenMRS</title>
<openmrs:css />
</head>
<body>
<%@ include file="/WEB-INF/template/banner.jsp" %>

&nbsp;<br />

<center>

<form method="POST">
	<table border="0" width="400">
		<tr>
			<td align="right" valign="top">Username:</td>
			<td valign="top">
				<spring:bind path="credentials.username">
					<input name="username" type="text" size="30" />
					<spring:hasBindErrors name="credentials">
						<font color="red">
							<br /><c:out value="${status.errorMessage}" />
						</font>
					</spring:hasBindErrors>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td align="right" valign="top">Password:</td>
			<td valign="top">
				<spring:bind path="credentials.password">
					<input name="password" type="password" size="30" />
					<spring:hasBindErrors name="credentials">
						<font color="red">
							<br /><c:out value="${status.errorMessage}" />
						</font>
					</spring:hasBindErrors>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td></td>
			<td>
				<input type="submit" value="Login" />
			</td>
		</tr>
	</table>
</form>

</center>

</body>
</html>