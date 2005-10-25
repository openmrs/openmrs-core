<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.api.PatientService" %>
<%@ page import="org.openmrs.Patient" %>
<%@ page import="org.openmrs.api.APIException" %>
<%@ page import="org.openmrs.web.Constants" %>

<openmrs:require privilege="Manage Patients" otherwise="/login.jsp" />

<script src='<%= request.getContextPath() %>/dwr/interface/DWRPatientService.js'></script>
<script src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script src='<%= request.getContextPath() %>/dwr/util.js'></script>

<script>

	var timeout;
	var searchOn;
	
	function showSearch() {
		findPatient.style.display = "";
		patientListing.style.display = "none";
		identifierBox.focus();
	}
	
	function searchBoxChange(event, obj) {
		if (event.altKey == false &&
			event.ctrlKey == false &&
			((event.keyCode >= 32 && event.keyCode <= 127) || event.keyCode == 8)) {
				clearTimeout(timeout);
				timeout = setTimeout("updatePatients()",obj.id=="identifier" ? 1000 : 500);
		}
	}
	
	function choose(obj) {
		if (obj.id == "identifier") {
			searchOn = "identifier";
		}
		else {
			searchOn = "name";
		}
	}
	
	function updatePatients() {
	    DWRUtil.removeAllRows("patientTableBody");
	    var identifier	= searchOn == "identifier" ? identifierBox.value : "";
	    var givenName	= givenNameBox.value;
	    var familyName	= familyNameBox.value;
	    var voided		= includeVoidedBox.checked;
	    DWRPatientService.findPatients(fillTable, identifier, givenName, familyName, voided);
	    patientListing.style.display = "";
	    return false;
	}
	
	var getCheckbox     = function(obj) {
		return "<input type='checkbox' name='patientId' value='" + obj.patientId + "'>";
		}
	var getIdentifier	= function(obj) {
		html = "<a href='editPatient.jsp?patientId=" + obj.patientId + "'";
		if (obj.voided)
			html = html + "class='voided'";
		
		html = html + ">" + obj.identifier + "</a>";
		return html; 
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
	    DWRUtil.addRows("patientTableBody", patientListItem, [ getCheckbox, getIdentifier, getGivenName, getFamilyName, getGender, getRace, getBirthdate, getMothersName]);
	}
	
</script>

<%
	Context context = (Context)session.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
	PatientService patientService = context.getPatientService();

	//voiding patients
	String[] patients = request.getParameterValues("patientId");
	String action  = request.getParameter("action");
	if (patients != null) {
		action = action.startsWith("Void") ? "voided" : "unvoided";
		Patient tmpPatient = null;
		for(int x = 0; x < patients.length; x++) {
				tmpPatient = context.getPatientService().getPatient(Integer.valueOf(patients[x]));
				try {
					if (action == "voided")
						patientService.voidPatient(tmpPatient, "REASON"); //TODO add reason
					else
						patientService.unvoidPatient(tmpPatient);
				}
				catch (APIException e)
				{
					session.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Patient cannot be " + action + " - " + e.getMessage());
				}
		}
		if (patients.length == 1)
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, "Patient '" + tmpPatient.getPatientId() + "' " + action);
		else
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, patients.length + " patients " + action);
	}
%>
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader" %>

<br />
<h2>Patient Management</h2>	
<br />

<b class="boxHeader">Select a Patient</b>
<div class="box" id="findPatient">
	<form id="findPatientForm" onSubmit="updatePatients(); return false;">
		<table>
			<tr>
				<td>Identifier</td>
				<td><input type="text" id="identifier" onFocus="choose(this)" onKeyUp="searchBoxChange(event, this)"></td>
			</tr>
			<tr><td></td><td><i>or</i></tr>
			<tr>
				<td>First Name</td>
				<td><input type="text" id="givenName" onFocus="choose(this)" onKeyUp="searchBoxChange(event, this)"></td>
			</tr>
			<tr>
				<td>Last Name</td>
				<td><input type="text" id="familyName" onFocus="javascript:choose(this)" onKeyUp="searchBoxChange(event, this)"></td>
			</tr>
			<tr>
				<td>Include Voided</td>
				<td><input type="checkbox" id="includeVoided" onClick="updatePatients()" /></td>
			</tr>
		</table>
		<!-- <input type="submit" value="Search" onClick="return updatePatients();"> -->
	</form>

	<br />
	
	<form id="patientListing">
		<table id="patientTable" width="100%">
		 <thead>
			 <tr>
			 	<th></th>
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
		 	<tr>
		 		<td colspan="8">
				 	<input type="submit" value="Void Selected Patients" name="action">
					<input type="submit" value="Unvoid Selected Patients" name="action">
				</td>
		 	<tr><td colspan="8"><i>Don't see the patient?</i> Use the <a href="createPatientForm.jsp">Create New Patient Form</a></td></tr>
		 </tfoot>
		</table>
	</form>
</div>

<script>

	var patientListing= document.getElementById("patientListing");
	var findPatient   = document.getElementById("findPatient");
	var identifierBox = document.getElementById("identifier");
	var givenNameBox  = document.getElementById("givenName");
	var familyNameBox = document.getElementById("familyName");
	var includeVoidedBox = document.getElementById("includeVoided");
	var patientTableBody = document.getElementById("patientTableBody");
	
	showSearch();
	
	// creates back button functionality
	if (identifierBox.value != "" || givenNameBox.value != "" || familyNameBox.value != "")
		updatePatients();

</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>