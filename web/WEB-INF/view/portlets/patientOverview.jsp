<%@ include file="/WEB-INF/template/include.jsp" %>

<div style="border: 1px black solid">
	<u><b><spring:message code="Program.title"/></b></u>
	<openmrs:portlet url="patientPrograms" id="patientPrograms" patientId="${patient.patientId}" parameters="allowEdits=true"/>
</div>
