<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:globalProperty key="visits.enabled" defaultValue="true" var="visitsEnabled"/>
<c:choose>		
	<c:when test='${visitsEnabled}'>
		<openmrs:hasPrivilege privilege="Patient Dashboard - View Visits Section">
			<div id="patientVisits">
				
				<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.VisitsTabHeader" type="html" parameters="patientId=${patient.patientId}" />
				<openmrs:portlet url="patientVisits" id="patientDashboardVisits" patientId="${patient.patientId}" />
				
			</div>
		</openmrs:hasPrivilege>
	</c:when>
	<c:otherwise>
		<openmrs:hasPrivilege privilege="Patient Dashboard - View Encounters Section">
			<div id="patientEncounters">
				
				<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.EncountersTabHeader" type="html" parameters="patientId=${patient.patientId}" />
				<openmrs:globalProperty var="maxEncs" key="dashboard.maximumNumberOfEncountersToShow" defaultValue="" />
				<openmrs:portlet url="patientEncounters" id="patientDashboardEncounters" patientId="${patient.patientId}" parameters="num=${maxEncs}|showPagination=true|formEntryReturnUrl=${pageContext.request.contextPath}/patientDashboard.form"/>
			</div>
		</openmrs:hasPrivilege>
	</c:otherwise>
</c:choose>