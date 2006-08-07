<%@ include file="/WEB-INF/template/include.jsp" %>

<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/easyAjax.js"></script>

<script type="text/javascript" src="<%= request.getContextPath() %>/dwr/interface/DWRRelationshipService.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/dwr/engine.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/dwr/util.js"></script>

<div style="border: 1px black solid">
	<u><b><spring:message code="Program.title"/></b></u>
	<openmrs:portlet url="patientPrograms" id="patientPrograms" patientId="${patient.patientId}" parameters="allowEdits=true"/>
</div>

<script language="JavaScript">
	function doCreateRelationship() {
		var fromId = document.getElementById('newRel_fromPerson').value;
		var toId = 'Patient.' + ${model.patientId};
		var relTypeId = document.getElementById('newRel_type').value;
		if (fromId != null && fromId != '' && toId != null && toId != '' && Number(relTypeId) > 0) {
			DWRRelationshipService.createRelationship(fromId, toId, relTypeId, refreshPage);
		} else {
			window.alert('Error');
		}
	}
	function refreshRelationshipTypeList(id) {
		var list = document.getElementById(id);
		DWRUtil.removeAllOptions(id);
		DWRUtil.addOptions(id, ["Select a relationship type..."]);
		DWRRelationshipService.getRelationshipTypes(function(relTypes) {
				DWRUtil.addOptions(id, relTypes, 'id', 'name');
				if (relTypes.length == 1) {
					DWRUtil.setValue(id, relTypes[0].id);
				}
			});
	}
</script>

TEST: ${model.patient.person} <openmrs_tag:person person="${model.patient.person}"/>

<div style="border: 1px black solid">
	<b><u>Relationships:</u></b>
	<br/>
	<c:forEach var="rel" items="${model.patientRelationships}">
		<c:choose>
			<c:when test="${rel.relative.patient.patientId == model.patientId}">
				<openmrs_tag:relationshipType relationshipType="${rel.relationship}"/>:
				<openmrs_tag:person person="${rel.person}"/>
			</c:when>
			<c:otherwise>
				<i>
				<openmrs_tag:relationshipType relationshipType="${rel.relationship}"/>
				of <openmrs_tag:person person="${rel.relative}"/>
				</i>
			</c:otherwise>
		</c:choose>
		
		<br/>
	</c:forEach>
	New relationship:
	<small>(personId or Patient.patientId or User.userId)</small> <input type="text" id="newRel_fromPerson"/>
	is the <select id="newRel_type"></select>
	of the current patient
	<input type="button" value="<spring:message code="general.add"/>" onClick="doCreateRelationship()"/>
</div>

<script language="JavaScript">
	refreshRelationshipTypeList('newRel_type');
</script>
