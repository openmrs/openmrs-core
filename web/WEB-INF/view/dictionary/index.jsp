<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:require privilege="" otherwise="/login.htm" redirect="/dictionary/index.htm" />

<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRConceptService.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/conceptSearch.js"></script>
<script type="text/javascript">

	var onSelect = function(conceptList) {
		location.href = "concept.form?conceptId=" + conceptList[0].conceptId;
	}

</script>

<h2><spring:message code="dictionary.title" /></h2>

<div id="findPatient">
	<b class="boxHeader"><spring:message code="Concept.find"/></b>
	<div class="box">
		<form method="get" onSubmit="return searchBoxChange('conceptSearchBody', null, searchText); return null;">
			<spring:message code="dictionary.searchBox"/> <input type="text" id="searchText" size="45" onkeyup="searchBoxChange('conceptSearchBody', event, this, 400);">
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
</div>


<%@ include file="/WEB-INF/template/footer.jsp" %>