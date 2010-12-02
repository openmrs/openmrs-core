<%@ include file="/WEB-INF/template/include.jsp" %>

<spring:message var="pageTitle" code="dictionary.titlebar" scope="page"/>

<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:require privilege="View Concepts" otherwise="/login.htm"
	redirect="/dictionary/index.htm" />

<openmrs:htmlInclude file="/dwr/interface/DWRConceptService.js"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables_jui.css"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js"/>
<openmrs:htmlInclude file="/scripts/jquery-ui/js/openmrsSearch.js" />

<script type="text/javascript">
	var lastSearch;
	$j(document).ready(function() {
		new OpenmrsSearch("findConcept", true, doConceptSearch, doSelectionHandler, 
				[{fieldName:"name", header:" "}, {fieldName:"preferredName", header:" "}],
				{searchLabel: '<spring:message code="Concept.search" javaScriptEscape="true"/>:', 
					includeVoidedLabel: '<spring:message code="SearchResults.includeRetired" javaScriptEscape="true"/>', 
					columnRenderers: [nameColumnRenderer, null], 
					columnVisibility: [true, false]
				});
	});
	
	function doSelectionHandler(index, data) {
		document.location = "concept.htm?conceptId=" + data.conceptId;
	}
	
	//searchHandler
	function doConceptSearch(text, resultHandler, getMatchCount, opts) {
		DWRConceptService.findCountAndConcepts(text, opts.includeVoided, null, null, null, null, opts.start, opts.length, getMatchCount, resultHandler);
	}
	
	//custom render, appends an arrow and preferredName it exists
	function nameColumnRenderer(oObj){
		if(oObj.aData[1] && $j.trim(oObj.aData[1]) != '')
			return "<span>"+oObj.aData[0]+"<span/><span class='otherHit'> &rArr; "+oObj.aData[1]+"<span/>";
		
		return "<span>"+oObj.aData[0]+"<span/>";
	}
</script>

<h2><spring:message code="dictionary.title" /></h2>

<a href="<%= request.getContextPath() %>/downloadDictionary.csv"><spring:message code="dictionary.download.link"/></a> <spring:message code="dictionary.download.description"/><br />
<br />

<c:choose>
	<c:when test="${conceptsLocked != 'true'}"> 
		<a href="concept.form"><spring:message code="Concept.add"/></a>
	</c:when>
	<c:otherwise>
		(<spring:message code="Concept.concepts.locked" />)
	</c:otherwise>
</c:choose>
<br /><br />

<div>
	<b class="boxHeader"><spring:message code="Concept.find"/></b>
	<div class="searchWidgetContainer">
		<div id="findConcept" <request:existsParameter name="autoJump">allowAutoJump='true'</request:existsParameter> ></div>
	</div>
</div>

<br/>

<openmrs:globalProperty key="concepts.locked" var="conceptsLocked"/>

<openmrs:extensionPoint pointId="org.openmrs.dictionary.index" type="html" />

<%@ include file="/WEB-INF/template/footer.jsp" %>