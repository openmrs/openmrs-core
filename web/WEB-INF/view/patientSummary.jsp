<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:require privilege="View Patients" otherwise="/login.htm" />

<c:set var="OPENMRS_DO_NOT_SHOW_PATIENT_SET" scope="request" value="true"/>
<%@ include file="/WEB-INF/template/headerMinimal.jsp" %>

<c:if test="${not empty patientSet}">
	<c:forEach var="ptId" items="${patientSet.patientIds}" varStatus="status">
		<div <c:if test="${status.index > 0}">style="page-break-before: always"</c:if>>
			<openmrs:portlet url="patientSummary" id="summary_${status.count}" patientId="${ptId}" />
		</div>
	</c:forEach>
</c:if>

<c:if test="${not empty param.patientId}">
	<openmrs:portlet url="patientSummary" id="patientDashboardSummary" patientId="${param.patientId}" />
</c:if>

<%@ include file="/WEB-INF/template/footerMinimal.jsp" %>