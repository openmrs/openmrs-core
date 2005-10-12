<%@ page import="org.openmrs.Patient" %>
<%
	if (patient == null) {
		Patient patient = new Patient();
%>

<c:forEach var="identifier" items="${patient.identifiers}">
	Identifier
		<input type="text" name="identifier" id="identifier" value="${identifier.identifier}" />
	Type
		<select name="identifierType">
			<openmrs:forEachRecord name="PatientIdentifierType">
				<option value="${record.patientIdentifierTypeId}">
					${record.name}
				</option>
			</openmrs:forEachRecord>
		</select>
	Location
		<select name="identifierLocation">
			<openmrs:forEachRecord name="Location">
				<option value="${record.locationId}">
					${record.name}
				</option>
			</openmrs:forEachRecord>
		</select>
