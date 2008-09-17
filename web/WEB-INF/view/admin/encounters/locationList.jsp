<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Locations" otherwise="/login.htm" redirect="/admin/encounters/location.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Location.manage.title"/></h2>	

<a href="location.form"><spring:message code="Location.add"/></a>

<openmrs:extensionPoint pointId="org.openmrs.admin.encounters.locationList.afterAdd" type="html" />

<br />
<br />

<b class="boxHeader"><spring:message code="Location.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th> </th>
			<th> <spring:message code="general.name" /> </th>
			<th> <spring:message code="general.description" /> </th>
		</tr>
		<c:forEach var="location" items="${locationList}">
			<tr>
				<td valign="top"><input type="checkbox" name="locationId" value="${location.locationId}"></td>
				<td valign="top">
					<a href="location.form?locationId=${location.locationId}">${location.name}</a> (${location.locationId})					
				</td>
				<td valign="top">${location.description}</td>
			</tr>
		</c:forEach>
	</table>
	<openmrs:extensionPoint pointId="org.openmrs.admin.encounters.locationList.inForm" type="html" />
	<input type="submit" value="<spring:message code="Location.delete"/>" name="action">
</form>

<openmrs:extensionPoint pointId="org.openmrs.admin.encounters.locationList.footer" type="html" />

<%@ include file="/WEB-INF/template/footer.jsp" %>