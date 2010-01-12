<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Identifier Types" otherwise="/login.htm" redirect="/admin/patients/patientIdentifierType.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="PatientIdentifierType.manage.title"/></h2>

<a href="patientIdentifierType.form"><spring:message code="PatientIdentifierType.add"/></a>

<br /><br />

<b class="boxHeader"><spring:message code="PatientIdentifierType.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th> <spring:message code="general.name"/> </th>
			<th> <spring:message code="general.description"/> </th>
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