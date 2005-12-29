<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Patients" otherwise="/login.htm" redirect="/admin/patients/addPatient.htm"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script src='<%= request.getContextPath() %>/dwr/interface/DWRPatientService.js'></script>
<script src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script src='<%= request.getContextPath() %>/scripts/openmrsSearch.js'></script>

<script type="text/javascript">

	var patientName;
	var birthdate;
	var gender;
	var form;
	var noPatientsFound = "No Patients Found.  Select to add a new Patient";
	var patientsFound   = "Add New Patient";
	
	function findObjects() {
		DWRPatientService.getSimilarPatients(preFillTable, patientName.value, birthdate.value, gender.value);
		return false;
	}
	
	function preFillTable(patients) {
		var text = new Array();
		if (patients.length < 1) {
			text.push(noPatientsFound);
			fillTable([]);
		}
		else {
			text.push(patientsFound);
			fillTable(patients);
		}
		setTimeout(DWRUtil.addRows(objectHitsTableBody, text, [getNumber, getTextLink]), 0);
	}
	
	function onSelect(patients) {
		if (patients[0].patientId == noPatientsFound)
			document.location = "patient.form";
		else
			document.location = "patient.form?patientId=" + patients[0].patientId;
	}
	
	var getTextLink = function(p) {
		var obj = document.createElement("a");
		obj.href = "patient.form?name=" + patientName.value + "&birthdate=" + birthdate.value + "&gender=" + gender.value;
		obj.className = "searchHit";
		obj.innerHTML = p;
		return obj
	}
	
	var init = function() {
			patientName = $("patientName");
			birthdate = $("birthdate");
			gender = $("gender");
			form = $("patientForm");
			patientName.focus();
		};
		
	window.onload = init;
	
</script>
<!-- patientSearch.js must be imported after the findObjects() definition for override -->
<script src='<%= request.getContextPath() %>/scripts/patientSearch.js'></script>

<br />
<h2><spring:message code="Patient.title"/></h2>

<form method="get" action="patient.form" onSubmit="return search(name, event, false, 0);" id="patientForm">
<table>
	<tr>
		<td><spring:message code="Patient.name"/></td>
		<td><input type="text" name="patientName" id="patientName" value="" onKeyUp="search(this, event, false, 500);" /></td>
	</tr>
	<tr>
		<td><spring:message code="Patient.birthdate"/></td>
		<td><input type="text" name="birthdate" id="birthdate" size="10" value="" /></td>
	</tr>
	<tr>
		<td><spring:message code="Patient.gender"/></td>
		<td><select name="gender" id="gender" onChange="search(patientName, event, false, 0);">
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