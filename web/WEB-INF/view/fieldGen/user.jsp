<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:htmlInclude file="/scripts/dojoConfig.js" />
<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />
<openmrs:htmlInclude file="/scripts/dojoUserSearchIncludes.js" />

<script type="text/javascript">
	dojo.addOnLoad( function() {
		dojo.event.topic.subscribe("${model.formFieldName}_search/select", 
			function(msg) {
				if (msg) {
					var user = msg.objs[0];
					var userPopup = dojo.widget.manager.getWidgetById("${model.formFieldName}_selection");
					userPopup.displayNode.innerHTML = '<a id="${model.formFieldName}_name" href="#View" >' + (user.firstName ? user.firstName : '') + ' ' + (user.middleName ? user.middleName : '') + ' ' + (user.lastName ? user.lastName : '') + '</a>';
					userPopup.hiddenInputNode.value = user.userId;
				}
			}
		);
	})
</script>

<div dojoType="UserSearch" widgetId="${model.formFieldName}_search" userId="${model.initialValue}" roles="${model.roles}"></div>
<div dojoType="OpenmrsPopup" widgetId="${model.formFieldName}_selection" hiddenInputName="${model.formFieldName}" searchWidget="${model.formFieldName}_search" searchTitle="<spring:message code="User.find" />"></div>
