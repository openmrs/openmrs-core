<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.jsp" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<script src='/openmrs/dwr/interface/DWRPatientService.js'></script>
<script src='/openmrs/dwr/engine.js'></script>
<script src='/openmrs/dwr/util.js'></script>

<script>
	
	function updatePatients() {
	    //alert(document.getElementById("patientTable").style.display);
	    document.getElementById("patientTable").style.display = "";
	    document.getElementById("selectForm").style.display = "none";
	    DWRUtil.removeAllRows("patientBody");
	    var identifier = document.getElementById("identifier").value;
	    var givenName = document.getElementById("givenName").value;
	    var familyName = document.getElementById("familyName").value;
	    DWRPatientService.findPatients(fillTable, identifier, givenName, familyName);
	    return false;
	}
	
	var getId			= function(obj) { return obj.patientId; };
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
	    DWRUtil.addRows("patientBody", patientListItem, [ getId, getIdentifier, getGivenName, getFamilyName, getGender, getRace, getBirthdate, getMothersName]);
	}
	
	function selectPatient(patientId) {
		document.getElementById("patientTable").style.display = "none";
		var selectForm = document.getElementById("selectForm");
		selectForm.style.display = "block";
		selectForm.patientId.value = patientId;
	}

</script>

<h2>Form Entry</h2>

<b>Please Find a Patient</b>
<br>
<form id="findPatient" onSubmit="return false;">
	<table>
		<tr>
			<td>Identifier</td>
			<td><input type="text" id="identifier"></td>
		</tr>
		<tr><td></td><td><i>or</i></tr>
		<tr>
			<td>First Name</td>
			<td><input type="text" id="givenName"></td>
		</tr>
		<tr>
			<td>Last Name</td>
			<td><input type="text" id="familyName"></td>
		</tr>
	</table>
	<input type="submit" value="Search" onClick="return updatePatients();">
</form>

<div id="patientListing">
	<table id="patientTable">
	 <thead>
		 <tr>
		 	<th>Id</th>
		 	<th>Identifier</th>
		 	<th>Given Name</th>
		 	<th>Family Name</th>
		 	<th>Gender</th>
		 	<th>Race</th>
		 	<th>BirthDate</th>
		 	<th>Mother's Name</th>
		 </tr>
	 </thead>
	 <tbody id="patientBody">
	 </tbody>
	 <tfoot>
	 	<tr><td colspan="8"><i>Don't see the patient?</i> Use the <a href="createPatientForm.jsp">Create New Patient Form</a></td></tr>
	 </tfoot>
	</table>
</div>

<br /><br />

<form id="selectForm" method="post" action="<%= request.getContextPath() %>/formDownload">
	
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

<script>

	document.getElementById("patientTable").style.display = "none";
	document.getElementById("selectForm").style.display = "none";

	<request:existsAttribute name="patientId">
		document.getElementById("identifier").value = "<%= request.getAttribute("patientId") %>";
		updatePatients();
	</request:existsAttribute>
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>
