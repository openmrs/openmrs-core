<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Concept Reference Terms" otherwise="/login.htm" redirect="/admin/concepts/conceptReferenceTerms.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/dwr/interface/DWRConceptService.js"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables_jui.css"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js"/>
<openmrs:htmlInclude file="/scripts/jquery-ui/js/openmrsSearch.js" />

<script type="text/javascript">
	$j(document).ready(function() {
		new OpenmrsSearch("findConceptReferenceTerm", true, doConceptReferenceTermSearch, doSelectionHandler, 
				[	{fieldName:"code", header:omsgs.conceptReferenceTermCode},
				 	{fieldName:"name", header:omsgs.name},
					{fieldName:"conceptSourceName", header:omsgs.conceptSource}
				],
                {
                    searchLabel: '<openmrs:message code="ConceptReferenceTerm.search" javaScriptEscape="true"/>',
                    includeVoidedLabel:omsgs.includeRetired
                });
	});
	
	function doSelectionHandler(index, data) {
		document.location = "conceptReferenceTerm.form?conceptReferenceTermId=" + data.conceptReferenceTermId;
	}
	
	//searchHandler for the Search widget
	function doConceptReferenceTermSearch(text, resultHandler, getMatchCount, opts) {
		DWRConceptService.findCountAndConceptReferenceTerms(text, null, opts.start, opts.length, opts.includeVoided, getMatchCount, resultHandler);
	}
</script>

<h2><openmrs:message code="ConceptReferenceTerm.title"/></h2>

<a href="conceptReferenceTerm.form"><openmrs:message code="ConceptReferenceTerm.add"/></a>

<br/><br/>

<div>
	<b class="boxHeader"><openmrs:message code="ConceptReferenceTerm.find"/></b>
	<div class="box">
		<div class="searchWidgetContainer" id="findConceptReferenceTerm"></div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>