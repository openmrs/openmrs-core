<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Provider Attribute Types, Purge Provider Attribute Types" otherwise="/login.htm" redirect="/admin/provider/providerAttributeType.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="ProviderAttributeType.manage.title"/></h2>

<a href="providerAttributeType.form"><spring:message code="ProviderAttributeType.add"/></a>

<br /><br />

<b class="boxHeader"><spring:message code="ProviderAttributeType.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th> <spring:message code="general.name"/> </th>
			<th> <spring:message code="general.description"/> </th>
		</tr>
		<c:forEach var="providerAttributeType" items="${providerAttributeTypeList}">
			<tr>
				<td valign="top">
					<a href="providerAttributeType.form?providerAttributeTypeId=${providerAttributeType.providerAttributeTypeId}">
						<c:choose>
							<c:when test="${providerAttributeType.retired == true}">
								<del>${providerAttributeType.name}</del>
							</c:when>
							<c:otherwise>
								${providerAttributeType.name}
							</c:otherwise>
						</c:choose>
					</a>
				</td>
				<td valign="top">${providerAttributeType.description}</td>
			</tr>
		</c:forEach>
	</table>
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>