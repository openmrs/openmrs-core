<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="searchLabel" required="false" %>
<%@ attribute name="searchLabelCode" required="false" %>
<%@ attribute name="initialValue" required="false" %> <%-- This should be a locationId --%>
<%@ attribute name="linkUrl" required="false" %>
<%@ attribute name="callback" required="false" %>

<openmrs:htmlInclude file="/scripts/dojoConfig.js" />
<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.LocationSearch");
	dojo.require("dojo.widget.openmrs.OpenmrsPopup");
</script>

<script type="text/javascript">	
	dojo.addOnLoad( function() {
		dojo.event.topic.subscribe("${formFieldName}_search/select", 
			function(msg) {
				if (msg) {
					var loc = msg.objs[0];
					locPopup = dojo.widget.manager.getWidgetById("${formFieldName}_selection");
					locSearch = dojo.widget.manager.getWidgetById("${formFieldName}_search");
					
					locPopup.displayNode.innerHTML = '<a id="${formFieldName}_name" href="#View" <c:if test="${not empty linkUrl}">onclick="return gotoUrl(\'${linkUrl}\', ' + loc.locationId + ')"</c:if>>' + loc.name + '</a>';
					locPopup.hiddenInputNode.value = loc.locationId;
					<c:if test="${not empty callback}">
						${callback}(loc.locationId);
					</c:if>
				}
			}
		);
	})
</script>

<span dojoType="LocationSearch" widgetId="${formFieldName}_search" locationId="${initialValue}"></span>
<c:if test="${not empty searchLabelCode}">
	<span dojoType="OpenmrsPopup" widgetId="${formFieldName}_selection" hiddenInputName="${formFieldName}" searchWidget="${formFieldName}_search" searchTitle="<spring:message code="${searchLabelCode}" />"></span>
</c:if> 
<c:if test="${empty searchLabelCode}">
	<span dojoType="OpenmrsPopup" widgetId="${formFieldName}_selection" hiddenInputName="${formFieldName}" searchWidget="${formFieldName}_search" searchTitle="${searchLabel}"></span>
</c:if> 
