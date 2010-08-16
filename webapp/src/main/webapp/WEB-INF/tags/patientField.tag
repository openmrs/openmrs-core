<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="searchLabel" required="false" %>
<%@ attribute name="searchLabelCode" required="false" %>
<%@ attribute name="searchLabelArguments" required="false" %>
<%@ attribute name="initialValue" required="false" %> <%-- This should be a patientId --%>
<%@ attribute name="linkUrl" required="false" %>
<%@ attribute name="callback" required="false" %>
<%@ attribute name="allowSearch" required="false" %>

<openmrs:htmlInclude file="/scripts/dojoConfig.js" />
<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.PatientSearch");
	dojo.require("dojo.widget.openmrs.OpenmrsPopup");
	
	dojo.addOnLoad( function() {
		dojo.event.topic.subscribe("${formFieldName}_search/select", 
			function(msg) {
				if (msg) {
					var patientPopup = dojo.widget.manager.getWidgetById("${formFieldName}_selection");
					
					var patient = msg.objs[0];

					var displayString = patient.personName;
					<c:if test="${not empty linkUrl}">
						displayString = '<a id="${formFieldName}_name" href="#View" onclick="return gotoPatient(\'${linkUrl}\', ' + patient.patientId + ')">' + displayString + '</a>';
					</c:if>
					patientPopup.displayNode.innerHTML = displayString;					
					
					patientPopup.hiddenInputNode.value = patient.patientId;
					<c:if test="${not empty callback}">
						${callback}(patient.patientId);
					</c:if>
				}
			}
		);
		
	})
	
	function gotoPatient(url, pId) {
		if (url === null || url === '') {
			return false;
		} else {
			window.location = url + "?patientId=" + pId;
		}
		return false;
	}
</script>

<div dojoType="PatientSearch" widgetId="${formFieldName}_search" patientId="${initialValue}"></div>
<c:if test="${not empty searchLabelCode}">
	<div dojoType="OpenmrsPopup" widgetId="${formFieldName}_selection" hiddenInputName="${formFieldName}" searchWidget="${formFieldName}_search" searchTitle="<spring:message code="${searchLabelCode}" arguments="${searchLabelArguments}" />" allowSearch="${allowSearch}"></div>
</c:if> 
<c:if test="${empty searchLabelCode}">
	<div dojoType="OpenmrsPopup" widgetId="${formFieldName}_selection" hiddenInputName="${formFieldName}" searchWidget="${formFieldName}_search" searchTitle="${searchLabel}" allowSearch="${allowSearch}"></div>
</c:if> 
