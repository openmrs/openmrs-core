<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:htmlInclude file="/dwr/interface/DWRPatientService.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRConceptService.js" />
<openmrs:htmlInclude file="/scripts/jquery/autocomplete/OpenmrsAutoComplete.js" />

<style type="text/css">
.ui-datepicker { z-index:10100; }
</style>

<script type="text/javascript">
	var problemStartDatePicker, problemEndDatePicker;
	
	$j(document).ready(function() {
		$j('#addActiveListProblem').dialog({
			autoOpen: false,
			modal: true,
			title: '<openmrs:message code="ActiveLists.problem.add" javaScriptEscape="true"/>',
			width: '30%',
			zIndex: 100,
			close: function() { $j("#problem_concept").autocomplete("close"); },
			buttons: { '<openmrs:message code="general.save"/>': function() { handleAddProblem(); },
					   '<openmrs:message code="general.cancel"/>': function() { $j(this).dialog("close"); }
			}
		});
		
		$j('#resolveActiveListProblem').dialog({
			autoOpen: false,
			modal: true,
			title: '<openmrs:message code="ActiveLists.problem.resolveTitle" javaScriptEscape="true"/>',
			width: '30%',
			zIndex: 100,
			buttons: { '<openmrs:message code="ActiveLists.resolve"/>': function() { handleResolveProblem(); },
				       '<openmrs:message code="general.cancel"/>': function() { $j(this).dialog("close"); }
			}
		});

		problemStartDatePicker = new DatePicker("<openmrs:datePattern/>", "problem_startDate", { defaultDate: parseDateFromStringToJs("<openmrs:datePattern/>", "${model.today}") });

		problemEndDatePicker = new DatePicker("<openmrs:datePattern/>", "problem_endDate", { defaultDate: parseDateFromStringToJs("<openmrs:datePattern/>", "${model.today}") });

		var problemCallback = new CreateCallback({onerror: showProblemAddError, onsuccess: hideProblemError});
		var autoProblemConcept = new AutoComplete("problem_concept", problemCallback.conceptCallback(), {
			select: function(event, ui) {
				$j('#problem_concept_id').val(ui.item.object.conceptId);
			}
		});

	});

	function doAddProblem() {
		currentlyEditingProblemId = null;
		
		$j('#problemError').hide();
		$j('#problem_concept').val("");
		$j('#problem_concept_id').val("");
		$j('#problem_modifier').val("");
		problemStartDatePicker.setDate("${model.today}");
		$j('#problem_comments').val("");

		$j('#addActiveListProblem').dialog("option", "title", '<openmrs:message code="ActiveLists.problem.add"/>');
		$j('#addActiveListProblem').dialog('open');
		$j('#problem_concept').focus();
	}
	
	function handleAddProblem() {
		var problem = $j('#problem_concept_id').val();
		var modifier = $j('#problem_modifier').val();
		var startDate = problemStartDatePicker.getDateAsString();
		var comments = $j('#problem_comments').val();

		if((problem == null) || (problem == '')) {
			showProblemAddError("<openmrs:message code="ActiveLists.problem.problemRequired"/>");
			return;
		}

        var patientId = <c:out value="${model.patientId}" />;

		if(currentlyEditingProblemId == null) {
			DWRPatientService.createProblem(patientId, problem, modifier, startDate, comments, refreshPage);
		}
		else {
			DWRPatientService.saveProblem(currentlyEditingProblemId, problem, modifier, startDate, comments, refreshPage);
		}
	}

	function doEditProblem(activeListId) {
		$j('#problemError').hide();
		var problem = findProblem(activeListId);
		if(problem == null) return;

		currentlyEditingProblemId = activeListId;
		$j('#problem_concept').val($j('#problem_conceptName_' + activeListId).html().trim());
		$j('#problem_concept_id').val(problem['problemId']);
		$j('#problem_modifier').val(problem['modifier']);
		problemStartDatePicker.setDate(problem['startDate']);
		$j('#problem_comments').val(problem['comments']);

		$j('#addActiveListProblem').dialog("option", "title", '<openmrs:message code="ActiveLists.problem.edit"/>');
		$j('#addActiveListProblem').dialog('open');
	}

	function findProblem(activeListId) {
		for(var i=0; i < problems.length; i++) {
			var a = problems[i];
			if(activeListId == a['activeListId']) return a;
		}
		return null;
	}

	function doResolveProblem(activeListId) {
		var problem = findProblem(activeListId);
		$j('input[name=problem_resolved]').val(["Removed"]);
		$j('#problem_other').val("");
		$j('#problem_reason').val(problem['comments']);
		problemEndDatePicker.setDate("${model.today}");
		
		currentlyEditingProblemId = activeListId;

		var name = $j('#problem_conceptName_' + activeListId).html().trim();
		$j('#resolveActiveListAllergy').dialog("option", "title", '<openmrs:message code="ActiveLists.problem.resolveTitle"/>: ' + name);
		$j('#resolveActiveListProblem').dialog('open');
	}

	function handleResolveProblem() {
		var resolved = $j('input[name=problem_resolved]:checked').val();
		var reason = $j('#problem_reason').val();
		var endDate = problemEndDatePicker.getDateAsString();

		if('Error' == resolved) {
			DWRPatientService.voidProblem(currentlyEditingProblemId, resolved, refreshPage);
		}
		else {
			DWRPatientService.removeProblem(currentlyEditingProblemId, reason, endDate, refreshPage);
		}
	}

	function doToggleShowingInactiveProblems() {
		$j('#removedProblemTable').toggle();
		var text = '<openmrs:message code="ActiveLists.problem.hideRemoved"/>';
		if(showingInactiveProblems == true) {
			text = '<openmrs:message code="ActiveLists.problem.showRemoved"/>';
		}
		$j('#inactiveProblemLink').html(text);
		showingInactiveProblems = !showingInactiveProblems;
	}

	function showProblemAddError(error) {
		$j('#problemError').html(error);
		$j('#problemError').show();
	}

	function hideProblemError(results) {
		$j('#problemError').hide();
	}

	var problems = new Array();
	var currentlyEditingProblemId = null;
	var showingInactiveProblems = false;
</script>

<div id="patientActiveListProblemPortlet">
<c:choose>
	<c:when test="${fn:length(model.problems) == 0}">
		<openmrs:message code="general.none"/><br/><br/>
	</c:when>
	<c:otherwise>
	<table style="margin: 0px 0px 1em 2em;" cellpadding="3" cellspacing="0" id="problemTable" class="alTable">
		<tr bgcolor="whitesmoke">
			<td><openmrs:message code="ActiveLists.problem.problem"/></td>
			<td><openmrs:message code="ActiveLists.date"/></td>
			<td><openmrs:message code="ActiveLists.problem.status"/></td>
			<td><openmrs:message code="ActiveLists.problem.comments"/></td>
			<td></td>
		</tr>
		<c:forEach var="problem" items="${model.problems}">
			<c:choose>
				<c:when test="${bgColor == 'white'}"><c:set var="bgColor" value="whitesmoke"/></c:when>
				<c:otherwise><c:set var="bgColor" value="white"/></c:otherwise>
			</c:choose>
			<tr bgcolor="${bgColor}">
				<script type="text/javascript">
					problems.push({"activeListId": "${problem.activeListId}",
									"problemId": "${problem.problem.conceptId}",
									"modifier": "${problem.modifier}",
									"startDate": parseDateFromStringToJs("<openmrs:datePattern/>", "<openmrs:formatDate date="${problem.startDate}" type="textbox" />"),
									"comments": "${problem.comments}",
									"endDate": parseDateFromStringToJs("<openmrs:datePattern/>", "<openmrs:formatDate date="${problem.endDate}" type="textbox" />"),
									"voidReason": "${problem.voidReason}"});
				</script>
				<td>
					<a href="javascript:doEditProblem(${problem.activeListId});"><span id="problem_conceptName_${problem.activeListId}"><openmrs_tag:concept conceptId="${problem.problem.conceptId}"/></span></a>
				</td>
				<td><openmrs:formatDate date="${problem.startDate}" type="textbox"/></td>
				<td>${problem.modifier.text}</td>
				<td>${problem.comments}</td>
				<td>
					<a href="javascript:doResolveProblem(${problem.activeListId})"><img src="images/delete.gif" border="0" title="<openmrs:message code="ActiveLists.resolve"/>"/></a>
				</td>
			</tr>
		</c:forEach>
	</table>
	</c:otherwise>
</c:choose>
	
	<div style="width: 100%">
		<a id="addActiveListProblemLink" href="javascript:doAddProblem();" title=""><openmrs:message code="ActiveLists.problem.add"/></a>
		<c:choose><c:when test="${fn:length(model.removedProblems) > 0}">		
			<a href="javascript:doToggleShowingInactiveProblems();" id="inactiveProblemLink" style="width: 100%; text-align: right"><openmrs:message code="ActiveLists.problem.showRemoved"/></a>
		</c:when></c:choose>
	</div>
	
<c:choose>
	<c:when test="${fn:length(model.removedProblems) > 0}">
		<br/>
		<div id="removedProblemTable" style="display: none">
			<openmrs:message code="ActiveLists.problem.removedProblem"/><br/>
			<table style="margin: 0px 0px 1em 2em;" cellpadding="3" cellspacing="0" class="alTable">
				<tr bgcolor="whitesmoke">
					<td><openmrs:message code="ActiveLists.problem.problem"/></td>
					<td><openmrs:message code="ActiveLists.date"/></td>
					<td><openmrs:message code="ActiveLists.problem.comments"/></td>
					<td><openmrs:message code="ActiveLists.resolvedOn"/></td>
				</tr>
				<c:forEach var="problem" items="${model.removedProblems}">
					<c:choose>
						<c:when test="${bgColor1 == 'white'}"><c:set var="bgColor1" value="whitesmoke"/></c:when>
						<c:otherwise><c:set var="bgColor1" value="white"/></c:otherwise>
					</c:choose>
					<tr bgcolor="${bgColor1}">
						<td>${problem.modifier.text} <openmrs_tag:concept conceptId="${problem.problem.conceptId}"/></td>
						<td><openmrs:formatDate date="${problem.startDate}" type="textbox"/></td>
						<td>${problem.comments}</td>
						<td><openmrs:formatDate date="${problem.endDate}" type="textbox"/></td>
					</tr>
				</c:forEach>
			</table>
		</div>
	</c:when>
</c:choose>
	
	<div id="addActiveListProblem" style="display: none">
		<div id="problemError" class="error"></div>
		<table style="margin: 0px 0px 1em 2em;">
			<tr>
				<td nowrap><openmrs:message code="ActiveLists.problem.problem"/> *</td>
				<td>
					<input type="text" id="problem_concept" size="20"/>
					<input type="hidden" id="problem_concept_id"/>
				</td>
			</tr>
			<tr>
				<td><openmrs:message code="ActiveLists.problem.status"/></td>
				<td>
					<select id="problem_modifier">
						<option value=""></option>
					<c:forEach var="problemModifier" items="${model.problemModifiers}">
						<option value="${problemModifier}">${problemModifier.text}</option>
					</c:forEach>
					</select>
				</td>
			</tr>
			<tr>
				<td><openmrs:message code="ActiveLists.startDate"/></td>
				<td><input type="text" id="problem_startDate" size="20"/></td>
			</tr>
			<tr>
				<td valign="top"><openmrs:message code="ActiveLists.problem.comments"/></td>
				<td>
					<textarea id="problem_comments" cols="18" rows="3" style="width: 100%"></textarea>
				</td>
			</tr>
		</table>
	</div>
	<div id="resolveActiveListProblem" style="display: none">
		<table style="margin: 0px 0px 1em 2em;">
			<tr>
				<td><input type="radio" name="problem_resolved" value="Removed" checked/><openmrs:message code="ActiveLists.problem.resolved"/></td>
				<td><input type="text" id="problem_endDate" size="20"/></td>
			</tr>
			<tr>
				<td colspan="2"><input type="radio" name="problem_resolved" value="Error"/><openmrs:message code="ActiveLists.resolve.error"/></td>
			</tr>
			<tr>
				<td colspan="2">
					<openmrs:message code="ActiveLists.problem.comments"/><br/>
					<textarea id="problem_reason" cols="18" rows="3" style="width: 100%"></textarea>
				</td>
			</tr>
		</table>
	</div>
</div>