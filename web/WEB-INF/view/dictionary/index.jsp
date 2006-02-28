<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:require privilege="View Concepts" otherwise="/login.htm"
	redirect="/dictionary/index.htm" />


<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRConceptService.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/openmrsSearch.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/conceptSearch.js"></script>
<script type="text/javascript">

	var onSelect = function(conceptList) {
		location.href = "concept.htm?conceptId=" + conceptList[0].conceptId;
	}
	
	function search(delay, event) {
		var searchBox = document.getElementById("searchText");
		var retired = document.getElementById("includeRetired").checked;
		return searchBoxChange('conceptSearchBody', searchBox, event, retired, delay);
	}
	
	function init() {
		document.getElementById("searchText").focus();
		DWRUtil.useLoadingMessage();
		<request:existsParameter name="phrase">
			<!-- the user posted a search phrase, mimic user entering it into box -->
			var searchText = document.getElementById('searchText');
			searchText.value = "<request:parameter name="phrase"/>";
			search(0);
		</request:existsParameter>
	}
	
	window.onload = init;
	
</script>

<style>
	#searchForm {
		padding: 0px;
		margin: 0px;
	}
</style>

<h2><spring:message code="dictionary.title" /></h2>

<a href="<%= request.getContextPath() %>/downloadDictionary.csv"><spring:message code="dictionary.download.link"/></a> <spring:message code="dictionary.download.description"/><br />
<br />

<div id="findPatient">
	<b class="boxHeader"><spring:message code="Concept.find"/></b>
	<div class="box">

		<form method="get" id="searchForm" onSubmit="return search(0); return null;">
			<div style="float: right">
				<input type="checkbox" id="includeRetired" value="true" onclick="search(0, event); searchText.focus();"><label for="includeRetired"><spring:message code="dictionary.includeRetired"/></label>
				<input type="checkbox" id="verboseListing" value="true" onclick="search(0, event); searchText.focus();"><label for="verboseListing"><spring:message code="dictionary.verboseListing"/></label>
			</div>
			<spring:message code="dictionary.searchBox"/> <input type="text" id="searchText" size="45" onkeyup="search(400, event);">
		</form>
		<table class="conceptSearchTable" cellspacing="0" cellpadding="1">
			<tbody id="conceptSearchBody">
				<tr>
					<td></td>
					<td></td>
				</tr>
			</tbody>
		</table>
	</div>
</div>

<br/>
<a href="concept.form"><spring:message code="Concept.add"/></a> (Use sparingly)

<%@ include file="/WEB-INF/template/footer.jsp" %>