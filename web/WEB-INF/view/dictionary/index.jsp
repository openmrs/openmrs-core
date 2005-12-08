<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:require privilege="" otherwise="/login.htm" redirect="/dictionary/index.htm" />

<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRConceptService.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/util.js'></script>
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

</script>

<h2><spring:message code="dictionary.title" /></h2>

<div id="findPatient">
	<b class="boxHeader"><spring:message code="Concept.find"/></b>
	<div class="box">
		<form method="get" id="searchForm" onSubmit="return search(0); return null;">
			<spring:message code="dictionary.searchBox"/> <input type="text" id="searchText" size="45" onkeyup="search(400, event);">
			<spring:message code="dictionary.includeRetired"/> <input type="checkbox" id="includeRetired" value="true" onclick="search(0)">
		</form>
		<table class="conceptSearchTable">
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
<a href="concept.form">Add new Concept</a> (Use sparingly)

<script type="text/javascript">
	document.getElementById("searchText").focus();
		
	<request:existsParameter name="phrase">
		<!-- the user posted a search phrase, mimic user entering it into box -->
		var searchText = document.getElementById('searchText');
		searchText.value = "<request:parameter name="phrase"/>";
		search(0);
	</request:existsParameter>
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>