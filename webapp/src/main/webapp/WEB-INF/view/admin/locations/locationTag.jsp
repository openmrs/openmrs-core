<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Location Tags" otherwise="/login.htm" redirect="/admin/locations/locationTag.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript">
	$j(document).ready(function() {
		$j('.toggleAddLocationTag').click(function(event) {
			$j('#addLocationTag').slideToggle('fast');
			event.preventDefault();
		});
	});
</script>

<h2><spring:message code="LocationTag.manage"/></h2>

<a class="toggleAddLocationTag" href="#"><spring:message code="LocationTag.add"/></a>
<div id="addLocationTag" style="border: 1px black solid; background-color: #e0e0e0; display: none">
	<form method="post" action="locationTagAdd.form">
		<table>
			<tr>
				<th><spring:message code="LocationTag.name"/></th>
				<td>
					<input type="text" name="name"/>
					<span class="required">*</span>
				</td>
			</tr>
			<tr>
				<th><spring:message code="LocationTag.description"/></th>
				<td><textarea name="description" rows="3" cols="72"></textarea></td>
			</tr>
			<tr>
				<th></th>
				<td>
					<input type="submit" value="<spring:message code="general.save"/>" />
					<input type="button" value="<spring:message code="general.cancel"/>" class="toggleAddLocationTag" />
				</td>
			</tr>
		</table>
	</form>
</div>

<openmrs:extensionPoint pointId="org.openmrs.admin.locations.locationTagList.afterAdd" type="html" />

<br />
<br />
<b class="boxHeader"><spring:message code="LocationTag.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th> <spring:message code="general.name" /> </th>
			<th> <spring:message code="general.description" /> </th>
			<th> <spring:message code="general.creator" /> </th>
			<th> <spring:message code="general.dateCreated" /> </th>
		</tr>
		<c:forEach var="locationTag" items="${locationTags}">
			<tr <c:if test="${locationTag.retired == true}"> class="retired" </c:if> >
				<td valign="top">
					<a href="locationTagEdit.form?locationTagId=${locationTag.locationTagId}">
						${locationTag.name}
					</a>
				</td>
				<td valign="top">
					${locationTag.description}
				</td>
				<td valign="top">
					<openmrs:format user="${locationTag.creator}"/>
				</td>
				<td valign="top">
					<openmrs:formatDate date="${locationTag.dateCreated}"/>
				</td>
			</tr>
		</c:forEach>
		<c:if test="${empty locationTags}">
			<tr><td colspan="4"><spring:message code="general.none"/></td></tr>
		</c:if>
	</table>
	<openmrs:extensionPoint pointId="org.openmrs.admin.locations.locationTagList.inForm" type="html" />
	
</form>

<openmrs:extensionPoint pointId="org.openmrs.admin.locations.locationTagList.footer" type="html" />

<%@ include file="/WEB-INF/template/footer.jsp" %>
