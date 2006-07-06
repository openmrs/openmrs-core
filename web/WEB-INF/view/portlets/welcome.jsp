<%@ include file="/WEB-INF/template/include.jsp" %>

<c:choose>
	<c:when test="${model.authenticatedUser != null && model.showName != 'false'}">
		<spring:message code="welcomeUser" arguments="${model.authenticatedUser.firstName}"/>
	</c:when>
	<c:otherwise>
		<spring:message code="welcome" />
	</c:otherwise>
</c:choose>