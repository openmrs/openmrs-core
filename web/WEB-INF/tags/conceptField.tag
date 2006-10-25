<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="searchLabel" required="false" %>
<%@ attribute name="initialValue" required="false" %>
<%@ attribute name="showAnswers" required="false" %>

<openmrs:htmlInclude file="/scripts/dojoConfig.js"></openmrs:htmlInclude>
<openmrs:htmlInclude file="/scripts/dojo/dojo.js"></openmrs:htmlInclude>

<script type="text/javascript">

	dojo.require("dojo.widget.openmrs.ConceptSearch");
	dojo.require("dojo.widget.openmrs.OpenmrsPopup");
	dojo.hostenv.writeIncludes();
	
	dojo.addOnLoad( function() {
		dojo.event.topic.subscribe("${formFieldName}_search/select", 
			function(msg) {
				if (msg) {
					var concept = msg.objs[0];
					var conceptPopup = dojo.widget.manager.getWidgetById("${formFieldName}_selection");
					conceptPopup.displayNode.innerHTML = concept.name;
					conceptPopup.hiddenInputNode.value = concept.conceptId;
				}
			}
		);
	})
</script>

<c:choose>
	<c:when test="${not empty showAnswers}">
		<div dojoType="ConceptSearch" widgetId="${formFieldName}_search" conceptId="${initialValue}" showVerboseListing="true" showAnswers="${showAnswers}"></div>
	</c:when>
	<c:otherwise>
		<div dojoType="ConceptSearch" widgetId="${formFieldName}_search" conceptId="${initialValue}" showVerboseListing="true"></div>
	</c:otherwise>
</c:choose>
<div dojoType="OpenmrsPopup" widgetId="${formFieldName}_selection" hiddenInputName="${formFieldName}" searchWidget="${formFieldName}_search" searchTitle="${searchLabel}"></div>
