<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Patients" otherwise="/login.htm" redirect="/admin/patients/patientIdentifierType.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="PatientIdentifierType.title"/></h2>

<form method="post">
<table>
	<tr>
		<td><spring:message code="general.name"/></td>
		<td>
			<spring:bind path="patientIdentifierType.name">
				<input type="text" name="name" value="${status.value}" size="35" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.description"/></td>
		<td>
			<spring:bind path="patientIdentifierType.description">
				<textarea name="description" rows="3" cols="40">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<c:if test="${patientIdentifierType.patientIdentifierTypeId != null}">
		<tr>
			<td><spring:message code="general.creator"/></td>
			<td>${patientIdentifierType.creator}</td>
		</tr>
		<tr>
			<td><spring:message code="general.dateCreated"/></td>
			<td>${patientIdentifierType.dateCreated}</td>
		</tr>
	</c:if>
</table>
<input type="hidden" name="patientIdentifierTypeId:int" value="${patientIdentifierType.patientIdentifierTypeId}">
<br />
<input type="submit" value="<spring:message code="PatientIdentifierType.save"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>