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

</script>

<h2><spring:message code="dictionary.title" /></h2>

<div id="findPatient">
	<b class="boxHeader"><spring:message code="Concept.find"/></b>
	<div class="box">
		<form method="get" id="searchForm" onSubmit="return searchBoxChange('conceptSearchBody', searchText, null, includeRetired.checked); return null;">
			<spring:message code="dictionary.searchBox"/> <input type="text" id="searchText" size="45" onkeyup="searchBoxChange('conceptSearchBody', this, event, includeRetired.checked, 400);">
			<spring:message code="dictionary.includeRetired"/> <input type="checkbox" id="includeRetired" value="true">
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

<script type="text/javascript">
	document.getElementById("searchText").focus();
		
	<request:existsParameter name="phrase">
		<!-- the user posted a search phrase, mimic user entering it into box -->
		var searchText = document.getElementById('searchText');
		searchText.value = "<request:parameter name="phrase"/>";
		searchBoxChange('conceptSearchBody', searchText);
	</request:existsParameter>
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>