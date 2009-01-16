<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:htmlInclude file="/scripts/easyAjax.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRRelationshipService.js" />
<openmrs:htmlInclude file="/dwr/util.js" />
<openmrs:htmlInclude file="/scripts/dojoConfig.js" />
<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.PersonSearch");
	dojo.require("dojo.widget.openmrs.OpenmrsPopup");
	
	function callbackAfterSelect(relType, person) {
		var personPopup = dojo.widget.manager.getWidgetById("add_rel_target_selection");

		var displayString = person.personName;
		personPopup.displayNode.innerHTML = displayString;
		personPopup.hiddenInputNode.value = person.personId;
	}
	
	function refreshRelationships() {
		DWRRelationshipService.getRelationships(${model.personId}, null, refreshRelationshipsCallback);
	}
	
	function refreshRelationshipsInitial() {
		var rels = new Array();
		var rel;
		<c:forEach var="rel" items="${model.personRelationships}">
			rel = new Object();
			rel.relationshipId = ${rel.relationshipId};
			rel.personA = '${fn:replace(rel.personA.personName, "'", "\\'")}';
			rel.personB = '${fn:replace(rel.personB.personName, "'", "\\'")}';
			rel.aIsToB = '${fn:replace(rel.relationshipType.aIsToB, "'", "\\'")}';
			rel.bIsToA = '${fn:replace(rel.relationshipType.bIsToA, "'", "\\'")}';
			rel.personAId = ${rel.personA.personId};
			rel.personBId = ${rel.personB.personId};
			rel.personAIsPatient = ${rel.personA.patient};
			rel.personBIsPatient = ${rel.personB.patient};
			rels.push(rel);
		</c:forEach>
		refreshRelationshipsCallback(rels);
	}
	
	function refreshRelationshipsCallback(rels) {
		dwr.util.removeAllOptions('new_relationships');
		if (rels.length == 0) {
			dwr.util.addOptions('new_relationships', [ '<spring:message code="general.none" javaScriptEscape="true" />' ]);
		}
		for (var i = 0; i < rels.length; ++i) {
			var rel = rels[i];
			var relId = rel.relationshipId;
			var str = '';
			if (rel.personAId == ${model.personId}) {
				str = rel.bIsToA + ': ';
				if (rel.personBIsPatient)
					str += '<a href="patientDashboard.form?patientId=' + rel.personBId + '">' + rel.personB + '</a>';
				else
					str += '<a href="personDashboard.form?personId=' + rel.personBId + '">' + rel.personB + '</a>';
			} else if (rel.personBId == ${model.personId}) {
				str = rel.aIsToB + ': ';
				if (rel.personAIsPatient)
					str += '<a href="patientDashboard.form?patientId=' + rel.personAId + '">' + rel.personA + '</a>';
				else
					str += '<a href="personDashboard.form?personId=' + rel.personAId + '">' + rel.personA + '</a>';
			}
			str += '&nbsp;<a id="del_rel_' + relId + '" href="javascript:showDiv(\'voidRel' + relId + '\'); hideDiv(\'del_rel_' + relId + '\');"><spring:message code="general.deleteLink" javaScriptEscape="true" /></a>';
			str += ' <span style="display: none; border: 1px black dashed; margin: 2px" id="voidRel' + relId + '">';
			str += ' <spring:message code="general.voidReasonQuestion" javaScriptEscape="true"/>: <input type="text" id="void_reason_' + relId + '"/>';
			str += ' <input type="button" value="<spring:message code="general.delete" javaScriptEscape="true"/>" onClick="handleDeleteRelationship(' + relId + ')"/>';
			str += ' <input type="button" value="<spring:message code="general.cancel" javaScriptEscape="true"/>" onClick="showDiv(\'del_rel_' + relId + '\'); hideDiv(\'voidRel' + relId + '\')"/>';
			str += '</span>';
			dwr.util.addOptions('new_relationships', [ str ], {escapeHtml: false});
		}
	}
	
	function handleAddRelationship() {
		var personIdB = ${model.personId};
		var personPopup = dojo.widget.manager.getWidgetById("add_rel_target_selection");
		var personIdA = personPopup.hiddenInputNode.value;
		var relType = dwr.util.getValue('add_relationship_type');
		if (relType == null || relType == '' || personIdA == null || personIdA == '' || personIdB == null || personIdB == '') {
			window.alert('<spring:message code="Relationship.error.everything" javaScriptEscape="true"/>');
			return;
		}
		if (personIdA == personIdB) {
			window.alert('<spring:message code="Relationship.error.same" javaScriptEscape="true"/>');
		}
		var reverseIndex = relType.indexOf('::reverse');
		if (reverseIndex > 0) {
			relType = relType.substring(0, reverseIndex);
			var temp = personIdA;
			personIdA = personIdB;
			personIdB = temp;
		}
		personPopup.hiddenInputNode.value = "";
		dwr.util.setValue('add_relationship_type', null);
		hideDiv('addRelationship');
		showDiv('addRelationshipLink');
		DWRRelationshipService.createRelationship(personIdA, personIdB, relType, refreshRelationships);
	}
	
	function handleDeleteRelationship(relationshipId) {
		var reason = dwr.util.getValue('void_reason_' + relationshipId);
		if (reason != null && reason != '') {
			DWRRelationshipService.voidRelationship(relationshipId, reason, refreshRelationships);
		}
	}
	
	function handlePickRelType(value, label) {
		dwr.util.setValue('add_relationship_type', value);
		document.getElementById('add_relationship_name').innerHTML = label;
		showDiv('add_rel_details');
	}
</script>

<div id="patientRelationshipPortlet">
	<ul id="new_relationships">
		<li><spring:message code="general.loading"/></li>
	</ul>
	
	<a id="addRelationshipLink" href="javascript:showDiv('addRelationship'); hideDiv('addRelationshipLink');"><spring:message code="Relationship.add"/></a>
	<div id="addRelationship" style="border: 1px black dashed; display: none">
		<spring:message code="Relationship.whatType"/>
		<table style="margin: 0px 0px 1em 2em;">
			<c:forEach var="relType" items="${model.relationshipTypes}">
				<tr>
					<c:choose>
						<c:when test="${relType.aIsToB == relType.bIsToA}">
							<td style="text-align: center; white-space: nowrap" align="center" colspan="3"><a href="javascript:handlePickRelType('${relType.relationshipTypeId}', '${relType.aIsToB}')">${relType.aIsToB}</a></td>
						</c:when>
						<c:otherwise>
							<td style="text-align: right; white-space: nowrap; width: 49%"><a onclick="handlePickRelType('${relType.relationshipTypeId}', '${relType.aIsToB}')">${relType.aIsToB}</a></td>
							<td width="2%">:</td>
							<td style="text-align: left; white-space: nowrap; width: 49%"><a onclick="handlePickRelType('${relType.relationshipTypeId}::reverse', '${relType.bIsToA}')">${relType.bIsToA}</a></td>
						</c:otherwise>
					</c:choose>
				</tr>
			</c:forEach>
		</table>

		<span id="add_rel_details" style="display: none">
			${model.person.personName}
			<spring:message code="Relationship.possessive"/>
			<i><span id="add_relationship_name"><spring:message code="Relationship.whatType"/></span></i>
			<input type="hidden" id="add_relationship_type"/>
			<spring:message code="Relationship.target"/>
			<openmrs_tag:personField formFieldName="add_rel_target" searchLabel="Find a Person" useOnKeyDown="${model.useOnKeyDown}" callback="callbackAfterSelect" canAddNewPerson="true" />
		</span>
		
		<br/>
		&nbsp;&nbsp;
		<input type="button" value="<spring:message code="general.save" javaScriptEscape="true"/>" onClick="handleAddRelationship()" />
		&nbsp;&nbsp;
		<input type="button" value="<spring:message code="general.cancel" javaScriptEscape="true"/>" onClick="showDiv('addRelationshipLink'); hideDiv('addRelationship'); hideDiv('add_rel_details');" />
	</div>
	
</div>

<script type="text/javascript">
	refreshRelationshipsInitial();
</script>