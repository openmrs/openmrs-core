<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Visit Attribute Types, Purge Visit Attribute Types" otherwise="/login.htm" redirect="/admin/visits/visitAttributeType.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="VisitAttributeType.manage.title"/></h2>

<a href="visitAttributeType.form"><openmrs:message code="VisitAttributeType.add"/></a>

<br /><br />

<b class="boxHeader"><openmrs:message code="VisitAttributeType.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th> <openmrs:message code="general.name"/> </th>
			<th> <openmrs:message code="general.description"/> </th>
		</tr>
		<c:forEach var="visitAttributeType" items="${visitAttributeTypeList}">
			<tr>
				<td valign="top">
					<a href="visitAttributeType.form?visitAttributeTypeId=${visitAttributeType.visitAttributeTypeId}">
						<c:choose>
							<c:when test="${visitAttributeType.retired == true}">
								<del>${visitAttributeType.name}</del>
							</c:when>
							<c:otherwise>
								${visitAttributeType.name}
							</c:otherwise>
						</c:choose>
					</a>
				</td>
				<td valign="top">${visitAttributeType.description}</td>
			</tr>
		</c:forEach>
	</table>
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>