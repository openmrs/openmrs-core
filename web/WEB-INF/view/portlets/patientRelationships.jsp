<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.User" %>
<%@ page import="org.openmrs.Person" %>
<%@ page import="org.openmrs.Relationship" %>
<%@ page import="org.openmrs.RelationshipType" %>
<%@ page import="org.openmrs.api.context.Context" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>

<openmrs:htmlInclude file="/scripts/easyAjax.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRRelationshipService.js" />
<openmrs:htmlInclude file="/dwr/engine.js" />
<openmrs:htmlInclude file="/dwr/util.js" />
<openmrs:htmlInclude file="/scripts/dojoConfig.js" />
<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.PersonSearch");
	dojo.require("dojo.widget.openmrs.OpenmrsPopup");
	
	dojo.addOnLoad( function() {
		dojo.event.topic.subscribe("add_rel_target_search/select", 
			function(msg) {
				if (msg) {
					var person = msg.objs[0];
					var personPopup = dojo.widget.manager.getWidgetById("add_rel_target_selection");

					var displayString = person.personName;
					personPopup.displayNode.innerHTML = displayString;
					personPopup.hiddenInputNode.value = person.personId;
				}
			}
		);
	})
	
	function refreshRelationships() {
		DWRRelationshipService.getRelationships(${model.personId}, null, function(rels) {
				DWRUtil.removeAllOptions('new_relationships');
				if (rels.length == 0) {
					DWRUtil.addOptions('new_relationships', [ '<spring:message code="general.none" javaScriptEscape="true" />' ]);
				}
				for (var i = 0; i < rels.length; ++i) {
					var rel = rels[i];
					var relId = rel.relationshipId;
					var str = '';
					if (rel.personAId == ${model.personId}) {
						str = rel.bIsToA + ': ';
						if (rel.personBType == 'Patient')
							str += '<a href="patientDashboard.form?patientId=' + rel.personBId + '">';
						str += rel.personB;
						if (rel.personBType == 'Patient')
							str += '</a>';
					} else if (rel.personBId == ${model.personId}) {
						str = rel.aIsToB + ': ';
						if (rel.personAType == 'Patient')
							str += '<a href="patientDashboard.form?patientId=' + rel.personAId + '">';
						str += rel.personA;
						if (rel.personAType == 'Patient')
							str += '</a>';
					}
					str += ' <a id="del_rel_' + relId + '" href="javascript:showDiv(\'voidRel' + relId + '\'); hideDiv(\'del_rel_' + relId + '\');"><spring:message code="general.deleteLink" javaScriptEscape="true" /></a>';
					str += ' <span style="display: none; border: 1px black dashed; margin: 2px" id="voidRel' + relId + '">';
					str += ' <spring:message code="general.voidReasonQuestion" javaScriptEscape="true"/>: <input type="text" id="void_reason_' + relId + '"/>';
					str += ' <input type="button" value="<spring:message code="general.delete" javaScriptEscape="true"/>" onClick="handleDeleteRelationship(' + relId + ')"/>';
					str += ' <input type="button" value="<spring:message code="general.cancel" javaScriptEscape="true"/>" onClick="showDiv(\'del_rel_' + relId + '\'); hideDiv(\'voidRel' + relId + '\')"/>';
					str += '</span>';
					DWRUtil.addOptions('new_relationships', [ str ]);
				}
			});
	}
	
	function handleAddRelationship() {
		var personIdB = ${model.personId};
		var personIdA = DWRUtil.getValue('add_rel_target');
		var relType = DWRUtil.getValue('add_relationship_type');
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
		DWRUtil.setValue('add_rel_target', null);
		DWRUtil.setValue('add_relationship_type', null);
		hideDiv('addRelationship');
		showDiv('addRelationshipLink');
		DWRRelationshipService.createRelationship(personIdA, personIdB, relType, refreshRelationships);
	}
	
	function handleDeleteRelationship(relationshipId) {
		var reason = DWRUtil.getValue('void_reason_' + relationshipId);
		if (reason != null && reason != '') {
			DWRRelationshipService.voidRelationship(relationshipId, reason, refreshRelationships);
		}
	}
	
	function handlePickRelType(value, label) {
		DWRUtil.setValue('add_relationship_type', value);
		document.getElementById('add_relationship_name').innerHTML = label;
		showDiv('add_rel_details');
	}
</script>

<div id="patientRelationshipPortlet">
	<ul id="new_relationships">
		<li><spring:message code="general.loading"/></li>
	</ul>
	
	<a id="addRelationshipLink" href="javascript:showDiv('addRelationship'); hideDiv('addRelationshipLink');">Add a New Relationship</a>
	<div id="addRelationship" style="border: 1px black dashed; display: none">
		<spring:message code="Relationship.whatType"/>
		<div style="text-align: center; margin: 0px 0px 1em 2em">
			<c:forEach var="relType" items="${model.relationshipTypes}">
				<c:if test="${relType.aIsToB == relType.bIsToA}">
					<td align="center" colspan="2"><a href="javascript:handlePickRelType('${relType.relationshipTypeId}', '${relType.aIsToB}')">${relType.aIsToB}</a></td>
				</c:if>
				<c:if test="${relType.aIsToB != relType.bIsToA}">
					<td align="right"><a href="javascript:handlePickRelType('${relType.relationshipTypeId}', '${relType.aIsToB}')">${relType.aIsToB}</a></td>
					:
					<td align="left"><a href="javascript:handlePickRelType('${relType.relationshipTypeId}::reverse', '${relType.bIsToA}')">${relType.bIsToA}</a></td>
				</c:if>
				<br/>
			</c:forEach>
		</div>

		<span id="add_rel_details" style="display: none">		
			${model.patient.personName}
			<spring:message code="Relationship.possessive"/>
			<i><span id="add_relationship_name"><spring:message code="Relationship.whatType"/></span></i>
			<input type="hidden" id="add_relationship_type"/>
			<spring:message code="Relationship.target"/>
			<div dojoType="PersonSearch" widgetId="add_rel_target_search"></div>
			<div dojoType="OpenmrsPopup" widgetId="add_rel_target_selection" hiddenInputName="add_rel_target" searchWidget="add_rel_target_search" searchTitle="Pick a person"></div> 
		</span>
		
		<br/>
		&nbsp;&nbsp;
		<input type="button" value="<spring:message code="general.save" javaScriptEscape="true"/>" onClick="handleAddRelationship()" />
		&nbsp;&nbsp;
		<input type="button" value="<spring:message code="general.cancel" javaScriptEscape="true"/>" onClick="showDiv('addRelationshipLink'); hideDiv('addRelationship'); hideDiv('add_rel_details');" />
	</div>
	
</div>

<script type="text/javascript">
	refreshRelationships();
</script>