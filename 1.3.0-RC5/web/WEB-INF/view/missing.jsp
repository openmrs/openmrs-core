<%@page isErrorPage="true" %>
<%@ include file="/WEB-INF/template/include.jsp" %>

<spring:message var="pageTitle" code="missing.title" scope="page"/>
<%@ include file="/WEB-INF/template/header.jsp" %>

<h2>Error 404</h2>

<br /><br />

The page "<b><%= request.getAttribute("javax.servlet.error.request_uri") %></b>"
cannot be found.  Check the link and try again.

<br/><br/>

<openmrs:extensionPoint pointId="org.openmrs.missing" type="html" />


<%@ include file="/WEB-INF/template/footer.jsp" %> 