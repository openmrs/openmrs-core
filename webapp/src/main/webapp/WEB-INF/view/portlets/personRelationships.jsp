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
	$j(document).ready(function() {
		$j('#addRelationship').dialog({
			autoOpen: false,
			modal: true,
			title: '<openmrs:message code="Relationship.add" javaScriptEscape="true"/>',
			width: '50%',
			zIndex: 100,
			buttons: { 
				'<openmrs:message code="general.save"/>': function() { handleAddRelationship(); },
				'<openmrs:message code="general.cancel"/>': function() { $j(this).dialog("close"); }
			}
		});

		$j("#addRelationshipLink").click(function(){
			clearAddRelationship();
			$j("#addRelationship").dialog("open");
			return false;
		});
		
		$j('#voidRelationship').dialog({
			autoOpen: false,
			modal: true,
			title: '<openmrs:message code="Relationship.remove" javaScriptEscape="true"/>',
			width: '50%',
			zIndex: 100,
			buttons: { 
				'<openmrs:message code="general.remove"/>': function() { handleVoidRelationship(); },
				'<openmrs:message code="general.cancel"/>': function() { $j(this).dialog("close"); }
			}
		});
		
		$j('#editRelationship').dialog({
			autoOpen: false,
			modal: true,
			title: '<openmrs:message code="Relationship.edit" javaScriptEscape="true"/>',
			width: '50%',
			zIndex: 100,
			buttons: { 
				'<openmrs:message code="general.save"/>': function() { handleEditRelationship(); },
				'<openmrs:message code="general.cancel"/>': function() { $j(this).dialog("close"); }
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
						'<img src="images/edit.gif" border="0" title="<openmrs:message code="general.edit"/>"/>' +
				   '</a>';
		},
		function(data) {
			return '<a href="javascript:voidRelationshipDialog(' + data[0] + ')" title="">' +
				'<img src="images/delete.gif" border="0" title="<openmrs:message code="general.remove"/>"/>' +
				'</a>';
		}
	];
	
	var relationships = {};	

	function refreshRelationshipsCallback(rels) {
		relationships = {};
		dwr.util.removeAllRows("relationshipTableContent");
		if (rels.length == 0) {
			$j("#no_relationships").html('<openmrs:message code="general.none" javaScriptEscape="true"/><br /><br />');
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

	function handleAddRelationship() {
		var personIdB = ${model.personId};	
		var personIdA = $j("#add_rel_target_id").val();
		if (personIdA == personIdB) {
			window.alert('<openmrs:message code="Relationship.error.same" javaScriptEscape="true"/>');
			return;
		}
		
		var relType = dwr.util.getValue('add_relationship_type');
		if (relType == null || relType == '' || personIdA == null || personIdA == '' || personIdB == null || personIdB == '') {
			window.alert('<openmrs:message code="Relationship.error.everything" javaScriptEscape="true"/>');
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
		DWRRelationshipService.createRelationship(personIdA, personIdB, relType, startDateString, createRelationshipCallback);
	}
	

	function  createRelationshipCallback(errmsgs)
	{		
		if(errmsgs == null)
		{
          refreshRelationships();
          $j("#invalid_start_date").hide();
          $j("#addRelationship").dialog("close");
        } 
        else 
        {  
        	for (var k=0; k<errmsgs.length; k++)
        		{        		
    			if (errmsgs[k]=='error.date.future')
    				{    				
    				$j("#addRelationship").dialog("open"); 	
    				showDiv('add_rel_details');  			
                    $j("#invalid_start_date").show(); 
                    $j('#addRelationship #add_rel_start_date').select();
        		    }
        		}
        }
	}
	
	function clearAddRelationship() {
		$j("#add_rel_target_id").val("");
		$j("#add_rel_display_id").val("");
		$j("#add_relationship_type").val("");
		$j("#add_rel_start_date").val("");
		$j("#invalid_start_date").hide();
		hideDiv('add_rel_details');
	}

	function editRelationshipDialog(relId) {
		$j("#editRelationship .relationship_desc").html(relationships[relId].desc);
		$j("#editRelationship #edit_relationship_id").val(relId);
		$j("#editRelationship #edit_rel_start_date").val(relationships[relId].startDate);
		$j("#editRelationship #edit_rel_end_date").val(relationships[relId].endDate);
		$j("#relationship_invalid_Date").hide();
		$j("#editRelationship").dialog("open");
	}

    function handleDateResult(validEndDate) {
        if(validEndDate==true) {
            refreshRelationships();
            $j("#relationship_invalid_Date").hide();
            $j("#editRelationship").dialog("close");
        } else {
            $j("#relationship_invalid_Date").show();
            $j('#editRelationship #edit_rel_end_date').select();
        }
    }

	function handleEditRelationship() {
		var relId = $j("#editRelationship #edit_relationship_id").val();
		var startDate = $j("#editRelationship #edit_rel_start_date").val();
		var endDate = $j("#editRelationship #edit_rel_end_date").val();
        DWRRelationshipService.changeRelationshipDates(relId, startDate, endDate, handleDateResult);
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
</script>

<div id="patientRelationshipPortlet">
	<div id="no_relationships">
		<openmrs:message code="general.loading"/><br />
	</div>

	<table style="margin: 0px 0px 1em 2em;" cellpadding="3" cellspacing="0" id="relationshipTable" class="relTable">
		<thead>
			<tr bgcolor="whitesmoke">
				<td><openmrs:message code="Relationship.relative"/></td>
				<td><openmrs:message code="Relationship.relationship"/></td>
				<td><openmrs:message code="Relationship.startDate"/></td>
				<td><openmrs:message code="Relationship.endDate"/></td>
				<td></td>
				<td></td>
			</tr>
		</thead>
		<tbody id="relationshipTableContent"></tbody>
	</table>

	<a id="addRelationshipLink" href="#"><openmrs:message code="Relationship.add"/></a>
	
	<div id="addRelationship">
		<openmrs:message code="Relationship.whatType"/>
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
			<c:out value="${model.person.personName}" /><openmrs:message code="Relationship.possessive"/>
			<i><span id="add_relationship_name"><openmrs:message code="Relationship.whatType"/></span></i>
			<input type="hidden" id="add_relationship_type"/>
			<openmrs:message code="Relationship.target"/>
			<openmrs_tag:personField formFieldName="add_rel_target" formFieldId="add_rel_target_id" displayFieldId="add_rel_display_id" searchLabel="Find a Person" canAddNewPerson="true"/>
			<br/>
			<openmrs:message code="Relationship.startDateQuestion"/>
			<openmrs_tag:dateField formFieldName="add_rel_start_date" startValue="" />
			<span id="invalid_start_date" class="error" >
			<openmrs:message code="error.date.future"/>
			</span>
		</span>
	</div>
	
	
	<div id="editRelationship">
		<input type="hidden" id="edit_relationship_id"/>
		<table>
			<tr>
				<th><openmrs:message code="Relationship.relative"/>:</th>
				<td><span class="relationship_desc"></span></td>
			</tr>
			<tr>
				<th><openmrs:message code="Relationship.startDateLong"/>:</th>
				<td><openmrs_tag:dateField formFieldName="edit_rel_start_date" startValue="" /></td>
			</tr>
			<tr>
				<th><openmrs:message code="Relationship.endDateLong"/>:</th>
				<td>
				<openmrs_tag:dateField formFieldName="edit_rel_end_date" startValue="" />
				<span id="relationship_invalid_Date" class="error" >
				<openmrs:message code="Relationship.InvalidDate.error"/>
				</span>
		 	   </td>
			</tr>
		</table>
	</div>
		
	<div id="voidRelationship">
		<div><openmrs:message code="Relationship.relative"/>: <span class="relationship_desc"></span></div>
		<br />
		<label for="relationship_void_reason"><openmrs:message code="general.reason"/>: </label>
		<input type="hidden" id="relationship_id"/>
		<input type="text" id="relationship_void_reason"/>
		<span id="relationship_empty_reason" class="error"><openmrs:message code="Relationship.emptyReason"/></span>
	</div>
</div>
