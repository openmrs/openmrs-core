<%@ include file="/WEB-INF/template/include.jsp" %>

<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/easyAjax.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/dwr/interface/DWRRelationshipService.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/dwr/engine.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/dwr/util.js"></script>

<div class="boxHeader"><spring:message code="Program.title"/></div>
<div class="box">
	<openmrs:portlet url="patientPrograms" id="patientPrograms" patientId="${patient.patientId}" parameters="allowEdits=true"/>
</div>

<p>

<openmrs:portlet url="patientRelationships" size="normal" patientId="${patient.patientId}" parameters="allowEditShownTypes=true|allowAddShownTypes=false|allowAddOtherTypes=true|allowVoid=true|showFrom=false|showTo=true|showTypes=Accompagnateur,Primary Care Physician|showOtherTypes=false"/>
