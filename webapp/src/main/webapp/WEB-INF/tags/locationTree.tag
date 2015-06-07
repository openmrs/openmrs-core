<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="id" required="false" type="java.lang.String" %>
<%@ attribute name="formFieldName" required="true" type="java.lang.String" %>
<%@ attribute name="initialValue" required="false" type="org.openmrs.Location" %>
<%@ attribute name="selectLeafOnly" required="false" type="java.lang.Boolean"%>
<%@ attribute name="selectableTags" required="false" type="java.lang.String"%>
<%@ attribute name="startFromTag" required="false" type="java.lang.String"%>


<c:if test="${empty id}">
	<c:set var="id" value='<%= "locTree" + Math.round(Math.random() * 1000) %>'/>
</c:if>

<c:if test="${empty selectLeafOnly}">
	<c:set var="selectLeafOnly" value="false"/>
</c:if>

<c:url var="jsonUrl" value="/q/locationHierarchy.json">
	<c:param name="selectLeafOnly" value="${selectLeafOnly}"/>
	<c:if test="${not empty selectableTags}">
		<c:forTokens var="tag" items="${selectableTags}" delims=",">
			<c:param name="selectableTags" value="${tag}"/>
		</c:forTokens>
	</c:if>
	<c:if test="${not empty startFromTag}">
		<c:param name="startFromTag" value="${startFromTag}"/>
	</c:if>
</c:url>

<openmrs:htmlInclude file="/scripts/jquery/jsTree/jquery.tree.min.js" />
<openmrs:htmlInclude file="/scripts/jquery/jsTree/themes/classic/style.css" />

<script type="text/javascript">
$j(document).ready(function() {
	$j('#${id}').tree({
			data: {
				type: "json",
				opts: {
					url: "${jsonUrl}"
				}
			},
			types: {
				"default" : {
					clickable : false,
					renameable : false,
					deletable : false,
					creatable : false,
					draggable : false,
					max_children : -1,
					max_depth : -1,
					valid_children : "all"
				},
				"nulloption" : {
					clickable : true,
					icon : { position : '-16px -16px' }
				},
				"selectable" : {
					clickable : true
				}
			},
			ui: {
				theme_name: "classic"
			},
			callback: {
				onselect: function(NODE, TREE_OBJ) {
					$j('#${id}_display').text($j(NODE).children(":first").text()).html();
					$j('#${id}_hidden_input').val($j(NODE).attr('id'));
					locationTreeTag_hideTree('${id}');
				}
			}
		});
	<c:if test="${empty initialValue}">
		$j('#${id}_display').html('<openmrs:message code="general.none" javaScriptEscape="true"/>');
	</c:if>
	<c:if test="${not empty initialValue}">
		$j('#${id}_hidden_input').val(${initialValue.locationId});
		$j('#${id}_display').html('<openmrs:message javaScriptEscape="true" text="${initialValue.name}"/>');
	</c:if>
	$j('#${id}_button').click(function() { locationTreeTag_showTree('${id}'); });
});

function locationTreeTag_getLocationId(id) {
	var temp = $j('#' + id + '_hidden_input').val();
	return temp == '' ? null : temp;
}

function locationTreeTag_showTree(id) {
	if( ! $j('#' + id).is(':visible') ) {
		var idToSelect = locationTreeTag_getLocationId(id);
		if (idToSelect) {
			locationTreeTag_revealNode('${id}', idToSelect);
		}
	}
	$j('#' + id).slideToggle('fast');
}

function locationTreeTag_hideTree(id) {
	$j('#' + id).slideUp('fast');
}

function locationTreeTag_revealNode(id, nodeId) {
	var myTree = $j.tree.reference('#' + id);
	var node = myTree.get_node('[id=' + nodeId + ']');
	myTree.select_branch(node);
	while (node) {
		myTree.open_branch(node);
		node = myTree.parent(node);
	}
}
</script>

<input type="hidden" name="${formFieldName}" id="${id}_hidden_input"/>
<span class="button" id="${id}_button" style="position: relative">
	<span id="${id}_display"></span>
	<div id="${id}" class="popupSearchForm" style="position: absolute; display: none"></div>
</span>
