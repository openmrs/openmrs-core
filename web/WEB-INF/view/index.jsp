<%@ include file="/WEB-INF/template/include.jsp" %>

<spring:message var="pageTitle" code="index.title" scope="page"/>
<%@ include file="/WEB-INF/template/header.jsp" %>

<c:set var="anyExtension" value="false" />
<openmrs:extensionPoint pointId="org.openmrs.navigation.homepage" type="html" varStatus="status">
	<c:set var="anyExtension" value="true" />
	<openmrs:portlet url="${extension.portletUrl}" parameters="${extension.portletParameters}" moduleId="${extension.moduleId}" />
</openmrs:extensionPoint>

<c:if test="${not anyExtension}">
	<br/>
	<center>
		<img src="${pageContext.request.contextPath}/images/openmrs_logo_large.gif" alt='<spring:message code="openmrs.title"/>' title='<spring:message code="openmrs.title"/>'/>
		
		<br/><br/><br/>
		
		<openmrs:portlet url="welcome" parameters="showName=true|showLogin=true" />
	</center>
</c:if>

<br />

<%@ include file="/WEB-INF/template/footer.jsp" %> 