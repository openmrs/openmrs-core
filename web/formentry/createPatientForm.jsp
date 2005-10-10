<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Users" otherwise="/openmrs/login.jsp" />

<%@ include file="/WEB-INF/template/header.jsp"%>

<h2>Create Patient</h2>

<br>
<form method="post" action="">
<table>
	<tr>
		<td>Identifier</td>
		<td><input type="text" name="identifier" id="identifier"></td>
		<td>
			<select name="identifierType">
				<openmrs:forEachType name="PatientIdentifierType">
					<option value="${type.patientIdentifierTypeId}">
						${type.name}
					</option>
				</openmrs:forEachType>
			</select>
		</td>
	</tr>
	<tr>
		<td>First Name</td>
		<td><input type="text" id="givenName"></td>
	</tr>
	<tr>
		<td>Last Name</td>
		<td><input type="text" id="familyName"></td>
	</tr>
</table>
<input type="submit" value="Search"></form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
