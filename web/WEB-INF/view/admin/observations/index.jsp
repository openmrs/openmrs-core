<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Observations" otherwise="/login.htm" redirect="/admin/observations/index.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Obs.manage.title"/></h2>

<a href="obs.form"><spring:message code="Obs.add"/></a>

<br/>
<br/>

<openmrs:portlet url="findObservation" size="full" />

<%--
<a href="${pageContext.request.contextPath}/admin/encounters/"><spring:message code="Obs.edit"/></a>
-
<spring:message code="Obs.edit.description"/>
--%>

<%@ include file="/WEB-INF/template/footer.jsp" %>
