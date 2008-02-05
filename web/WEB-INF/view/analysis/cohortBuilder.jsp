<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patient Cohorts" otherwise="/login.htm" redirect="/cohortBuilder.list" />

<c:set var="OPENMRS_DO_NOT_SHOW_PATIENT_SET" scope="request" value="true"/>

<%@ include file="/WEB-INF/template/header.jsp" %>

<script type="text/javascript" src='${pageContext.request.contextPath}/dwr/engine.js'></script>
<script type="text/javascript" src='${pageContext.request.contextPath}/dwr/util.js'></script>
<script type="text/javascript" src='${pageContext.request.contextPath}/dwr/interface/DWRCohortBuilderService.js'></script>
<script type="text/javascript" src='${pageContext.request.contextPath}/dwr/interface/DWRPatientService.js'></script>
<script type="text/javascript" src='${pageContext.request.contextPath}/dwr/interface/DWRPatientSetService.js'></script>
<openmrs:htmlInclude file="/dwr/interface/DWRProgramWorkflowService.js" />
<openmrs:htmlInclude file="/scripts/dojoConfig.js"></openmrs:htmlInclude>
<openmrs:htmlInclude file="/scripts/dojo/dojo.js"></openmrs:htmlInclude>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<openmrs:globalProperty var="SHOW_LAST_N" defaultValue="5" key="cohort.cohortBuilder.showLastSearches"/>

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.ConceptSearch");
	dojo.hostenv.writeIncludes();
	
	dojo.addOnLoad( function() {
		dojo.event.topic.subscribe("concept_to_filter_search/select", 
			function(msg) {
				if (msg) {
					var concept = msg.objs[0];
					showPossibleFilters(concept);
				}
			}
		);
		
		document.getElementById('concept_to_filter_search').focus();
	})
	
	// tab ids should be searchTab_concept
	// tab content ids should be searchTab_concept_content
	function changeSearchTab(tabObj, focusToId) {
		if (typeof tabObj == 'string')
			tabObj = document.getElementById(tabObj);

		if (tabObj) {
			var tabs = tabObj.parentNode.parentNode.getElementsByTagName('a');
			for (var i = 0; i < tabs.length; ++i) {
				if (tabs[i].className.indexOf('current') != -1) {
					manipulateClass('remove', tabs[i], 'current');
				}
				var tabContentId = tabs[i].id + '_content';
				if (tabs[i].id == tabObj.id)
					showLayer(tabContentId);
				else
					hideLayer(tabContentId);
			}
			addClass(tabObj, 'current');
			
			if (focusToId)
				document.getElementById(focusToId).focus();
			else
				tabObj.blur();
		}
	}
	
	function classFilterTemplate(concept) {
		if (concept.className == 'Program') {
			var str = '<form method="post" action="cohortBuilder.form">';
			str += '<input type="hidden" name="method" value="addDynamicFilter"/>';
			str += '<input type="hidden" name="filterClass" value="org.openmrs.reporting.ProgramPatientFilter" />';
			str += '<input type="hidden" name="vars" value="program#org.openmrs.Program,fromDate#java.util.Date,toDate#java.util.Date"/>';
			str += '<input type="hidden" name="program" value="concept.' + concept.conceptId + '"/>';
			str += 'In ' + concept.name;
			str += ' <input type="submit" value="Search"/>';
			str += '</form>';			
			return str;
		} else if (concept.className == 'State' || concept.className == 'Workflow')
			return 'Workflow/State filter not yet implemented';
		else if (concept.className == 'Drug') {
			var str = '<form method="post" action="cohortBuilder.form">';
			str += '<input type="hidden" name="method" value="addDynamicFilter"/>';
			str += '<input type="hidden" name="filterClass" value="org.openmrs.reporting.DrugOrderPatientFilter" />';
			str += '<input type="hidden" name="vars" value="groupMethod#org.openmrs.api.PatientSetService$GroupMethod,drugConcept#org.openmrs.Concept"/>';
			str += '<select name="groupMethod"><option value="">Taking ' + concept.name + '</option><option value="NONE">Taking nothing</option></select>';
			str += '<input type="hidden" name="drugConcept" value="' + concept.conceptId + '"/>';
			str += ' <input type="submit" value="Search"/>';
			str += '</form>';			
			return str;
		}
		return null;
	}
	
	function obsFilterTemplate(concept) {
		var hl7Abbrev = concept.hl7Abbreviation;
		if (hl7Abbrev == 'ZZ')
			//return 'Handling Datatype N/A not yet implemented. Any suggestions on how it should behave?';
			return null;
		if (hl7Abbrev != 'NM' && hl7Abbrev != 'ST' && hl7Abbrev != 'CWE' && hl7Abbrev != 'DT' && hl7Abbrev != 'TS') {
			return null;
		}
		var lookupAnswers = false;
		var str = '<form method="post" action="cohortBuilder.form">';
		str += '<input type="hidden" name="method" value="addDynamicFilter"/>';
		str += '<input type="hidden" name="filterClass" value="org.openmrs.reporting.ObsPatientFilter" />';
		str += '<input type="hidden" name="vars" value="timeModifier#org.openmrs.api.PatientSetService$TimeModifier,question#org.openmrs.Concept,modifier#org.openmrs.api.PatientSetService$Modifier,';
		if (hl7Abbrev == 'CWE')
			str += 'value#org.openmrs.Concept';
		else
			str += 'value#java.lang.Object';
		str += ',withinLastMonths#java.lang.Integer,withinLastDays#java.lang.Integer,sinceDate#java.util.Date,untilDate#java.util.Date"/>';
		str += '<input type="hidden" name="question" value="' + concept.conceptId + '"/>';
		str += '<h4>Patients with observations whose <i>question</i> is ' + concept.name + '.</h4>';
		if (hl7Abbrev == 'NM')
			str += '<br/><span style="margin-left: 40px">Which observations? <select name="timeModifier"><option value="ANY">Any</option><option value="NO">None</option><option value="FIRST">Earliest</option><option value="LAST" selected="true">Most Recent</option><option value="MIN">Lowest</option><option value="MAX">Highest</option><option value="AVG">Average</option></select></span> ';
		else if (hl7Abbrev == 'DT' || hl7Abbrev == 'TS')
			str += '<br/><span style="margin-left: 40px">Which observations? <select name="timeModifier"><option value="ANY" selected="true">Any</option><option value="NO">None</option><option value="MIN">Earliest Value</option><option value="MAX">Most Recent Value</option><option value="FIRST">Earliest Recorded</option><option value="LAST">Most Recent Recorded</option></select></span> ';
		else if (hl7Abbrev == 'ST' || hl7Abbrev == 'CWE')
			str += '<br/><span style="margin-left: 40px">Which observations? <select name="timeModifier"><option value="ANY">Any</option><option value="NO">None</option><option value="FIRST">Earliest</option><option value="LAST" selected="true">Most Recent</option></select></span> ';
		if (hl7Abbrev == 'NM') {
			str += ' <br/><br/><span style="margin-left: 40px">';
			str += ' <spring:message code="CohortBuilder.optionalPrefix" /> What values? ';
			str += ' <select name="modifier" id="modifier"><option value="LESS_THAN">&lt;</option><option value="LESS_EQUAL">&lt;=</option><option value="EQUAL">=</option><option value="GREATER_EQUAL">&gt;=</option><option value="GREATER_THAN">&gt;</option></select> ';
			str += '</span>';
		} else if (hl7Abbrev == 'DT' || hl7Abbrev == 'TS') {
			str += ' <br/><br/><span style="margin-left: 40px">';
			str += ' <spring:message code="CohortBuilder.optionalPrefix" /> What values? ';
			str += ' <select name="modifier" id="modifier"><option value="LESS_THAN">before</option><option value="LESS_EQUAL" selected="true">on or before</option><option value="EQUAL">on</option><option value="GREATER_EQUAL">on or after</option><option value="GREATER_THAN">after</option></select> ';
			str += '</span>';
		} else if (hl7Abbrev == 'ST' || hl7Abbrev == 'CWE') {
			str += ' <br/><br/><span style="margin-left: 40px">';
			str += ' <spring:message code="CohortBuilder.optionalPrefix" /> <spring:message code="CohortBuilder.whatValueQuestion" /> ';
			str += ' <input type="hidden" name="modifier" value="EQUAL" /> ';
			str += '</span>';
		}
		if (hl7Abbrev == 'NM' || hl7Abbrev == 'ST') {
			str += '<input type="text" name="value" size="10"/>';
			if (concept.units != null)
				str += ' ' + concept.units;
		} else if (hl7Abbrev == 'DT' || hl7Abbrev == 'TS') {
			str += '<input type="text" name="value" size="10" onClick="showCalendar(this)"/>';
		} else if (hl7Abbrev == 'CWE') {
			str += '<select name="value" id="replace_with_answer_options"><option value=""><spring:message code="general.loading"/></option></select>';
			lookupAnswers = true;
		}
		str += ' <br/><br/><span style="margin-left: 40px">';
		str += ' <spring:message code="CohortBuilder.optionalPrefix" /> <spring:message code="CohortBuilder.whenPrefix" /> <spring:message code="CohortBuilder.withinMonthsAndDays" arguments="withinLastMonths,withinLastDays" />';
		str += '</span>';
		str += ' <br/><br/><span style="margin-left: 40px">';
		str += ' <spring:message code="CohortBuilder.optionalPrefix" /> <spring:message code="CohortBuilder.dateRangePrefix" /> <spring:message code="CohortBuilder.fromDateToDate" arguments="sinceDate,untilDate" />';
		str += '</span>';
		str += ' <br/><br/><input type="submit" value="Search"/>';
		str += ' &nbsp;&nbsp;&nbsp;&nbsp;<input type="button" value="<spring:message code="general.cancel" />" onClick="hideLayer(\'concept_filter_box\')"/>';
		str += '</form>';
		if (lookupAnswers) {
			DWRConceptService.getAnswersForQuestion(concept.conceptId, function(list) {
					DWRUtil.removeAllOptions('replace_with_answer_options');
					DWRUtil.addOptions('replace_with_answer_options', [" "]);
					DWRUtil.addOptions('replace_with_answer_options', list, 'conceptId', 'name');
				});
		}
		return str;
	}
	
	function obsValueFilterTemplate(concept) {
		var str = '<form method="post" action="cohortBuilder.form">';
		str += '<input type="hidden" name="method" value="addDynamicFilter"/>';
		str += '<input type="hidden" name="filterClass" value="org.openmrs.reporting.ObsPatientFilter" />';
		str += '<input type="hidden" name="vars" value="timeModifier#org.openmrs.api.PatientSetService$TimeModifier,modifier#org.openmrs.api.PatientSetService$Modifier,value#org.openmrs.Concept,withinLastMonths#java.lang.Integer,withinLastDays#java.lang.Integer,sinceDate#java.util.Date,untilDate#java.util.Date"/>';
		str += ' <h4>Patients with observations whose <i>answer</i> is ';
		str += '<input type="hidden" name="modifier" value="EQUAL" /> ';
		str += '<input type="hidden" name="value" value="' + concept.conceptId + '"/>';
		str += concept.name + '.</h4>';
		str += ' <br/><span style="margin-left: 40px">';
		str += ' <select name="timeModifier"><option value="ANY">Patients who have these observations</option><option value="NO">Patients who do not have these observations</option></select> ';
		str += '</span>';
		str += ' <br/><br/><span style="margin-left: 40px">';
		str += ' <spring:message code="CohortBuilder.optionalPrefix" /> When? Within the last ';
		str += ' <input type="text" name="withinLastMonths" value="" size="2" />';
		str += ' months and/or';
		str += ' <input type="text" name="withinLastDays" value="" size="2" />';
		str += ' days';
		str += '</span>';
		str += ' <br/><br/><span style="margin-left: 40px">';
		str += ' <spring:message code="CohortBuilder.optionalPrefix" /> Date range? since ';
		str += ' <input type="text" name="sinceDate" size="10" value="" onClick="showCalendar(this)" />';
		str += ' and/or until ';
		str += ' <input type="text" name="untilDate" size="10" value="" onClick="showCalendar(this)" />';
		str += '</span>';
		str += ' <br/><br/><input type="submit" value="Search"/>';
		str += ' &nbsp;&nbsp;&nbsp;&nbsp;<input type="button" value="<spring:message code="general.cancel" />" onClick="hideLayer(\'concept_filter_box\')"/>';
		str += '</form>';
		return str;
	}
	
	function possibleFilterHelper(filter) {
		return '<div style="background: #f6f6f6; border: 1px #808080 solid; padding: 0.5em; margin: 0.5em">' + filter + '</div>';
	}
		
	function showPossibleFilters(concept) {
		var div = document.getElementById('concept_filter_box');
		var str = '';
		var filter = obsFilterTemplate(concept);
		if (filter != null)
			str += possibleFilterHelper(filter);
		filter = classFilterTemplate(concept);
		if (filter != null)
			str += possibleFilterHelper(filter);
		filter = obsValueFilterTemplate(concept);
		if (filter != null)
			str += possibleFilterHelper(filter);
		
		div.innerHTML = str;
		showLayer('concept_filter_box');
	}
	
	function handleSaveCohort() {
		if (currentPatientSet == null) {
			window.alert("<spring:message code="PatientSet.stillLoading"/>");
			return;
		} else {
			var cohortName = $('saveCohortName').value;
			var cohortDescr = $('saveCohortDescription').value;
			var ids = currentPatientSet.commaSeparatedPatientIds;
			DWRCohortBuilderService.saveCohort(cohortName, cohortDescr, ids,
					function() { window.alert('Saved cohort: ' + cohortName); }
				);
			hideLayer('saveCohortDiv');
		}		
	}
	
	function handleLoadButton() {
		if ($('loadBox').style.display == 'none') {
			hideLayer('saveBox');
			DWRCohortBuilderService.getSearchHistories(function(histories) {
					var loadBox = $('loadBox');
					loadBox.innerHTML = '';
					if (histories.length == 0)
						loadBox.innerHTML = '<spring:message javaScriptEscape="true" code="CohortBuilder.searchHistory.load.none"/>';
					else {
						var str = '<h4><u><spring:message javaScriptEscape="true" code="CohortBuilder.searchHistory.load"/></u></h4>';
						str += '<ul>';
						for (var i = 0; i < histories.length; ++i) {
							str += '<li><a href="javascript:loadSearchHistory(' + histories[i].id + ')">' + histories[i].name + ' <small>(' + histories[i].description + ')</small></a></li>';
						}
						str += '</ul>';
						str += '<input type="button" value="<spring:message code="general.cancel"/>" onClick="hideLayer(\'loadBox\')"/>';
						loadBox.innerHTML = str;
					}
					showLayer('loadBox');
				});
		} else {
			hideLayer('loadBox');
		}
	}
	
	function loadSearchHistory(id) {
		DWRCohortBuilderService.loadSearchHistory(id, function() { refreshPage(); });
	}
	
	function getCurrentPatientIds() {
		if (currentPatientSet != null) {
			return currentPatientSet.commaSeparatedPatientIds;
		} else {
			window.alert("<spring:message code="PatientSet.stillLoading"/>");
			return null;
		}
	}
	
	function linkSubmitHelper(idPrefix) {
		if (currentPatientSet != null) {
			document.getElementById(idPrefix + "_ptIds").value = currentPatientSet.commaSeparatedPatientIds;
			var fromDate = document.getElementById("nrFromDate");
			var toDate = document.getElementById("nrToDate");
			var fDate = "";
			var tDate = "";
			if ( fromDate )
				document.getElementById(idPrefix + "_fDate").value = fromDate.value;
			if ( toDate )
				document.getElementById(idPrefix + "_tDate").value = toDate.value;
			
			document.getElementById(idPrefix + "_form").submit();
			hideLayer('_linkMenu');
		} else {
			window.alert("<spring:message code="PatientSet.stillLoading"/>");
		}
	}
	
	// linkObj is an element in the form that needs to be submitted, such as the A or INPUT element that was pressed to submit it.
	function submitLink(linkObj) {
		if (currentPatientSet != null) {
			var form = linkObj.parentNode.parentNode;
			for (var i = 0; i < form.elements.length; ++i)
				if (form.elements[i].name == 'patientIds')
					form.elements[i].value = currentPatientSet.commaSeparatedPatientIds;
			form.submit();
		} else {
			window.alert("<spring:message code="PatientSet.stillLoading"/>");
		}
	}
	
	function showSaveFilterDialog(index, name) {
		var tempName = '#' + (index + 1);
		if (name != null && name != '')
			tempName += ' (<i>' + name + '</i>)';
		$('saveFilterTitle').innerHTML = tempName;
		$('saveFilterIndex').value = index;
		$('saveFilterName').value = '';
		$('saveFilterDescription').value = '';
		$('saveFilterSaveButton').style.disabled = 'false';
		$('saveFilterCancelButton').style.disabled = 'false';
		showLayer('saveFilterBox');
		$('saveFilterName').focus();
	}
	
	function handleSavedFilterMenuButton() {
		if ($('saved_filters').style.display == 'none') {
			$('saved_searches').innerHTML = '<li><spring:message code="general.loading"/></li>';
			$('saved_cohorts').innerHTML = '<li><spring:message code="general.loading"/></li>';
			showLayer('saved_filters');
			DWRCohortBuilderService.getSavedSearches(function(searches) {
					var str = '<ul>';
					if (searches.length == 0)
						str = '<spring:message code="general.none"/>';
					else {
						for (var i = 0; i < searches.length; ++i) {
							str += '<li><a href="cohortBuilder.form?method=addFilter&search_id=' + searches[i].id + '">' + searches[i].name + ' <small>(' + searches[i].description + ')</small></a></li>';
						}
						str += '</ul>';
					}
					$('saved_searches').innerHTML = str;
				});
			DWRCohortBuilderService.getSavedCohorts(function(cohorts) {
					var str = '<ul>';
					if (cohorts.length == 0)
						str = '<spring:message code="general.none"/>';
					else {
						for (var i = 0; i < cohorts.length; ++i) {
							str += '<form id="load_cohort_' + cohorts[i].id + '" method="post" action="cohortBuilder.form">';
							str += '<input type="hidden" name="method" value="addFilter"/>';
							str += '<input type="hidden" name="cohort_id" value="' + cohorts[i].id + '"/>';
							str += '<li><a href="javascript:document.getElementById(\'load_cohort_' + cohorts[i].id + '\').submit()">' + cohorts[i].name + '</a> <small>' + cohorts[i].description + '</small></li>';
							str += '</form>';
						}
						str += '</ul>';
					}
					$('saved_cohorts').innerHTML = str;
				});
		} else {
			hideLayer('saved_filters');
		}
	}
	
	function handleSaveFilter() {
		var index = $('saveFilterIndex').value;
		var name = $('saveFilterName').value;
		var descr = $('saveFilterDescription').value;
		$('saveFilterSaveButton').style.disabled = 'true';
		$('saveFilterCancelButton').style.disabled = 'true';
		DWRCohortBuilderService.saveHistoryElement(name, descr, index, function(success) {
				if (success) {
					window.alert('Saved #' + (index + 1) + ' as "' + name + '"');
				} else {
					window.alert('Failed to save.');
				}
				$('saveFilterSaveButton').style.disabled = 'false';
				$('saveFilterCancelButton').style.disabled = 'false';
				hideLayer('saveFilterBox');
			});
	}
	
	function refreshWorkflowOptions() {
		hideDiv('workflow');
		var program = DWRUtil.getValue('program');
		if (program == null || program == '') {
			DWRUtil.removeAllOptions('workflow');
			refreshStateOptions();
		} else
			DWRProgramWorkflowService.getWorkflowsByProgram(program, function(wfs) {
					DWRUtil.removeAllOptions('workflow');
					DWRUtil.addOptions('workflow', [" "]);
					DWRUtil.addOptions('workflow', wfs, 'id', 'name');
					if (wfs.length > 0)
						showDiv('workflow');
					refreshStateOptions();
				});
	}
	
	function refreshStateOptions() {
		hideDiv('state');
		var workflow = DWRUtil.getValue('workflow');
		if (workflow == null) {
			DWRUtil.removeAllOptions('state');
		} else
			DWRProgramWorkflowService.getStatesByWorkflow(workflow, function (states) {
					DWRUtil.removeAllOptions('state');
					DWRUtil.addOptions('state', states, 'id', 'name');
					if (states.length < 10)
						$('state').size = states.length;
					else
						$('state').size = 10;
					if (states.length > 0)
						showDiv('state');
				});
	}
	
	function toggleDrugOrderDateOptions(sel) {
		toggleLayer('drugOrderDateOption_current');
		toggleLayer('drugOrderDateOption_other');
	}
	
</script>

<script type="text/javascript">
	<%--
	This doesn't work yet, because there are no converters for patient filters
	function refreshHistory() {
		var div = document.getElementById('cohort_builder_search_history');
		div.innerHTML = 'Loading...';
		DWRCohortBuilderService.getUserSearchHistory(function(csh) {
				var str = '<ol>';
				var searches = csh.items;
				var numSearches = searches.length;
				for (var i = 0; i < numSearches; ++i) {
					var search = searches[i];
					var filter = search.filter;
					var result = search.cachedResult;
					str += '<li>' + filter.name + ' <small>(' + filter.description + ')</small>';
					if (result != null)
						str += ' (' + result.size + ' <spring:message code="CohortBuilder.numResults"/>)';
					str += '</li>';
				}
				str += '</ol>';
				div.innerHTML = 'testing: ' + str;
			});
	}
	--%>
	
	function previewPageTo(index) {
		if (index < 0)
			index = currentPatientSet.size - patientPageSize;
		fromPatientIndex = index;
		refreshPreview();
	}
	
	function previewPageBy(delta) {
		fromPatientIndex += delta;
		refreshPreview();
	}

	var fromPatientIndex = 0;
	var patientPageSize = 15;
	
	function refreshPreview() {
		var div = document.getElementById('cohort_builder_preview');
		showLayer('cohort_builder_preview_loading_message');
		hideLayer('cohort_builder_preview_numbers');
		hideLayer('cohort_builder_preview_patients');
		var method = DWRUtil.getValue('cohort_builder_preview_method');
		if (method == 'last')
			DWRCohortBuilderService.getLastResult(displayPreview);
		else if (method == 'and')
			DWRCohortBuilderService.getResultCombineWithAnd(displayPreview);
		else if (method == 'or')
			DWRCohortBuilderService.getResultCombineWithOr(displayPreview);
		else // should be an integer: a zero-based index into the search history
			DWRCohortBuilderService.getResultForSearch(method, displayPreview);
	}
	
	var currentPatientSet = null;
	var goesUntilLast = false;

	function displayPreview(ps) {
		cohort_setPatientIds(ps.commaSeparatedPatientIds);
		currentPatientSet = ps;
		var ids = ps.patientIds;		
	}
	
	function removeHiddenDivs() {
		var divs = document.getElementsByTagName("DIV");
		var i = 0;
		while (i < divs.length) {
			var div = divs[i];
			if (div.style.display == "none") {
				div.parentNode.removeChild(div);
			}
			else {
				i = i + 1;
			}
		}
	}

</script>

<h2><spring:message code="CohortBuilder.title"/></h2>	

<div id="cohort_builder_add_filter" style="padding: 4px">
	<b><spring:message code="general.search"/></b>

	<span style="padding: 3px; margin: 0px 3px; background-color: #ffffdd; border: 1px black solid">
		<a href="javascript:handleSavedFilterMenuButton()"><spring:message code="CohortBuilder.savedFilterMenu"/></a>
	</span>
	<div id="saved_filters" style="position: absolute; z-index: 1; border: 1px black solid; background-color: #ffffdd; display: none; padding: 4px">
		<h4><spring:message code="CohortBuilder.savedSearches" /></h4>
		<ul id="saved_searches"></ul>
		<h4><spring:message code="CohortBuilder.savedCohorts" /></h4>
		<ul id="saved_cohorts"></ul>
		<br/>
		&nbsp; <input type="button" value="<spring:message code="general.cancel"/>" onclick="handleSavedFilterMenuButton()"/>
	</div>

	<c:if test="${fn:length(model.shortcuts) > 0}">
		<c:forEach var="shortcut" items="${model.shortcuts}" varStatus="status">
			<span style="padding: 3px 0px; margin: 0px 3px; background-color: #ffffaa; border: 1px black solid">
				<c:if test="${shortcut.concrete}">
					<a href="cohortBuilder.form?method=addFilter&filter_id=${shortcut.patientFilter.reportObjectId}"><spring:message code="${shortcut.label}"/></a>
				</c:if>
				<c:if test="${!shortcut.concrete}">
					<form id="shortcut${shortcut.label}" method="post" action="cohortBuilder.form" style="display: inline">
						<c:if test="${!shortcut.hasPromptArgs}">
							<a href="javascript:document.getElementById('shortcut${shortcut.label}').submit()">
						</c:if>
						<spring:message code="${shortcut.label}"/>
						<c:if test="${!shortcut.hasPromptArgs}">
							</a>
						</c:if>
						<input type="hidden" name="method" value="addDynamicFilter"/>
						<input type="hidden" name="filterClass" value="${shortcut.className}"/>
						<input type="hidden" name="vars" value="${shortcut.vars}"/>
						<c:forEach var="arg" items="${shortcut.args}">
							<c:if test="${empty arg.argClass}">
								<spring:message code="${arg.argName}"/>
							</c:if>
							<c:if test="${arg.argClass != null}">
								<c:choose>
									<c:when test="${not empty arg.argValue}">
										<input type="hidden" name="${arg.argName}" value="${arg.argValue}"/>
									</c:when>
									<c:otherwise>
										<spring:message code="${shortcut.label}.${arg.argName}"/>
										<openmrs:fieldGen type="${arg.argClass.name}" formFieldName="${arg.argName}" val="" parameters="optionHeader=[blank]|fieldLength=10" />
									</c:otherwise>
								</c:choose>
							</c:if>
						</c:forEach>
						<c:if test="${shortcut.hasPromptArgs}">
							<input type="submit" value="Go"/>
						</c:if>
					</form>
				</c:if>
			</span>
		</c:forEach>
	</c:if>
	
	<br/>
	
	<div id="cohortSearchTabs">
		<ul>
			<li>&nbsp;</li>
			<li><a id="searchTab_concept" href="#" onClick="changeSearchTab(this, 'concept_to_filter_search')"><spring:message code="CohortBuilder.searchTab.concept"/></a></li>
			<li><a id="searchTab_attribute" href="#" onClick="changeSearchTab(this)"><spring:message code="CohortBuilder.searchTab.personAttribute"/></a></li>
			<li><a id="searchTab_encounter" href="#" onClick="changeSearchTab(this)"><spring:message code="CohortBuilder.searchTab.encounter"/></a></li>
			<li><a id="searchTab_program" href="#" onClick="changeSearchTab(this)"><spring:message code="CohortBuilder.searchTab.program"/></a></li>
			<li><a id="searchTab_drugOrder" href="#" onClick="changeSearchTab(this)"><spring:message code="CohortBuilder.searchTab.drugOrder"/></a></li>
			<li><a id="searchTab_composition" href="#" onClick="changeSearchTab(this, 'composition')"><spring:message code="CohortBuilder.searchTab.composition"/></a></li>
		</ul>
	</div>
	
	<div id="cohortSearchTabContent" style="border: 1px black solid; border-top: none; padding: 4px 5px 2px 10px;">
	
		<div id="searchTab_concept_content" style="display: none">
			<div dojoType="ConceptSearch" widgetId="concept_to_filter_search" conceptId="" searchLabel='<spring:message code="CohortBuilder.addConceptFilter"/>' showVerboseListing="true" includeVoided="false"></div>
			<div id="concept_filter_box" style="display: none; border-top: 1px #aaaaaa solid"></div>
		</div>
		
		<div id="searchTab_attribute_content" style="display: none">
			<div style="background: #f6f6f6; border: 1px #808080 solid; padding: 0.5em; margin: 0.5em">
				<form method="post" action="cohortBuilder.form">
					<input type="hidden" name="method" value="addDynamicFilter"/>
					<input type="hidden" name="filterClass" value="org.openmrs.reporting.PatientCharacteristicFilter" />
					<input type="hidden" name="vars" value="gender#java.lang.String,minBirthdate#java.util.Date,maxBirthdate#java.util.Date,minAge#java.lang.Integer,maxAge#java.lang.Integer,aliveOnly#java.lang.Boolean,deadOnly#java.lang.Boolean" />
					<h4>Search by Demographics:</h4>
					<br/>
					<table>
						<tr>
							<td align="right">Gender:</td>
							<td>
								<select name="gender">
									<option value=""><spring:message code="general.allOptions" /></option>
									<option value="m"><spring:message code="Person.gender.male" /></option>
									<option value="f"><spring:message code="Person.gender.female" /></option>
								</select>
							</td>
						</tr>
						<tr>
							<td align="right">Age:</td>
							<td>
								between <input type="text" name="minAge" size="3"/> and <input type="text" name="maxAge" size="3"/> years
							</td>
						</tr>
						<tr>
							<td align="right">Birthdate:</td>
							<td>
								between <input type="text" name="minBirthdate" size="10" onClick="showCalendar(this)" /> and <input type="text" name="maxBirthdate" size="10" onClick="showCalendar(this)" />
							</td>
						</tr>
						<tr>
							<td align="right"></td>
							<td>
								<input type="checkbox" name="aliveOnly" value="true" /> Alive only
								&nbsp;&nbsp;&nbsp;&nbsp;
								<input type="checkbox" name="deadOnly" value="true" /> Dead only
							</td>
						</tr>
					</table>
					<input type="submit" value="<spring:message code="general.search"/>" />
					<br/>
				</form>
			</div>
			<div style="background: #f6f6f6; border: 1px #808080 solid; padding: 0.5em; margin: 0.5em">
				<form method="post" action="cohortBuilder.form">
					<input type="hidden" name="method" value="addDynamicFilter"/>
					<input type="hidden" name="filterClass" value="org.openmrs.reporting.PersonAttributeFilter" />
					<input type="hidden" name="vars" value="attribute#org.openmrs.PersonAttributeType,value#java.lang.String" />
					
					<h4>Search by Person Attributes</h4>
					<br/>
					Which attribute?
					<select name="attribute">
						<option value="">Any attribute value</option>
						<c:forEach var="attributeType" items="${model.personAttributeTypes}">
							<option value="${attributeType.personAttributeTypeId}">${attributeType.name}</option>
						</c:forEach>
					</select>
					&nbsp;&nbsp;&nbsp;
					<spring:message code="CohortBuilder.whatValueQuestion" />
					<input type="text" name="value" size="30"/>
					<br/>
					<input type="submit" value="<spring:message code="general.search"/>" />
				</form>
			</div>
		</div>
		
		<div id="searchTab_encounter_content" style="display: none">
			<div style="background: #f6f6f6; border: 1px #808080 solid; padding: 0.5em; margin: 0.5em">
				<h4><spring:message code="CohortBuilder.addEncounterFilter"/></h4>
				<ul><li>
				<form method="post" action="cohortBuilder.form">
					<input type="hidden" name="method" value="addDynamicFilter"/>
					<input type="hidden" name="filterClass" value="org.openmrs.reporting.EncounterPatientFilter" />
					<input type="hidden" name="vars" value="encounterTypeList#*org.openmrs.EncounterType,location#org.openmrs.Location,form#org.openmrs.Form,atLeastCount#java.lang.Integer,atMostCount#java.lang.Integer,withinLastMonths#java.lang.Integer,withinLastDays#java.lang.Integer,sinceDate#java.util.Date,untilDate#java.util.Date" />
					Patients having encounters
					<table style="margin-left: 40px">
						<tr valign="top">
							<td>
								<spring:message code="CohortBuilder.optionalPrefix" />
							</td>
							<td>
								of type
							</td>
							<td>
								(Leave blank for All Encounter Types)
								<br/>
								<select name="encounterTypeList" multiple="true" size="10">
									<c:forEach var="encType" items="${model.encounterTypes}">
										<option value="${encType.encounterTypeId}">${encType.name}</option>
									</c:forEach>
								</select>
							</td>
						</tr>
						<tr valign="top">
							<td>
								<spring:message code="CohortBuilder.optionalPrefix" />
							</td>
							<td>
								at location
							</td>
							<td>
								<select name="location">
									<option value=""><spring:message code="general.allOptions"/></option>
									<c:forEach var="location" items="${model.locations}">
										<option value="${location.locationId}">${location.name}</option>
									</c:forEach>
								</select>
							</td>
						</tr>
						<tr valign="top">
							<td>
								<spring:message code="CohortBuilder.optionalPrefix" />
							</td>
							<td>
								from form
							</td>
							<td>
								<select name="form">
									<option value=""><spring:message code="general.allOptions"/></option>
									<c:forEach var="form" items="${model.forms}">
										<option value="${form.formId}">${form.name}</option>
									</c:forEach>
								</select>
							</td>
						</tr>
						<tr valign="top">
							<td>
								<spring:message code="CohortBuilder.optionalPrefix" />
							</td>
							<td colspan="2">
								at least this many
								<input type="text" size="3" name="atLeastCount" />
								and up to this many
								<input type="text" size="3" name="atMostCount" />
							</td>
						</tr>
						<tr valign="top">
							<td>
								<spring:message code="CohortBuilder.optionalPrefix" />
							</td>
							<td colspan="2">
								within the last <input type="text" size="3" name="withinLastMonths" />months
								and <input type="text" size="3" name="withinLastDays" />days
							</td>
						</tr>
						<tr valign="top">
							<td>
								<spring:message code="CohortBuilder.optionalPrefix" />
							</td>
							<td colspan="2">
								since <input type="text" size="10" name="sinceDate" onClick="showCalendar(this)" />
								until <input type="text" size="10" name="untilDate" onClick="showCalendar(this)" />
							</td>
						</tr>
					</table>
					<input type="submit" value="<spring:message code="general.search" />"/>
				</form>
				</li></ul>
			</div>
			
			<div style="background: #f6f6f6; border: 1px #808080 solid; padding: 0.5em; margin: 0.5em">
				<h4><spring:message code="CohortBuilder.addLocationFilter"/></h4>
				<ul><li>
				<form method="post" action="cohortBuilder.form">
					<input type="hidden" name="method" value="addDynamicFilter"/>
					<input type="hidden" name="filterClass" value="org.openmrs.reporting.LocationPatientFilter" />
					<input type="hidden" name="vars" value="location#org.openmrs.Location,calculationMethod#org.openmrs.api.PatientSetService$PatientLocationMethod" />
					Patients belonging to
					<select name="location">
						<option value=""><spring:message code="general.none" /></option>
						<c:forEach var="location" items="${model.locations}">
							<option value="${location.locationId}">${location.name}</option>
						</c:forEach>
					</select>
					according to method
					<select name="calculationMethod">
						<option value="PATIENT_HEALTH_CENTER">Assigned Health Center</option>
						<option value="ANY_ENCOUNTER">Any Encounter</option>
						<option value="LATEST_ENCOUNTER">Most Recent Encounter</option>
						<option value="EARLIEST_ENCOUNTER">Earliest Encounter</option>
					</select>			
					<br/>
					<input type="submit" value="<spring:message code="general.search" />"/>
				</form>
				</li></ul>
			</div>
		</div>
	
		<div id="searchTab_program_content" style="display: none">
			<div style="background: #f6f6f6; border: 1px #808080 solid; padding: 0.5em; margin: 0.5em">
				<form method="post" action="cohortBuilder.form">
					<input type="hidden" name="method" value="addDynamicFilter"/>
					<input type="hidden" name="filterClass" value="org.openmrs.reporting.ProgramStatePatientFilter" />
					<input type="hidden" name="vars" value="program#org.openmrs.Program,stateList#*org.openmrs.ProgramWorkflowState,withinLastMonths#java.lang.Integer,withinLastDays#java.lang.Integer,sinceDate#java.util.Date,untilDate#java.util.Date" />
		
					<h4><spring:message code="CohortBuilder.addProgramFilter"/></h4>
					<table>
						<tr>
							<td>Program:</td>
							<td>
								<select name="program" id="program" onChange="refreshWorkflowOptions()">
									<option value=""></option>
									<c:forEach var="program" items="${model.programs}">
										<option value="${program.programId}">${program.concept.name.name}</option>
									</c:forEach>
								</select>
							</td>
						</tr>
						<tr>
							<td>Workflow:</td>
							<td>
								<select style="display: none" name="workflow" id="workflow" onChange="refreshStateOptions()"></select>
							</td>
						</tr>
						<tr valign="top">
							<td>State:</td>
							<td>
								<select style="display: none" name="stateList" id="state" multiple="true" size="1"></select>
							</td>
						</tr>
						<tr>
							<td>When?</td>
							<td>
								<spring:message code="CohortBuilder.optionalPrefix" />
								on or after:<input type="text" size="10" name="sinceDate" onClick="showCalendar(this)" />
								<br/>
								<spring:message code="CohortBuilder.optionalPrefix" />
								on or before:<input type="text" size="10" name="untilDate" onClick="showCalendar(this)" />
							</td>
						</tr>
					</table>
					<input type="submit" value="<spring:message code="general.search" />"/>
				</form>
			</div>
		</div>
		
		<div id="searchTab_drugOrder_content" style="display: none">
			<spring:message code="CohortBuilder.addDrugOrderFilter"/>
			<div style="background: #f6f6f6; border: 1px #808080 solid; padding: 0.5em; margin: 0.5em">
				<form method="post" action="cohortBuilder.form" onSubmit="removeHiddenDivs()">
					<input type="hidden" name="method" value="addDynamicFilter"/>
					<input type="hidden" name="filterClass" value="org.openmrs.reporting.DrugOrderFilter" />
					<input type="hidden" name="vars" value="withinLastMonths#java.lang.Integer,withinLastDays#java.lang.Integer,sinceDate#java.util.Date,untilDate#java.util.Date,anyOrAll#org.openmrs.api.PatientSetService$GroupMethod,drugList#*org.openmrs.Drug,drugSets#*org.openmrs.Concept" />
					<b>Patients taking specific drugs</b>
					<br/><br/>
					<select name="anyOrAll">
						<option value="ANY">Any</option>
						<option value="ALL">All</option>
						<option value="NONE">None</option>
					</select>
					of the following:
					(hold down CTRL if you want to select more than one, or leave blank if you don't care which drugs)
					<br/><br/>
					<table style="margin-left: 40px">
						<tr>
							<td>
								Drugs:
								<br/>
								<select name="drugList" multiple="true" size="10">
									<c:forEach var="drug" items="${model.drugs}">
										<option value="${drug.drugId}"/>${drug.name}</option>
									</c:forEach>
								</select>			
							</td>
							<td>
								<c:if test="${fn:length(model.drugSets) > 0}">
									Drug Sets:
									<br/>
									<select name="drugSets" multiple="true" size="10">
										<c:forEach var="drugSet" items="${model.drugSets}">
											<option value="${drugSet.conceptId}">${drugSet.name}</option>
										</c:forEach>
									</select>
								</c:if>
								<c:forEach var="drugSet" items="${drugSets}">
								</c:forEach>
							</td>
						</tr>
					</table>
					<span style="margin-left: 40px">
					</span>
					<br/>
					<br/>
					<div id="drugOrderDateOption_current">
						<input type="hidden" name="withinLastDays" value="0"/>
						When?
						&nbsp;
						Current drug regimen
						&nbsp;&nbsp;&nbsp;&nbsp;
						<a href="javascript:toggleDrugOrderDateOptions()">[all]</a>
					</div>
					<div id="drugOrderDateOption_other" style="display: none">
						When?
						&nbsp;
						All drug regimens
						&nbsp;&nbsp;&nbsp;&nbsp;
						<a href="javascript:toggleDrugOrderDateOptions()">[current]</a>
						<br/>
							&nbsp;&nbsp;&nbsp;&nbsp;
							<spring:message code="CohortBuilder.optionalPrefix" />
							<spring:message code="CohortBuilder.whenPrefix" />
							<spring:message code="CohortBuilder.withinMonthsAndDays" arguments="withinLastMonths,withinLastDays" />
						<br/>
							&nbsp;&nbsp;&nbsp;&nbsp;
							<spring:message code="CohortBuilder.optionalPrefix" />
							<spring:message code="CohortBuilder.dateRangePrefix" />
							<spring:message code="CohortBuilder.fromDateToDate" arguments="sinceDate,untilDate" />
						<br/>
					</div>
					
					<br/>
					<input type="submit" value="<spring:message code="general.search" />"/>
				</form>
			</div>
			<div style="background: #f6f6f6; border: 1px #808080 solid; padding: 0.5em; margin: 0.5em">
				<form method="post" action="cohortBuilder.form">
					<input type="hidden" name="method" value="addDynamicFilter"/>
					<input type="hidden" name="filterClass" value="org.openmrs.reporting.DrugOrderStopFilter" />
					<input type="hidden" name="vars" value="withinLastMonths#java.lang.Integer,withinLastDays#java.lang.Integer,sinceDate#java.util.Date,untilDate#java.util.Date,drugList#*org.openmrs.Drug,genericDrugList#*org.openmrs.Concept,discontinued#java.lang.Boolean,discontinuedReasonList#*org.openmrs.Concept" />
					<input type="hidden" name="discontinued" value="true" />
					<b>Patients who stopped or changed a drug</b>
					<br/><br/>
						<spring:message code="CohortBuilder.optionalPrefix" />
						<spring:message code="CohortBuilder.whenPrefix" />
						<spring:message code="CohortBuilder.withinMonthsAndDays" arguments="withinLastMonths,withinLastDays" />
					<br/>
						<spring:message code="CohortBuilder.optionalPrefix" />
						<spring:message code="CohortBuilder.dateRangePrefix" />
						<spring:message code="CohortBuilder.fromDateToDate" arguments="sinceDate,untilDate" />
					<br/>
					<table><tr valign="top"><td style="padding-right: 20px">
						<spring:message code="CohortBuilder.optionalPrefix" />
						Reason for stop/change: <br/>
						(leave blank for <spring:message code="general.allOptions" />)
						<br/>
						<select name="discontinuedReasonList" size="10" multiple="true">
							<c:forEach var="reason" items="${model.orderStopReasons}">
								<option value="${reason.conceptId}">${reason.name.name}</option>
							</c:forEach>
						</select>
					</td><td style="padding-right: 20px">
						<spring:message code="CohortBuilder.optionalPrefix" />
						Only these drugs: <br/>
						(leave blank for <spring:message code="general.allOptions" />)
						<br/>
						<select multiple="true" size="10" name="drugList">
							<c:forEach var="drug" items="${model.drugs}">
								<option value="${drug.drugId}">${drug.name}</option>
							</c:forEach>
						</select>
					</td><td>
						<spring:message code="CohortBuilder.optionalPrefix" />
						Only these generics: <br/>
						(leave blank for <spring:message code="general.allOptions" />)
						<br/>
						<select multiple="true" size="10" name="genericDrugList">
							<c:forEach var="concept" items="${model.drugConcepts}">
								<option value="${concept.conceptId}">${concept.name.name}</option>
							</c:forEach>
						</select>
					</td></tr></table>
					
					<br/>
					<input type="submit" value="<spring:message code="general.search" />"/>
				</form>
			</div>
		</div>
		
		<div id="searchTab_composition_content" style="display: none">
			<form method="post" action="cohortBuilder.form">
				<input type="hidden" name="method" value="addFilter"/>
				<h4><spring:message code="CohortBuilder.addCompositionFilter"/></h4>
				<i><spring:message code="CohortBuilder.compositionHelp"/></i>
				<br/>
				<br/>
				<spring:message code="general.search"/>:
				<input type="text" name="composition" id="composition" size="72"/>
				<br/>
				<br/>
				<input type="submit" value="<spring:message code="general.add"/>"/>
			</form>
		</div>
	</div>
	
</div>

<div id="cohort_builder_search_history" style="padding: 4px; border: 1px black solid; background-color: #e8e8e8">

	<div id="saveBox" style="position: absolute; z-index: 1; border: 1px black solid; background-color: #ffe0e0; display: none">
		<form method="post" action="cohortBuilder.form">
			<input type="hidden" name="method" value="saveHistory"/>
			<table>
				<tr>
					<th colspan="2"><spring:message code="CohortBuilder.searchHistory.save"/></th>
				</tr>
				<tr>
					<td><spring:message code="general.name"/></td>
					<td><input type="text" name="name" id="saveBoxName"/></td>
				</tr>
				<tr>
					<td><spring:message code="general.description"/></td>
					<td><input type="text" name="description" size="60"/></td>
				</tr>
				<!--
				<tr>
					<td><spring:message code="CohortBuilder.privateOrShared"/></td>
					<td><spring:message code="general.notYetImplemented"/></td>
				</tr>
				<tr>
					<td><spring:message code="CohortBuilder.keepUntil"/></td>
					<td>
						<select>
							<option value=""><spring:message code="CohortBuilder.keepUntil.forever"/></option>
							<option value=""><spring:message code="general.nWeeks" arguments="4"/> (<spring:message code="general.notYetImplemented"/>)</option>
							<option value=""><spring:message code="general.nWeeks" arguments="2"/> (<spring:message code="general.notYetImplemented"/>)</option>
							<option value=""><spring:message code="general.nWeeks" arguments="1"/> (<spring:message code="general.notYetImplemented"/>)</option>
						</select>
					</td>
				</tr>
				-->
				<tr>
					<td></td>
					<td>
						<input style="margin: 0em 1em" type="submit" value="<spring:message code="general.save"/>"/>
						<input style="margin: 0em 1em" type="button" value="<spring:message code="general.cancel"/>" onClick="toggleLayer('saveBox');"/>
					</td>
				</tr>
			</table>
		</form>
	</div>

	<div id="loadBox" style="position: absolute; margin: 1em; padding: 1em; z-index: 1; border: 1px black solid; background-color: #ffe0e0; display: none"></div>

	<h3>
		<spring:message code="CohortBuilder.searchHistory"/>
		<c:if test="${model.searchHistory.size > 0}">
			<a href="#" onclick="toggleLayer('saveBox'); hideLayer('loadBox'); document.getElementById('saveBoxName').focus(); return false;" title="<spring:message code="CohortBuilder.searchHistory.save"/>">
				<img src="${pageContext.request.contextPath}/images/save.gif" style="border: 0px" />
			</a>
		</c:if>
		<a href="#" onClick="handleLoadButton(); return false;" title='<spring:message code="CohortBuilder.searchHistory.load"/>'>
			<img src="${pageContext.request.contextPath}/images/open.gif" style="border: 0px" />
		</a>
		<form method="post" action="cohortBuilder.form" style="display: inline">
			<input type="hidden" name="method" value="clearHistory"/>
			<input type="image" title="<spring:message code="CohortBuilder.searchHistory.clear"/>" src="${pageContext.request.contextPath}/images/delete.gif"/>
		</form>
	</h3>

	<div id="saveFilterBox" style="padding: 1em; position: absolute; z-index: 1; border: 1px black solid; background-color: #ffe0e0; display: none">
		<b><u><spring:message code="CohortBuilder.cohortDefinition.save"/></u></b>
		<br/><br/>
		<spring:message code="general.saving" arguments="<span id='saveFilterTitle'></span>"/>
		<br/><br/>
		<spring:message code="general.name"/>: <input type="text" id="saveFilterName"/> <br/>
		<spring:message code="general.description"/>: <input type="text" id="saveFilterDescription" size="60"/> <br/><br/>
		<input type="hidden" id="saveFilterIndex"/>
		<div align="center">
			<input type="button" id="saveFilterSaveButton" value="<spring:message code="general.save"/>" onClick="handleSaveFilter()"/>
			<input type="button" id="saveFilterCancelButton" value="<spring:message code="general.cancel"/>" onClick="toggleLayer('saveFilterBox')"/>
		</div>
	</div>	
			
	<c:if test="${model.searchHistory.size == 0}">
		<div><spring:message code="CohortBuilder.searchHistory.none"/></div>
	</c:if>
	<c:if test="${model.searchHistory.size > SHOW_LAST_N}">
		<div id="fullSearchHistory" style="display: none">
			<div style="text-align: center">
				<a href="javascript:hideLayer('fullSearchHistory'); showLayer('showFullSearchHistoryButton')"><spring:message code="CohortBuilder.searchHistory.showRecent"/></a>
			</div>
	</c:if>
	<c:forEach var="item" items="${model.searchHistory.items}" varStatus="iter">
		<c:if test="${model.searchHistory.size > SHOW_LAST_N && iter.index == (model.searchHistory.size - SHOW_LAST_N)}">
			</div>
			<div id="showFullSearchHistoryButton" style="text-align: center">
				<a href="javascript:showLayer('fullSearchHistory'); hideLayer('showFullSearchHistoryButton')"><spring:message code="CohortBuilder.searchHistory.showFull"/></a>
			</div>
		</c:if>
		
		<table style="margin: 1px 4px; width: 100%; border: 1px black solid;
			<c:if test="${item.saved}"> background-color: #c0d0ff </c:if>
			<c:if test="${!item.saved}"> background-color: #e0ffe0 </c:if>
		">
			<tr>
				<td width="25">
					<c:set var="temp" value="${iter.index}"/>
					<c:if test="${iter.last}">
						<c:set var="temp" value="last"/>
					</c:if>
					<a href="#" onClick="DWRUtil.setValue('cohort_builder_preview_method', '${temp}'); refreshPreview();">
						${iter.count}.
					</a>
				</td>
				<td>
					${item.name}
					<c:if test="${not empty item.description}">
						<small>${item.description}</small>
					</c:if>
				</td>
				<td align="right">
					<span id="results_for_filter_${iter.count}">
						<c:if test="${item.cachedResult != null}">
							${item.cachedResult.size}
						</c:if>
						<c:if test="${item.cachedResult == null}">
							?
						</c:if>
					</span>
					<spring:message code="CohortBuilder.numResults"/>
					<c:if test="${item.cachedResult != null}">
						<small>(cached)</small>
					</c:if>
					<c:if test="${item.saved}">
						<small>(<spring:message code="general.saved" />)</small>
					</c:if>
					<c:if test="${!item.saved}">
						<a href="#" onclick="showSaveFilterDialog(${iter.index}, '${item.name}'); return false;" title='<spring:message code="CohortBuilder.saveFilterDefinition.help"/>'><img src="${pageContext.request.contextPath}/images/save.gif" style="border: 0px;" /></a>
					</c:if>
					<a href="cohortBuilder.form?method=removeFilter&index=${iter.index}" title='<spring:message code="CohortBuilder.removeFilter.help"/>'><img src="${pageContext.request.contextPath}/images/delete.gif" style="border: 0px;"/></a>
				</td>
				<c:if test="${item.cachedResult == null}">
					<script type="text/javascript">
						DWRCohortBuilderService.getResultCountForSearch(${iter.index},
							function(count) {
								var temp = document.getElementById('results_for_filter_${iter.count}');
								temp.innerHTML = count;
							});
					</script>
				</c:if>
			</tr>
		</table>
	</c:forEach>
</div>

<div id="cohort_builder_preview" style='padding: 4px<c:if test="${model.searchHistory.size == 0}">; display: none"</c:if>'>

	<div id="cohort_builder_button_panel" style="padding: 1px 4px; margin: 4px 0px">
		<table width="100%">
			<tr>
				<td>
					<spring:message code="CohortBuilder.displayMethod"/>
					<select id="cohort_builder_preview_method" onChange="refreshPreview()">
						<option value="last"><spring:message code="CohortBuilder.displayMethod.last"/></option>
						<option value="and"><spring:message code="CohortBuilder.displayMethod.and"/></option>
						<option value="or"><spring:message code="CohortBuilder.displayMethod.or"/></option>
						<c:set var="temp" value="${model.searchHistory.size - 2}"/>
						<c:if test="${temp < 0}">
							<c:set var="temp" value="0"/>
						</c:if>
						<c:forEach varStatus="index" items="${model.searchHistory.items}" end="${temp}">
							<option value="${index.index}">${index.count}</option>
						</c:forEach>
					</select>
				</td>
			</tr>
		</table>
	</div>

	<div style="border: 1px gray solid; padding: 0.5em; margin: 0.5em">
		<openmrs:portlet url="cohort" parameters="linkUrl=patientDashboard.form" />
	</div>

	<div id="cohort_builder_actions" style="position: relative; border: 1px black solid">
		<div id="saveCohortDiv" style="position: absolute; margin: 1em; padding: 1em; bottom: 0px; border: 2px black solid; background-color: #f6f6f6; display: none">
			<b><u>Save Cohort (i.e. list of patient ids)</u></b>
			<br/><br/>
			<table>
				<tr>
					<td width="30px"><spring:message code="general.name"/></td>
					<td style="text-align:left"><input type="text" id="saveCohortName"/></td>
				</tr>
				<tr>
					<td width="30px"><spring:message code="general.description"/></td>
					<td style="text-align:left"><input type="text" id="saveCohortDescription"/></td>
				</tr>
				<tr>
					<td></td>
					<td style="text-align:left">
						<input type="button" value="<spring:message code="general.save"/>" onClick="handleSaveCohort()" />
						&nbsp;
						<input type="button" value="<spring:message code="general.cancel"/>" onClick="toggleLayer('saveCohortDiv')" />
					</td>
				</tr>
			</table>
		</div>

		<c:if test="${fn:length(model.links) > 0}">
			<div id="_linkMenu" style="	border: 1px solid black; background-color: #f0f0a0; position: absolute; bottom: 0px; padding-right: 1.2em; z-index: 1; display: none">
				<br />
				&nbsp;&nbsp;&nbsp;<span style="width: 200px; text-align: right;"><a href="javascript:hideLayer('_linkMenu');" >[<spring:message code="general.close" />]</a></span>
				<ul>
					<c:forEach var="item" items="${model.links}" varStatus="loopStatus">
						<li>
							<form method="post" action="${item.url}" id="link_${loopStatus.index}_form" style="display: inline" <c:if test="${model.linkTarget != null}">target="${model.linkTarget}"</c:if>>
								<input type="hidden" name="patientIds" id="link_${loopStatus.index}_ptIds" value=""/>
								<c:forEach var="arg" items="${item.arguments}">
									<input type="hidden" name="${arg.name}" value="${arg.value}"/>
								</c:forEach>
								<input type="hidden" name="fDate" id="link_${loopStatus.index}_fDate" value="" />
								<input type="hidden" name="tDate" id="link_${loopStatus.index}_tDate" value="" />
								<a href="javascript:linkSubmitHelper('link_${loopStatus.index}')">
									<spring:message code="${item.label}"/>
								</a>
							</form>
						</li>
					</c:forEach>
				</ul>
				&nbsp;&nbsp;<spring:message code="general.dateConstraints" /> (<spring:message code="general.optional" />):<br />
				<table style="padding-left: 15px;">
					<tr>
						<td><spring:message code="general.fromDate" />:</td>
						<td><openmrs:fieldGen type="java.util.Date" formFieldName="nrFromDate" val="" /></td>
					</tr>
					<tr>
						<td><spring:message code="general.toDate" />:</td>
						<td><openmrs:fieldGen type="java.util.Date" formFieldName="nrToDate" val="" /></td>
					</tr>
				</table>
				<br />
			</div>
		</c:if>

		<b><spring:message code="CohortBuilder.actionsMenu"/></b>
		
		<openmrs:extensionPopupMenu
			pointId="org.openmrs.quickWebReports"
			popupDivId="webReportPopupMenu"
			label="CohortBuilder.reportsPopupButton"
			position="above"
			parameters="patientIds=javascript:getCurrentPatientIds()" />

		<a href="#" title='<spring:message code="CohortBuilder.saveCohort.help" />' onClick="toggleLayer('saveCohortDiv'); document.getElementById('saveCohortName').focus(); return false;">
			<img src="${pageContext.request.contextPath}/images/save.gif" style="border: 0px" />
		</a>

		<c:if test="${fn:length(model.links) > 0}">
			<a href="#" onClick="javascript:toggleLayer('_linkMenu')" style="border: 1px black solid"><spring:message code="Analysis.linkButton"/></a>
		</c:if>

	</div>

</div>

<script type="text/javascript">
	changeSearchTab('searchTab_concept');
	refreshPreview();
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %> 