<%@ include file="/WEB-INF/template/include.jsp" %>

<c:choose>
	<c:when test="${name != null}">
		<spring:message code="welcomeUser" arguments="${name}"/>
	</c:when>
	<c:otherwise>
		<spring:message code="welcome" />
	</c:otherwise>
</c:choose>