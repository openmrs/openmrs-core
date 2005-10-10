<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.User" %>

<openmrs:require privilege="Create Patient" otherwise="/openmrs/login.jsp" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<script src='/openmrs/dwr/interface/DWRPatientService.js'></script>
<script src='/openmrs/dwr/engine.js'></script>
<script src='/openmrs/dwr/util.js'></script>

<script>
	
	function updatePatients() {
	    
	    document.getElementById("patientTable").style.display = "table";
	    document.getElementById("selectForm").style.display = "none";
	    DWRUtil.removeAllRows("patientBody");
	    var identifier = document.getElementById("identifier").value;
	    var givenName = document.getElementById("givenName").value;
	    var familyName = document.getElementById("familyName").value;
	    DWRPatientService.findPatients(fillTable, identifier, givenName, familyName);
	}
	
	var getId			= function(obj) { return obj.patientId };
	var getIdentifier	= function(obj) { 
			var str = "";
			str += "<a href=javascript:selectPatient(" + obj.patientId + ")>";
			str += obj.identifier;
			str += "</a>";
			return str;
		};
	var getGivenName	= function(obj) { return obj.givenName  };
	var getFamilyName	= function(obj) { return obj.familyName };
	var getGender		= function(obj) { return obj.gender };
	var getRace			= function(obj) { return obj.race };
	var getBirthdate	= function(obj) { 
			var str = '';
					str += obj.birthdate.getMonth() + '-';
			str += obj.birthdate.getDate() + '-'
			str += (obj.birthdate.getYear() + 1900);
			
			if (obj.birthdateEstimated)
				str += " (?)"
			
			return str;
		};
	var getMothersName  = function(obj) { return obj.mothersName  };
	
	function fillTable(patientListItem) {
	    DWRUtil.addRows("patientBody", patientListItem, [ getId, getIdentifier, getGivenName, getFamilyName, getGender, getRace, getBirthdate, getMothersName]);
	}
	
	function selectPatient(patientId) {
		document.getElementById("patientTable").style.display = "none";
		var selectForm = document.getElementById("selectForm");
		selectForm.style.display = "block";
		selectForm.patientId.value = patientId;
	}

</script>

<h2>Create Patient</h2>

<b></b>
<br>
<form method="post">
	<table>
		<tr>
			<td>Identifier</td>
			<td><input type="text" name="identifier" id="identifier"></td>
			<td>
				<select name="identifierType">
				</select>
			</td>
		</tr>
		<tr>
			<td>First Name</td>
			<td><input type="text" id="givenName"></td>
		</tr>
		<tr>
			<td>Last Name</td>
			<td><input type="text" id="familyName"></td>
		</tr>
	</table>
	<input type="submit" value="Search" >
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>
