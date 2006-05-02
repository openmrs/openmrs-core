<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Patients" otherwise="/login.htm" redirect="/admin/patients/findDuplicatePatients.htm"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script src='<%= request.getContextPath() %>/dwr/interface/DWRPatientService.js'></script>
<script src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script src='<%= request.getContextPath() %>/scripts/openmrsSearch.js'></script>
<script src='<%= request.getContextPath() %>/scripts/patientSearch.js'></script>

<script type="text/javascript">

	var form;
	var tbody;
	var searchOn;
	var savedSearchOn;
	
	function onSelect(patients) {
		return false;
	}
	
	var init = function() {
		form = $("patientForm");
		tbody = $("patientTableBody");
		searchOn = new Array();
		$("patientsFound").style.display = "none";
		
		var inputs = document.getElementsByTagName("input");
		for (var i=0; i<inputs.length; i++) {
			var input = inputs[i];
			if (input.type == "checkbox") {
				selectAttribute(input);
			}
		}
		
		DWRUtil.useLoadingMessage();
	};
	
	onload=init;
	
	var getCheckbox = function(patient) {
		if (typeof patient == "string") return "";
		var td = document.createElement("td");
		var input = document.createElement("input");
		input.type = "checkbox";
		input.name = "patientId";
		input.value = patient.patientId;
		td.appendChild(input);
		return td;
	}
	
	var getPatientId = function(patient) {
		if (typeof patient == "string") return "";
		return patient.patientId;
	}
	
	var fillTable = function(patients) {
		var funcs = [getCheckbox, getPatientId].concat(customCellFunctions);
		
		var customRowOptions = {'rowCreator':rowCreator};
		
		$("patientsFound").style.display = "";
		
		$("patientListSize").innerHTML = patients.length;
		
		resetForm();
		DWRUtil.removeAllRows(tbody);
		DWRUtil.addRows(tbody, patients, funcs, customRowOptions);
	}
	
	function selectAttribute(input) {
		if (input.checked == true) {
			// add the checked box
			var found = false;
			for (var i = 0; i < searchOn.length; i++) {
				if (searchOn[i] == input.value)
					found = true;
			}
			if (!found)
				searchOn.push(input.value);
		}
		else {
			// remove the checked box
			for (var i = 0; i < searchOn.length; i++) {
				if (searchOn[i] == input.value)
					searchOn[i] = null;
			}
		}
		return true;
	}
	
	function showSearch() {
		if (searchOn.length > 1) {
			savedSearchOn = searchOn;
			DWRPatientService.findDuplicatePatients(fillTable, searchOn, null);
		}
	}
		
	window.onload = init;
	
</script>

<style>
	.searchIndex { display: none; }
</style>

<h2><spring:message code="Patient.merge.title"/></h2>

<spring:message code="Patient.merge.search_on"/>: <br/>
<input type="checkbox" name="attr" id="identifier" value="identifier" onclick="selectAttribute(this)" onactivate="selectAttribute(this)"/><label for="identifier"><spring:message code="Patient.identifier"/></label> <br/>
<input type="checkbox" name="attr" id="gender" value="gender" onclick="selectAttribute(this)" onactivate="selectAttribute(this)"/><label for="gender"><spring:message code="Patient.gender"/></label> <br/>
<input type="checkbox" name="attr" id="tribe" value="tribe" onclick="selectAttribute(this)" onactivate="selectAttribute(this)"/><label for="tribe"><spring:message code="Patient.tribe"/></label> <br/>
<input type="checkbox" name="attr" id="birthdate" value="birthdate" onclick="selectAttribute(this)" onactivate="selectAttribute(this)"/><label for="birthdate"><spring:message code="Patient.birthdate"/></label> <br/>
<input type="checkbox" name="attr" id="givenName" value="givenName" onclick="selectAttribute(this)" onactivate="selectAttribute(this)"/><label for="givenName"><spring:message code="PatientName.givenName"/></label> <br/>
<input type="checkbox" name="attr" id="middleName" value="middleName" onclick="selectAttribute(this)" onactivate="selectAttribute(this)"/><label for="middleName"><spring:message code="PatientName.middleName"/></label> <br/>
<input type="checkbox" name="attr" id="familyName" value="familyName" onclick="selectAttribute(this)" onactivate="selectAttribute(this)"/><label for="familyName"><spring:message code="PatientName.familyName"/></label> <br/>
<br/>
<input type="checkbox" name="attr" id="includeVoided" value="includeVoided" onclick="selectAttribute(this)" onactivate="selectAttribute(this)"/><label for="includeVoided"><spring:message code="Patient.merge.includeVoided"/></label> <br/>

<br />
<input type="button" value='<spring:message code="general.search"/>' onclick="showSearch()" /><br />

<i>(<spring:message code="Patient.merge.minimum"/>)</i>

<br /><br />

<form action="mergePatients.form" id="patientsFound">
	<span id="patientListSize"></span> <spring:message code="Patient.returned"/>.
	<spring:message code="Patient.merge.select"/>
	<table cellspacing="0" cellpadding="1" width="100%">
		<tbody id="patientTableBody">
		</tbody>
	</table>
	<input type="submit" value='<spring:message code="general.continue"/>'/>
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>