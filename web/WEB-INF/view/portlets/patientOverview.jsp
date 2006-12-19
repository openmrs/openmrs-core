<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:htmlInclude file="/scripts/easyAjax.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRRelationshipService.js" />
<openmrs:htmlInclude file="/dwr/engine.js" />
<openmrs:htmlInclude file="/dwr/util.js" />

<openmrs:globalProperty var="importantIdentifiers" key="patient_identifier.importantTypes" />

<c:if test="${not empty importantIdentifiers}">
	<div class="boxHeader${model.patientVariation}"><spring:message code="Patient.identifiers" /></div>
	<div class="box${model.patientVariation}">
		<openmrs:portlet url="patientIdentifiers" size="normal" patientId="${model.patientId}" parameters="showIfSet=true|showIfMissing=true|highlightIfMissing=false" />
	</div>
	<p>
</c:if>

<div class="boxHeader${model.patientVariation}"><spring:message code="Program.title"/></div>
<div class="box${model.patientVariation}">
	<openmrs:portlet url="patientPrograms" id="patientPrograms" patientId="${patient.patientId}" parameters="allowEdits=true"/>
</div>
<br/>

<openmrs:globalProperty var="conceptIdsToUse" key="dashboard.overview.showConcepts" />
<c:if test="${not empty conceptIdsToUse}">
	<div class="boxHeader${model.patientVariation}"><spring:message code="Patient.groups"/></div>
	<div class="box${model.patientVariation}">
		<openmrs:portlet url="customMostRecentObs" size="normal" patientId="${patient.patientId}" parameters="conceptIds=${conceptIdsToUse}|allowNew=true" />
	</div>
	
	<br/>
</c:if>

<div class="boxHeader${model.patientVariation}"><spring:message code="Relationship.patient.providers" /></div>
<div class="box${model.patientVariation}">
	<openmrs:portlet url="patientRelationships" size="normal" patientId="${patient.patientId}" parameters="allowEditShownTypes=true|allowAddShownTypes=false|allowAddOtherTypes=true|allowVoid=true|showFrom=false|showTo=true|showTypes=Accompagnateur|showOtherTypes=false"/>
</div>
