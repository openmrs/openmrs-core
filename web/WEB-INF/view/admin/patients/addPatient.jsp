<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/admin/patients/addPatient.htm"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script src='<%= request.getContextPath() %>/dwr/interface/DWRPatientService.js'></script>
<script src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script src='<%= request.getContextPath() %>/scripts/openmrsSearch.js'></script>
<script src='<%= request.getContextPath() %>/scripts/validation.js'></script>
<script src='<%= request.getContextPath() %>/scripts/patientSearch.js'></script>

<script type="text/javascript">

	var patientName;
	var birthyear;
	var age;
	var gender;
	var form;
	var noPatientsFound;
	var patientsFound ;
	var inputChanged = false;
	
	function findObjects(text) {
		patientName = text;
		birthyear = $("birthyear").value.toString();
		age = $("age").value.toString();
		gender = "";
		var options = document.getElementsByTagName("input");
		for (var i=0; i<options.length;i++) {
			if (options[i].type == 'radio' && options[i].name=='gender' && options[i].checked == true)
				gender = options[i].value.toString();
		}
		DWRPatientService.getSimilarPatients(preFillTable, text, birthyear, age, gender);
		return false;
	}
	
	function preFillTable(patients) {
		$("patientTable").style.display = "";
		
		if (patients.length < 1) {
			if (patientName != "" && (birthyear != "" || age != "") && gender != "") {
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
		return "newPatient.form?name=" + patientName + "&birthyear=" + birthyear + "&gender=" + gender + "&age=" + age;
	}
	
	var init = function() {

		form = $("patientForm");

		noPatientsFound = "<a href='#' class='searchHit' onclick='document.location=getHref()'>No Patients Found.  Select to add a new Patient</a>";
		patientsFound = "<a href='#' class='searchHit' onclick='document.location=getHref()'>Add New Patient</a>";
		$("patientName").focus();
		$("patientTable").style.display = "none";
		
		DWRUtil.useLoadingMessage();
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
	
	function cancel() {
		window.location = "${pageContext.request.contextPath}/formentry";
	}
	
</script>

<style>
	tr th#patientGender, tr th#patientAge, .patientGender, .patientAge {
		text-align: center;
	}
</style>

<h2><spring:message code="Patient.title"/></h2>

<form method="get" action="" onSubmit="return search(patientName, null, false, 0);" id="patientForm">
	<table>
		<tr>
			<td><spring:message code="Patient.name"/></td>
			<td><input type="text" name="patientName" id="patientName" value=""/></td>
		</tr>
		<tr>
			<td><spring:message code="Patient.birthyear"/></td>
			<td>
				<input type="text" name="birthyear" id="birthyear" size="5" value="" onChange="inputChanged=true;" onFocus="exitNumberMode(patientName)" />
				<spring:message code="Patient.age.or"/>
				<input type="text" name="age" id="age" size="5" value="" onChange="inputChanged=true;" onFocus="exitNumberMode(patientName)" />
			</td>
		</tr>
		<tr>
			<td><spring:message code="Patient.gender"/></td>
			<td>
				<openmrs:forEachRecord name="gender">
					<input type="radio" name="gender" id="${record.key}" value="${record.key}" <c:if test="${record.key == status.value}">checked</c:if> onclick="inputChanged=true" onFocus="exitNumberMode(patientName)" /><label for="${record.key}"> <spring:message code="Patient.gender.${record.value}"/> </label>
				</openmrs:forEachRecord>
			</td>
		</tr>
	</table>
	
	<input type="button" value="<spring:message code="general.continue"/>" onClick="return search(patientName, null, false, 0);"/> &nbsp; &nbsp; 
	<input type="button" value="<spring:message code="general.cancel" />" name="action" id="cancelButton" onClick="return cancel()">
	
	<br /><br />
	
	<table cellspacing="0" cellpadding="1" id="patientTable">
		<thead>
			<tr>
			 	<th class="searchIndex"> </th>
			 	<th class="patientIdentifier"> <spring:message code="Patient.identifier"/> </th>
			 	<th> <spring:message code="PatientName.givenName"/> </th>
			 	<th> <spring:message code="PatientName.middleName"/> </th>
			 	<th> <spring:message code="PatientName.familyName"/> </th>
			 	<th id='patientAge'> <spring:message code="Patient.age"/> </th>
			 	<th id='patientGender'> <spring:message code="Patient.gender"/> </th>
			 	<th> <spring:message code="Patient.tribe"/> </th>
			 	<th></th>
			 	<th> <spring:message code="Patient.birthdate"/> </th>
			 </tr>
		</thead>
		<tbody id="patientTableBody">
		</tbody>
	</table>
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>