<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:require privilege="View Patients" otherwise="/login.htm" />

<c:set var="OPENMRS_DO_NOT_SHOW_PATIENT_SET" scope="request" value="true"/>
<%@ include file="/WEB-INF/template/headerMinimal.jsp" %>

<openmrs:portlet url="patientSummary" id="patientDashboardSummary" patientId="${param.patientId}" />

<%@ include file="/WEB-INF/template/footerMinimal.jsp" %>