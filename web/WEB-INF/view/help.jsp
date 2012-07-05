<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:message var="pageTitle" code="help.title" scope="page"/>
<%@ include file="/WEB-INF/template/header.jsp" %>

<h2><openmrs:message code="help.title"/></h2>

<br />
<openmrs:message code="help.text"/>
<br />
<br />
<openmrs:message code="help.text2"/>


<br/>

<openmrs:extensionPoint pointId="org.openmrs.help" type="html" />


<%@ include file="/WEB-INF/template/footer.jsp" %>