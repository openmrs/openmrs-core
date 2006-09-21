<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:htmlInclude file="${pageContext.request.contextPath}/scripts/easyAjax.js" />
<openmrs:htmlInclude file="${pageContext.request.contextPath}/dwr/interface/DWRRelationshipService.js" />
<openmrs:htmlInclude file="${pageContext.request.contextPath}/dwr/engine.js" />
<openmrs:htmlInclude file="${pageContext.request.contextPath}/dwr/util.js" />

<div class="box">
	<openmrs:portlet url="customMostRecentObs" size="normal" parameters="globalPropertyKey=dashboard.overview.showConcepts" />
</div>

<p>

<div class="boxHeader"><spring:message code="Program.title"/></div>
<div class="box">
	<openmrs:portlet url="patientPrograms" id="patientPrograms" patientId="${patient.patientId}" parameters="allowEdits=true"/>
</div>

<p>

<div class="boxHeader"><spring:message code="Relationship.patient.providers" /></div>
<openmrs:portlet url="patientRelationships" size="normal" patientId="${patient.patientId}" parameters="allowEditShownTypes=true|allowAddShownTypes=false|allowAddOtherTypes=true|allowVoid=true|showFrom=false|showTo=true|showTypes=Accompagnateur|showOtherTypes=false"/>
