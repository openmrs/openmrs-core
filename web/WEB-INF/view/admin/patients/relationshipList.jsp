<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Relationships" otherwise="/login.htm" redirect="/admin/patients/relationshipList.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script src='<%= request.getContextPath() %>/scripts/validation.js'></script>

<script src='<%= request.getContextPath() %>/dwr/interface/DWRPatientService.js'></script>
<script src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script src='<%= request.getContextPath() %>/scripts/openmrsSearch.js'></script>
<script src='<%= request.getContextPath() %>/scripts/patientSearch.js'></script>

<script>
	
	function showSearch() {
		patientListing.style.display = "none";
		searchBox.focus();
	}
	
	function onSelect(arr) {
		document.location = "relationship.form?patientId=" + arr[0].patientId;
	}
	
	function findObjects(text) {
		DWRPatientService.findPatients(fillTable, text, $('includeVoided').checked);
		patientListing.style.display = "";
		return false;
	}
	
</script>

<style>
	tr th#patientGender, tr th#patientAge, .patientGender, .patientAge {
		text-align: center;
	}
</style>

<h2><spring:message code="Relationship.title"/></h2>

<div id="findPatient">
	<b class="boxHeader"><spring:message code="Patient.find"/></b>
	<div class="box">
		<form id="findPatientForm" onSubmit="return search(searchBox, event, includeVoided.checked, 0);">
			<table>
				<tr>
					<td><spring:message code="formentry.searchBox"/></td>
					<td><input type="text" id="searchBox" onKeyUp="search(this, event, includeVoided.checked, 400)"></td>
					<td><spring:message code="formentry.includeVoided"/><input type="checkbox" id="includeVoided" onClick="search(searchBox, event, includeVoided.checked, 0); searchBox.focus();" /></td>
				</tr>
			</table>
		</form>
		<div id="patientListing">
			<table id="patientTable" cellspacing="0" cellpadding="1">
			 <thead>
				 <tr>
				 	<th class="searchIndex"> </th>
				 	<th class="patientIdentifier"><spring:message code="Patient.identifier"/></th>
				 	<th><spring:message code="PatientName.givenName"/></th>
				 	<th><spring:message code="PatientName.middleName"/></th>
				 	<th><spring:message code="PatientName.familyName"/></th>
				 	<th id='patientAge'> <spring:message code="Patient.age"/> </th>
				 	<th id='patientGender'> <spring:message code="Patient.gender"/> </th>
				 	<th><spring:message code="Patient.tribe"/></th>
				 	<th></th>
				 	<th><spring:message code="Patient.birthdate"/></th>
				 </tr>
			 </thead>
			 <tbody id="patientTableBody">
			 </tbody>
			</table>
		</div>
	</div>
</div>

<div id="patientSummary">
</div>


<script>

	var patientListing	= document.getElementById("patientListing");
	var searchBox		= document.getElementById("searchBox");
	
	showSearch();
	
	<request:existsAttribute name="patientId">
		var pats = new Array();
		var pats[0] = new Object();
		pats[0].patientId = request.getAttribute("patientId");
		onSelect(pats);
	</request:existsAttribute>
	
	// creates back button functionality
	if (searchBox.value != "")
		searchBoxChange(searchBox, event, $('includeVoided').checked, 0);
	
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>
