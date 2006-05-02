<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Concepts" otherwise="/login.htm" redirect="/admin/concepts/conceptDrug.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script src='<%= request.getContextPath() %>/dwr/interface/DWRConceptService.js'></script>
<script src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script src='<%= request.getContextPath() %>/scripts/openmrsSearch.js'></script>
<script src='<%= request.getContextPath() %>/scripts/conceptSearch.js'></script>

<script>
	<request:existsParameter name="autoJump">
		autoJump = <request:parameter name="autoJump"/>;
	</request:existsParameter>
	var savedText = "";
	
	function onSelect(arr) {
		document.location = "conceptDrug.form?drugId=" + arr[0].drugId + "&phrase=" + savedText;
	}
	
	function findObjects(text) {
		savedText = text;
		if (debugBox) debugBox.innerHTML += '<br> Entering findObjects for: ' + text;
		//must have at least 2 characters entered or that character be a number
		if (text.length > 1 || (parseInt(text) >= 0 && parseInt(text) <= 9)) {
	    	DWRConceptService.findDrugs(fillTable, text, includeRetired);
		    if (debugBox) debugBox.innerHTML += '<br> DWRConceptService.findDrugs called';
		}
		else {
			var msg = new Array();
			msg.push("Invalid number of search characters");
			fillTable(msg);
		}
		searchListing.style.display = "";
	    return false;
	}
	function search(event, delay) {
		searchBoxChange("searchTableBody", searchBox, event, includeVoided.checked, delay);
		return false;
	}	
	function showSearch() {
		searchListing.style.display = "none";
		searchBox.focus();
	}
</script>

<h2><spring:message code="ConceptDrug.title"/></h2>

<a href="conceptDrug.form"><spring:message code="ConceptDrug.add"/></a><br/><br/>

<div id="findConceptDrug">
	<b class="boxHeader"><spring:message code="ConceptDrug.find"/></b>
	<div class="box">
		<form id="findConceptDrugForm" onSubmit="return search(event, 0);">
			<table>
				<tr>
					<td><spring:message code="ConceptDrug.search"/></td>
					<td><input type="text" id="searchBox" onKeyUp="search(event, 500)" size="40"></td>
					<td style="display: none"><spring:message code="formentry.includeVoided"/><input type="checkbox" id="includeVoided" onClick="search(event, 0); searchBox.focus();" /></td>
				</tr>
			</table>
		</form>
		<div id="conceptDrugListing">
			<table id="conceptDrugTable" cellspacing="0" cellpadding="1" width="100%">
			 <thead>
				 <tr>
				 	<th> </th>
				 	<th><spring:message code="general.name"/></th>
				 </tr>
			 </thead>
			 <tbody id="searchTableBody">
			 </tbody>
			</table>
		</div>
	</div>
</div>

<script>

	var includeVoided	= document.getElementById("includeVoided");
	var searchListing	= document.getElementById("conceptDrugListing");
	var searchBox		= document.getElementById("searchBox");
	
	showSearch();
	
	<request:existsParameter name="conceptDrugId">
		var encs = new Array();
		var encs[0] = new Object();
		encs[0].conceptDrugId = request.getAttribute("conceptDrugId");
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
