<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="searchLabel" required="false" %>
<%@ attribute name="roles" required="false" %>
<%@ attribute name="initialValue" required="false" %>
<%@ attribute name="linkUrl" required="false" %>

<script type="text/javascript">
	var djConfig = {isDebug: true};
</script>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js"></openmrs:htmlInclude>

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.UserSearch");
	dojo.require("dojo.widget.openmrs.OpenmrsPopup");
	dojo.hostenv.writeIncludes();
	
	dojo.addOnLoad( function() {
		dojo.event.topic.subscribe("${formFieldName}_search/select", 
			function(msg) {
				if (msg) {
					var user = msg.objs[0];
					var userPopup = dojo.widget.manager.getWidgetById("${formFieldName}_selection");
					userPopup.displayNode.innerHTML = '<a id="${formFieldName}_name" href="#View" onclick="return gotoUser(null, ' + user.userId + ')">' + (user.firstName ? user.firstName : '') + ' ' + (user.middleName ? user.middleName : '') + ' ' + (user.lastName ? user.lastName : '') + '</a>';
					userPopup.hiddenInputNode.value = user.userId;
				}
			}
		);
	})
</script>

<script type="text/javascript">
	function gotoUser(tagName, userId) {
		if ('${linkUrl}' != '') {
			if (userId == null) {
				userId = $(tagName).value;
			}
			window.location = "${linkUrl}?userId=" + userId;
		}
		return false;
	}
</script>

<table width="100%">
	<tr><td>Select User:</td>
	<td>
		<div dojoType="UserSearch" widgetId="${formFieldName}_search" userId="${initialValue}" roles="${roles}"></div>
		<div dojoType="OpenmrsPopup" widgetId="${formFieldName}_selection" hiddenInputName="${formFieldName}" searchWidget="${formFieldName}_search" searchTitle="${searchLabel}"></div>
	</td></tr>
</table>
