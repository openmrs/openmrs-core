<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.api.PatientService" %>
<%@ page import="org.openmrs.User" %>

<openmrs:require privilege="Form Entry" otherwise="/openmrs/login.jsp" />

<%
	Context context = (Context)session.getAttribute("__openmrs_context");
	PatientService patientService = context.getPatientService();
	pageContext.setAttribute("patientService", patientService);
%>

<%@ include file="/WEB-INF/template/header.jsp" %>

<h2>Form Entry</h2>

<b>Please Find a Patient</b>
<br><br>
<i>Enter known information</i>
<form method="get">
	<table>
		<tr>
			<td>Identifier</td>
			<td><input type="text" name="identifier" value="<request:attribute name="identifier" />"></td>
		</tr>
		<tr>
			<td>First Name</td>
			<td><input type="text" name="firstName" value="<request:attribute name="firstName" />"></td>
		</tr>
		<tr>
			<td>Last Name</td>
			<td><input type="text" name="lastName" value="<request:attribute name="lastName" />"></td>
		</tr>
	</table>
	<input type="submit" value="Search">
</form>

<request:existsAttribute name="identifier">
    <% pageContext.set("patients", (Object)patientService.getPatientsByIdentifier((String)request.getAttribute("identifer"))); %>
	<c:forEach var="patient" items="${patients}">
		<jsp:useBean id="patient" type="org.openmrs.Patient" scope="page"/>
		<tr>
			<td>
				<a href="editPatient.jsp?id=<c:out value="${patient.patientId}"/>">
					<c:out value="${patient.patientId}"/>
				</a>
			</td>
			<td><c:out value="${patient.gender}"/></td>
			<td><c:out value="${patient.race}"/></td>
			<td><c:out value="${patient.birthdate}"/></td>
		</tr>
	</c:forEach>
</request:existsAttribute>

<%@ include file="/WEB-INF/template/footer.jsp" %>