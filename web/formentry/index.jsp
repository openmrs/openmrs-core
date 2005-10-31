<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.jsp" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<script src='<%= request.getContextPath() %>/validation.js'></script>

<script src='<%= request.getContextPath() %>/dwr/interface/DWRPatientService.js'></script>
<script src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script src='<%= request.getContextPath() %>/dwr/util.js'></script>

<script>

	var timeout;
	var searchOn;
	var identifier;
	var name;
	
	function showSearch() {
		findPatient.style.display = "";
		patientListing.style.display = "none";
		patientSummary.style.display = "none";
		selectForm.style.display = "none";
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
		if (showError(isValidCheckDigit(searchBox.value), searchBox, "Invalid identifier"))
			updatePatients();
	}
	
	function updatePatients() {
	    DWRUtil.removeAllRows("patientTableBody");
	    DWRPatientService.findPatients(fillTable, searchBox.value, searchType.value, 0);
	    patientListing.style.display = "";
	    return false;
	}
	
	var getIdentifier	= function(obj) { 
			var str = "";
			str += "<a href=javascript:selectPatient(" + obj.patientId + ")>";
			str += obj.identifier;
			str += "</a>";
			return str;
		};
	var getGivenName	= function(obj) { return obj.givenName;  };
	var getFamilyName	= function(obj) { return obj.familyName; };
	var getGender		= function(obj) { return obj.gender; };
	var getRace			= function(obj) { return obj.race; };
	var getBirthdate	= function(obj) { 
			var str = '';
			str += obj.birthdate.getMonth() + '-';
			str += obj.birthdate.getDate() + '-';
			str += (obj.birthdate.getYear() + 1900);
			
			if (obj.birthdateEstimated)
				str += " (?)";
			
			return str;
		};
	var getMothersName  = function(obj) { return obj.mothersName;  };
	
	function fillTable(patientListItem) {
	    DWRUtil.addRows("patientTableBody", patientListItem, [getIdentifier, getGivenName, getFamilyName, getGender, getRace, getBirthdate, getMothersName]);
	}
	
	function selectPatient(patientId) {
		findPatient.style.display = "none";
		patientSummary.style.display = "";
		selectForm.style.display = "";
		selectFormForm.patientId.value = patientId;
		DWRPatientService.getPatient(fillPatientDetails, patientId);
	}
	
	function fillPatientDetails(patient) {
		var html = "<b class='boxHeader'>Patient Information</b>";
		html = html + "<a href='editPatient?patientId=" + patient.patientId + "' style='float:right'>Edit patient</a>";
		html = html + "<b>Name</b>:" + patient.givenName + " " + patient.familyName + "<br />";
		html = html + "<b>Gender</b>:" + patient.gender + "<br />";
		html = html + "<b>Address</b>:" + patient.address + "<br />...";
		html = html + "<br /><input type='button' value='Switch Patient' onClick='showSearch(); patientListing.style.display = \"\";'>";
		patientSummary.innerHTML = html;
	}

</script>

<h2>Form Entry</h2>

<div id="findPatient">
	<b class="boxHeader">1. Find a Patient</b>
	<div class="box">
		<br>
		<form id="findPatientForm" onSubmit="updatePatients(); return false;">
			<table>
				<tr>
					<td>Identifier or Patient Name</td>
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
				 	<th>Identifier</th>
				 	<th>Given Name</th>
				 	<th>Family Name</th>
				 	<th>Gender</th>
				 	<th>Race</th>
				 	<th>BirthDate</th>
				 	<th>Mother's Name</th>
				 </tr>
			 </thead>
			 <tbody id="patientTableBody">
			 </tbody>
			 <tfoot>
			 	<tr><td colspan="8"><br /><i>Don't see the patient?</i> Use the <a href="createPatientForm.jsp">Create New Patient Form</a></td></tr>
			 </tfoot>
			</table>
		</div>
	</div>
</div>

<div id="patientSummary">
</div>

<br />

<b class="boxHeader">2. Select a form</b>
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
				<td><input type="radio" name="formType" value="pedInitial" id="pedInitial"></td>
				<td><label for="pedInitial">Ped Initial</label></td>
			</tr>
			<tr>
				<td><input type="radio" name="formType" value="pedReturn" id="pedReturn"></td>
				<td><label for="pedReturn">Ped Return</label></td>
			</tr>
		</table>
		<input type="hidden" name="patientId" id="patientId" value="">
		<input type="submit" value="Download Form">
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
