<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:htmlInclude file="/scripts/easyAjax.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRRelationshipService.js" />
<openmrs:htmlInclude file="/dwr/engine.js" />
<openmrs:htmlInclude file="/dwr/util.js" />

<openmrs:globalProperty var="conceptIdsToUse" key="dashboard.overview.showConcepts" />
<c:if test="${not empty conceptIdsToUse}">
	<div class="box">
		<openmrs:portlet url="customMostRecentObs" size="normal" parameters="conceptIds=${conceptIdsToUse}" />
	</div>
	
	<br/>
</c:if>

<div class="boxHeader"><spring:message code="Program.title"/></div>
<div class="box">
	<openmrs:portlet url="patientPrograms" id="patientPrograms" patientId="${patient.patientId}" parameters="allowEdits=true"/>
</div>

<br/>

<div class="boxHeader"><spring:message code="Relationship.patient.providers" /></div>
<openmrs:portlet url="patientRelationships" size="normal" patientId="${patient.patientId}" parameters="allowEditShownTypes=true|allowAddShownTypes=false|allowAddOtherTypes=true|allowVoid=true|showFrom=false|showTo=true|showTypes=Accompagnateur|showOtherTypes=false"/>
