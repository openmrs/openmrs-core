<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="searchLabel" required="false" %>
<%@ attribute name="initialValue" required="false" %>
<%@ attribute name="showAnswers" required="false" %>
<%@ attribute name="showOther" required="false" %>
<%@ attribute name="otherValue" required="false" %>
<%@ attribute name="onSelectFunction" required="false" %>

<openmrs:htmlInclude file="/scripts/dojoConfig.js"></openmrs:htmlInclude>
<openmrs:htmlInclude file="/scripts/dojo/dojo.js"></openmrs:htmlInclude>

<script type="text/javascript">

	dojo.require("dojo.widget.openmrs.ConceptSearch");
	dojo.require("dojo.widget.openmrs.OpenmrsPopup");
	
	dojo.addOnLoad( function() {
		dojo.event.topic.subscribe("${formFieldName}_search/select", 
			function(msg) {
				if (msg) {
					var concept = msg.objs[0];
					var conceptPopup = dojo.widget.manager.getWidgetById("${formFieldName}_selection");
					conceptPopup.displayNode.innerHTML = concept.name;
					conceptPopup.hiddenInputNode.value = concept.conceptId;
					dojo.debug("Before adding if statement");
					<c:if test="${not empty model.showOther}">
						dojo.debug("Inside if statement, with cId is " + concept.conceptId + " and showOther is ${model.showOther}");
						// if showOther is the concept that is selected, show a text field so user can enter that "other" data
						conceptPopup.showOtherInputNode(concept.conceptId, ${model.showOther});
					</c:if>
					<c:if test="${not empty onSelectFunction}">
					   ${onSelectFunction}(concept);
					</c:if>
				}
			}
		);
	})	
</script>

<c:set var="conceptFieldTag_anyAnswers" value="false"/>
<c:if test="${not empty model.showAnswers}">
	<openmrs:forEachRecord name="answer" concept="${model.showAnswers}">
		<c:set var="conceptFieldTag_anyAnswers" value="true"/>
	</openmrs:forEachRecord>
	<c:if test="${!conceptFieldTag_anyAnswers}">
		<% org.apache.commons.logging.LogFactory.getLog(getClass()).error("No answers found for concept id: " + showAnswers + " for field: " + formFieldName); %>
	</c:if>
</c:if>

<c:choose>
	<c:when test="${not empty model.showAnswers && not empty model.showOther}">
		<div dojoType="ConceptSearch" widgetId="${formFieldName}_search" conceptId="${initialValue}" showVerboseListing="true" showAnswers="${showAnswers}" showOther="${showOther}" <c:if test="${conceptFieldTag_anyAnswers == 'true'}">performInitialSearch="true"</c:if>></div>
	</c:when>
	<c:when test="${not empty model.showAnswers}">
		<div dojoType="ConceptSearch" widgetId="${formFieldName}_search" conceptId="${initialValue}" showVerboseListing="true" showAnswers="${showAnswers}" <c:if test="${conceptFieldTag_anyAnswers == 'true'}">performInitialSearch="true"</c:if>></div>
	</c:when>
	<c:when test="${not empty model.showOther}">
		<div dojoType="ConceptSearch" widgetId="${formFieldName}_search" conceptId="${initialValue}" showVerboseListing="true" showOther="${showOther}"></div>
	</c:when>
	<c:otherwise>
		<div dojoType="ConceptSearch" widgetId="${formFieldName}_search" conceptId="${initialValue}" showVerboseListing="true"></div>
	</c:otherwise>
</c:choose>
<div dojoType="OpenmrsPopup" widgetId="${formFieldName}_selection" hiddenInputName="${formFieldName}" searchWidget="${formFieldName}_search" searchTitle="${searchLabel}" otherValue="${otherValue}"></div>
