<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Locations" otherwise="/login.htm" redirect="/admin/locations/location.list" />
<openmrs:message var="pageTitle" code="Location.title" scope="page"/>
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/dojoConfig.js" />
<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.addOnLoad( function() {
		toggleRowVisibilityForClass("locationTable", "retired", false);
	})
</script>

<h2><openmrs:message code="Location.manage.title"/></h2>

<a href="location.form"><openmrs:message code="Location.add"/></a>

<openmrs:extensionPoint pointId="org.openmrs.admin.locations.locationList.afterAdd" type="html" />

<br />
<br />

<b class="boxHeader">
	<a style="display: block; float: right"
		href="#"
		onClick="return toggleRowVisibilityForClass('locationTable', 'retired', false);">
		<openmrs:message code="general.toggle.retired" />
	</a>
	<openmrs:message code="Location.list.title"/>
</b>

<form method="post" class="box">
	<table id="locationTable">
		<tr>
			<th> </th>
			<th> <openmrs:message code="general.name" /> </th>
			<th> <openmrs:message code="general.description" /> </th>
			<th> <openmrs:message code="Location.tags" /> </th>
		</tr>
		<c:forEach var="location" items="${locationList}">
			<tr <c:if test="${location.retired}">class="retired"</c:if>>
				<td valign="top"><input type="checkbox" name="locationId" value="${location.locationId}"></td>
				<td valign="top">
					<a href="location.form?locationId=${location.locationId}"><c:out value="${location.name}"/></a> (${location.locationId})
				</td>
				<td valign="top"><c:out value="${location.description}"/></td>
				<td valign="top">
					<c:forEach var="tag" items="${location.tags}" varStatus="vs">
						<openmrs:format locationTag="${tag}"/><c:if test="${!vs.last}">,</c:if>
					</c:forEach>
				</td>
			</tr>
		</c:forEach>
	</table>
	<openmrs:extensionPoint pointId="org.openmrs.admin.locations.locationList.inForm" type="html" />
	<input type="submit" value="<openmrs:message code="Location.delete"/>" name="action">
</form>

<openmrs:extensionPoint pointId="org.openmrs.admin.locations.locationList.footer" type="html" />

<%@ include file="/WEB-INF/template/footer.jsp" %>