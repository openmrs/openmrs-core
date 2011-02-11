<%@ include file="/WEB-INF/template/include.jsp" %>

<c:choose>
	<c:when test="${model.authenticatedUser != null}">
		<c:choose>
			<c:when test="${model.showName != 'false'}">
				<spring:message code="welcomeUser" arguments="${model.authenticatedUser.personName.givenName},${fn:substring(pageContext.request.contextPath,1,-1)}" />
			</c:when>
			<c:otherwise>
				<spring:message code="welcome" arguments="${fn:substring(pageContext.request.contextPath,1,-1)}" />
			</c:otherwise>
		</c:choose>
		<c:if test="${model.customText != ''}">
			${model.customText}
		</c:if>
	</c:when>
	<c:otherwise>
		<spring:message code="welcome" arguments="${fn:substring(pageContext.request.contextPath,1,-1)}" />
		<c:if test="${model.showLogin == 'true'}">
			<br/>
			<openmrs:portlet url="login" parameters="redirect=${model.redirect}" />
		</c:if>
	</c:otherwise>
</c:choose>