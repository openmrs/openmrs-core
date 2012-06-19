<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:hasPrivilege privilege="Patient Dashboard - View Demographics Section">
	<div id="patientDemographics">
		
		<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.DemographicsTabHeader" type="html" parameters="patientId=${patient.patientId}" />
		<openmrs:portlet url="patientDemographics" id="patientDashboardDemographics" patientId="${patient.patientId}"/>
		
	</div>
</openmrs:hasPrivilege>