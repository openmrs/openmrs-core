<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:hasPrivilege privilege="Patient Dashboard - View Regimen Section">	
	<div id="patientRegimen">

		<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.RegimenTabHeader" type="html" parameters="patientId=${patient.patientId}" />
		<openmrs:globalProperty var="displayDrugSetIds" key="dashboard.regimen.displayDrugSetIds" defaultValue="ANTIRETROVIRAL DRUGS,TUBERCULOSIS TREATMENT DRUGS" />
		<openmrs:portlet url="patientRegimen" id="patientDashboardRegimen" patientId="${patient.patientId}" parameters="displayDrugSetIds=${displayDrugSetIds}" />
		
	</div>
</openmrs:hasPrivilege>