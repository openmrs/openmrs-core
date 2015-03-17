<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Encounter Types" otherwise="/login.htm" redirect="/admin/encounters/encounterType.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="EncounterType.manage.title"/></h2>	

<a href="encounterType.form"><openmrs:message code="EncounterType.add"/></a> 

<openmrs:extensionPoint pointId="org.openmrs.admin.encounters.encounterTypeList.afterAdd" type="html" />

<br />
<br />

<b class="boxHeader"><openmrs:message code="EncounterType.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th> <openmrs:message code="general.name" /> </th>
			<th> <openmrs:message code="general.description" /> </th>
		</tr>
		<c:forEach var="encounterType" items="${encounterTypeList}">
			<tr>
				<td valign="top">
					<a href="encounterType.form?encounterTypeId=${encounterType.encounterTypeId}">
						<c:choose>
							<c:when test="${encounterType.retired == true}">
								<del>${encounterType.name}</del>
							</c:when>
							<c:otherwise>
								${encounterType.name}
							</c:otherwise>
						</c:choose>
					</a>
				</td>
				<td valign="top">${encounterType.description}</td>
			</tr>
		</c:forEach>
	</table>
	<openmrs:extensionPoint pointId="org.openmrs.admin.encounters.encounterTypeList.inForm" type="html" />
	
</form>

<openmrs:extensionPoint pointId="org.openmrs.admin.encounters.encounterTypeList.footer" type="html" />

<%@ include file="/WEB-INF/template/footer.jsp" %>