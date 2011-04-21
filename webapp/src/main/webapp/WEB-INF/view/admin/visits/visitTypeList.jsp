<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Visit Types" otherwise="/login.htm" redirect="/admin/visits/visitType.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="VisitType.manage.title"/></h2>

<a href="visitType.form"><spring:message code="VisitType.add"/></a>

<br /><br />

<b class="boxHeader"><spring:message code="VisitType.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th> <spring:message code="general.name"/> </th>
			<th> <spring:message code="general.description"/> </th>
		</tr>
		<c:forEach var="visitType" items="${visitTypeList}">
			<tr>
				<td valign="top">
					<a href="visitType.form?visitTypeId=${visitType.visitTypeId}">
						<c:choose>
							<c:when test="${visitType.retired == true}">
								<del>${visitType.name}</del>
							</c:when>
							<c:otherwise>
								${visitType.name}
							</c:otherwise>
						</c:choose>
					</a>
				</td>
				<td valign="top">${visitType.description}</td>
			</tr>
		</c:forEach>
	</table>
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>