<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Encounter Roles" otherwise="/login.htm" redirect="/admin/encounters/encounterRole.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="EncounterRole.manage.title"/></h2>

<a href="encounterRole.form"><openmrs:message code="EncounterRole.add"/></a>

<br />
<br />

<b class="boxHeader">
	<openmrs:message code="EncounterRole.list.title"/>
</b>

<form method="post" class="box">
	<table id="encounterRoleTable">
		<tr>
			<th> <openmrs:message code="general.name" /> </th>
			<th> <openmrs:message code="general.description" /> </th>
		</tr>
		<c:forEach var="encounterRole" items="${encounterRoles}">
			<tr>
				<td valign="top">
					<a href="encounterRole.form?encounterRoleId=${encounterRole.encounterRoleId}">
						<c:choose>
							<c:when test="${encounterRole.retired == true}">
								<del><c:out value="${encounterRole.name}"/></del>
							</c:when>
							<c:otherwise>
								<c:out value="${encounterRole.name}"/>
							</c:otherwise>
						</c:choose>
					</a>
				</td>
				<td valign="top"><c:out value="${encounterRole.description}"/></td>
			</tr>
		</c:forEach>
	</table>
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>