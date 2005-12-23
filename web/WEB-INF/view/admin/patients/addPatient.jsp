<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Patients" otherwise="/login.htm" redirect="/admin/patients/addPatient.htm"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script src='<%= request.getContextPath() %>/dwr/interface/DWRPatientService.js'></script>
<script src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script src='<%= request.getContextPath() %>/scripts/openmrsSearch.js'></script>

<script type="text/javascript">

	var name;
	var birthdate;
	var gender;
	var form;
	var noPatientsFound = "No Patients Found.  Add a new Patient";
	
	function findObjects() {
		DWRPatientService.getSimilarPatients(preFillTable, name.value, birthdate.value, gender.selectedValue);
		return false;
	}
	
	function preFillTable(patients) {
		if (patients.length < 1) {
			var patient = new Object();
			patients[0].patientId = noPatientsFound;
		}
		fillTable(patients);
	}
	
	function onSelect(patients) {
		if (patients[0].patientId == noPatientsFound)
			document.location = "patient.form";
		else
			document.location = "patient.form?patientId=" + patients[0].patientId;
	}
	
	document.onload = init;
	
	var init = function() {
			name = $("name");
			birthdate = $("birthdate");
			gender = $("gender");
			form = $("patientForm");
		};
	
</script>
<!-- patientSearch.js must be imported after the findObjects() definition for override -->
<script src='<%= request.getContextPath() %>/scripts/patientSearch.js'></script>

<br />
<h2><spring:message code="Patient.title"/></h2>

<form method="get" action="patient.form" onSubmit="return search(name, event, false, 0);" id="patientForm">
<table>
	<tr>
		<td><spring:message code="Patient.name"/></td>
		<td><input type="text" name="name" id="name" value="" onKeyUp="search(this, event, false, 500);" /></td>
	</tr>
	<tr>
		<td><spring:message code="Patient.birthdate"/></td>
		<td><input type="text" name="birthdate" id="birthdate" size="10" value="" /></td>
	</tr>
	<tr>
		<td><spring:message code="Patient.gender"/></td>
		<td><select name="gender" id="gender" onChange="search(0);">
				<option value="M">Male</option>
				<option value="F">Female</option>
			</select>
		</td>
	</tr>
</table>
<br />

<div id="patientsFound">
	<table>
		<tbody id="patientTableBody">
		</tbody>
	</table>
</div>

</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>