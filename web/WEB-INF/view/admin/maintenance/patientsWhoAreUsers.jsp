<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/admin/maintenance/patientsWhoAreUsers.list" />

<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="localHeader.jsp" %>

<h2><spring:message code="PatientsWhoAreUsers.overview"/></h2>

<spring:message code="PatientsWhoAreUsers.description"/>

<br/><br/>
<b><spring:message code="PatientsWhoAreUsers.count" arguments="${fn:length(usernameToPatientIdentifiers)}"/></b>

<c:if test="${fn:length(usernameToPatientIdentifiers) == 0}">
	<b>: <spring:message code="PatientsWhoAreUsers.noneMessage"/></b>
</c:if>

<c:if test="${fn:length(usernameToPatientIdentifiers) > 0}">
	<table>
		<tr>
			<th><spring:message code="PatientsWhoAreUsers.username"/></th>
			<th><spring:message code="PatientsWhoAreUsers.identifiers"/></th>
		</tr>
		<c:forEach var="row" items="${usernameToPatientIdentifiers}">
			<tr>
				<td>${row.key}</td>
				<td>
					<c:forEach var="identifier" items="${row.value}">
						${identifier}<br/>
					</c:forEach>
				</td>
			</tr>
		</c:forEach>
	</table>
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp"%>