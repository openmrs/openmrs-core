<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:hasPrivilege privilege="Patient Dashboard - View Graphs Section">
	<div id="patientGraphs">
	
		<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.GraphsTabHeader" type="html" parameters="patientId=${patient.patientId}" />
		<openmrs:portlet url="patientGraphs" id="patientGraphsPortlet" patientId="${patient.patientId}"/>
		
	</div>
</openmrs:hasPrivilege>