<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="searchLabel" required="false" %>
<%@ attribute name="searchLabelCode" required="false" %>
<%@ attribute name="searchLabelArguments" required="false" %>
<%@ attribute name="initialValue" required="false" %> <%-- This should be a patientId --%>
<%@ attribute name="linkUrl" required="false" %>
<%@ attribute name="callback" required="false" %>

<openmrs:htmlInclude file="/scripts/dojoConfig.js" />
<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	var showTribe = "<openmrs:globalProperty key="use_patient_attribute.tribe" defaultValue="false" />";
	var showHealthCenter = "<openmrs:globalProperty key="use_patient_attribute.healthCenter" defaultValue="false" />";

	dojo.require("dojo.widget.openmrs.PatientSearch");
	dojo.require("dojo.widget.openmrs.OpenmrsPopup");
	
	dojo.addOnLoad( function() {
		patientPopup = dojo.widget.manager.getWidgetById("${formFieldName}_selection");
		patientSearch = dojo.widget.manager.getWidgetById("${formFieldName}_search");

		dojo.event.topic.subscribe("${formFieldName}_search/select", 
			function(msg) {
				if (msg) {
					var patient = msg.objs[0];
					patientPopup.displayNode.innerHTML = '<a id="${formFieldName}_name" href="#View" <c:if test="${not empty linkUrl}">onclick="return gotoUrl("${linkUrl}", ' + patient.patientId + ')"</c:if>>' + (patient.familyName ? patient.familyName : '') + ' ' + (patient.givenName ? patient.givenName : '') + '</a>';
					patientPopup.hiddenInputNode.value = patient.patientId;
					<c:if test="${not empty callback}">
						${callback}(patient.patientId);
					</c:if>
				}
			}
		);
		
		patientSearch.getCellFunctions = function() {
			var arr = new Array();
			
			arr.push(this.simpleClosure(patientSearch, "getNumber")); 
			arr.push(this.simpleClosure(patientSearch, "getId")); 
			arr.push(this.simpleClosure(patientSearch, "getGiven")); 
			arr.push(this.simpleClosure(patientSearch, "getMiddle")); 
			arr.push(this.simpleClosure(patientSearch, "getFamily")); 
			arr.push(this.simpleClosure(patientSearch, "getAge")); 
			arr.push(this.simpleClosure(patientSearch, "getGender")); 
			if (showTribe == 'true')
				arr.push(this.simpleClosure(patientSearch, "getTribe")); 
			if (showHealthCenter == 'true')
				arr.push(this.simpleClosure(patientSearch, "getHealthCenter")); 

			return arr;
		};
		
		patientSearch.getHeaderCellContent = function() {
			var arr = new Array();
			arr.push('');
			arr.push('<spring:message code="Patient.identifier" javaScriptEscape="true"/>');
			arr.push('<spring:message code="PatientName.givenName" javaScriptEscape="true"/>');
			arr.push('<spring:message code="PatientName.middleName" javaScriptEscape="true"/>');
			arr.push('<spring:message code="PatientName.familyName" javaScriptEscape="true"/>');
			arr.push('<spring:message code="Patient.age" javaScriptEscape="true"/>');
			arr.push('<spring:message code="Patient.gender" javaScriptEscape="true"/>');
			if (showTribe == 'true')
				arr.push('<spring:message code="Patient.tribe" javaScriptEscape="true"/>');
			if (showHealthCenter == 'true')
				arr.push('<spring:message code="Patient.healthCenter" javaScriptEscape="true"/>');
			
			return arr;
		};
		
	})
</script>

<div dojoType="PatientSearch" widgetId="${formFieldName}_search" patientId="${initialValue}"></div>
<c:if test="${not empty searchLabelCode}">
	<div dojoType="OpenmrsPopup" widgetId="${formFieldName}_selection" hiddenInputName="${formFieldName}" searchWidget="${formFieldName}_search" searchTitle="<spring:message code="${searchLabelCode}" arguments="${searchLabelArguments}" />"></div>
</c:if> 
<c:if test="${empty searchLabelCode}">
	<div dojoType="OpenmrsPopup" widgetId="${formFieldName}_selection" hiddenInputName="${formFieldName}" searchWidget="${formFieldName}_search" searchTitle="${searchLabel}"></div>
</c:if> 
