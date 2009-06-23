<%@ include file="/WEB-INF/template/include.jsp" %>

<spring:message var="pageTitle" code="index.title" scope="page"/>
<%@ include file="/WEB-INF/template/header.jsp" %>

<c:set var="anyExtension" value="false" />
<openmrs:authentication>
	<c:if test="${authenticatedUser != null}">
		<openmrs:extensionPoint pointId="org.openmrs.navigation.homepage" type="html" varStatus="status">
			<c:set var="anyExtension" value="true" />
			<openmrs:portlet url="${extension.portletUrl}" parameters="${extension.portletParameters}" moduleId="${extension.moduleId}" />
		</openmrs:extensionPoint>
	</c:if>
</openmrs:authentication>

<c:if test="${not anyExtension}">
<center>
<img src="${pageContext.request.contextPath}<spring:theme code="image.logo.large"/>" alt='<spring:message code="openmrs.title"/>' title='<spring:message code="openmrs.title"/>'/>
	
	<br/><br/><br/>
	
	<openmrs:portlet url="welcome" parameters="showName=true|showLogin=true" />
</center>
</c:if>

<br />

<%@ include file="/WEB-INF/template/footer.jsp" %> 