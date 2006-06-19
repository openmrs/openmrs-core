<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Encounters" otherwise="/login.htm" redirect="/admin/encounters/index.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script src='<%= request.getContextPath() %>/scripts/validation.js'></script>

<script src='<%= request.getContextPath() %>/dwr/interface/DWREncounterService.js'></script>
<script src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script src='<%= request.getContextPath() %>/scripts/openmrsSearch.js'></script>
<script src='<%= request.getContextPath() %>/scripts/encounterSearch.js'></script>

<script>
	<request:existsParameter name="autoJump">
		autoJump = <request:parameter name="autoJump"/>;
	</request:existsParameter>
	
	function onSelect(arr) {
		document.location = "encounter.form?encounterId=" + arr[0].encounterId + "&phrase=" + savedText;
	}
</script>

<h2><spring:message code="Encounter.title"/></h2>

<a href="encounter.form"><spring:message code="Encounter.add"/></a><br/><br/>

<div id="findEncounter">
	<b class="boxHeader"><spring:message code="Encounter.find"/></b>
	<div class="box">
		<form id="findEncounterForm" onSubmit="return search(event, 0);">
			<table>
				<tr>
					<td><spring:message code="Encounter.search"/></td>
					<td><input type="text" id="searchBox" onKeyUp="search(event, 500)"></td>
					<td><spring:message code="formentry.includeVoided"/><input type="checkbox" id="includeVoided" onClick="search(event, 0); searchBox.focus();" /></td>
				</tr>
			</table>
		</form>
		<div id="encounterListing">
			<table id="encounterTable" cellspacing="0" cellpadding="1" width="100%">
			 <thead>
				 <tr>
				 	<th> </th>
				 	<th><spring:message code="Patient.name"/></th>
				 	<th><spring:message code="Encounter.type"/></th>
				 	<th><spring:message code="Encounter.form"/></th>
				 	<th><spring:message code="Encounter.provider"/></th>
				 	<th><spring:message code="Encounter.location"/></th>
				 	<th><spring:message code="Encounter.datetime"/></th>
				 </tr>
			 </thead>
			 <tbody id="searchTableBody">
			 </tbody>
			</table>
		</div>
	</div>
</div>

<script>

	var encounterListing= document.getElementById("encounterListing");
	var searchBox		= document.getElementById("searchBox");
	var includeVoided	= document.getElementById("includeVoided");
	
	showSearch();
	
	<request:existsParameter name="encounterId">
		var encs = new Array();
		var encs[0] = new Object();
		encs[0].encounterId = request.getAttribute("encounterId");
		onSelect(encs);
	</request:existsParameter>
	
	<request:existsParameter name="phrase">
		searchBox.value = '<request:parameter name="phrase" />';
	</request:existsParameter>
	
	// creates back button functionality
	if (searchBox.value != "")
		search(null, 0);
	
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>
