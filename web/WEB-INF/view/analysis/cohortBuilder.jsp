<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patient Sets" otherwise="/login.htm" redirect="/cohortBuilder.list" />

<c:set var="OPENMRS_DO_NOT_SHOW_PATIENT_SET" scope="request" value="true"/>

<%@ include file="/WEB-INF/template/header.jsp" %>

<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRCohortBuilderService.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRPatientService.js'></script>
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
	
	function showPossibleFilters(concept) {
		var div = document.getElementById('concept_filter_box');
		if (concept.hl7Abbreviation == 'NM') {
			var str = '<form method="post" action="cohortBuilder.form">';
			str += '<input type="hidden" name="method" value="addDynamicFilter"/>';
			str += '<input type="hidden" name="filterClass" value="org.openmrs.reporting.NumericObsPatientFilter" />';
			str += '<input type="hidden" name="vars" value="timeModifier#org.openmrs.api.PatientSetService$TimeModifier,concept#org.openmrs.Concept,modifier#org.openmrs.api.PatientSetService$Modifier,value#java.lang.Double,withinLastMonths#java.lang.Integer"/>';
			str += '<select name="timeModifier"><option value="ANY">ANY</option><option value="NO">NO</option><option value="FIRST">FIRST</option><option value="LAST">LAST</option><option value="MIN">MIN</option><option value="MAX">MAX</option><option value="AVG">AVG</option></select> ';
			str += '<input type="hidden" name="concept" value="' + concept.conceptId + '"/>';
			str += concept.name;
			str += ' <select name="modifier" id="modifier"><option value="LESS_THAN">&lt;</option><option value="LESS_EQUAL">&lt;=</option><option value="EQUAL">=</option><option value="GREATER_EQUAL">&gt;=</option><option value="GREATER_THAN">&gt;</option></select> ';
			str += '<input type="text" name="value" size="10"/>';
			str += ' within the last ';
			str += ' <input type="text" name="withinLastMonths" value="" size="2" />';
			str += ' months';
			str += '<input type="submit" value="Add"/>';
			str += '</form>';
			div.innerHTML = str;
		} else {
			div.innerHTML = 'At present only numeric concepts are supported. (Not: ' + concept.hl7Abbreviation + ')';
		}
		showLayer('concept_filter_box');
	}
</script>


<script type="text/javascript">
	function refreshHistory() {
		var div = document.getElementById('cohort_builder_preview');
		var method = DWRUtil.getValue('cohort_builder_preview_method');
		div.innerHTML = 'Loading... (' + method + ')';
		if (method == 'last')
			DWRCohortBuilderService.getLastResult(displayHistory);
		else if (method == 'and')
			DWRCohortBuilderService.getResultCombineWithAnd(displayHistory);
		else if (method == 'or')
			DWRCohortBuilderService.getResultCombineWithOr(displayHistory);
		else // should be an integer: a zero-based index into the search history
			DWRCohortBuilderService.getResultForSearch(method, displayHistory);
	}

	function displayHistory(ps) {
		var div = document.getElementById('cohort_builder_preview');
		var str = '<b>' + ps.size + ' results</b><br/>';
		var ids = ps.patientIds;
		for (var i = 0; i < ids.length; ++i) {
			if (i < 0) { //TODO: why doesn't this work?
				var pli = DWRPatientService.getPatient(ids[i] + 1 - 1);
				str += pli.givenName + ' ' + pli.familyName + '<br/>';
			} else
				str += 'ptId ' + ids[i] + '<br/>';
		}
		div.innerHTML = str;
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
					<c:if test="${iter.count == model.searchHistory.size}"> <%-- TODO: is there a single method is varstatus for this? --%>
						<c:set var="temp" value="last"/>
					</c:if>
					<a href="#" onClick="DWRUtil.setValue('cohort_builder_preview_method', '${temp}'); refreshHistory();">
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
					<%--
						I'm commenting this out because deleting a filter will screw up the display name of any composition filter (e.g. "3 and 4") under it.
						<a href="cohortBuilder.form?method=removeFilter&index=${iter.index}">[x]</a>
					--%>
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

	<a href="javascript:toggleLayer('cohort_builder_add_filter')">Add</a>
	<div id="cohort_builder_add_filter" style="border: 1px black solid; background-color: #e0e0ff;">
		<form method="post" action="cohortBuilder.form">
			<input type="hidden" name="method" value="addFilter"/>
			Add composition filter <i>(e.g. &quot;1 and 2&quot;)</i>:
			<input type="text" name="composition"/>
			<input type="submit" value="Add"/>
		</form>
		
		Add concept filter:
		<div dojoType="ConceptSearch" widgetId="concept_to_filter_search" conceptId="" showVerboseListing="true"></div>
		<div dojoType="OpenmrsPopup" widgetId="concept_to_filter_selection" hiddenInputName="concept_to_filter" searchWidget="concept_to_filter_search" searchTitle="Concept to filter on"></div>
		<div id="concept_filter_box" style="background-color: white; border: 1px black dashed; display: none"></div>
		
		Add saved filter:
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
</div>

<div id="cohort_builder_button_panel" style="border: 1px black solid">
	<table width="100%">
		<tr>
			<td>
				Display Method:
				<select id="cohort_builder_preview_method" onChange="refreshHistory()">
					<option value="last">Last</option>
					<option value="and">All searches ANDed together</option>
					<option value="or">All searches ORed together</option>
					<c:set var="temp" value="${model.searchHistory.size - 2}"/>
					<c:if test="${temp < 0}">
						<c:set var="temp" value="0"/>
					</c:if>
					<c:forEach varStatus="index" items="${model.searchHistory.items}" end="">
						<option value="${index.index}">${index.count}</option>
					</c:forEach>
				</select>
			</td>
			<td>
				<c:if test="${model.searchHistory.size == 0}">
					<span style="color: gray">Save</span>
				</c:if>
				<c:if test="${model.searchHistory.size > 0}">
					<a href="javascript:toggleLayer('saveBox')">Save</a>
					<div id="saveBox" style="border: 1px black solid; background-color: #ffe0e0; display: none">
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
				</c:if>
			</td>
			<td>
				[Load]
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

<div id="cohort_builder_preview" style="border: 1px black solid; background-color: #e0e0e0">
	Original:
	<c:set var="thisPatientSet" value="${model.searchHistory.lastPatientSet.patientIds}" />
	<h3>Results (${fn:length(thisPatientSet)})</h3>
	<c:forEach var="ptId" items="${thisPatientSet}" end="10">
		${ptId} <br/>
	</c:forEach>
	<c:if test="${fn:length(thisPatientSet) > 10}">
		...
	</c:if>
</div>

<script type="text/javascript">
	refreshHistory();
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %> 