<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/formentry/index.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<div id="xdebugBox" style="float: right; width: 350px;"></div>

<script src='<%= request.getContextPath() %>/dwr/interface/DWRPatientService.js'></script>
<script src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script src='<%= request.getContextPath() %>/scripts/openmrsSearch.js'></script>
<script src='<%= request.getContextPath() %>/scripts/patientSearch.js'></script>
<script src='<%= request.getContextPath() %>/scripts/validation.js'></script>

<script>

	var patient;
	var autoJump = true;
	<request:existsParameter name="autoJump">
		autoJump = <request:parameter name="autoJump"/>;
	</request:existsParameter>
	
	function showSearch() {
		findPatient.style.display = "";
		patientListing.style.display = "none";
		savedText = "";
		searchBox.focus();
	}
	
	function onSelect(arr) {
		if (arr[0].patientId != null) {
			document.location = "patientSummary.form?patientId=" + arr[0].patientId + "&phrase=" + savedText;
		}
		else if (arr[0].href != null) {
			document.location = arr[0].href;
		}
	}
	
	function findObjects(text) {
		if (text.length > 2) {
			savedText = text;
			DWRPatientService.findPatients(preFillTable, text, includeRetired);
		}
		else {
			var msg = new Array();
			msg.push("Invalid number of search characters");
			fillTable(msg, [getNumber, getString]);
		}
		patientListing.style.display = "";
		return false;
	}
	
	function allowAutoJump() {
		if (autoJump == false) {
			autoJump = true;
			return false;
		}
		//	only allow the first item to be automatically selected if:
		//		the entered text is a string or the entered text is a valid identifier
		return (savedText.match(/\d/) == false || isValidCheckDigit(savedText));	
	}
	
</script>

<style>
	#findPatientForm {
		padding: 0px;
		margin: 0px;
	}
	.searchIndex, .searchIndexHighlight {
		vertical-align: middle;
	}
	tr th#patientGender, tr th#patientAge, .patientGender, .patientAge {
		text-align: center;
	}
</style>

<h3><spring:message code="formentry.title"/></h3>

<div id="findPatient">
	<b class="boxHeader"><spring:message code="formentry.step1"/></b>
	<div class="box">
		<form id="findPatientForm" onSubmit="return search(searchBox, event, false, 0);">
			<table>
				<tr>
					<td><spring:message code="formentry.searchBox"/></td>
					<td><input type="text" id="searchBox" size="40" onKeyUp="search(this, event, false, 400)"></td>
					<td><label for="verbose"><spring:message code="Patient.verboseListing"/></label><input type="checkbox" id="verbose" onClick="toggleVerbose(this, 'div', 'description')"></td>
				</tr>
			</table>
			<!-- <input type="submit" value="Search" onClick="return updatePatients();"> -->
		</form>
		<div id="patientListing">
			<table id="patientTable" cellpadding="1" cellspacing="0">
			 <thead id="patientTableHead">
				 <tr>
				 	<th class="searchIndex"> </th>
				 	<th class="patientIdentifier"> <spring:message code="Patient.identifier"/> </th>
				 	<th> <spring:message code="PatientName.givenName"/> </th>
				 	<th> <spring:message code="PatientName.middleName"/> </th>
				 	<th> <spring:message code="PatientName.familyName"/> </th>
				 	<th id='patientAge'> <spring:message code="Patient.age"/> </th>
				 	<th id='patientGender'> <spring:message code="Patient.gender"/> </th>
				 	<th> <spring:message code="Patient.tribe"/> </th>
				 	<th> </th>
				 	<th> <spring:message code="Patient.birthdate"/> </th>
				 </tr>
			 </thead>
			 <tbody id="patientTableBody">
			 </tbody>
			</table>
		</div>
	</div>
</div>

<script>
	
	var patientListing= document.getElementById("patientListing");
	var findPatient   = document.getElementById("findPatient");
	var searchBox		= document.getElementById("searchBox");
	var findPatientForm = document.getElementById("findPatientForm");
	
	function init() {
		DWRUtil.useLoadingMessage();
	
		<request:existsParameter name="patientId">
			<!-- User has 'patientId' in the request params -- selecting that patient -->
			var pats = new Array();
			pats.push(new Object());
			pats[0].patientId = '<request:parameter name="patientId"/>';
			onSelect(pats);
		</request:existsParameter>
		
		<request:existsParameter name="phrase">
			<!-- User has 'phrase' in the request params -- searching on that -->
			searchBox.value = '<request:parameter name="phrase"/>';
		</request:existsParameter>
	
		showSearch();

		// creates back button functionality
		if (searchBox.value != "") {
			search(searchBox, null, false, 0);
			exitNumberMode(searchBox);
		}
			
		changeClassProperty("description", "display", "none");
	}
		
	window.onload=init;
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>