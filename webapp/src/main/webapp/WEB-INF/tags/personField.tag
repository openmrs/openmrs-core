<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="formFieldId" required="false" %>
<%@ attribute name="searchLabel" required="false" %>
<%@ attribute name="searchLabelCode" required="false" %>
<%@ attribute name="searchLabelArguments" required="false" %>
<%@ attribute name="roles" required="false" %>
<%@ attribute name="initialValue" required="false" %> <%-- This should be a personId --%>
<%@ attribute name="linkUrl" required="false" %>
<%@ attribute name="callback" required="false" %>
<%@ attribute name="canAddNewPerson" required="false" %>
<%@ attribute name="useOnKeyDown" required="false" %>

<openmrs:htmlInclude file="/scripts/dojoConfig.js" />
<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRPersonService.js" />


<script type="text/javascript">
	
	dojo.require("dojo.widget.openmrs.PersonSearch");
	dojo.require("dojo.widget.openmrs.OpenmrsPopup");
	
	dojo.addOnLoad( function() {
		dojo.event.topic.subscribe("${formFieldName}_search/select", 
			function(msg) {
				if (msg) {
					var person = msg.objs[0];
					var personPopup = dojo.widget.manager.getWidgetById("${formFieldName}_selection");

					var displayString = person.personName;
					<c:if test="${not empty linkUrl}">
						displayString = '<a id="${formFieldName}_name" href="#View" onclick="return gotoPerson(\'${linkUrl}\', ' + person.personId + ')">' + displayString + '</a>';
					</c:if>
					personPopup.displayNode.innerHTML = displayString;
					
					personPopup.hiddenInputNode.value = person.personId;

					<c:if test="${not empty callback}">
						var relPrefix = "boxForRelType_";
						var relType = "${formFieldName}";
						if ( relType.indexOf(relPrefix) >= 0 ) {
							relType = relType.substring(relPrefix.length);
						}
						${callback}(relType, person);
					</c:if>
				}
			}
		);
	})
		
	function gotoPerson(url, pId) {
		if (url === null || url === '') {
			return false;
		} else {
			window.location = url + "?personId=" + pId;
		}
		return false;
	}
</script>

<div dojoType="PersonSearch" widgetId="${formFieldName}_search" personId="${initialValue}" roles="${roles}" canAddNewPerson="${canAddNewPerson}" useOnKeyDown="${useOnKeyDown}" ></div>
<c:if test="${not empty searchLabelCode}">
	<div dojoType="OpenmrsPopup" widgetId="${formFieldName}_selection" hiddenInputName="${formFieldName}" hiddenInputId="${formFieldId}" searchWidget="${formFieldName}_search" searchTitle="<spring:message code="${searchLabelCode}" arguments="${searchLabelArguments}" />" ></div>
</c:if> 
<c:if test="${empty searchLabelCode}">
	<div dojoType="OpenmrsPopup" widgetId="${formFieldName}_selection" hiddenInputName="${formFieldName}" hiddenInputId="${formFieldId}" searchWidget="${formFieldName}_search" searchTitle="${searchLabel}" ></div>
</c:if> 
