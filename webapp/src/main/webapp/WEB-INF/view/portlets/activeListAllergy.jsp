<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:htmlInclude file="/dwr/interface/DWRPatientService.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRConceptService.js" />
<openmrs:htmlInclude file="/scripts/jquery/autocomplete/OpenmrsAutoComplete.js" />

<style type="text/css">
.ui-datepicker { z-index:10100; }
.alTable td {
	padding-right: 10px;
	padding-left: 10px;
}
</style>

<script type="text/javascript">
	var allergyStartDatePicker;
	
	$j(document).ready(function() {
		$j('#addActiveListAllergy').dialog({
			autoOpen: false,
			modal: true,
			title: '<openmrs:message code="ActiveLists.allergy.add" javaScriptEscape="true"/>',
			width: '30%',
			zIndex: 100,
			close: function() { $j("#allergy_concept").autocomplete("close"); $j("#allergy_reaction").autocomplete("close"); },
			buttons: { '<openmrs:message code="general.save"/>': function() { handleAddAllergy(); },
					   '<openmrs:message code="general.cancel"/>': function() { $j(this).dialog("close"); }
			}
		});
		
		$j('#resolveActiveListAllergy').dialog({
			autoOpen: false,
			modal: true,
			title: '<openmrs:message code="ActiveLists.allergy.resolveTitle" javaScriptEscape="true"/>',
			width: '30%',
			zIndex: 100,
			buttons: { '<openmrs:message code="ActiveLists.resolve"/>': function() { handleResolveAllergy(); },
				       '<openmrs:message code="general.cancel"/>': function() { $j(this).dialog("close"); }
			}
		});

		allergyStartDatePicker = new DatePicker("<openmrs:datePattern/>", "allergy_startDate", { defaultDate: parseDateFromStringToJs("<openmrs:datePattern/>", "${model.today}") });

		var allergenClasses = "<openmrs:globalProperty key='allergy.allergen.ConceptClasses'/>".split(",");
		var allergyCallback = new CreateCallback(
									{onerror: showAllergyAddError, 
									 onsuccess: hideAllergyError,
									 includeClasses: allergenClasses
									 });
		var autoAllergyConcept = new AutoComplete("allergy_concept", allergyCallback.conceptCallback(), {
			select: function(event, ui) {
				$j('#allergy_concept_id').val(ui.item.object.conceptId);
			}
		});

		var reactionClasses = "<openmrs:globalProperty key='allergy.reaction.ConceptClasses'/>".split(",");
		var reactionCallback = new CreateCallback(
									{onerror:showAllergyAddError, 
									 onsuccess: hideAllergyError,
									 includeClasses: reactionClasses
									});
		var autoReactionConcept = new AutoComplete("allergy_reaction", reactionCallback.conceptCallback(), {
			select: function(event, ui) {
				$j('#allergy_reaction_id').val(ui.item.object.conceptId);
			}
		});
		
	});

	function doAddAllergy() {
		
		// in case someone started editing/resolving an allergy already
		currentlyEditingAllergyId = null;
		
		$j('#allergyError').hide();
		$j('#allergy_concept').val("");
		$j('#allergy_concept_id').val("");
		$j('#allergy_reaction').val("");
		$j('#allergy_reaction_id').val("");
		$j('#allergy_type').val("");
		allergyStartDatePicker.setDate("${model.today}");
		
		$j('#allergy_severity').val("");
		$j('#allergy_reaction').val("");

		$j('#addActiveListAllergy').dialog("option", "title", '<openmrs:message code="ActiveLists.allergy.add"/>');
		$j('#addActiveListAllergy').dialog('open');
		$j('#allergy_concept').focus();
	}
	
	function handleAddAllergy() {
		var allergen = $j('#allergy_concept_id').val();
		var type = $j('#allergy_type').val();
		var startDate = allergyStartDatePicker.getDateAsString();
		var severity = $j('#allergy_severity').val();
		var reaction = $j('#allergy_reaction_id').val();

		if((allergen == null) || (allergen == '')) {
			showAllergyAddError("<openmrs:message code="ActiveLists.allergy.allergenRequired"/>");
			return;
		}

		var patientId = <c:out value="${model.patientId}" />;

		if(currentlyEditingAllergyId == null) {
			DWRPatientService.createAllergy(patientId, allergen, type, startDate, severity, reaction, refreshPage);
		}
		else {
			DWRPatientService.saveAllergy(currentlyEditingAllergyId, allergen, type, startDate, severity, reaction, refreshPage);
		}
	}

	function doEditAllergy(activeListId) {
		$j('#allergyError').hide();
		var allergy = findAllergy(activeListId);
		if(allergy == null) return;

		currentlyEditingAllergyId = activeListId;
		$j('#allergy_concept').val($j('#allergen_conceptName_' + activeListId).html().trim());
		$j('#allergy_concept_id').val(allergy['allergenId']);
		$j('#allergy_type').val(allergy['type']);
		allergyStartDatePicker.setDate(allergy['startDate']);
		$j('#allergy_severity').val(allergy['severity']);
		$j('#allergy_reaction').val($j('#reaction_conceptName_' + activeListId).html().trim());
		$j('#allergy_reaction_id').val(allergy['reactionId']);

		$j('#addActiveListAllergy').dialog("option", "title", '<openmrs:message code="ActiveLists.allergy.edit"/>');
		$j('#addActiveListAllergy').dialog('open');
	}

	function findAllergy(activeListId) {
		for(var i=0; i < allergies.length; i++) {
			var a = allergies[i];
			if(activeListId == a['activeListId']) return a;
		}
		return null;
	}

	function doResolveAllergy(activeListId) {
		$j('input[name=allergy_resolved]').val(["Removed"]);
		$j('#allergy_reason').val("");
		
		currentlyEditingAllergyId = activeListId;

		var name = $j('#allergen_conceptName_' + activeListId).html().trim();
		$j('#resolveActiveListAllergy').dialog("option", "title", '<openmrs:message code="ActiveLists.allergy.resolveTitle"/>: ' + name);
		$j('#resolveActiveListAllergy').dialog('open');
	}

	function handleResolveAllergy() {
		var resolved = $j('input[name=allergy_resolved]:checked').val();

		if('Error' == resolved) {
			DWRPatientService.voidAllergy(currentlyEditingAllergyId, resolved, refreshPage);
		}
		else {
			DWRPatientService.removeAllergy(currentlyEditingAllergyId, resolved, refreshPage);
		}
	}

	function doToggleShowingInactiveAllergies() {
		$j('#removedAllergyTable').toggle();
		var text = '<openmrs:message code="ActiveLists.allergy.hideRemoved"/>';
		if(showingInactiveAllergies == true) {
			text = '<openmrs:message code="ActiveLists.allergy.showRemoved"/>';
		}
		$j('#inactiveAllergyLink').html(text);
		showingInactiveAllergies = !showingInactiveAllergies;
	}

	function showAllergyAddError(error) {
		$j('#allergyError').html(error);
		$j('#allergyError').show();
	}

	function hideAllergyError(results) {
		$j('#allergyError').hide();
	}

	var allergies = new Array();
	var currentlyEditingAllergyId = null;
	var showingInactiveAllergies = false;
</script>

<div id="patientActiveListAllergyPortlet">
<c:choose>
	<c:when test="${fn:length(model.allergies) == 0}">
		<openmrs:message code="general.none"/><br/><br/>
	</c:when>
	<c:otherwise>
	<table style="margin: 0px 0px 1em 2em;" cellpadding="3" cellspacing="0" id="allergyTable" class="alTable">
		<tr bgcolor="whitesmoke">
			<td><openmrs:message code="ActiveLists.allergy.allergen"/></td>
			<td><openmrs:message code="ActiveLists.date"/></td>
			<td><openmrs:message code="ActiveLists.allergy.reaction"/></td>
			<td><openmrs:message code="ActiveLists.allergy.severity"/></td>
			<td></td>
		</tr>
		<c:forEach var="allergy" items="${model.allergies}">
			<c:choose>
				<c:when test="${bgColor == 'white'}"><c:set var="bgColor" value="whitesmoke"/></c:when>
				<c:otherwise><c:set var="bgColor" value="white"/></c:otherwise>
			</c:choose>
			<tr bgcolor="${bgColor}">
				<script type="text/javascript">
					allergies.push({"activeListId": "${allergy.activeListId}",
									"allergenId": "${allergy.allergen.conceptId}",
									"type": "${allergy.allergyType}",
									"startDate": parseDateFromStringToJs("<openmrs:datePattern/>", "<openmrs:formatDate date="${allergy.startDate}" type="textbox" />"),
									"severity": "${allergy.severity}",
									"reactionId": "${allergy.reaction.conceptId}",
									"endDate": parseDateFromStringToJs("<openmrs:datePattern/>", "<openmrs:formatDate date="${allergy.endDate}" type="textbox" />"),
									"voidReason": "${allergy.voidReason}"});
				</script>
				<td>
					<c:choose>
						<c:when test="${allergy.endDate == null}">
							<a href="javascript:doEditAllergy(${allergy.activeListId});"><span id="allergen_conceptName_${allergy.activeListId}"><openmrs_tag:concept conceptId="${allergy.allergen.conceptId}"/></span></a>
						</c:when>
						<c:otherwise>
							<span id="allergen_conceptName_${allergy.activeListId}"><openmrs_tag:concept conceptId="${allergy.allergen.conceptId}"/></span>
						</c:otherwise>
					</c:choose>
				</td>
				<td><openmrs:formatDate date="${allergy.startDate}" type="textbox" /></td>
				<td><span id="reaction_conceptName_${allergy.activeListId}"><openmrs_tag:concept conceptId="${allergy.reaction.conceptId}"/></span></td>
				<td>${allergy.severity}</td>
				<td>
					<a href="javascript:doResolveAllergy(${allergy.activeListId})" title=""><img src="images/delete.gif" border="0" title="<openmrs:message code="ActiveLists.resolve"/>"/></a>
				</td>
			</tr>
		</c:forEach>
	</table>
	</c:otherwise>
</c:choose>

	<div style="width: 100%">
		<a id="addActiveListAllergyLink" href="javascript:doAddAllergy();" title=""><openmrs:message code="ActiveLists.allergy.add"/></a>
		<c:choose><c:when test="${fn:length(model.removedAllergies) > 0}">		
			<a href="javascript:doToggleShowingInactiveAllergies();" id="inactiveAllergyLink" style="width: 100%; text-align: right"><openmrs:message code="ActiveLists.allergy.showRemoved"/></a>
		</c:when></c:choose>
	</div>
	
<c:choose>
	<c:when test="${fn:length(model.removedAllergies) > 0}">
		<br/>
		<div id="removedAllergyTable" style="display: none">
			<openmrs:message code="ActiveLists.allergy.removedAllergies"/><br/>
			<table style="margin: 0px 0px 1em 2em;" cellpadding="3" cellspacing="0" class="alTable">
				<tr bgcolor="whitesmoke">
					<td><openmrs:message code="ActiveLists.allergy.allergen"/></td>
					<td><openmrs:message code="ActiveLists.date"/></td>
					<td><openmrs:message code="ActiveLists.allergy.reaction"/></td>
					<td><openmrs:message code="ActiveLists.allergy.severity"/></td>
					<td><openmrs:message code="ActiveLists.resolvedOn"/></td>
				</tr>
				<c:forEach var="allergy" items="${model.removedAllergies}">
					<c:choose>
						<c:when test="${bgColor1 == 'white'}"><c:set var="bgColor1" value="whitesmoke"/></c:when>
						<c:otherwise><c:set var="bgColor1" value="white"/></c:otherwise>
					</c:choose>
					<tr bgcolor="${bgColor1}">
						<td><openmrs_tag:concept conceptId="${allergy.allergen.conceptId}"/></td>
						<td><openmrs:formatDate date="${allergy.startDate}" type="textbox"/></td>
						<td><openmrs_tag:concept conceptId="${allergy.reaction.conceptId}"/></td>
						<td>${allergy.severity}</td>
						<td><openmrs:formatDate date="${allergy.endDate}" type="textbox"/></td>
					</tr>
				</c:forEach>
			</table>
		</div>
	</c:when>
</c:choose>
	
	<div id="addActiveListAllergy" style="display: none">
		<div id="allergyError" class="error"></div>
		<table style="margin: 0px 0px 1em 2em;">
			<tr>
				<td nowrap><openmrs:message code="ActiveLists.allergy.allergen"/> *</td>
				<td>
					<input type="text" id="allergy_concept" size="20"/>
					<input type="hidden" id="allergy_concept_id"/>
				</td>
			</tr>
			<tr>
				<td style="white-space: nowrap"><openmrs:message code="ActiveLists.startDate"/></td>
				<td><input type="text" id="allergy_startDate" size="20"/></td>
			</tr>
			<tr>
				<td><openmrs:message code="ActiveLists.allergy.reaction"/></td>
				<td>
					<input type="text" id="allergy_reaction" size="20"/>
					<input type="hidden" id="allergy_reaction_id"/>
				</td>
			</tr>
			<tr>
				<td><openmrs:message code="ActiveLists.allergy.severity"/></td>
				<td>
					<select id="allergy_severity">
						<option value=""></option>
					<c:forEach var="allergySeverity" items="${model.allergySeverities}">
						<option value="${allergySeverity}">${allergySeverity}</option>
					</c:forEach>
					</select>
				</td>
			</tr>
		</table>
	</div>
	<div id="resolveActiveListAllergy" style="display: none">
		<table style="margin: 0px 0px 1em 2em;">
			<tr>
				<td><input type="radio" name="allergy_resolved" value="Removed" checked/><openmrs:message code="ActiveLists.removed.remove"/></td>
			</tr>
			<tr>
				<td><input type="radio" name="allergy_resolved" value="Error"/><openmrs:message code="ActiveLists.resolve.error"/></td>
			</tr>
		</table>
	</div>
</div>