<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:message var="pageTitle" code="dictionary.titlebar" scope="page"/>
<openmrs:message var="pageTitle" code="dictionary.title" scope="page"/>
<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:require privilege="View Concepts" otherwise="/login.htm"
	redirect="/dictionary/index.htm" />

<openmrs:htmlInclude file="/dwr/interface/DWRConceptService.js"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables_jui.css"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js"/>
<openmrs:htmlInclude file="/scripts/jquery-ui/js/openmrsSearch.js" />

<script type="text/javascript">
	var lastSearch;
	
	// Get relevant parameter value, in current URL
	function getURLParameter(name) {
		return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.search)||[,""])[1].replace(/\+/g, '%20')) || null;
	}
	
	$j(document).ready(function() {
		new OpenmrsSearch("findConcept", true, doConceptSearch, doSelectionHandler, 
				[{fieldName:"name", header:" "}, {fieldName:"preferredName", header:" "}],
				{searchLabel: '<openmrs:message code="Concept.search" javaScriptEscape="true"/>',
                    searchPlaceholder:'<openmrs:message code="Concept.search.placeholder" javaScriptEscape="true"/>',
					includeVoidedLabel: '<openmrs:message code="SearchResults.includeRetired" javaScriptEscape="true"/>', 
					columnRenderers: [nameColumnRenderer, null], 
					columnVisibility: [true, false],
					searchPhrase:'<request:parameter name="phrase"/>',
					showIncludeVerbose: true,
					verboseHandler: doGetVerbose,
					showSearchButton: true,
					lastSearchParams : getURLParameter('lastSearchText')
						? {
							'lastSearchText' : getURLParameter('lastSearchText'),
							'includeVoided' : getURLParameter('includeVoided'),
							'includeVerbose' : getURLParameter('includeVerbose')
						}
						: null
				});
	});
	
	function doSelectionHandler(index, data) {
		
		// Check the browser compatibility and, add an entry to history
		if (window.history.pushState) {
			window.history.pushState(
				{}, "",
				document.location.pathname + "?lastSearchText=" + document.getElementById('inputNode').value
				+ "&includeVoided=" + ($j('#includeVoided').is(':checked') ? 1 : 0)
				+ "&includeVerbose=" + ($j('#includeVerbose').is(':checked') ? 1 : 0)
			);
		}
		
		document.location = "concept.htm?conceptId=" + data.conceptId;
	}
	
	//searchHandler
	function doConceptSearch(text, resultHandler, getMatchCount, opts) {
		DWRConceptService.findCountAndConcepts(text, opts.includeVoided, null, null, null, null, opts.start, opts.length, getMatchCount, resultHandler);
	}
	
	//custom render, appends an arrow and preferredName it exists
	function nameColumnRenderer(oObj){
		if(oObj.aData[1] && $j.trim(oObj.aData[1]) != '')
			return "<span>"+oObj.aData[0]+"</span><span class='otherHit'> &rArr; "+oObj.aData[1]+"</span>";
		
		return "<span>"+oObj.aData[0]+"</span>";
	}
	
	//generates and returns the verbose text
	function doGetVerbose(index, data){
		if(!data)
			return "";
		return "#"+data.conceptId+": "+data.description;
	}
</script>

<h2><openmrs:message code="dictionary.title" /></h2>

<a href="<%= request.getContextPath() %>/downloadDictionary.csv"><openmrs:message code="dictionary.download.link"/></a> <openmrs:message code="dictionary.download.description"/><br />
<br />

<openmrs:hasPrivilege privilege="Manage Concepts">
	<c:choose>
		<c:when test="${conceptsLocked != 'true'}"> 
			<a href="concept.form"><openmrs:message code="Concept.add"/></a>
		</c:when>
		<c:otherwise>
			(<openmrs:message code="Concept.concepts.locked" />)
		</c:otherwise>
	</c:choose>
	<br /><br />
</openmrs:hasPrivilege>

<div>
	<b class="boxHeader"><openmrs:message code="Concept.find"/></b>
	<div class="box">
		<div class="searchWidgetContainer" id="findConcept"></div>
	</div>
</div>

<br/>

<openmrs:globalProperty key="concepts.locked" var="conceptsLocked"/>

<openmrs:extensionPoint pointId="org.openmrs.dictionary.index" type="html" />

<%@ include file="/WEB-INF/template/footer.jsp" %>