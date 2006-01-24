<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Encounters" otherwise="/login.htm" redirect="/admin/encounters/index.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script src='<%= request.getContextPath() %>/scripts/validation.js'></script>

<script src='<%= request.getContextPath() %>/dwr/interface/DWREncounterService.js'></script>
<script src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script src='<%= request.getContextPath() %>/scripts/openmrsSearch.js'></script>

<script>
	var savedText = "";
	function showSearch() {
		encounterListing.style.display = "none";
		searchBox.focus();
	}
	
	function onSelect(arr) {
		document.location = "encounter.form?encounterId=" + arr[0].encounterId + "&phrase=" + savedText;
	}
	
	function findObjects(text) {
		savedText = text;
		DWREncounterService.findEncounters(fillTable, text, $('includeVoided').checked);
		encounterListing.style.display = "";
		return false;
	}
	
	var getPatient = function(enc) {
		if (typeof enc == 'string') return enc;
		return enc.patientName;
	}
	
	var getType = function(enc) {
		if (typeof enc != 'string')
			return enc.encounterType;
	}
	
	var getProvider = function(enc) {
		if (typeof enc != 'string')
			return enc.providerName;
	}
	
	var getLocation = function(enc) {
		if (typeof enc != 'string')
			return enc.location;
	}
	
	var getDateTime = function(enc) {
		if (typeof p == 'string') return "";
		return getDateString(enc.encounterDateTime);
	}
	
	var customCellFunctions = [getNumber, getPatient, getType, getProvider, getLocation, getDateTime];
	
	function search(obj, event, retired, delay) {
		searchBoxChange("encounterTableBody", obj, event, retired, delay);
		return false;
	}
	
</script>

<h2><spring:message code="Encounter.title"/></h2>

<a href="encounter.form"><spring:message code="Encounter.add"/></a><br/><br/>

<div id="findEncounter">
	<b class="boxHeader"><spring:message code="Encounter.find"/></b>
	<div class="box">
		<form id="findEncounterForm" onSubmit="return search(searchBox, event, includeVoided.checked, 0);">
			<table>
				<tr>
					<td><spring:message code="Encounter.search"/></td>
					<td><input type="text" id="searchBox" onKeyUp="search(this, event, includeVoided.checked, 400)"></td>
					<td style="display: none"><spring:message code="formentry.includeVoided"/><input type="checkbox" id="includeVoided" onClick="search(searchBox, event, includeVoided.checked, 0); searchBox.focus();" /></td>
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
				 	<th><spring:message code="Encounter.provider"/></th>
				 	<th><spring:message code="Encounter.location"/></th>
				 	<th><spring:message code="Encounter.datetime"/></th>
				 </tr>
			 </thead>
			 <tbody id="encounterTableBody">
			 </tbody>
			</table>
		</div>
	</div>
</div>

<div id="encounterSummary">
</div>


<script>

	var encounterListing	= document.getElementById("encounterListing");
	var searchBox		= document.getElementById("searchBox");
	
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
		searchBoxChange("encounterTableBody", searchBox, null, 0, 0);
	
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>
