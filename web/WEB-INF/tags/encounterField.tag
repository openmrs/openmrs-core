<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="searchLabel" required="false" %>
<%@ attribute name="searchLabelCode" required="false" %>
<%@ attribute name="initialValue" required="false" %> <%-- This should be an encounterId --%>
<%@ attribute name="linkUrl" required="false" %>
<%@ attribute name="callback" required="false" %>

<openmrs:htmlInclude file="/scripts/dojoConfig.js" />
<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.EncounterSearch");
	dojo.require("dojo.widget.openmrs.OpenmrsPopup");
	
	dojo.addOnLoad( function() {
		encPopup = dojo.widget.manager.getWidgetById("${formFieldName}_selection");
		encSearch = dojo.widget.manager.getWidgetById("${formFieldName}_search");

		dojo.event.topic.subscribe("${formFieldName}_search/select", 
			function(msg) {
				if (msg) {
					var enc = msg.objs[0];
					encPopup.displayNode.innerHTML = '<a id="${formFieldName}_name" href="#View" <c:if test="${not empty linkUrl}">onclick="return gotoEncounter(\'${linkUrl}\', ' + enc.encounterId + ')"</c:if>>' + (enc.formName ? enc.formName : '') + ' (' + enc.encounterDateString + ')</a>';
					encPopup.hiddenInputNode.value = enc.encounterId;
					<c:if test="${not empty callback}">
						${callback}(enc.encounterId);
					</c:if>
				}
			}
		);
		
	})

	function gotoEncounter(url, eId) {
		if (url === null || url === '') {
			return false;
		} else {
			window.location = url + "?encounterId=" + eId;
		}
		return false;
	}
</script>

<div dojoType="EncounterSearch" widgetId="${formFieldName}_search" encounterId="${initialValue}"></div>
<c:if test="${not empty searchLabelCode}">
	<div dojoType="OpenmrsPopup" widgetId="${formFieldName}_selection" hiddenInputName="${formFieldName}" searchWidget="${formFieldName}_search" searchTitle="<spring:message code="${searchLabelCode}" />"></div>
</c:if> 
<c:if test="${empty searchLabelCode}">
	<div dojoType="OpenmrsPopup" widgetId="${formFieldName}_selection" hiddenInputName="${formFieldName}" searchWidget="${formFieldName}_search" searchTitle="${searchLabel}"></div>
</c:if> 
