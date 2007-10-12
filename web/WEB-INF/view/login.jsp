<%@ include file="/WEB-INF/template/include.jsp" %>

<spring:message var="pageTitle" code="login.title" scope="page"/>
<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:portlet url="login"/>

<openmrs:extensionPoint pointId="org.openmrs.login" type="html" />
		
<%@ include file="/WEB-INF/template/footer.jsp" %>