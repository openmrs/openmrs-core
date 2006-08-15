<%@ include file="/WEB-INF/template/include.jsp" %>
<%--
showFrom=true:	show relationships where this patient is the *person*
showTo=true:	show relationships where this patient is the *relative*

showTypes=relationshipType1,relationshipType2,...,relationshipTypeN
	show a widget for each type specified, regardless of whether any relationship of that type is defined yet.
	For example:
		--------------------------------------
		| Accompagnateur         | Bob Smith |
		| Primary Care Physician |           |
		--------------------------------------

showOtherTypes=true: show a listing of relationships of types not listed in showTypes
allowEditShownTypes=true: have a [change] button for the shown types (for example to let you have just one Accompagnateur at a time)
allowAddShownTypes=true:  have an [add] button for the shown types
allowAddOtherTypes=true:  have an [add] button that lets you specify type (but not from the ones shown)
allowVoid=true:			  let the user void existing relationship

refreshOnChange=relationshipType1,...relationshipTypeN
refreshOnChange=*
	upon editing the specified relationship type (or any relationship type) reload the page. NOT YET IMPLEMENTED
--%>

<style>

</style>

<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/easyAjax.js"></script>

<script type="text/javascript" src="<%= request.getContextPath() %>/dwr/interface/DWRRelationshipService.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/dwr/engine.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/dwr/util.js"></script>

<script language="JavaScript">
	var typesToShow = "${model.showTypes}".split(",");
	var typesToRefresh = "${model.refreshOnChange}".split(",");
	
	<c:if test="${model.showOtherTypes == 'true' && model.allowAddOtherTypes=='true'}">
		DWRRelationshipService.getRelationshipTypes(refreshAddRelationshipBoxes);
	</c:if>
	
	function refreshShownTypes() {
		for (var i = 0; i < typesToShow.length; ++i) {
			var relType = typesToShow[i];
			document.getElementById('boxForRelType_' + relType).innerHTML = '<spring:message code="general.none"/>';
		}
		for (var i = 0; i < typesToShow.length; ++i) {
			var relType = typesToShow[i];
			DWRRelationshipService.getRelationshipsToPerson(${model.personId}, relType, function(relationships) {
					for (var j = 0; j < relationships.length; ++j) {
						var rel = relationships[j];
						var tmp = $('boxForRelType_' + rel.relationshipType).innerHTML;
						if (j == 0) {
							tmp = '';
						}
						if (tmp != '') { tmp += '<br/>'; }
						tmp += rel.fromName;
						<c:if test="${model.allowVoid}">
							tmp += ' <a href="javascript:handleVoidRelationship(' + rel.relationshipId + ')" class="voidButton">[x]</a>';
						</c:if>
						$('boxForRelType_' + rel.relationshipType).innerHTML = tmp;
					}
				});
		}
	}
	
	var otherTypesCellFuncs = [
		function(rel) { return rel.relationshipType + ':'; },
		function(rel) { return rel.fromName
							<c:if test="${model.allowVoid == 'true'}">
								+ '<a href="javascript:handleVoidRelationship(' + rel.relationshipId + ')" class="voidButton">[x]</a>'
							</c:if>
						; }
	];

	function refreshOtherTypes() {
		DWRRelationshipService.getRelationshipsToPerson(${model.personId}, null, handleRefreshOtherTypes);
	}
	
	function contains(collection, value) {
		for (var i = 0; i < collection.length; ++i)
			if (collection[i] == value)
				return true;
		return false;
	}
	
	function handleRefreshOtherTypes(rels) {
		var notShownAlready = new Array();
		for (var i = 0; i < rels.length; ++i) {
			if (!contains(typesToShow, rels[i].relationshipType)) {
				notShownAlready.push(rels[i]);
			}
		}
		DWRUtil.removeAllRows('PR_otherRelationshipsBody');
		DWRUtil.addRows('PR_otherRelationshipsBody', notShownAlready, otherTypesCellFuncs,
			{
				cellCreator:function(options) {
				    var td = document.createElement("td");
				    // TODO: why is options not what it's documented to be?
				    if (options.cellNum == 0) {
				    	td.className = "patientSummaryLabel";
				    } else if (options.cellNum == 1) {
				    	td.className = "patientSummaryValue";
				    }
				    return td;
				}
			});
	}
	
	function handleVoidRelationship(relationshipId) {
		DWRRelationshipService.voidRelationship(relationshipId, refreshAllRelationships);
	}
	
	function handleChangeRelation(relType, fromBoxId) {
		var fromPersonId = DWRUtil.getValue($(fromBoxId));
		var toPersonId = ${model.personId};
		$(fromBoxId).value = '';
		DWRRelationshipService.setRelationshipTo(fromPersonId, toPersonId, relType, refreshAllRelationships);
	}
	
	function handleAddRelationship(fromBoxId, relTypeBoxId) {
		var fromPersonId = DWRUtil.getValue($(fromBoxId));
		var toPersonId = ${model.personId};
		var relTypeId = DWRUtil.getValue($(relTypeBoxId));
		$(fromBoxId).value = '';
		DWRRelationshipService.createRelationship(fromPersonId, toPersonId, relTypeId, refreshAllRelationships);
	}
	
	function refreshAllRelationships() {
		<c:if test="${model.showTypes != null}">
			refreshShownTypes();
		</c:if>
		<c:if test="${model.showOtherTypes == 'true'}">
			refreshOtherTypes();
		</c:if>
	}
	
	function refreshAddRelationshipBoxes(relTypes) {
		var typesNotAlreadyShown = new Array();
		for (var i = 0; i < relTypes.length; ++i) {
			if (!contains(typesToShow, relTypes[i].name)) {
				typesNotAlreadyShown.push(relTypes[i]);
			}
		}
		DWRUtil.removeAllOptions('PR_otherAddRelType');
		DWRUtil.addOptions('PR_otherAddRelType', ["Select a relationship type..."]);
		DWRUtil.addOptions('PR_otherAddRelType', typesNotAlreadyShown, 'id', 'name');
		if (typesNotAlreadyShown.length == 1) {
			DWRUtil.setValue('PR_otherAddRelType', typesNotAlreadyShown[0].id);
		}
	}
	
</script>

<c:if test="${model.showTypes != null || model.showOtherTypes == 'true'}">
	<table>
	<c:if test="${model.showTypes != null}">
		<tbody>
		<c:forTokens var="relType" items="${model.showTypes}" delims=",">
			<tr valign="bottom">
				<td class="patientSummaryLabel">${relType}:</td>
				<td id="boxForRelType_${relType}" class="patientSummaryValue"></td>
				<c:if test="${model.allowEditShownTypes == 'true'}">
					<td>
						&nbsp;&nbsp;&nbsp;&nbsp;
						<span class="patientSummaryLabel">Change to:</span>
						<input type="text" id="change_${relType}_to" />
						<input type="button" value="Change" onClick="handleChangeRelation('${relType}', 'change_${relType}_to')" />
					</td>
				</c:if>
			</tr>
		</c:forTokens>
		</tbody>
	</c:if>
	<c:if test="${model.showOtherTypes == 'true'}">
		<tbody id="PR_otherRelationshipsBody"></tbody>
	</c:if>
	</table>
	<c:if test="${model.showOtherTypes == 'true' && model.allowAddOtherTypes == 'true'}">
		Add relationship:
		<small>(personId or Patient.patientId or User.userId)</small> <input type="text" id="PR_otherAddRelFromPerson"/>
		is the <select id="PR_otherAddRelType"></select>
		<input type="button" value="<spring:message code="general.add"/>" onClick="handleAddRelationship('PR_otherAddRelFromPerson', 'PR_otherAddRelType')"/>
	</c:if>

</c:if>

<script language="JavaScript">
	refreshAllRelationships();
</script>