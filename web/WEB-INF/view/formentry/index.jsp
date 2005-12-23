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
	
	function showSearch() {
		findPatient.style.display = "";
		patientListing.style.display = "none";
		selectForm.style.display = "none";
		patientSummary.style.display = "none";
		searchBox.focus();
	}
	
	function onSelect(arr) {
		DWRPatientService.getPatient(fillPatientDetails, arr[0].patientId);
	}
	
	function fillPatientDetails(p) {
		findPatient.style.display = "none";
		patientSummary.style.display = "";
		selectForm.style.display = "";
		selectFormForm.patientId.value = p.patientId;
		patient = p;
		$("name").innerHTML = p.givenName + " " + p.familyName;
		$("gender").innerHTML = p.gender;
		var html = "";
		if (p.address.address1 != null)
			html = p.address.address1 + "<br/>";
		if (p.address.address2 != null)
			html = html + p.address.address2 + "<br/>";
		html = html + p.address.cityVillage + ", ";
		html = html + p.address.stateProvince + " ";
		html = html + p.address.country + " ";
		html = html + p.address.postalCode;
		$("address").innerHTML = html;
		$("identifiers").innerHTML = p.identifier;
	}
	
	function editPatient() {
		window.open('<%= request.getContextPath() %>/admin/patients/patient.form?patientId=' + patient.patientId);
		return false;
	}
	
</script>

<h2><spring:message code="formentry.title"/></h2>

<div id="findPatient">
	<b class="boxHeader"><spring:message code="formentry.step1"/></b>
	<div class="box">
		<form id="findPatientForm" onSubmit="return search(searchBox, event, includeVoided.checked, 0);">
			<table>
				<tr>
					<td><spring:message code="formentry.searchBox"/></td>
					<td><input type="text" id="searchBox" onKeyUp="search(this, event, includeVoided.checked, 400)"></td>
					<td><spring:message code="formentry.includeVoided"/><input type="checkbox" id="includeVoided" onClick="search(searchBox, event, this.checked, 0); searchBox.focus();" /></td>
				</tr>
			</table>
			<!-- <input type="submit" value="Search" onClick="return updatePatients();"> -->
		</form>
		<div id="patientListing">
			<table id="patientTable">
			 <thead>
				 <tr>
				 	<th> </th>
				 	<th><spring:message code="Patient.identifier"/></th>
				 	<th><spring:message code="PatientName.familyName"/></th>
				 	<th><spring:message code="PatientName.givenName"/></th>
				 	<th><spring:message code="PatientName.middleName"/></th>
				 	<th><spring:message code="Patient.gender"/></th>
				 	<th><spring:message code="Patient.race"/></th>
				 	<th><spring:message code="Patient.birthdate"/></th>
				 	<th><spring:message code="Patient.mothersName"/></th>
				 </tr>
			 </thead>
			 <tbody id="patientTableBody">
			 </tbody>
			 <tfoot>
			 	<tr><td colspan="8"><br />
			 		<i><spring:message code="formentry.patient.missing"/></i> 
			 		<a href="${pageContext.request.contextPath}/admin/patients/addPatient.htm"><spring:message code="Patient.create"/></a>
				</td></tr>
			 </tfoot>
			</table>
		</div>
	</div>
</div>

<div id="patientSummary">
	<b class='boxHeader'><spring:message code="formentry.patient.info"/></b>
	<a href='index.htm' onClick="return editPatient();" style='float:right'><spring:message code="Patient.edit"/></a>
	<table>
		<tr>
			<td><b><spring:message code="general.name"/></b></td><td id="name"></td>
			<td valign="top"><b><spring:message code="Patient.identifiers"/></b></td><td id="identifiers"></td>
		</tr>
		
		<tr>
			<td><b><spring:message code="Patient.gender"/></b></td><td id="gender"></td>
		</tr>
		<tr><td valign="top"><b><spring:message code="PatientAddress.address1"/></b><td id="address"></td></tr>
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
	
	showSearch();
	
	<request:existsAttribute name="patientId">
		var pats = new Array();
		var pats[0] = new Object();
		pats[0].patientId = request.getAttribute("patientId");
		onSelect(pats);
	</request:existsAttribute>
	
	// creates back button functionality
	if (searchBox.value != "")
		searchBoxChange(searchBox, event, $('includeVoided').checked, 0);
	
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>
