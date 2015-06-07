<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Identifier Types" otherwise="/login.htm" redirect="/admin/patients/patientIdentifierType.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="PatientIdentifierType.manage.title"/></h2>

<openmrs:globalProperty key="patientIdentifierTypes.locked" var="PatientIdentifierTypesLocked"/>
<c:if test="${PatientIdentifierTypesLocked != 'true'}"> <a href="patientIdentifierType.form" > <openmrs:message code="PatientIdentifierType.add" /> </a> </c:if>

<br /><br />

<b class="boxHeader"><openmrs:message code="PatientIdentifierType.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th> <openmrs:message code="general.name"/> </th>
			<th> <openmrs:message code="general.description"/> </th>
		</tr>
		<c:forEach var="patientIdentifierType" items="${patientIdentifierTypeList}">
			<tr>
				<td valign="top">
					<a href="patientIdentifierType.form?patientIdentifierTypeId=${patientIdentifierType.patientIdentifierTypeId}">
						<c:choose>
							<c:when test="${patientIdentifierType.retired == true}">
								<del>${patientIdentifierType.name}</del>
							</c:when>
							<c:otherwise>
								${patientIdentifierType.name}
							</c:otherwise>
						</c:choose>
					</a>
				</td>
				<td valign="top">${patientIdentifierType.description}</td>
			</tr>
		</c:forEach>
	</table>
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>