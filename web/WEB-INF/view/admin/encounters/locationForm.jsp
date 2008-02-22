<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Locations" otherwise="/login.htm" redirect="/admin/encounters/location.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Location.title"/></h2>

<openmrs:extensionPoint pointId="org.openmrs.admin.encounters.locationForm.afterTitle" type="html" parameters="locationId=${location.locationId}" />

<spring:hasBindErrors name="location">
	<spring:message code="fix.error"/>
	<br />
</spring:hasBindErrors>
<form method="post">
<table>
	<tr>
		<td><spring:message code="general.name"/></td>
		<td colspan="5">
			<spring:bind path="location.name">
				<input type="text" name="name" value="${status.value}" size="35" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.description"/></td>
		<td valign="top" colspan="5">
			<spring:bind path="location.description">
				<textarea name="description" rows="3" cols="40">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<spring:nestedPath path="location">
		<openmrs:portlet url="addressLayout" id="addressPortlet" size="full" parameters="layoutShowTable=false|layoutShowExtended=false|layoutShowErrors=false" />
	</spring:nestedPath>
	<c:if test="${!(location.creator == null)}">
		<tr>
			<td><spring:message code="general.createdBy" /></td>
			<td>
				${location.creator.personName} -
				<openmrs:formatDate date="${location.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
</table>
<openmrs:extensionPoint pointId="org.openmrs.admin.encounters.locationForm.inForm" type="html" parameters="locationId=${location.locationId}" />
<br />
<input type="submit" value="<spring:message code="Location.save"/>">
</form>

<openmrs:extensionPoint pointId="org.openmrs.admin.encounters.locationForm.footer" type="html" parameters="locationId=${location.locationId}" />

<%@ include file="/WEB-INF/template/footer.jsp" %>