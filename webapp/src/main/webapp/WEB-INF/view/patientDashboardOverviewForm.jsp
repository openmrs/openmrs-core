<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:htmlInclude file="/scripts/easyAjax.js" />

<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables.css" />
<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js" />

<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui-1.7.2.custom.min.js" />
<link href="<openmrs:contextPath/>/scripts/jquery-ui/css/<spring:theme code='jqueryui.theme.name' />/jquery-ui.custom.css" type="text/css" rel="stylesheet" />

<openmrs:htmlInclude file="/scripts/flot/jquery.flot.js" />
<openmrs:htmlInclude file="/scripts/flot/jquery.flot.multiple.threshold.js"/> 

<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/patientDashboard.form" />

<openmrs:hasPrivilege privilege="Patient Dashboard - View Overview Section">
		<div id="patientOverview">
			
			<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.OverviewTabHeader" type="html" parameters="patientId=${patient.patientId}" />
			<openmrs:portlet url="patientOverview" id="patientDashboardOverview" patientId="${patient.patientId}"/>
			
		</div>
</openmrs:hasPrivilege>
