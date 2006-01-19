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
		var links = new Array();
		if (patients.length < 1) {
			if (patientName != "" && birthyear != "" && gender != "") {
				var href = getTextLink(document.createElement("a")).href;
				document.location = href;
			}
			else {
				links.push(noPatientsFound);
				fillTable([]);	//this call sets up the table/info bar
			}
		}
		else {
			links.push(patientsFound);	//setup links for appending to the end
			fillTable(patients);		//continue as normal
		}
		DWRUtil.addRows(objectHitsTableBody, links, [getNumber, getTextLink]);
		setTimeout("showHighlight()", 0);	//assumption for this page only: we're only here because the enter key was pressed
	}
	
	function onSelect(patients) {
		if (patients[0].patientId == null) // this is a [no]PatientsFound link
			document.location = patients[0].href;
		else
			document.location = "${pageContext.request.contextPath}/formentry/index.htm?phrase=" + patients[0].identifier;
	}
	
	var getTextLink = function(link) {
		link.href = "newPatient.form?name=" + patientName + "&birthyear=" + birthyear + "&gender=" + gender;
		link.className = "searchHit";
		return link;
	}
	
	var init = function() {

			form = $("patientForm");
			noPatientsFound = document.createElement("a");
			patientsFound   = document.createElement("a");
			noPatientsFound.innerHTML = "No Patients Found.  Select to add a new Patient";
			patientsFound.innerHTML   = "Add New Patient";
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

<br />
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
		<table>
			<tbody id="patientTableBody">
			</tbody>
		</table>
	</div>
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>