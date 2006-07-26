<%@ include file="/WEB-INF/template/include.jsp" %>

<c:choose>
	<c:when test="${model.authenticatedUser != null}">
		<c:choose>
			<c:when test="${model.showName != 'false'}">
				<spring:message code="welcomeUser" arguments="${model.authenticatedUser.firstName}"/>
			</c:when>
			<c:otherwise>
				<spring:message code="welcome" />
			</c:otherwise>
		</c:choose>
	</c:when>
	<c:otherwise>
		<spring:message code="welcome" />
		<c:if test="${model.showLogin == 'true'}">
			<br/>
			<openmrs:portlet url="login"/>
		</c:if>
	</c:otherwise>
</c:choose>