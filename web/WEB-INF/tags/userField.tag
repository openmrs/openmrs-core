<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="searchLabel" required="false" %>
<%@ attribute name="roles" required="false" %>
<%@ attribute name="initialValue" required="false" %>
<%@ attribute name="linkUrl" required="false" %>

<script type="text/javascript">
	
	dojo.addOnLoad( function() {
		dojo.event.topic.subscribe("${formFieldName}_search/select", 
			function(msg) {
				if (msg) {
					var user = msg.objs[0];
					var userPopup = dojo.widget.manager.getWidgetById("${formFieldName}_selection");
					userPopup.displayNode.innerHTML = '<a id="${formFieldName}_name" href="#View" onclick="return gotoUrl("${linkUrl}", ' + user.userId + ')">' + (user.firstName ? user.firstName : '') + ' ' + (user.middleName ? user.middleName : '') + ' ' + (user.lastName ? user.lastName : '') + '</a>';
					userPopup.hiddenInputNode.value = user.userId;
				}
			}
		);
	})
</script>

<span class="userSearchLabel">${searchLabel}</span>
<div dojoType="UserSearch" widgetId="${formFieldName}_search" userId="${initialValue}" roles="${roles}" class="userSearchField"></div>
<div dojoType="OpenmrsPopup" widgetId="${formFieldName}_selection" hiddenInputName="${formFieldName}" searchWidget="${formFieldName}_search" searchTitle="${searchLabel}" class="userSearchPopup"></div>
