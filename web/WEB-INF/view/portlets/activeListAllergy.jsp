<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:htmlInclude file="/scripts/easyAjax.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRPatientService.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRConceptService.js" />
<openmrs:htmlInclude file="/dwr/util.js" />
<openmrs:htmlInclude file="/scripts/jquery/autocomplete/jquery.autocomplete.js" />
<openmrs:htmlInclude file="/scripts/jquery/autocomplete/jquery.autocomplete.css" />

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
			title: '<spring:message code="ActiveLists.allergy.add" javaScriptEscape="true"/>',
			width: '30%',
			zIndex: 100,
			buttons: { '<spring:message code="general.save"/>': function() { handleAddAllergy(); },
					   '<spring:message code="general.cancel"/>': function() { $j(this).dialog("close"); }
			}
		});
		
		$j('#resolveActiveListAllergy').dialog({
			autoOpen: false,
			modal: true,
			title: '<spring:message code="ActiveLists.allergy.resolveTitle" javaScriptEscape="true"/>',
			width: '30%',
			zIndex: 100,
			buttons: { '<spring:message code="ActiveLists.resolve"/>': function() { handleResolveAllergy(); },
				       '<spring:message code="general.cancel"/>': function() { $j(this).dialog("close"); }
			}
		});

		allergyStartDatePicker = new DatePicker("<openmrs:datePattern/>", "allergy_startDate", { defaultDate: parseDateFromStringToJs("<openmrs:datePattern/>", "${model.today}") });

		var allergyCallback = new ConceptServiceCallback(showAllergyAddError);
		var autoAllergyConcept = new AutoComplete("allergy_concept", allergyCallback.callback, {
			onItemSelect: function(li) {
				//set the value of the id
				$j('#allergy_concept_id').val(li.selectValue);
			}
		});

		var reactionCallback = new ConceptServiceCallback(showAllergyAddError, 'Symptom');
		var autoReactionConcept = new AutoComplete("allergy_reaction", reactionCallback.callback, {
			onItemSelect: function(li) {
				$j('#allergy_reaction_id').val(li.selectValue);
			}
		});

//THIS IS FOR JQUERY 1.8.1
//		$j("#allergy_concept").autocomplete({
//			minLength: 2,
//			source: function(request, response) {
//				alert("term=" + request.term);
//				DWRConceptService.findConcepts(request.term, true, ["Symptom"], [], [], [], false, function(objs) {
//					// convert objs from single obj into array (if needed)
//					if(objs.length == null) {
//						objs = [objs];
//					}
//
//					//check if we have an error
//					if(objs.length >= 1) {
//						if(typeof objs[0] == 'string') {
//							//then we have an error
//							alert(objs[0]);
//							return;
//						}
//					}
//
//					alert("objs=" + objs);
//
//					//we get returned ConceptListItem objects (or ConceptDrugListItem)
//					response($j.map(objs, function(item) {
//						return {
//							label: item.name,
//							value: item.name
//						};
//					}));
//				})
//			},
//		});
		
	});

	function ConceptServiceCallback(errorHandler, includedClass) {
		this.callback = function(q, response) {
			if(includedClass == null) {
				includedClass = [];
			}
			else {
				if(typeof includedClass == 'string') {
					includedClass = [includedClass];
				}
			}
			
			DWRConceptService.findConcepts(q, false, includedClass, [], [], [], false, function(objs) {
				//convert objs from single obj into array (if needed)
				if(objs.length == null) {
					objs = [objs];
				}

				//check if we have an error
				if(objs.length >= 1) {
					if(typeof objs[0] == 'string') {
						//we have an error
						if(errorHandler) errorHandler(objs[0]);
						return;
					}
				}

				response($j.map(objs, function(item) {
					return { name: item.name, value: item.conceptId };
				}));
			});
		}
	}

	function doAddAllergy() {
		$j('#allergyError').hide();
		$j('#allergy_concept').val("");
		$j('#allergy_concept_id').val("");
		$j('#allergy_reaction').val("");
		$j('#allergy_reaction_id').val("");
		$j('#allergy_type').val("");
		allergyStartDatePicker.setDate("${model.today}");
		
		$j('#allergy_severity').val("");
		$j('#allergy_reaction').val("");

		$j('#addActiveListAllergy').dialog("option", "title", '<spring:message code="ActiveLists.allergy.add"/>');
		$j('#addActiveListAllergy').dialog('open');
	}
	
	function handleAddAllergy() {
		var allergen = $j('#allergy_concept_id').val();
		var type = $j('#allergy_type').val();
		var startDate = allergyStartDatePicker.getDateAsString();
		var severity = $j('#allergy_severity').val();
		var reaction = $j('#allergy_reaction_id').val();

		if((allergen == null) || (allergen == '')) {
			alert("Allergen required");
			return;
		}

		var patientId = ${model.patientId};

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

		$j('#addActiveListAllergy').dialog("option", "title", '<spring:message code="ActiveLists.allergy.edit"/>');
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
		$j('#resolveActiveListAllergy').dialog("option", "title", '<spring:message code="ActiveLists.allergy.resolveTitle"/>: ' + name);
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
		var text = '<spring:message code="ActiveLists.allergy.hideRemoved"/>';
		if(showingInactiveAllergies == true) {
			text = '<spring:message code="ActiveLists.allergy.showRemoved"/>';
		}
		$j('#inactiveAllergyLink').html(text);
		showingInactiveAllergies = !showingInactiveAllergies;
	}

	function showAllergyAddError(error) {
		$j('#allergyError').html(error);
		$j('#allergyError').show();
	}

	var allergies = new Array();
	var currentlyEditingAllergyId = null;
	var showingInactiveAllergies = false;
</script>

<div id="patientActiveListAllergyPortlet">
<c:choose>
	<c:when test="${fn:length(model.allergies) == 0}">
		<spring:message code="general.none"/><br/><br/>
	</c:when>
	<c:otherwise>
	<table style="margin: 0px 0px 1em 2em;" cellpadding="3" cellspacing="0" id="allergyTable" class="alTable">
		<tr bgcolor="whitesmoke">
			<td><spring:message code="ActiveLists.allergy.allergen"/></td>
			<td><spring:message code="ActiveLists.date"/></td>
			<td><spring:message code="ActiveLists.allergy.reaction"/></td>
			<td><spring:message code="ActiveLists.allergy.severity"/></td>
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
					<a href="javascript:doResolveAllergy(${allergy.activeListId})" title=""><img src="images/delete.gif" border="0" title="<spring:message code="ActiveLists.resolve"/>"/></a>
				</td>
			</tr>
		</c:forEach>
	</table>
	</c:otherwise>
</c:choose>

	<div style="width: 100%">
		<a id="addActiveListAllergyLink" href="javascript:doAddAllergy();" title=""><spring:message code="ActiveLists.allergy.add"/></a>
		<c:choose><c:when test="${fn:length(model.removedAllergies) > 0}">		
			<a href="javascript:doToggleShowingInactiveAllergies();" id="inactiveAllergyLink" style="width: 100%; text-align: right"><spring:message code="ActiveLists.allergy.showRemoved"/></a>
		</c:when></c:choose>
	</div>
	
<c:choose>
	<c:when test="${fn:length(model.removedAllergies) > 0}">
		<br/>
		<div id="removedAllergyTable" style="display: none">
			Removed Allergies<br/>
			<table style="margin: 0px 0px 1em 2em;" cellpadding="3" cellspacing="0" class="alTable">
				<tr bgcolor="whitesmoke">
					<td><spring:message code="ActiveLists.allergy.allergen"/></td>
					<td><spring:message code="ActiveLists.date"/></td>
					<td><spring:message code="ActiveLists.allergy.reaction"/></td>
					<td><spring:message code="ActiveLists.allergy.severity"/></td>
					<td><spring:message code="ActiveLists.resolvedOn"/></td>
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
				<td nowrap><spring:message code="ActiveLists.allergy.allergen"/> *</td>
				<td>
					<input type="text" id="allergy_concept" size="20"/>
					<input type="hidden" id="allergy_concept_id"/>
				</td>
			</tr>
			<tr>
				<td><spring:message code="ActiveLists.startDate"/></td>
				<td><input type="text" id="allergy_startDate" size="20"/></td>
			</tr>
			<tr>
				<td><spring:message code="ActiveLists.allergy.reaction"/></td>
				<td>
					<input type="text" id="allergy_reaction" size="20"/>
					<input type="hidden" id="allergy_reaction_id"/>
				</td>
			</tr>
			<tr>
				<td><spring:message code="ActiveLists.allergy.severity"/></td>
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
				<td><input type="radio" name="allergy_resolved" value="Removed" checked/><spring:message code="ActiveLists.removed.remove"/></td>
			</tr>
			<tr>
				<td><input type="radio" name="allergy_resolved" value="Error"/><spring:message code="ActiveLists.resolve.error"/></td>
			</tr>
		</table>
	</div>
</div>