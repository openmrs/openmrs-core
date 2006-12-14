<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patient Sets" otherwise="/login.htm" redirect="/cohortBuilder.list" />

<c:set var="OPENMRS_DO_NOT_SHOW_PATIENT_SET" scope="request" value="true"/>

<%@ include file="/WEB-INF/template/header.jsp" %>

<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRCohortBuilderService.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRPatientService.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRPatientSetService.js'></script>
<openmrs:htmlInclude file="/scripts/dojoConfig.js"></openmrs:htmlInclude>
<openmrs:htmlInclude file="/scripts/dojo/dojo.js"></openmrs:htmlInclude>

<script type="text/javascript">

	dojo.require("dojo.widget.openmrs.ConceptSearch");
	dojo.require("dojo.widget.openmrs.OpenmrsPopup");
	dojo.hostenv.writeIncludes();
	
	dojo.addOnLoad( function() {
		dojo.event.topic.subscribe("concept_to_filter_search/select", 
			function(msg) {
				if (msg) {
					var concept = msg.objs[0];
					var conceptPopup = dojo.widget.manager.getWidgetById("concept_to_filter_selection");
					conceptPopup.displayNode.innerHTML = concept.name;
					conceptPopup.hiddenInputNode.value = concept.conceptId;
					showPossibleFilters(concept);
				}
			}
		);
	})
	
	function classFilterTemplate(concept) {
		if (concept.className == 'State' || concept.className == 'Workflow' || concept.className == 'Program')
			return 'Program/Workflow/State filter not yet implemented';
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
			return 'Handling Datatype N/A not yet implemented. Any suggestions on how it should behave?';
		if (hl7Abbrev != 'NM' && hl7Abbrev != 'ST' && hl7Abbrev != 'CWE') {
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
		str += ',withinLastMonths#java.lang.Integer"/>';
		if (hl7Abbrev == 'NM')
			str += '<select name="timeModifier"><option value="ANY">ANY</option><option value="NO">NO</option><option value="FIRST">FIRST</option><option value="LAST">LAST</option><option value="MIN">MIN</option><option value="MAX">MAX</option><option value="AVG">AVG</option></select> ';
		else if (hl7Abbrev == 'ST' || hl7Abbrev == 'CWE')
			str += '<select name="timeModifier"><option value="ANY">ANY</option><option value="NO">NO</option><option value="FIRST">FIRST</option><option value="LAST">LAST</option></select> ';
		str += '<input type="hidden" name="question" value="' + concept.conceptId + '"/>';
		str += concept.name;
		if (hl7Abbrev == 'NM')
			str += ' <select name="modifier" id="modifier"><option value="LESS_THAN">&lt;</option><option value="LESS_EQUAL">&lt;=</option><option value="EQUAL">=</option><option value="GREATER_EQUAL">&gt;=</option><option value="GREATER_THAN">&gt;</option></select> ';
		else if (hl7Abbrev == 'ST' || hl7Abbrev == 'CWE') {
			str += ' is ';
			str += '<input type="hidden" name="modifier" value="EQUAL" /> ';
		}
		if (hl7Abbrev == 'NM' || hl7Abbrev == 'ST')
			str += '<input type="text" name="value" size="10"/>';
		else if (hl7Abbrev == 'CWE') {
			str += '<select name="value" id="replace_with_answer_options"><option value="">Loading...</option></select>';
			lookupAnswers = true;
		}
		str += ' within the last ';
		str += ' <input type="text" name="withinLastMonths" value="" size="2" />';
		str += ' months';
		str += ' <input type="submit" value="Search"/>';
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
		
	function showPossibleFilters(concept) {
		var div = document.getElementById('concept_filter_box');
		var str = '';
		str += '<ul>'
		var filter = obsFilterTemplate(concept);
		if (filter != null)
			str += '<li>' + filter + '</li>';
		var filter = classFilterTemplate(concept);
		if (filter != null)
			str += '<li>' + filter + '</li>';
		
		str += '</ul>';
		div.innerHTML = str;
		showLayer('concept_filter_box');
	}
	
	function handleLoadButton() {
		if ($('loadBox').style.display == 'none') {
			hideLayer('saveBox');
			DWRCohortBuilderService.getSearchHistories(function(histories) {
					var loadBox = $('loadBox');
					loadBox.innerHTML = '';
					if (histories.length == 0)
						loadBox.innerHTML = 'No saved histories';
					else {
						var str = '<ul>';
						for (var i = 0; i < histories.length; ++i) {
							str += '<li><a href="javascript:loadSearchHistory(' + histories[i].id + ')">' + histories[i].name + ' <small>(' + histories[i].description + ')</small></a></li>';
						}
						str += '</ul>';
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
	
	function linkSubmitHelper(idPrefix) {
		if (currentPatientSet != null) {
			document.getElementById(idPrefix + "_ptIds").value = currentPatientSet.commaSeparatedPatientIds;
			document.getElementById(idPrefix + "_form").submit();
		} else {
			window.alert("<spring:message code="PatientSet.stillLoading"/>");
		}
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
						str += ' (' + result.size + ' results)';
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
		currentPatientSet = ps;
		var ids = ps.patientIds;
		var n = ids.length;
		if (fromPatientIndex >= n)
			fromPatientIndex = n - patientPageSize;
		if (fromPatientIndex < 0)
			fromPatientIndex = 0;
		var toPatientIndex = fromPatientIndex + patientPageSize - 1;
		var patientIds = "";
		for (var i = fromPatientIndex; i <= toPatientIndex; ++i) {
			if (i == n) break;
			if (i > fromPatientIndex)
				patientIds += ",";
			patientIds += ids[i];
		}
		goesUntilLast = (toPatientIndex + 1) >= n;
		$('previewFromIndex').innerHTML = fromPatientIndex + 1;
		$('previewToIndex').innerHTML = toPatientIndex > n ? n : (toPatientIndex + 1);
		$('previewTotalNumber').innerHTML = n;
		hideLayer('cohort_builder_preview_patients');
		var str = '<i>' + n + ' results</i>';
		DWRPatientSetService.getPatients(patientIds, function(list) {
				var str = '';
				if (fromPatientIndex > 0)
					str += '&nbsp;&nbsp;...<br/>';
				for (var j = 0; j < list.length; ++j) {
					var pli = list[j];
					str += "&nbsp;&nbsp;" + (fromPatientIndex + j + 1) + ". ";
					str += '<a href="patientDashboard.form?patientId=' + pli.patientId + '">';
					str += pli.givenName + " " + pli.familyName + " (" + pli.age + " year old " + (pli.gender == "M" ? "Male" : "Female") + ")";
					str += "</a><br/>";
				}
				if (!goesUntilLast)
					str += '&nbsp;&nbsp;...<br/>';
				var div = document.getElementById('cohort_builder_preview_patients');
				$('cohort_builder_preview_patients').innerHTML = str;
				hideLayer('cohort_builder_preview_loading_message');
				showLayer('cohort_builder_preview_numbers');
				showLayer('cohort_builder_preview_patients');
			});
	}
</script>

<div id="cohort_builder_search_history" style="padding: 4px; border: 1px black solid">
	<h3>Search History</h3>
	<c:if test="${model.searchHistory.size == 0}">
		<div>No search history</div>
	</c:if>
	<c:forEach var="item" items="${model.searchHistory.items}" varStatus="iter">
		<table style="margin: 1px 4px; width: 100%; border: 1px black solid; background-color: #e0ffe0">
			<tr>
				<td width="25">
					<c:set var="temp" value="${iter.index}"/>
					<c:if test="${iter.count == model.searchHistory.size}"> <%-- TODO: is there a single method in varstatus for this? --%>
						<c:set var="temp" value="last"/>
					</c:if>
					<a href="#" onClick="DWRUtil.setValue('cohort_builder_preview_method', '${temp}'); refreshPreview();">
						${iter.count}.
					</a>
				</td>
				<td>
					${item.filter.name}
					<small>(${item.filter.description})</small>
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
					results
					<c:if test="${item.cachedResult != null}">
						<small>(cached)</small>
					</c:if>
					<a href="cohortBuilder.form?method=removeFilter&index=${iter.index}">[x]</a>
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

	<p/>
	<%-- <a href="javascript:toggleLayer('cohort_builder_add_filter')">Add</a> --%>
	<div id="cohort_builder_add_filter" style="border: 1px black solid; background-color: #e0e0ff;">
		Shortcuts/Wizards: specify these in openmrs-servlet.xml or as a global property?
		
		<br/>
		<c:if test="${fn:length(model.savedFilters) > 0}">
			<a href="javascript:toggleLayer('saved_filters')">Add saved filter:</a>
			<div id="saved_filters" style="display: none">
				<ul>
					<c:forEach var="savedFilter" items="${model.savedFilters}">
						<li><a href="cohortBuilder.form?method=addFilter&filter_id=${savedFilter.reportObjectId}">${savedFilter.name}</a></li>
					</c:forEach>
				</ul>
				<%--
				<form method="post" action="cohortBuilder.form">
					<input type="hidden" name="method" value="addFilter"/>
					<select name="filter_id">
						<c:forEach var="savedFilter" items="${model.savedFilters}">
							<option value="${savedFilter.reportObjectId}">${savedFilter.name}</option>
						</c:forEach>
					</select>
					<input type="submit" value="Add"/>
				</form>
				--%>
			</div>
		</c:if>

		<br/>
		<form method="post" action="cohortBuilder.form">
			<input type="hidden" name="method" value="addFilter"/>
			Add composition filter:
			<input type="text" name="composition" size="40"/>
			<input type="submit" value="Add"/>
			<br/>
			<i><small>
				&nbsp;&nbsp;&nbsp;&nbsp;
				e.g. &quot;(1 and 2) or not 3&quot;
				&nbsp;&nbsp;
				Temporary limitation: you can't put AND and OR in a phrase without parentheses, so &quot;1 and 2 or 3&quot; won't work.
			</small></i>
		</form>
		
		Add concept filter:
		<div dojoType="ConceptSearch" widgetId="concept_to_filter_search" conceptId="" showVerboseListing="true"></div>
		<div dojoType="OpenmrsPopup" widgetId="concept_to_filter_selection" hiddenInputName="concept_to_filter" searchWidget="concept_to_filter_search" searchTitle="Concept to filter on"></div>
		<div id="concept_filter_box" style="background-color: white; border: 1px black dashed; display: none"></div>
	</div>
</div>

<div id="cohort_builder_button_panel" style="padding: 1px 4px; margin: 4px 0px; background-color: #e0e0e0">
	<table width="100%">
		<tr>
			<td>
				Display Method:
				<select id="cohort_builder_preview_method" onChange="refreshPreview()">
					<option value="last">Last</option>
					<option value="and">All searches ANDed together</option>
					<option value="or">All searches ORed together</option>
					<c:set var="temp" value="${model.searchHistory.size - 2}"/>
					<c:if test="${temp < 0}">
						<c:set var="temp" value="0"/>
					</c:if>
					<c:forEach varStatus="index" items="${model.searchHistory.items}" end="${temp}">
						<option value="${index.index}">${index.count}</option>
					</c:forEach>
				</select>
			</td>
			<td>
				<c:if test="${model.searchHistory.size == 0}">
					<span style="color: gray">Save</span>
				</c:if>
				<c:if test="${model.searchHistory.size > 0}">
					<a href="javascript:toggleLayer('saveBox'); hideLayer('loadBox')">Save</a>
				</c:if>
				<div id="saveBox" style="position: absolute; z-index: 1; border: 1px black solid; background-color: #ffe0e0; display: none">
					<form method="post" action="cohortBuilder.form">
						<input type="hidden" name="method" value="saveHistory"/>
						<table>
							<tr>
								<td>Save As</td>
								<td><input type="text" name="name"/></td>
							</tr>
							<tr>
								<td>Description</td>
								<td><input type="text" name="description" size="60"/></td>
							</tr>
							<tr>
								<td>Private/Shared</td>
								<td>Not Yet Implemented</td>
							</tr>
							<tr>
								<td>Store Until</td>
								<td>
									<select>
										<option value="">Until I delete it</option>
										<option value="">4 weeks (Not Yet Implemented)</option>
										<option value="">2 weeks (Not Yet Implemented)</option>
										<option value="">1 week (Not Yet Implemented)</option>
									</select>
								</td>
							</tr>
						</table>
						<input type="submit" value="Save"/>
					</form>
				</div>
			</td>
			<td>
				<a href="javascript:handleLoadButton()">[Load]</a>
				<div id="loadBox" style="position: absolute; z-index: 1; border: 1px black solid; background-color: #ffe0e0; display: none"></div>
			</td>
			<td>
				<form method="post" action="cohortBuilder.form">
					<input type="hidden" name="method" value="clearHistory"/>
					<input type="submit" value="Clear History"/>
				</form>
			</td>
		</tr>
	</table>
</div>

<div id="cohort_builder_preview" style="border: 1px black solid; padding: 4px">
	<div id="cohort_builder_preview_numbers" style="display: none">
		<b><u>
		<a href="javascript:previewPageTo(0)">|&lt;-</a>
		&nbsp;&nbsp;&nbsp;&nbsp;
		<a href="javascript:previewPageBy(-patientPageSize)">&lt;-</a>
		&nbsp;&nbsp;&nbsp;&nbsp;
		Displaying <span id="previewFromIndex">#</span> to <span id="previewToIndex">#</span> of <span id="previewTotalNumber">#</span> patients.
		&nbsp;&nbsp;&nbsp;&nbsp;
		<a href="javascript:previewPageBy(patientPageSize)">-&gt;</a>
		&nbsp;&nbsp;&nbsp;&nbsp;
		<a href="javascript:previewPageTo(-1)">-&gt;|</a>
		</u></b>
	</div>
	<div id="cohort_builder_preview_no_filters" <c:if test="${model.searchHistory.size != 0}">style="display: none"</c:if> >
		<h4>No searches.</h4>
	</div>
	<div id="cohort_builder_preview_loading_message">
		Loading...
	</div>
	<div id="cohort_builder_preview_patients" style="display: none"></div>
</div>

<div id="cohort_builder_actions" style="border: 1px black solid">
	<c:if test="${fn:length(model.links) > 0}">
		(placeholder taken straight from on-the-fly-analysis:
		<span style="position: relative" onMouseOver="javascript:showLayer('_linkMenu')" onMouseOut="javascript:hideLayer('_linkMenu')">
			<a class="analysisShortcutBarButton"><spring:message code="Analysis.linkButton"/></a>
			<div id="_linkMenu" class="analysisShortcutMenu" style="display: none">
				<ul>
					<c:forEach var="item" items="${model.links}" varStatus="loopStatus">
						<li>
							<form method="post" action="${item.url}" id="link_${loopStatus.index}_form" style="display: inline" <c:if test="${model.linkTarget != null}">target="${model.linkTarget}"</c:if>>
								<input type="hidden" name="patientIds" id="link_${loopStatus.index}_ptIds" value=""/>
								<c:forEach var="arg" items="${item.arguments}">
									<input type="hidden" name="${arg.name}" value="${arg.value}"/>
								</c:forEach>
								<a href="javascript:linkSubmitHelper('link_${loopStatus.index}')">
									<spring:message code="${item.label}"/>
								</a>
							</form>
						</li>
					</c:forEach>
				</ul>
			</div>
		</span>
	</c:if>
</div>

<script type="text/javascript">
	refreshPreview();
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %> 