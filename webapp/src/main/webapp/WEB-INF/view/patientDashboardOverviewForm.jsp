<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:hasPrivilege privilege="Patient Dashboard - View Overview Section">
		<div id="patientOverview">
			<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.OverviewTabHeader" type="html" parameters="patientId=${patient.patientId}" />
			<openmrs:portlet url="patientOverview" id="patientDashboardOverview" patientId="${patient.patientId}"/>
		</div>
</openmrs:hasPrivilege>