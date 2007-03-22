<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

<h2><spring:message code="help.title"/></h2>

<br />
<spring:message code="help.text"/>
<br />
<br />
<spring:message code="help.text2"/>


<br/>

<openmrs:extensionPoint pointId="org.openmrs.help" type="html" />


<%@ include file="/WEB-INF/template/footer.jsp" %>