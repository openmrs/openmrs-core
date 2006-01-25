<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Observations" otherwise="/login.htm" redirect="/admin/observations/index.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script src='<%= request.getContextPath() %>/dwr/interface/DWRObsService.js'></script>
<script src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script src='<%= request.getContextPath() %>/scripts/openmrsSearch.js'></script>

<script>
	var savedText = "";
	function showSearch() {
		obsListing.style.display = "none";
		searchBox.focus();
	}
	
	function onSelect(arr) {
		document.location = "obs.form?obsId=" + arr[0].obsId + "&phrase=" + savedText;
	}
	
	function findObjects(text) {
		savedText = text;
		DWRObsService.findObs(fillTable, text, $('includeVoided').checked);
		obsListing.style.display = "";
		return false;
	}
	
	function getEncounter(ob) {
		if (typeof ob == 'string') return ob;
		return ob.encounter;
	}
	
	var getPatient = function(ob) {
		if (typeof ob == 'string') return '';
		return ob.patientName;
	}
	
	var getConcept = function(ob) {
		if (typeof ob != 'string')
			return ob.conceptName;
	}
	
	var getOrder = function(ob) {
		if (typeof ob != 'string')
		var str = '';
		if (ob.order != null) 
			str = ob.order;
		return str;
	}
	
	var getLocation = function(ob) {
		if (typeof ob != 'string')
			return ob.location;
	}
	
	var getDateTime = function(ob) {
		if (typeof p == 'string') return "";
		return getDateString(ob.dateTime);
	}
	
	var customCellFunctions = [getNumber, getEncounter, getPatient, getConcept, getOrder, getLocation, getDateTime];
	
	function search(obj, event, retired, delay) {
		searchBoxChange("obsTableBody", obj, event, retired, delay);
		return false;
	}
	
</script>

<h2><spring:message code="Obs.title"/></h2>

<a href="obs.form"><spring:message code="Obs.add"/></a><br/><br/>

<div id="findObs">
	<b class="boxHeader"><spring:message code="Obs.find"/></b>
	<div class="box">
		<form id="findObsForm" onSubmit="return search(searchBox, event, includeVoided.checked, 0);">
			<table>
				<tr>
					<td><spring:message code="Obs.search"/></td>
					<td><input type="text" id="searchBox" onKeyUp="search(this, event, includeVoided.checked, 400)"></td>
					<td><spring:message code="formentry.includeVoided"/><input type="checkbox" id="includeVoided" onClick="search(searchBox, event, includeVoided.checked, 0); searchBox.focus();" /></td>
				</tr>
			</table>
		</form>
		<div id="obsListing">
			<table id="obsTable" cellspacing="0" cellpadding="1" width="100%">
			 <thead>
				 <tr>
				 	<th> </th>
				 	<th><spring:message code="Obs.encounter"/></th>
				 	<th><spring:message code="Patient.name"/></th>
				 	<th><spring:message code="Obs.concept"/></th>
				 	<th><spring:message code="Obs.order"/></th>
				 	<th><spring:message code="Obs.location"/></th>
				 	<th><spring:message code="Obs.datetime"/></th>
				 </tr>
			 </thead>
			 <tbody id="obsTableBody">
			 </tbody>
			</table>
		</div>
	</div>
</div>

<div id="obsSummary">
</div>


<script>

	var obsListing	= document.getElementById("obsListing");
	var searchBox		= document.getElementById("searchBox");
	
	showSearch();
	
	<request:existsParameter name="obsId">
		var encs = new Array();
		var encs[0] = new Object();
		encs[0].obsId = request.getAttribute("obsId");
		onSelect(encs);
	</request:existsParameter>
	
	<request:existsParameter name="phrase">
		searchBox.value = '<request:parameter name="phrase" />';
	</request:existsParameter>
	
	// creates back button functionality
	if (searchBox.value != "")
		searchBoxChange("obsTableBody", searchBox, null, 0, 0);
	
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>
