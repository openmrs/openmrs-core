<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Encounters" otherwise="/login.htm" redirect="/admin/encounters/index.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/dwr/interface/DWREncounterService.js"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables_jui.css"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js"/>
<openmrs:htmlInclude file="/scripts/jquery-ui/js/openmrsSearch.js" />

<script type="text/javascript">
	var lastSearch;
	$j(document).ready(function() {
		new OpenmrsSearch("findEncounter", true, doEncounterSearch, doSelectionHandler, 
				[	{fieldName:"personName", header:omsgs.patientName},
					{fieldName:"encounterType", header:omsgs.encounterType},
					{fieldName:"formName", header:omsgs.encounterForm},
					{fieldName:"providerName", header:omsgs.encounterProvider},
					{fieldName:"location", header:omsgs.encounterLocation},
					{fieldName:"encounterDateString", header:omsgs.encounterDate}
				],
				{searchLabel: '<spring:message code="Encounter.search" javaScriptEscape="true"/>'});
	});
	
	function doSelectionHandler(index, data) {
		document.location = "encounter.form?encounterId=" + data.encounterId + "&phrase=" + lastSearch;
	}
	
	//searchHandler for the Search widget
	function doEncounterSearch(text, resultHandler, opts) {
		lastSearch = text;
		DWREncounterService.findCountAndEncounters(text, opts.includeVoided, opts.start, opts.length, resultHandler);
	}
</script>
<style>
.widgetSearchBox{
	width: 99%;
	padding: 5px 2px 5px 2px;
	text-align: left;
	margin-left: auto;
	margin-right: auto;
	font-size: .9em;
}
</style>

<h2><spring:message code="Encounter.title"/></h2>

<a href="encounter.form"><spring:message code="Encounter.add"/></a>

<openmrs:extensionPoint pointId="org.openmrs.admin.encounters.index.afterAdd" type="html" />

<br/><br/>

<div>
	<b class="boxHeader"><spring:message code="Encounter.find"/></b>
	<div class="widgetSearchBox">
		<div id="findEncounter" <request:existsParameter name="autoJump">allowAutoJump='true'</request:existsParameter> ></div>
	</div>
</div>

<openmrs:extensionPoint pointId="org.openmrs.admin.encounters.index.footer" type="html" />

<%@ include file="/WEB-INF/template/footer.jsp" %>