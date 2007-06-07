<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:portlet url="login"/>

<openmrs:extensionPoint pointId="org.openmrs.login" type="html" />
		
<%@ include file="/WEB-INF/template/footer.jsp" %>