<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:globalProperty var="enableFormEntryTab" key="FormEntry.enableDashboardTab" defaultValue="true"/>

<c:if test="${enableFormEntryTab}">
	<openmrs:hasPrivilege privilege="Form Entry">
		<div id="formEntry">
			<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.FormEntryTabHeader" type="html" parameters="patientId=${patient.patientId}" />
			<openmrs:portlet url="personFormEntry" id="formEntryPortlet" personId="${patient.personId}" parameters="showDecoration=true|showLastThreeEncounters=true|returnUrl=${pageContext.request.contextPath}/patientDashboard.form"/>
		</div>
	</openmrs:hasPrivilege>
</c:if>		