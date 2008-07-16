<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="searchLabel" required="false" %>
<%@ attribute name="searchLabelCode" required="false" %>
<%@ attribute name="roles" required="false" %>
<%@ attribute name="initialValue" required="false" %> <%-- This should be a userId --%>
<%@ attribute name="linkUrl" required="false" %>
<%@ attribute name="callback" required="false" %>

<openmrs:htmlInclude file="/scripts/dojoConfig.js" />
<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.UserSearch");
	dojo.require("dojo.widget.openmrs.OpenmrsPopup");
</script>

<script type="text/javascript">
	
	dojo.addOnLoad( function() {
		
		dojo.event.topic.subscribe("${formFieldName}_search/select", 
			function(msg) {
				if (msg) {
					var user = msg.objs[0];
					var userPopup = dojo.widget.manager.getWidgetById("${formFieldName}_selection");
					
					var displayString = user.personName;
					<c:if test="${not empty linkUrl}">
						displayString = '<a id="${formFieldName}_name" href="#View" onclick="return gotoUser(\'${linkUrl}\', ' + user.userId + ')">' + displayString + '</a>';
					</c:if>
					userPopup.displayNode.innerHTML = displayString;
					
					userPopup.displayNode.innerHTML = '<a id="${formFieldName}_name" href="#View" <c:if test="${not empty linkUrl}">onclick="return gotoUser(\'${linkUrl}\', ' + user.userId + ')"</c:if>>' + user.personName + '</a>';
					userPopup.hiddenInputNode.value = user.userId;
					<c:if test="${not empty callback}">
						var relPrefix = "boxForRelType_";
						var relType = "${formFieldName}";
						if ( relType.indexOf(relPrefix) >= 0 ) {
							relType = relType.substring(relPrefix.length);
						}
						${callback}(relType, user.userId);
					</c:if>
				}
			}
		);
	})
	
</script>

<div class="userSearchLabel">${searchLabel}</div>
<div dojoType="UserSearch" widgetId="${formFieldName}_search" userId="${initialValue}" roles="${roles}"></div>
<c:if test="${not empty searchLabelCode}">
	<div dojoType="OpenmrsPopup" widgetId="${formFieldName}_selection" hiddenInputName="${formFieldName}" searchWidget="${formFieldName}_search" searchTitle="<spring:message code="${searchLabelCode}" />"></div>
</c:if> 
<c:if test="${empty searchLabelCode}">
	<div dojoType="OpenmrsPopup" widgetId="${formFieldName}_selection" hiddenInputName="${formFieldName}" searchWidget="${formFieldName}_search" searchTitle="${searchLabel}"></div>
</c:if> 
