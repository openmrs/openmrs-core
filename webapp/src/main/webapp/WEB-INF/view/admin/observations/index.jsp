<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Observations" otherwise="/login.htm" redirect="/admin/observations/index.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="Obs.manage.title"/></h2>
<openmrs:hasPrivilege privilege="Add Observations">
	<a href="obs.form"><openmrs:message code="Obs.add"/></a>
    <br/>
    <br/>
</openmrs:hasPrivilege>

<openmrs:portlet url="findObservation" size="full" />

<script>

// put focus on the first input element
$j("input:text:visible:enabled:first").focus();

</script>

<%--
<a href="${pageContext.request.contextPath}/admin/encounters/"><openmrs:message code="Obs.edit"/></a>
-
<openmrs:message code="Obs.edit.description"/>
--%>

<%@ include file="/WEB-INF/template/footer.jsp" %>
