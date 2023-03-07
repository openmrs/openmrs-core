<%@ include file="/WEB-INF/template/include.jsp"%>

<c:forEach var="resource" items="${data}" varStatus="status">
	<tr
		class="<c:choose><c:when test="${status.index % 2 == 0}">d0</c:when><c:otherwise>d1</c:otherwise></c:choose> <c:choose><c:when test="${resource.subResource}">subresource</c:when><c:otherwise>resource</c:otherwise></c:choose>" 
		<c:if test="${!empty resource.subResources}">id="${resource.name}"</c:if>
		 >

		<td <c:if test="${resource.subResource}">class="subResourceCell"</c:if>>
            <c:if test="${resource.subResource}"><spring:message code="webservices.rest.help.subresource"/>:</c:if>
            <c:if test="${!empty resource.subResources}"><div class="parentResource expand"></div><div class="resourceText"></c:if>
		    ${resource.name}
            <c:if test="${!empty resource.subtypeHandlerForResourceName}"><spring:message code="webservices.rest.help.extends"/> ${resource.subtypeHandlerForResourceName}</c:if>
          <c:if test="${!empty resource.subResources}"></div></c:if>
        </td>
		<td <c:if test="${resource.subResource}">class="subResourceCell"</c:if>>${resource.url}</td>
	<!-- <td>
		   <c:forEach var="ver" items="${resource.supportedOpenMRSVersion}">
		     ${ver} 
		   </c:forEach>
		</td>-->

		<td >
			<table class="innerTable <c:if test="${resource.subResource}"> subResourceRepresention</c:if>">
				<c:forEach var="representation" items="${resource.representations}">
					<tr>
						<td>${representation.name}: ${representation.properties}</td>
					</tr>
				</c:forEach>
			</table>
		</td>

	</tr>
	<c:if test="${!empty resource.subResources}">
		<c:set var="data" value="${resource.subResources}" scope="request"/>
		<jsp:include page="resources.jsp"/>
	</c:if>
    <c:if test="${!empty resource.subtypeHandlers}">
        <c:set var="data" value="${resource.subtypeHandlers}" scope="request"/>
        <jsp:include page="resources.jsp"/>
    </c:if>
</c:forEach>