<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Provider Attribute Types, Purge Provider Attribute Types" otherwise="/login.htm" redirect="/admin/provider/providerAttributeType.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="ProviderAttributeType.manage.title"/></h2>

<a href="providerAttributeType.form"><openmrs:message code="ProviderAttributeType.add"/></a>

<br /><br />

<b class="boxHeader"><openmrs:message code="ProviderAttributeType.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th> <openmrs:message code="general.name"/> </th>
			<th> <openmrs:message code="general.description"/> </th>
		</tr>
		<c:forEach var="providerAttributeType" items="${providerAttributeTypeList}">
			<tr>
				<td valign="top">
					<a href="providerAttributeType.form?providerAttributeTypeId=${providerAttributeType.providerAttributeTypeId}">
						<c:choose>
							<c:when test="${providerAttributeType.retired == true}">
								<del><c:out value="${providerAttributeType.name}"/></del>
							</c:when>
							<c:otherwise>
								<c:out value="${providerAttributeType.name}"/>
							</c:otherwise>
						</c:choose>
					</a>
				</td>
				<td valign="top"><c:out value="${providerAttributeType.description}"/></td>
			</tr>
		</c:forEach>
	</table>
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>