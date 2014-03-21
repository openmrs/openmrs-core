<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Edit Patients" otherwise="/login.htm"
	redirect="/admin/patients/findDuplicatePatients.htm" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.PatientSearch");

	var searchWidget;
	var searchOn;

	var getCheckbox = function(patient) {
		if (typeof patient == "string")
			return "";
		var td = document.createElement("td");
		var input = document.createElement("input");
		input.type = "checkbox";
		input.name = "patientId";
		input.value = patient.patientId;
		td.appendChild(input);
		return td;
	}

	var getPatientId = function(patient) {
		if (typeof patient == "string")
			return "";
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
		} else {
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

	dojo.addOnLoad(function() {

		searchWidget = dojo.widget.manager.getWidgetById("pSearch");

		searchOn = new Array();
		jQuery('#patientsFound').css('display', "none");

		var inputs = document.getElementsByTagName("input");
		for (var i = 0; i < inputs.length; i++) {
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
			var arr = dojo.widget.openmrs.PatientSearch.prototype
					.getCellFunctions();

			arr.splice(1, 0, getCheckbox);
			arr.splice(2, 0, getPatientId);

			return arr;
		};

		dojo.event.topic.subscribe("pSearch/objectsFound", function(msg) {

			if (msg.objs.length == 1) {

				jQuery("#patientListSize").html(0);
				dojo.style.hide("patientsSelect");
				groupDuplicateResults(searchOn, msg.objs);
			} else {
				jQuery("#patientListSize").html(msg.objs.length);
				dojo.style.show("patientsSelect");
				groupDuplicateResults(searchOn, msg.objs);
			}

			jQuery('#patientsFound').css('display', "");

		});

		function subset(arr1, arr2) //true if arr1 subset of arr2 else false?
		{
			var check = 0;
			for (var i = 0; i < arr1.length; i++) {
				for (var j = 0; j < arr2.length; j++) {
					if (arr1[i] == arr2[j]) {
						check++;
					}
				}
			}
			alert(check);
			if (check == arr1.length)
				return true;
			else
				return false;
		}

		function groupDuplicateResults(search, result) {

			console.log(search);
			//console.log(result);

			var numPatients = jQuery("#patientListSize").html();

			if (numPatients == '0') {
				return;
			}

			var patients = new Array();

			for (var i = 0; i < result.length; i++) {

				var patient = new Object();
				patient.identifier = result[i].identifier;
				patient.gender = result[i].gender;
				patient.birthdate = result[i].birthdate;
				patient.given = result[i].givenName;
				patient.middle = result[i].middleName;
				patient.family = result[i].familyName;
				patients.push(patient);

			}

			var map1 = new Object(); //map for gender
			var grp1 = new Array();
			var map2 = new Object(); //map for givenName
			var grp2 = new Array();
			var map3 = new Object(); //map for identifier
			var grp3 = new Array();
			var map4 = new Object(); //map for birthdate
			var grp4 = new Array();
			var map5 = new Object(); //map for middleName
			var grp5 = new Array();
			var map6 = new Object(); //map for familyName
			var grp6 = new Array();

			var map = new Array();
			var grp = new Array();

			for (var i = 0; i < searchOn.length; i++) {

				switch (searchOn[i]) {
				case 'gender':
					for (var j = 0; j < patients.length; j++) {

						if (!map1[patients[j].gender]) {
							map1[patients[j].gender] = new Array();
							map1[patients[j].gender].push(j);
						} else {
							map1[patients[j].gender].push(j);
						}
					}
					var keys = Object.keys(map1);

					for ( var k in keys) {
						grp1.push(map1[keys[k]]);
					}

					map.push(map1);
					grp.push(grp1);
					console.log(keys);
					console.log(grp);
					break;

				case 'givenName':

					for (var j = 0; j < patients.length; j++) {

						if (!map2[patients[j].given]) {
							map2[patients[j].given] = new Array();
							map2[patients[j].given].push(j);
						} else {
							map2[patients[j].given].push(j);
						}
					}
					var keys = Object.keys(map2);

					for ( var k in keys) {
						grp2.push(map2[keys[k]]);
					}

					map.push(map2);
					grp.push(grp2);

					console.log(keys);
					console.log(grp);

					break;

				case 'identifier':

					for (var j = 0; j < patients.length; j++) {

						if (!map3[patients[j].identifier]) {
							map3[patients[j].identifier] = new Array();
							map3[patients[j].identifier].push(j);
						} else {
							map3[patients[j].identifier].push(j);
						}
					}

					var keys = Object.keys(map3);
					for ( var k in keys) {
						grp3.push(map3[keys[k]]);
					}

					map.push(map3);
					grp.push(grp3);

					console.log(grp);
					break;

				case 'birthdate':

					for (var j = 0; j < patients.length; j++) {

						if (!map4[patients[j].birthdate]) {
							map4[patients[j].birthdate] = new Array();
							map4[patients[j].birthdate].push(j);
						} else {
							map4[patients[j].birthdate].push(j);
						}
					}

					var keys = Object.keys(map4);
					for ( var k in keys) {
						grp4.push(map4[keys[k]]);
					}

					map.push(map4);
					grp.push(grp4);
					console.log(grp);
					break;

				case 'middleName':

					for (var j = 0; j < patients.length; j++) {

						if (!map5[patients[j].middle]) {
							map5[patients[j].middle] = new Array();
							map5[patients[j].middle].push(j);
						} else {
							map5[patients[j].middle].push(j);
						}
					}
					var keys = Object.keys(map5);
					for ( var k in keys) {
						grp5.push(map5[keys[k]]);
					}

					map.push(map5);
					grp.push(grp5);
					console.log(grp);
					break;

				case 'familyName':

					for (var j = 0; j < patients.length; j++) {

						if (!map6[patients[j].family]) {
							map6[patients[j].family] = new Array();
							map6[patients[j].family].push(j);
						} else {
							map6[patients[j].family].push(j);
						}
					}

					var keys = Object.keys(map6);
					for ( var k in keys) {
						grp6.push(map6[keys[k]]);
					}

					map.push(map6);
					grp.push(grp6);
					console.log(grp);
					break;

				default:
					//alert('default encountered');	
				}
			}

			//console.log(map);
			//console.log(grp);

			if (search[1] == null) {
				alert('Please select minimum two attributes for grouping');
				return;
			}

			var finalgrp = new Array();

			for (var i = 0; i < 1; i++) {

				for (var j = 0; j < grp[i].length; j++) {

					for (var k = 0; k < grp[i + 1].length; k++) {

						if (i == 0 || finalgrp.length == 0) {
							var res = intersect(grp[i][j], grp[i + 1][k]);
							if (res != -1) {
								finalgrp.push(res);
							}
						}
					}
				}
			}
			//console.log('finalgrp..');
			//console.log(finalgrp);

			var finalgrp2 = new Array();
			for (var j = 2; j < grp.length; j++) {

				for (var k = 0; k < grp[j].length; k++) {

					for (var i = 0; i < finalgrp.length; i++) {

						var res = intersect(finalgrp[i], grp[j][k]);

						if (res != -1)
							finalgrp2.push(res);
					}
				}
				finalgrp = finalgrp2;
				finalgrp2 = [];
			}

			//console.log('finalgrp..');
			var groups = "<div style='background:gold'> Groups of patients identifiers that can be merged based on the selected attributes:</div><br>";
			for (i in finalgrp) {
				//groups += '{' + finalgrp[i] + '}';
				groups += 'Group ' + (parseInt(i)+1) + ': { ';
				for (j in finalgrp[i]) {
					if (j < finalgrp[i].length - 1)
						groups += patients[finalgrp[i][j]].identifier + ', ';
					else
						groups += patients[finalgrp[i][j]].identifier;
				}
				groups += ' } ' + '<br>';
			}

			//alert(groups);

			jQuery("#patientsToMerge").html(groups);
		}

		function intersect(arr1, arr2) {
			var intgrp = new Array();

			for (var i = 0; i < arr1.length; i++) {
				for (var j = 0; j < arr2.length; j++) {
					if (arr1[i] == arr2[j]) {
						intgrp.push(arr1[i]);
					}
				}
			}
			if (intgrp.length == 0)
				return -1;
			else {
				console.log(intgrp);
				return intgrp;

			}
		}

		searchWidget.findObjects = function(phrase) {
			if (searchOn.length > 1)
				DWRPatientService.findDuplicatePatients(searchOn, searchWidget
						.simpleClosure(searchWidget, "doObjectsFound"));
		}

	});
</script>

<style>
.searchIndex,.searchIndexHighlight {
	display: none;
}

#searchNode,#searchInfoBar {
	display: none;
}
</style>

<h2>
	<openmrs:message code="Patient.merge.title" />
</h2>

<openmrs:message code="Patient.merge.search_on" />
<span class="required">*</span>
:
<br />
<input type="checkbox" name="attr" id="identifier" value="identifier"
	onclick="selectAttribute(this)" onactivate="selectAttribute(this)" />
<label for="identifier"><openmrs:message
		code="Patient.identifier" /></label>
<br />
<input type="checkbox" name="attr" id="gender" value="gender"
	onclick="selectAttribute(this)" onactivate="selectAttribute(this)" />
<label for="gender"><openmrs:message code="Person.gender" /></label>
<br />
<input type="checkbox" name="attr" id="birthdate" value="birthdate"
	onclick="selectAttribute(this)" onactivate="selectAttribute(this)" />
<label for="birthdate"><openmrs:message code="Person.birthdate" /></label>
<br />
<input type="checkbox" name="attr" id="givenName" value="givenName"
	onclick="selectAttribute(this)" onactivate="selectAttribute(this)" />
<label for="givenName"><openmrs:message
		code="PersonName.givenName" /></label>
<br />
<input type="checkbox" name="attr" id="middleName" value="middleName"
	onclick="selectAttribute(this)" onactivate="selectAttribute(this)" />
<label for="middleName"><openmrs:message
		code="PersonName.middleName" /></label>
<br />
<input type="checkbox" name="attr" id="familyName" value="familyName"
	onclick="selectAttribute(this)" onactivate="selectAttribute(this)" />
<label for="familyName"><openmrs:message
		code="PersonName.familyName" /></label>
<br />
<br />
<input type="checkbox" name="attr" id="includeVoided"
	value="includeVoided" onclick="selectAttribute(this)"
	onactivate="selectAttribute(this)" />
<label for="includeVoided"><openmrs:message
		code="Patient.merge.includeVoided" /></label>
<br />

<br />
<input type="button" value='<openmrs:message code="general.search"/>'
	onclick="showSearch(event)" />
<br />

<i>(<openmrs:message code="Patient.merge.minimum" />)
</i>

<br />
<br />

<div id="mergePatientPopup">
	<div id="mergePatientPopupLoading">
		<openmrs:message code="general.loading" />
	</div>
	<iframe id="mergePatientPopupIframe" name="mergePatientPopupIframe"
		width="100%" height="100%" marginWidth="0" marginHeight="0"
		frameBorder="0" scrolling="auto"></iframe>
</div>
<script type="text/javascript">
	$j(document).ready(function() {
		$j('#mergePatientPopup').dialog({
			title : '<openmrs:message code="Patient.merge.title"/>',
			autoOpen : false,
			draggable : false,
			resizable : false,
			width : '95%',
			modal : true,
			open : function(a, b) {
				$j('#mergePatientPopupLoading').show();
			}
		});
		$j("#mergePatientPopupIframe").load(function() {
			$j('#mergePatientPopupLoading').hide();
		});
	});

	function showMergePatientPopup() {
		$j('#mergePatientPopup').dialog('option', 'height',
				$j(window).height() - 50).dialog('open');
		return true;
	}
</script>

<form action="mergePatients.form" id="patientsFound"
	target="mergePatientPopupIframe">
	<span id="patientListSize"></span>
	<openmrs:message code="Patient.returned" />
	. <span id="patientsSelect"><openmrs:message
			code="Patient.merge.select" />
		<p>
			<span id="patientsToMerge" style="background-color: yellow;">
				<openmrs:message code="Patient.to.merge" />
			</span>
		<div dojoType="PatientSearch" widgetId="pSearch" inputId="searchNode"
			tableHeight="1000"></div> </span> <input type="hidden" name="modalMode"
		value="true" /> <input type="submit"
		value='<openmrs:message code="general.continue"/>'
		onclick="showMergePatientPopup();" />
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>