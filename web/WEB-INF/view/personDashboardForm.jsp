<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:require privilege="View Persons" otherwise="/login.htm" redirect="/personDashboard.form" />

<c:set var="OPENMRS_VIEWING_PERSON_ID" scope="request" value="${person.personId}"/>

<spring:message var="pageTitle" code="personDashboard.title" scope="page"/>
<%@ include file="/WEB-INF/template/header.jsp" %>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<c:if test="${person.patient}">
	<a href="patientDashboard.form?patientId=${person.personId}">
		<spring:message code="patientDashboard.viewDashboard"/>
	</a>
	<br/>
</c:if>

<br/>

<openmrs:portlet url="personHeader" id="patientDashboardHeader" personId="${person.personId}"/>

<br/>

<openmrs:hasPrivilege privilege="Person Dashboard - View Relationships">
	<div class="boxHeader"><spring:message code="Relationship.relationships" /></div>
	<div class="box">
		<openmrs:portlet url="personRelationships" size="normal" personId="${person.personId}"/>
	</div>
</openmrs:hasPrivilege>

<%@ include file="/WEB-INF/template/footer.jsp" %>