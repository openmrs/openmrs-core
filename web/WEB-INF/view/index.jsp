<%@ include file="/WEB-INF/template/include.jsp" %>

<spring:message var="pageTitle" code="index.title" scope="page"/>
<%@ include file="/WEB-INF/template/header.jsp" %>

<br/>

<center>
	<img src="${pageContext.request.contextPath}/images/openmrs_logo_large.gif" alt='<spring:message code="openmrs.title"/>' title='<spring:message code="openmrs.title"/>'/>
	
	<br/><br/><br/>
	
	<openmrs:portlet url="welcome" parameters="showName=true|showLogin=true" />
</center>

<br />

<%@ include file="/WEB-INF/template/footer.jsp" %> 