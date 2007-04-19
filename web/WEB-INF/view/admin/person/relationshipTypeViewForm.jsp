<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Relationship Types" otherwise="/login.htm" redirect="/admin/person/relationshipTypeViews.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="RelationshipType.views.title"/></h2>

<a href="relationshipType.form"><spring:message code="RelationshipType.add"/></a> | 
<a href="relationshipTypeViews.form"><spring:message code="RelationshipType.views.title"/></a>

<br /><br />

<b class="boxHeader"><spring:message code="RelationshipType.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th> <spring:message code="RelationshipType.names"/> </th>
			<th> <spring:message code="RelationshipType.views.order"/> </th>
			<th> <spring:message code="RelationshipType.views.preferred"/> </th>
		</tr>
		<c:forEach var="relationshipType" items="${relationshipTypeList}">
			<tr>
				<td><a href="relationshipType.form?relationshipTypeId=${relationshipType.relationshipTypeId}">
					   ${relationshipType}
					</a>
				</td>
				<td>
					<input type="hidden" name="relationshipTypeIds" value="${relationshipType.relationshipTypeId}">
					<select name="displayOrders">
						<c:forEach var="i" begin="1" end="${fn:length(relationshipTypeList)}">
							<option value="${i}" <c:if test="${relationshipType.weight == i}">selected</c:if>>${i}</option>
						</c:forEach>
					</select>
				</td>
				<td>
					<input type="checkbox" name="preferredTypes" value="${relationshipType.relationshipTypeId}" <c:if test="${fn:contains(preferredTypes, relationshipType)}">checked</c:if> />
				</td>
			</tr>
		</c:forEach>
	</table>
	<input type="submit" value="<spring:message code="general.save"/>">
</form>

<br/>

<b><spring:message code="RelationshipType.views.order"/>:</b> <spring:message code="RelationshipType.views.order.help"/> <br/>
<b><spring:message code="RelationshipType.views.preferred"/>:</b> <spring:message code="RelationshipType.views.preferred.help"/> <br/>




<%@ include file="/WEB-INF/template/footer.jsp" %>