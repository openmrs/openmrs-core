<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/formentry/index.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<script src='<%= request.getContextPath() %>/validation.js'></script>

<script src='<%= request.getContextPath() %>/dwr/interface/DWRPatientService.js'></script>
<script src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script src='<%= request.getContextPath() %>/dwr/util.js'></script>

<script>

	var timeout;
	var patient;
	
	function showSearch() {
		findPatient.style.display = "";
		patientListing.style.display = "none";
		selectForm.style.display = "none";
		patientSummary.style.display = "none";
		searchBox.focus();
	}
	
	function searchBoxChange(event, obj) {
		if (event.altKey == false &&
			event.ctrlKey == false &&
			((event.keyCode >= 32 && event.keyCode <= 127) || event.keyCode == 8)) {
				clearTimeout(timeout);
				if (Math.abs(event.keyCode - 48) < 10) { //if keyCode is a digit
					searchType.value = "identifier";
					timeout = setTimeout("validateIdentifier()", 300);
				}
				else {
					searchType.value = "name";
					showError(true, obj, "");
					timeout = setTimeout("updatePatients()",400);
				}
		}
	}
	
	function validateIdentifier() {
		if (showError(isValidCheckDigit(searchBox.value), searchBox, '<spring:message code="error.identifier"/>'))
			updatePatients();
	}
	
	function updatePatients() {
	    DWRUtil.removeAllRows("patientTableBody");
	    DWRPatientService.findPatients(fillTable, searchBox.value, searchType.value, 0);
	    patientListing.style.display = "";
	    return false;
	}
	
	var getButton		= function(obj) {
			var html = "";
			html += "<input type='button' onClick='selectPatient(";
			html += obj.patientId;
			html += ")' class='small' value='<spring:message code="general.select"/>";
			return html;
		};
	var getIdentifier	= function(obj) { return obj.identifier; };
	var getGivenName	= function(obj) { return obj.givenName;  };
	var getFamilyName	= function(obj) { return obj.familyName; };
	var getGender		= function(obj) { return obj.gender; };
	var getRace			= function(obj) { return obj.race; };
	var getBirthdate	= function(obj) { 
			var str = '';
			if (obj.birthdate != null) {
				str += obj.birthdate.getMonth() + 1 + '-';
				str += obj.birthdate.getDate() + '-';
				str += (obj.birthdate.getYear() + 1900);
			}
			
			if (obj.birthdateEstimated)
				str += " (?)";
			
			return str;
		};
	var getMothersName  = function(obj) { return obj.mothersName;  };
	
	function fillTable(patientListItem) {
	    DWRUtil.addRows("patientTableBody", patientListItem, [getButton, getIdentifier, getGivenName, getFamilyName, getGender, getRace, getBirthdate, getMothersName]);
	}
	
	function selectPatient(patientId) {
		findPatient.style.display = "none";
		patientSummary.style.display = "";
		selectForm.style.display = "";
		selectFormForm.patientId.value = patientId;
		DWRPatientService.getPatient(fillPatientDetails, patientId);
	}
	
	function fillPatientDetails(p) {
		patient = p;
		document.getElementById("name").innerHTML = p.givenName + " " + p.familyName;
		document.getElementById("gender").innerHTML = p.gender;
		html = p.address.address1 + "<br/>";
		if (p.address.address2 != null)
			html = html + p.address.address2 + "<br/>";
		html = html + p.address.cityVillage + ", ";
		html = html + p.address.stateProvince + " ";
		html = html + p.address.country + " ";
		html = html + p.address.postalCode;
		document.getElementById("address").innerHTML = html;
		document.getElementById("identifiers").innerHTML = p.identifier;
	}
	
	function editPatient() {
		window.open('<%= request.getContextPath() %>/admin/patients/patient.form?patientId=' + patient.patientId);
		return false;
	}

</script>

<h2>Form Entry</h2>

<div id="findPatient">
	<b class="boxHeader"><spring:message code="formentry.step1"/></b>
	<div class="box">
		<br>
		<form id="findPatientForm" onSubmit="updatePatients(); return false;">
			<table>
				<tr>
					<td><spring:message code="formentry.searchBox"/></td>
					<td><input type="text" id="searchBox" onKeyUp="searchBoxChange(event, this)"></td>
					<input type="hidden" id="searchType">
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
				 	<th><spring:message code="PatientName.givenName"/></th>
				 	<th><spring:message code="PatientName.familyName"/></th>
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
			 		<a href="${pageContext.request.contextPath}/admin/patients/patient.form"><spring:message code="Patient.create"/></a>
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
			<td><b>Name</b></td><td id="name"></td>
			<td valign="top"><b>Identifiers</b></td><td id="identifiers"></td>
		</tr>
		
		<tr>
			<td><b>Gender</b></td><td id="gender"></td>
		</tr>
		<tr><td valign="top"><b>Address</b><td id="address"></td></tr>
	</table>
	<br /><input type='button' value='<spring:message code="formentry.patient.switch"/>' onClick='showSearch(); patientListing.style.display = "";'>
</div>

<br />

<b class="boxHeader"><spring:message code="formentry.step2"/></b>
<div id="selectForm" class="box">
	<br />
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
	var searchType		= document.getElementById("searchType");
	var patientTableBody= document.getElementById("patientTableBody");
	var findPatientForm = document.getElementById("findPatientForm");
	var selectFormForm  = document.getElementById("selectFormForm");
	var patientSummary  = document.getElementById("patientSummary");
	
	showSearch();
	
	<request:existsAttribute name="patientId">
		selectPatient(request.getAttribute("patientId"));
	</request:existsAttribute>
	
	// creates back button functionality
	if (searchBox.value != "")
		updatePatients();
	
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>
