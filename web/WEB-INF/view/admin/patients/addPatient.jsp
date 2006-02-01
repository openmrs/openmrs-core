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
	var birthyear;
	var gender;
	var form;
	var noPatientsFound;
	var patientsFound ;
	var inputChanged = false;
	
	function findObjects(text) {
		patientName = text;
		birthyear = $("birthyear").value.toString();
		gender = $("gender").value.toString();
		DWRPatientService.getSimilarPatients(preFillTable, text, birthyear, gender);
		return false;
	}
	
	function preFillTable(patients) {
		if (patients.length < 1) {
			if (patientName != "" && birthyear != "" && gender != "") {
				document.location = getHref();
			}
			else {
				patients.push(noPatientsFound);
			}
		}
		else {
			patients.push(patientsFound);	//setup links for appending to the end
		}
		fillTable(patients);		//continue as normal
	}
	
	function onSelect(patients) {
		document.location = "${pageContext.request.contextPath}/formentry/patientSummary.form?patientId=" + patients[0].patientId;
	}
	
	var getHref = function() {
		return "newPatient.form?name=" + patientName + "&birthyear=" + birthyear + "&gender=" + gender;
	}
	
	var init = function() {

			form = $("patientForm");

			noPatientsFound = "<a href='#' class='searchHit' onclick='document.location=getHref()'>No Patients Found.  Select to add a new Patient</a>";
			patientsFound = "<a href='#' class='searchHit' onclick='document.location=getHref()'>Add New Patient</a>";
			$("patientName").focus();
		};
		
	window.onload = init;
	
	var allowNewSearch = function() {
		if (inputChanged = true) {
			inputChanged = false;
			return true;
		}
		return false;
	}
	
	var allowAutoJump = function() {
		return false;
	}
	
</script>
<!-- patientSearch.js must be imported after the findObjects() definition for override -->
<script src='<%= request.getContextPath() %>/scripts/patientSearch.js'></script>

<h2><spring:message code="Patient.title"/></h2>

<form method="get" action="newPatient.form" onSubmit="return search(patientName, null, false, 0);" id="patientForm">
	<table>
		<tr>
			<td><spring:message code="Patient.name"/></td>
			<td><input type="text" name="patientName" id="patientName" value=""/></td>
		</tr>
		<tr>
			<td><spring:message code="Patient.birthyear"/></td>
			<td><input type="text" name="birthyear" id="birthyear" size="5" value="" onChange="inputChanged=true;" onFocus="exitNumberMode(patientName)" /></td>
		</tr>
		<tr>
			<td><spring:message code="Patient.gender"/></td>
			<td><select name="gender" id="gender" onChange="inputChanged=true;" onFocus="exitNumberMode(patientName)">
					<openmrs:forEachRecord name="gender">
						<option value="${record.key}"><spring:message code="Patient.gender.${record.value}"/></option>
					</openmrs:forEachRecord>
				</select>
			</td>
		</tr>
	</table>
	
	<input type="submit" value="<spring:message code="general.continue"/>" onClick="return search(patientName, null, false, 0);"/>
	
	<br /><br />
	
	<div id="patientsFound">
		<table cellspacing="0" cellpadding="1">
			<tbody id="patientTableBody">
			</tbody>
		</table>
	</div>
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>