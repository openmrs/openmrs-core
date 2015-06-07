<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:globalProperty var="applicationName" key="application.name" defaultValue="OpenMRS"/>
<c:choose>
	<c:when test="${model.authenticatedUser != null}">
		<c:choose>
			<c:when test="${model.showName != 'false'}">
				<openmrs:message code="welcomeUser" htmlEscape="false" arguments="${model.authenticatedUser.personName.givenName},${applicationName}" />
			</c:when>
			<c:otherwise>
				<openmrs:message htmlEscape="false" code="welcome" arguments="${applicationName}" />
			</c:otherwise>
		</c:choose>
		<c:if test="${model.customText != ''}">
			${model.customText}
		</c:if>
	</c:when>
	<c:otherwise>
		<openmrs:message htmlEscape="false" code="welcome" arguments="${applicationName}" />
		<c:if test="${model.showLogin == 'true'}">
			<br/>
			<openmrs:portlet url="login" parameters="redirect=${model.redirect}" />
		</c:if>
	</c:otherwise>
</c:choose>