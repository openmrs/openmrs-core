<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/formentry/index.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<script src='<%= request.getContextPath() %>/dwr/interface/DWRPatientService.js'></script>
<script src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script src='<%= request.getContextPath() %>/scripts/openmrsSearch.js'></script>
<script src='<%= request.getContextPath() %>/scripts/patientSearch.js'></script>
<script src='<%= request.getContextPath() %>/scripts/validation.js'></script>

<script>

	var patient;
	var savedText;
	
	function showSearch() {
		findPatient.style.display = "";
		patientListing.style.display = "none";
		selectForm.style.display = "none";
		patientSummary.style.display = "none";
		savedText = "";
		searchBox.focus();
	}
	
	function onSelect(arr) {
		if (arr[0].patientId != null) {
			DWRPatientService.getPatient(fillPatientDetails, arr[0].patientId);
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
	
	function preFillTable(patients) {
		var links = new Array();
		patientTableHead.style.display = "";
		if (patients.length < 1) {
			if (savedText.match(/\d/)) {
				if (isValidCheckDigit(savedText) == false) {
					//the user didn't input an identifier with a valid check digit
					patientTableHead.style.display = "none";
					var img = getProblemImage();
					var tmp = invalidCheckDigitText + " <img src='" + img.src + "' title='" + img.title + "' />";
					links.push(tmp);
					links.push(noPatientsFoundText);
					links.push(searchOnPatientNameText);
				}
				else {
					//the user did input a valid identifier, but we don't have it
					links.push(noPatientsFoundText);
					links.push(addPatientLink);
				}
			}
			else {
				// the user put in a text search
				links.push(noPatientsFoundText);
				links.push(addPatientLink);
			}
			fillTable([]);	//this call sets up the table/info bar
		}
		else {
			if (patients.length > 1 || isValidCheckDigit(savedText) == false) {
				links.push(addPatientLink);	//setup links for appending to the end
			}
			fillTable(patients);		//continue as normal
		}
		
		DWRUtil.addRows(objectHitsTableBody, links, [getNumber, getString]);
		
		if (keyCode == ENTERKEY)
			setTimeout("showHighlight()", 0);
		
		return false;
	};
	
	function fillPatientDetails(p) {
		findPatient.style.display = "none";
		patientSummary.style.display = "";
		selectForm.style.display = "";
		selectFormForm.patientId.value = p.patientId;
		selectFormForm.elements[0].focus();
		patient = p;
		$("name").innerHTML = p.givenName + " " + p.middleName + " " + p.familyName;
		$("gender").innerHTML = p.gender;
		$("address1").innerHTML = p.address.address1;
		$("address2").innerHTML = p.address.address2;
		$("identifier").innerHTML = p.identifier;
		$("tribe").innerHTML = p.tribe;
		$("birthdate").innerHTML = getBirthday(p);
		$("mothersName").innerHTML = p.mothersName;
	}
	
	function editPatient() {
		//TODO make this function just modify the current form to include text boxes
		window.open('<%= request.getContextPath() %>/admin/patients/patient.form?patientId=' + patient.patientId);
		return false;
	}
	
	function allowAutoJump() {
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
</style>

<h2><spring:message code="formentry.title"/></h2>

<div id="findPatient">
	<b class="boxHeader"><spring:message code="formentry.step1"/></b>
	<div class="box">
		<form id="findPatientForm" onSubmit="return search(searchBox, event, false, 0);">
			<table>
				<tr>
					<td><spring:message code="formentry.searchBox"/></td>
					<td><input type="text" id="searchBox" onKeyUp="search(this, event, false, 400)"></td>
				</tr>
			</table>
			<!-- <input type="submit" value="Search" onClick="return updatePatients();"> -->
		</form>
		<div id="patientListing">
			<table id="patientTable">
			 <thead id="patientTableHead">
				 <tr>
				 	<th> </th>
				 	<th><spring:message code="Patient.identifier"/></th>
				 	<th><spring:message code="PatientName.familyName"/></th>
				 	<th><spring:message code="PatientName.givenName"/></th>
				 	<th><spring:message code="PatientName.middleName"/></th>
				 	<th><spring:message code="Patient.gender"/></th>
				 	<th><spring:message code="Patient.tribe"/></th>
				 	<th><spring:message code="Patient.birthdate"/></th>
				 	<th><spring:message code="Patient.mothersName"/></th>
				 </tr>
			 </thead>
			 <tbody id="patientTableBody">
			 </tbody>
			</table>
		</div>
	</div>
</div>

<div id="patientSummary">
	<b class='boxHeader'><spring:message code="formentry.patient.info"/></b>
	<a href='index.htm' onClick="return editPatient();" style='float:right'><spring:message code="Patient.edit"/></a>
	<table>
		<tr>
			<td valign="top"><b><spring:message code="Patient.identifier"/></b></td>
			<td id="identifier"></td>
		</tr>
		<tr>
			<td><b><spring:message code="general.name"/></b></td>
			<td id="name"></td>
		</tr>
		
		<tr>
			<td><b><spring:message code="Patient.gender"/></b></td>
			<td id="gender"></td>
		</tr>
		<tr><td valign="top"><b><spring:message code="PatientAddress.address1"/></b><td id="address1"></td></tr>
		<tr><td valign="top"><b><spring:message code="PatientAddress.address2"/></b><td id="address2"></td></tr>
		<tr><td valign="top"><b><spring:message code="Tribe.name"/></b><td id="tribe"></td></tr>
		<tr><td valign="top"><b><spring:message code="Patient.birthdate"/></b><td id="birthdate"></td></tr>
		<tr><td valign="top"><b><spring:message code="Patient.mothersName"/></b><td id="mothersName"></td></tr>
	</table>
	<br /><input type='button' value='<spring:message code="formentry.patient.switch"/>' onClick='showSearch(); patientListing.style.display = "";'>
</div>

<br />

<b class="boxHeader"><spring:message code="formentry.step2"/></b>
<div id="selectForm" class="box">
	<form id="selectFormForm" method="post" action="<%= request.getContextPath() %>/formDownload">
		<table>
			<tr>
				<td><input type="radio" name="formType" value="adultInitial" id="adultInitial"></td>
				<td><label for="adultInitial">Adult Initial</label></td>
			</tr>
			<tr>
				<td><input type="radio" name="formType" value="adultReturn" id="adultReturn"></td>
				<td><label for="adultReturn">Adult Return</label></td>
			</tr>
			<tr>
				<td><input type="radio" name="formType" value="adultReturn_local" id="adultReturn_local"></td>
				<td><label for="adultReturn_local">Adult Return (Localhost)</label></td>
			</tr>
			<tr>
				<td><input type="radio" name="formType" value="pedInitial" id="pedInitial"></td>
				<td><label for="pedInitial">Ped Initial</label></td>
			</tr>
			<tr>
				<td><input type="radio" name="formType" value="pedReturn" id="pedReturn"></td>
				<td><label for="pedReturn">Ped Return</label></td>
			</tr>
		</table>
		<input type="hidden" name="patientId" id="patientId" value="">
		<input type="submit" value="<spring:message code="formentry.download.form"/>">
	</form>
</div>

<script>
	
	var patientListing= document.getElementById("patientListing");
	var selectForm    = document.getElementById("selectForm");
	var findPatient   = document.getElementById("findPatient");
	var searchBox		= document.getElementById("searchBox");
	var findPatientForm = document.getElementById("findPatientForm");
	var selectFormForm  = document.getElementById("selectFormForm");
	var patientSummary  = document.getElementById("patientSummary");
	var patientTableHead= document.getElementById("patientTableHead");
	
	var invalidCheckDigitText   = "Invalid check digit. ";
	var searchOnPatientNameText = "Please search on part of the patient's name. ";
	var noPatientsFoundText     = "No patients found. ";
	var addPatientLink = document.createElement("a");
	addPatientLink.href= "${pageContext.request.contextPath}/admin/patients/addPatient.htm";
	addPatientLink.innerHTML = "Add a new patient ";
	
	function init() {
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
		if (searchBox.value != "")
			search(searchBox, null, false, 0);
		
	}
		
	window.onload=init;
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>
