<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Providers" otherwise="/login.htm" redirect="/admin/provider/index.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/dwr/interface/DWRProviderService.js"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables_jui.css"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js"/>
<openmrs:htmlInclude file="/scripts/jquery-ui/js/openmrsSearch.js" />

<script type="text/javascript">
	var lastSearch;
	
	$j(document).ready(
	function() {
		new OpenmrsSearch("findProvider", true, doProviderSearch, doSelectionHandler, 
				[	{fieldName:"displayName", header:omsgs.providerName},
					{fieldName:"identifier", header:omsgs.providerIdentifier}
				],
                {
                    searchLabel: '<openmrs:message code="Provider.search" javaScriptEscape="true"/> ',
                    searchPlaceholder:'<openmrs:message code="Provider.search.placeholder" javaScriptEscape="true"/>',
                    doSearchWhenEmpty: true,
                    includeVoidedLabel: '<openmrs:message code="SearchResults.includeRetired" javaScriptEscape="true"/>'
                });
	});
	
	
	function doSelectionHandler(index, data) {
		document.location = "provider.form?providerId=" + data.providerId;
	}
	
	//searchHandler for the Search widget
	function doProviderSearch(text, resultHandler, getMatchCount, opts) {
		lastSearch = text;
		DWRProviderService.findProviderCountAndProvider(text,opts.includeVoided,opts.start, opts.length,resultHandler);
	}


	
</script>

<h2><openmrs:message code="Provider.title"/></h2>

<openmrs:hasPrivilege privilege="Manage Provider">
	<a href="provider.form"><openmrs:message code="Provider.add"/></a>
</openmrs:hasPrivilege>



<br/><br/>

<div>
	<b class="boxHeader"><openmrs:message code="Provider.find"/></b>
	<div class="box">
		<div class="searchWidgetContainer" id="findProvider"> </div>
	</div>
</div>



<%@ include file="/WEB-INF/template/footer.jsp" %>