<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/admin/patients/index.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script src='<%= request.getContextPath() %>/scripts/validation.js'></script>

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
			var str = "";
			str += "<input type='button' onClick='selectPatient(";
			str += obj.patientId;
			str += ")' class='small' value='<spring:message code="general.select"/>";
			return str;
		};
	var getEditLink		= function(obj) {
			var str = "";
			str += "<a href='patient.form?patientId=";
			str += obj.patientId;
			str += "'>Edit</a>";
			return str;
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
	    DWRUtil.addRows("patientTableBody", patientListItem, [getEditLink, getIdentifier, getGivenName, getFamilyName, getGender, getRace, getBirthdate, getMothersName]);
	}
	
</script>

<h2>Form Entry</h2>

<div id="findPatient">
	<b class="boxHeader"><spring:message code="Patient.find"/></b>
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
</div>


<script>

	var patientListing= document.getElementById("patientListing");
	var selectForm    = document.getElementById("selectForm");
	var findPatient   = document.getElementById("findPatient");
	var searchBox		= document.getElementById("searchBox");
	var searchType		= document.getElementById("searchType");
	var patientTableBody= document.getElementById("patientTableBody");
	var findPatientForm = document.getElementById("findPatientForm");
	
	showSearch();
	
	<request:existsAttribute name="patientId">
		selectPatient(request.getAttribute("patientId"));
	</request:existsAttribute>
	
	// creates back button functionality
	if (searchBox.value != "")
		updatePatients();
	
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>
