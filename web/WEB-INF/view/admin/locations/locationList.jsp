<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Locations" otherwise="/login.htm" redirect="/admin/locations/location.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/dojoConfig.js" />
<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.addOnLoad( function() {
		toggleRowVisibilityForClass("locationTable", "retired", false);
	})
</script>

<h2><spring:message code="Location.manage.title"/></h2>

<a href="location.form"><spring:message code="Location.add"/></a>

<openmrs:extensionPoint pointId="org.openmrs.admin.locations.locationList.afterAdd" type="html" />

<br />
<br />

<b class="boxHeader">
	<a style="display: block; float: right"
		href="#"
		onClick="return toggleRowVisibilityForClass('locationTable', 'retired', false);">
		<spring:message code="general.toggle.retired" />
	</a>
	<spring:message code="Location.list.title"/>
</b>

<form method="post" class="box">
	<table id="locationTable">
		<tr>
			<th> </th>
			<th> <spring:message code="general.name" /> </th>
			<th> <spring:message code="general.description" /> </th>
		</tr>
		<c:forEach var="location" items="${locationList}">
			<tr <c:if test="${location.retired}">class="retired"</c:if>>
				<td valign="top"><input type="checkbox" name="locationId" value="${location.locationId}"></td>
				<td valign="top">
					<a href="location.form?locationId=${location.locationId}">${location.name}</a> (${location.locationId})
				</td>
				<td valign="top">${location.description}</td>
			</tr>
		</c:forEach>
	</table>
	<openmrs:extensionPoint pointId="org.openmrs.admin.locations.locationList.inForm" type="html" />
	<input type="submit" value="<spring:message code="Location.delete"/>" name="action">
</form>

<openmrs:extensionPoint pointId="org.openmrs.admin.locations.locationList.footer" type="html" />

<%@ include file="/WEB-INF/template/footer.jsp" %>