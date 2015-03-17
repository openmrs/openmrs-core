<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:htmlInclude file="/scripts/easyAjax.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRRelationshipService.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRPatientService.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRObsService.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRConceptService.js" />
<openmrs:htmlInclude file="/dwr/engine.js" />
<openmrs:htmlInclude file="/dwr/util.js" />

<openmrs:globalProperty var="importantIdentifiers" key="patient_identifier.importantTypes" />
<openmrs:globalProperty key="use_patient_attribute.healthCenter" defaultValue="false" var="showHealthCenter"/>

<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.overviewBox" type="html" parameters="patientId=${model.patient.patientId}" requiredClass="org.openmrs.module.web.extension.BoxExt">
	<openmrs:hasPrivilege privilege="${extension.requiredPrivilege}">
		<div class="boxHeader${model.patientVariation}"><openmrs:message code="${extension.title}" /></div>
		<div class="box${model.patientVariation}"><openmrs:message code="${extension.content}" />
  			<c:if test="${extension.portletUrl != null}">
   				<openmrs:portlet url="${extension.portletUrl}" moduleId="${extension.moduleId}" id="${extension.portletUrl}" patientId="${patient.patientId}" parameters="allowEdits=true"/>
 			</c:if>
		</div>
		<br />
	</openmrs:hasPrivilege>
</openmrs:extensionPoint>

<openmrs:hasPrivilege privilege="Patient Overview - View Patient Actions">
<div id="patientActionsBoxHeader" class="boxHeader${model.patientVariation}"><openmrs:message code="Patient.actions" /></div>
<div id="patientActionsBox" class="box${model.patientVariation}">
	<table id="patientActions">
		<tr class="patientActionsRow">
			<td id="patientActionsPatientSummary">
				<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.patientSummary" parameters="patientId=${model.patient.patientId}">
					<a href="javascript:window.open('<openmrs_tag:url value="${extension.url}"/>?patientId=<c:out value="${model.patient.patientId}" />', 'summaryWindow', 'toolbar=no,width=660,height=600,resizable=yes,scrollbars=yes').focus()"><c:out value="${extension.label}" /></a>
				</openmrs:extensionPoint>
			</td>
		</tr>
	</table>
	<table id="patientActions">
		<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.patientActionsContent" type="html" parameters="patientId=${model.patient.patientId}"/> 
	</table>
</div>
<br />

<c:if test="${not empty importantIdentifiers}">
	<div id="patientIdentifiersBoxHeader" class="boxHeader${model.patientVariation}"><openmrs:message code="Patient.identifiers" /></div>
	<div id="patientIdentifiersBox" class="box${model.patientVariation}">
		<openmrs:portlet url="patientIdentifiers" size="normal" patientId="${model.patientId}" parameters="showIfSet=true|showIfMissing=true|highlightIfMissing=false" />
	</div>
	<p>
</c:if>
</openmrs:hasPrivilege>

<openmrs:hasPrivilege privilege="Patient Overview - View Programs">
	<div id="patientProgramsBoxHeader" class="boxHeader${model.patientVariation}"><openmrs:message code="Program.title"/></div>
	<div id="patientProgramsBox" class="box${model.patientVariation}">
		<openmrs:portlet url="patientPrograms" id="patientPrograms" patientId="${patient.patientId}" parameters="allowEdits=true"/>
	</div>
	<br/>

<openmrs:globalProperty var="conceptIdsToUse" key="dashboard.overview.showConcepts" />
<c:if test="${not empty conceptIdsToUse}">
	<div id="patientMostRecentObsBoxHeader" class="boxHeader${model.patientVariation}"><openmrs:message code="patientDashboard.mostRecentObs"/></div>
	<div id="patientMostRecentObsBox" class="box${model.patientVariation}">
		<openmrs:portlet url="customMostRecentObs" size="normal" patientId="${patient.patientId}" parameters="conceptIds=${conceptIdsToUse}|allowNew=true" />
	</div>
	<br/>
</c:if>
</openmrs:hasPrivilege>

<openmrs:hasPrivilege privilege="Patient Overview - View Relationships">
	<div id="patientRelationshipsBoxHeader" class="boxHeader${model.patientVariation}"><openmrs:message code="Relationship.relationships" /></div>
	<div id="patientRelationshipsBox" class="box${model.patientVariation}">
		<openmrs:portlet url="personRelationships" size="normal" patientId="${patient.patientId}" />
	</div>
	<br/>
</openmrs:hasPrivilege>

<openmrs:hasPrivilege privilege="Patient Overview - View Allergies">
	<div id="patientActiveListsAllergyBoxHeader" class="boxHeader${model.patientVariation}"><openmrs:message code="ActiveLists.allergy.title" /></div>
	<div id="patientActiveListsAllergyBox" class="box${model.patientVariation}">
		<openmrs:portlet url="activeListAllergy" patientId="${patient.patientId}" parameters="type=allergy"/>
	</div>
	<br/>
</openmrs:hasPrivilege>

<openmrs:hasPrivilege privilege="Patient Overview - View Problem List">
	<div id="patientActiveListsProblemBoxHeader" class="boxHeader${model.patientVariation}"><openmrs:message code="ActiveLists.problem.title" /></div>
	<div id="patientActiveListsProblemBox" class="box${model.patientVariation}">
		<openmrs:portlet url="activeListProblem" patientId="${patient.patientId}" parameters="type=problem"/>
	</div>
	<br/>
</openmrs:hasPrivilege>
