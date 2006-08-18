<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="searchLabel" required="false" %>
<%@ attribute name="roles" required="false" %>
<%@ attribute name="initialValue" required="false" %>
<%@ attribute name="linkUrl" required="false" %>

<openmrs:htmlInclude file="/scripts/dojoConfig.js" />
<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />
<openmrs:htmlInclude file="/scripts/dojoUserSearchIncludes.js" />

<script type="text/javascript">
	
	dojo.addOnLoad( function() {
		dojo.event.topic.subscribe("${formFieldName}_search/select", 
			function(msg) {
				if (msg) {
					var user = msg.objs[0];
					var userPopup = dojo.widget.manager.getWidgetById("${formFieldName}_selection");
					userPopup.displayNode.innerHTML = '<a id="${formFieldName}_name" href="#View" <c:if test="${not empty linkUrl}">onclick="return gotoUrl("${linkUrl}", ' + user.userId + ')"</c:if>>' + (user.firstName ? user.firstName : '') + ' ' + (user.middleName ? user.middleName : '') + ' ' + (user.lastName ? user.lastName : '') + '</a>';
					userPopup.hiddenInputNode.value = user.userId;
					if ( handleChangeRelationById ) {
						var relPrefix = "boxForRelType_";
						var relType = "${formFieldName}";
						if ( relType.indexOf(relPrefix) >= 0 ) {
							relType = relType.substring(relPrefix.length);
						}
						handleChangeRelationById(relType, user.userId);
					}
				}
			}
		);
	})
</script>

<div class="userSearchLabel">${searchLabel}</div>
<div dojoType="UserSearch" widgetId="${formFieldName}_search" userId="${initialValue}" roles="${roles}"></div>
<div dojoType="OpenmrsPopup" widgetId="${formFieldName}_selection" hiddenInputName="${formFieldName}" searchWidget="${formFieldName}_search" searchTitle="${searchLabel}"></div>
