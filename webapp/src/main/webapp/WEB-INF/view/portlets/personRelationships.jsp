<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:htmlInclude file="/dwr/engine.js" />
<openmrs:htmlInclude file="/dwr/util.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRRelationshipService.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRPersonService.js" />

<style type="text/css">
.relTable td {
	padding-right: 10px;
	padding-left: 10px;
}
#editRelationship th {
	text-align:right;
}
</style>

<script type="text/javascript">
	var personA=null;
	
	$j(document).ready(function() {
		$j('#addRelationship').dialog({
			
			autoOpen: false,
			modal: true,
			title: '<spring:message code="Relationship.add" javaScriptEscape="true"/>',
			width: '50%',
			zIndex: 100,
			buttons: { 
				'<spring:message code="general.save"/>': function() { handleNewRelationship(); },
				'<spring:message code="general.cancel"/>': function() { $j(this).dialog("close"); }
			}
		});

		$j("#addRelationshipLink").click(function(){
			clearAddRelationship();
			$j("#addRelationship").dialog("open");
			return false;
		});
		
		$j("#add_new_person").click(function(){
			$j('#find_person').hide();
			$j('#create_new_person').show();
		});
		$j("#find_existing_person").click(function(){
			$j('#find_person').show();
			$j('#create_new_person').hide();
		});
		
		$j('#voidRelationship').dialog({
			autoOpen: false,
			modal: true,
			title: '<spring:message code="Relationship.remove" javaScriptEscape="true"/>',
			width: '50%',
			zIndex: 100,
			buttons: { 
				'<spring:message code="general.remove"/>': function() { handleVoidRelationship(); },
				'<spring:message code="general.cancel"/>': function() { $j(this).dialog("close"); }
			}
		});
		
		$j('#editRelationship').dialog({
			autoOpen: false,
			modal: true,
			title: '<spring:message code="Relationship.edit" javaScriptEscape="true"/>',
			width: '50%',
			zIndex: 100,
			buttons: { 
				'<spring:message code="general.save"/>': function() { handleEditRelationship(); },
				'<spring:message code="general.cancel"/>': function() { $j(this).dialog("close"); }
			}
		});

		refreshRelationshipsInitial();
	});

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
			rel.startDate = '<openmrs:formatDate date="${rel.startDate}" type="textbox"/>';
			rel.endDate = '<openmrs:formatDate date="${rel.endDate}" type="textbox"/>';
			rels.push(rel);
		</c:forEach>
		refreshRelationshipsCallback(rels);
	}
	
	var relTableCellFuncs = [
		function(data) { return data[1]; },
		function(data) { return data[2]; },
		function(data) { return data[3]; },
		function(data) { return data[4]; },
		function(data) {
			return '<a href="javascript:editRelationshipDialog(' + data[0] + ')" title="">' +
						'<img src="images/edit.gif" border="0" title="<spring:message code="general.edit"/>"/>' +
				   '</a>';
		},
		function(data) {
			return '<a href="javascript:voidRelationshipDialog(' + data[0] + ')" title="">' +
				'<img src="images/delete.gif" border="0" title="<spring:message code="general.remove"/>"/>' +
				'</a>';
		}
	];
	
	var relationships = {};	

	function refreshRelationshipsCallback(rels) {
		relationships = {};
		dwr.util.removeAllRows("relationshipTableContent");
		if (rels.length == 0) {
			$j("#no_relationships").html('<spring:message code="general.none" javaScriptEscape="true"/><br /><br />');
			hideDiv("relationshipTable");
			showDiv("no_relationships");
		} else {
			for (var i = 0; i < rels.length; ++i) {
				var rel = rels[i];
				var relation = rel.personAId == ${model.personId} ? rel.bIsToA : rel.aIsToB;
				var relative = '';

				if (rel.personAId == ${model.personId}) {
					if (rel.personBIsPatient)
						relative = '<a href="patientDashboard.form?patientId=' + rel.personBId + '">' + rel.personB + '</a>';
					else
						relative = '<a href="personDashboard.form?personId=' + rel.personBId + '">' + rel.personB + '</a>';
				} else if (rel.personBId == ${model.personId}) {
					if (rel.personAIsPatient)
						relative = '<a href="patientDashboard.form?patientId=' + rel.personAId + '">' + rel.personA + '</a>';
					else
						relative = '<a href="personDashboard.form?personId=' + rel.personAId + '">' + rel.personA + '</a>';
				}

				rel.desc = relative + " (" + relation + ")";
				relationships[rel.relationshipId] = rel;
				dwr.util.addRows('relationshipTableContent', 
						[ [rel.relationshipId, relative, relation, rel.startDate, rel.endDate] ], 
						relTableCellFuncs, 
						{escapeHtml: false});
			}
			hideDiv("no_relationships");
			showDiv("relationshipTable");
		}
	}
	function handleNewRelationship() {
		if ( $j('#find_person').is(':visible') ) {
			personA = $j("#add_rel_target_id").val();
	 		handleAddRelationship();
		} else {
			var starterDateString = $j("#add_birth_start_date").val();
			var birthdate = parseSimpleDate(starterDateString, '<openmrs:datePattern />');
			var age = getAge(birthdate);
			var gender = $j('input[name="add_new_gender"]:checked').val();
			var givenName = $j('#first_name').val();
			var middleName = $j('#middle_name').val();
			var familyName = $j('#family_name').val();
			if (givenName == null || givenName == '' || familyName == null || familyName == '') {
				window.alert('<spring:message code="Relationship.createPerson.nameRequired"/>');
				return;
			}
			if (gender == null || gender == '') {
				window.alert('<spring:message code="Person.gender.required"/>');
				return;
			}
			DWRPersonService.createPerson(givenName, middleName, familyName,
										  starterDateString, "<openmrs:datePattern/>", age, gender,
					 					  { callback: function(personObject) {
						 						personA=personObject.personId;				
						 						handleAddRelationship();
						 					} });
			 
			clearNewPerson();
		}
	}
	function handleAddRelationship() {
		
		var personIdB = ${model.personId};	
		var personIdA=personA;
		if (personIdA == personIdB) {
			window.alert('<spring:message code="Relationship.error.same" javaScriptEscape="true"/>');
			return;
		}
		
		var relType = dwr.util.getValue('add_relationship_type');
		if (relType == null || relType == '' || personIdA == null || personIdA == '' || personIdB == null || personIdB == '') {
			window.alert('<spring:message code="Relationship.error.everything" javaScriptEscape="true"/>');
			return;
		}
		
		var reverseIndex = relType.indexOf('::reverse');
		if (reverseIndex > 0) {
			relType = relType.substring(0, reverseIndex);
			var temp = personIdA;
			personIdA = personIdB;
			personIdB = temp;
		}
		
		var startDateString = $j("#add_rel_start_date").val();

		$j("#addRelationship").dialog("close");
		clearAddRelationship();	
		DWRRelationshipService.createRelationship(personIdA, personIdB, relType, startDateString, refreshRelationships);
	}
	

	function clearAddRelationship() {
		$j("#add_rel_target_id").val("");
		$j("#add_rel_display_id").val("");
		$j("#add_relationship_type").val("");
		$j("#add_rel_start_date").val("");
		hideDiv('add_rel_details');
	}
	function clearNewPerson() {
		$j("#first_name").val("");
		$j("#middle_name").val("");
		$j("#family_name").val("");
		$j("#add_birth_start_date").val("");
		$j('input[name="add_new_gender"]:checked').removeAttr('checked');
		$j('#find_person').show();
		$j('#create_new_person').hide();
	}
	function editRelationshipDialog(relId) {
		$j("#editRelationship .relationship_desc").html(relationships[relId].desc);
		$j("#editRelationship #edit_relationship_id").val(relId);
		$j("#editRelationship #edit_rel_start_date").val(relationships[relId].startDate);
		$j("#editRelationship #edit_rel_end_date").val(relationships[relId].endDate);
		$j("#editRelationship").dialog("open");
	}

	function handleEditRelationship() {
		var relId = $j("#editRelationship #edit_relationship_id").val();
		var startDate = $j("#editRelationship #edit_rel_start_date").val();
		var endDate = $j("#editRelationship #edit_rel_end_date").val();
		$j("#editRelationship").dialog("close");
		DWRRelationshipService.changeRelationshipDates(relId, startDate, endDate, refreshRelationships);
	}
	
	function voidRelationshipDialog(relId) {
		$j("#voidRelationship .relationship_desc").html(relationships[relId].desc);
		$j("#voidRelationship #relationship_id").val(relId);
		$j("#voidRelationship #relationship_void_reason").val("");
		$j("#relationship_empty_reason").hide();
		$j("#voidRelationship").dialog("open");
		$j("#voidRelationship #relationship_void_reason").focus();
	}

	function handleVoidRelationship() {
		var relId = $j("#voidRelationship #relationship_id").val();
		var reason = $j("#voidRelationship #relationship_void_reason").val();
		if (reason != null && reason.trim().length > 0) {
			$j("#voidRelationship").dialog("close");
			DWRRelationshipService.voidRelationship(relId, reason, refreshRelationships);
		}
		else{
			$j("#relationship_empty_reason").show();
		}
	}

	function handlePickRelType(value, label) {
		dwr.util.setValue('add_relationship_type', value);
		document.getElementById('add_relationship_name').innerHTML = label;
		showDiv('add_rel_details');
	}

    /**
	 * Caches the id of the checkbox of the selected preferred patientIdentifier
	 *	 
	 * @param elementId the id of the radioButton for the selected identifier checkbox
	 */	
	function setPrefIdentifierElementId(elementId){
		prefIdentifierElementId = elementId;			
	}

	function getAge(d, now) {
		if (d == null)
			return null;
		if (typeof(now) == 'undefined') now = new Date();
		var age = -1;
		while (now >= d) {
			age++;
			d.setFullYear(d.getFullYear() + 1);
		}
		return age;
	}
</script>

<div id="patientRelationshipPortlet">
	<div id="no_relationships">
		<spring:message code="general.loading"/><br />
	</div>

	<table style="margin: 0px 0px 1em 2em;" cellpadding="3" cellspacing="0" id="relationshipTable" class="relTable">
		<thead>
			<tr bgcolor="whitesmoke">
				<td><spring:message code="Relationship.relative"/></td>
				<td><spring:message code="Relationship.relationship"/></td>
				<td><spring:message code="Relationship.startDate"/></td>
				<td><spring:message code="Relationship.endDate"/></td>
				<td></td>
				<td></td>
			</tr>
		</thead>
		<tbody id="relationshipTableContent"></tbody>
	</table>

	<a id="addRelationshipLink" href="#"><spring:message code="Relationship.add"/></a>
	
	<div id="addRelationship">
		<spring:message code="Relationship.whatType"/>
		<table style="margin: 0px 0px 1em 2em;">
			<c:forEach var="relType" items="${model.relationshipTypes}">
				<tr>
					<c:choose>
						<c:when test="${relType.aIsToB == relType.bIsToA}">
							<td style="text-align: center; white-space: nowrap" align="center" colspan="3">
								<a href="javascript:handlePickRelType('${relType.relationshipTypeId}', '${relType.aIsToB}')">${relType.aIsToB}</a>
							</td>
						</c:when>
						<c:otherwise>
							<td style="text-align: right; white-space: nowrap; width: 49%">
								<a onclick="handlePickRelType('${relType.relationshipTypeId}', '${relType.aIsToB}')">${relType.aIsToB}</a>
							</td>
							<td width="2%">:</td>
							<td style="text-align: left; white-space: nowrap; width: 49%">
								<a onclick="handlePickRelType('${relType.relationshipTypeId}::reverse', '${relType.bIsToA}')">${relType.bIsToA}</a>
							</td>
						</c:otherwise>
					</c:choose>
				</tr>
			</c:forEach>
		</table>
		
		<span id="add_rel_details" style="display: none">
			<hr/>
			${model.person.personName}<spring:message code="Relationship.possessive"/>
			<i><span id="add_relationship_name"><spring:message code="Relationship.whatType"/></span></i>
			<input type="hidden" id="add_relationship_type"/>
			<spring:message code="Relationship.target"/>

			<div style="background-color: #e0e0e0; margin-top: 0.5em;">
				<div id="find_person">
					<button style="float: right" id="add_new_person"><spring:message code="Relationship.createPerson.switch"/></button>
					<spring:message code="Relationship.existingPerson.title"/>
					<openmrs_tag:personField formFieldName="add_rel_target" formFieldId="add_rel_target_id" displayFieldId="add_rel_display_id" searchLabel="Find a Person" />
				</div>
	
				<div id="create_new_person" style="display: none">
					<button style="float: right" id="find_existing_person" href="#"><spring:message code="Relationship.existingPerson.switch"/></button>
					<spring:message code="Relationship.createPerson.title"/>
					<table>
						<tr><td><spring:message code="PersonName.givenName"/></td><td><spring:message code="PersonName.middleName"/></td><td><spring:message code="PersonName.familyName"/></td></tr>
						<tr><td><input id="first_name" type="text"/></td><td><input id="middle_name" type="text"/></td><td><input id="family_name" type="text"/></td></tr>
					</table>
						
					<spring:message code="Person.gender"/>:
					<openmrs:forEachRecord name="gender">
						<input type="radio" name="add_new_gender" id="gender_${record.key}" value="${record.key}" />
						<label for="gender_${record.key}">
							<spring:message code="Person.gender.${record.value}"/>
						</label>
					</openmrs:forEachRecord>
					<br/>
					
					<spring:message code="Person.birthdate"/>:
					<openmrs_tag:dateField formFieldName="add_birth_start_date" startValue="" />		
				</div>
			</div>
			
			<br/>
			<spring:message code="Relationship.startDateQuestion"/>
			<openmrs_tag:dateField formFieldName="add_rel_start_date" startValue="" />
		</span>
	</div>
	
	
	<div id="editRelationship">
		<input type="hidden" id="edit_relationship_id"/>
		<table>
			<tr>
				<th><spring:message code="Relationship.relative"/>:</th>
				<td><span class="relationship_desc"></span></td>
			</tr>
			<tr>
				<th><spring:message code="Relationship.startDateLong"/>:</th>
				<td><openmrs_tag:dateField formFieldName="edit_rel_start_date" startValue="" /></td>
			</tr>
			<tr>
				<th><spring:message code="Relationship.endDateLong"/>:</th>
				<td><openmrs_tag:dateField formFieldName="edit_rel_end_date" startValue="" /></td>
			</tr>
		</table>
	</div>
		
	<div id="voidRelationship">
		<div><spring:message code="Relationship.relative"/>: <span class="relationship_desc"></span></div>
		<br />
		<label for="relationship_void_reason"><spring:message code="general.reason"/>: </label>
		<input type="hidden" id="relationship_id"/>
		<input type="text" id="relationship_void_reason"/>
		<span id="relationship_empty_reason" class="error"><spring:message code="Relationship.emptyReason"/></span>
	</div>
</div>
