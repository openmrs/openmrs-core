<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Patients" otherwise="/login.htm" redirect="/admin/patients/findDuplicatePatients.htm"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.PatientSearch");

	var searchWidget;
	var searchOn;
	
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
	
	function showSearch(e) {
		searchWidget.findObjects(e);
	}
	
	dojo.addOnLoad( function() {
		
		searchWidget = dojo.widget.manager.getWidgetById("pSearch");
		
		searchOn = new Array();
		$('patientsFound').style.display = "none";
		
		var inputs = document.getElementsByTagName("input");
		for (var i=0; i<inputs.length; i++) {
			var input = inputs[i];
			if (input.type == "checkbox") {
				selectAttribute(input);
			}
		}
		
		var row = searchWidget.headerRow;
		var th = document.createElement("th");
		th.innerHTML = "Patient Id";
		row.insertBefore(th, row.firstChild.nextSibling);
		
		searchWidget.showAddPatientLink = false;
		
		searchWidget.getCellFunctions = function() {
			//alert("super: " + dojo.widget.openmrs.PatientSearch.prototype);
			var arr = dojo.widget.openmrs.PatientSearch.prototype.getCellFunctions();
			
			arr.splice(1, 0, getCheckbox);
			arr.splice(2, 0, getPatientId);
			
			return arr;
		};
		
		dojo.event.topic.subscribe("pSearch/objectsFound", 
			function(msg) {	
				$("patientListSize").innerHTML = msg.objs.length;
				$('patientsFound').style.display = "";
			}
		);
		
		searchWidget.findObjects = function(phrase) {
			if (searchOn.length > 1)
				DWRPatientService.findDuplicatePatients(searchOn, searchWidget.simpleClosure(searchWidget, "doObjectsFound"));
		}
		
	});
	
</script>

<style>
	.searchIndex, .searchIndexHighlight { display: none; }
	#searchNode, #searchInfoBar  { display: none; }
</style>

<h2><spring:message code="Patient.merge.title"/></h2>

<spring:message code="Patient.merge.search_on"/>: <br/>
<input type="checkbox" name="attr" id="identifier" value="identifier" onclick="selectAttribute(this)" onactivate="selectAttribute(this)"/><label for="identifier"><spring:message code="Patient.identifier"/></label> <br/>
<input type="checkbox" name="attr" id="gender" value="gender" onclick="selectAttribute(this)" onactivate="selectAttribute(this)"/><label for="gender"><spring:message code="Person.gender"/></label> <br/>
<input type="checkbox" name="attr" id="birthdate" value="birthdate" onclick="selectAttribute(this)" onactivate="selectAttribute(this)"/><label for="birthdate"><spring:message code="Person.birthdate"/></label> <br/>
<input type="checkbox" name="attr" id="givenName" value="givenName" onclick="selectAttribute(this)" onactivate="selectAttribute(this)"/><label for="givenName"><spring:message code="PersonName.givenName"/></label> <br/>
<input type="checkbox" name="attr" id="middleName" value="middleName" onclick="selectAttribute(this)" onactivate="selectAttribute(this)"/><label for="middleName"><spring:message code="PersonName.middleName"/></label> <br/>
<input type="checkbox" name="attr" id="familyName" value="familyName" onclick="selectAttribute(this)" onactivate="selectAttribute(this)"/><label for="familyName"><spring:message code="PersonName.familyName"/></label> <br/>
<br/>
<input type="checkbox" name="attr" id="includeVoided" value="includeVoided" onclick="selectAttribute(this)" onactivate="selectAttribute(this)"/><label for="includeVoided"><spring:message code="Patient.merge.includeVoided"/></label> <br/>

<br />
<input type="button" value='<spring:message code="general.search"/>' onclick="showSearch(event)" /><br />

<i>(<spring:message code="Patient.merge.minimum"/>)</i>

<br /><br />

<form action="mergePatients.form" id="patientsFound">
	<span id="patientListSize"></span> <spring:message code="Patient.returned"/>.
	<spring:message code="Patient.merge.select"/>
	<div dojoType="PatientSearch" widgetId="pSearch" inputId="searchNode" tableHeight="1000"></div>
	<input type="submit" value='<spring:message code="general.continue"/>'/>
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>