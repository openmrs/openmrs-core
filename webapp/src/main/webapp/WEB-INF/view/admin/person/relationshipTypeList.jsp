<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Relationship Types" otherwise="/login.htm" redirect="/admin/person/relationshipType.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="RelationshipType.manage.title"/></h2>

<a href="relationshipType.form"><openmrs:message code="RelationshipType.add"/></a> | 
<a href="relationshipTypeViews.form"><openmrs:message code="RelationshipType.views.title"/></a>

<br /><br />

<b class="boxHeader"><openmrs:message code="RelationshipType.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th> <openmrs:message code="RelationshipType.names"/> </th>
			<th> <openmrs:message code="general.description"/> </th>
		</tr>
		<c:forEach var="relationshipType" items="${relationshipTypeList}">
			<tr>
				<td valign="top">
					<a href="relationshipType.form?relationshipTypeId=${relationshipType.relationshipTypeId}">
						<c:choose>
							<c:when test="${relationshipType.retired == true}">
								<del>${relationshipType}</del>
							</c:when>
							<c:otherwise>
								${relationshipType}
							</c:otherwise>
						</c:choose>
					</a>
				</td>
				<td valign="top">${relationshipType.description}</td>
			</tr>
		</c:forEach>
	</table>
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>