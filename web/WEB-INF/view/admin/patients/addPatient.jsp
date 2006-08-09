<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/admin/patients/addPatient.htm"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/dojo/dojo.js"></script>

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.PatientSearch");

	var searchWidget;
	var patientName;
	var birthyear;
	var age;
	var gender;
	var inputChanged = true;
	
	dojo.addOnLoad( function() {
		
		searchWidget = dojo.widget.manager.getWidgetById("pSearch");			
		
		dojo.event.topic.subscribe("pSearch/select", 
			function(msg) {
				document.location = "${pageContext.request.contextPath}/formentry/patientSummary.form?patientId=" + msg.objs[0].patientId;
			}
		);
		
		dojo.event.topic.subscribe("pSearch/objectsFound", 
			function(msg) {
				var patients = msg.objs;
				if (patients.length < 1) {
					if (patientName != "" && (birthyear != "" || age != "") && gender != "")
						document.location = getHref();
					else
						patients.push(noPatientsFound);
				}
				else {
					patients.push(patientsFound);	//setup links for appending to the end
				}
			}
		);
		
		searchWidget.doFindObjects = function(phrase) {
			patientName = phrase.toString();
			birthyear = $("birthyear").value.toString();
			age = $("age").value.toString();
			gender = "";
			var options = document.getElementsByTagName("input");
			for (var i=0; i<options.length;i++) {
				if (options[i].type == 'radio' && options[i].name=='gender' && options[i].checked == true)
					gender = options[i].value.toString();
			}
			DWRPatientService.getSimilarPatients(this.simpleClosure(this, "doObjectsFound"), patientName, birthyear, age, gender);
			return false;
		};
		
		searchWidget.allowNewSearch = function() {
			if (inputChanged == true) {
				inputChanged = false;
				if (this.text != "")
					this.lastPhraseSearched = "";
				return true;
			}
			return false;
		};
		
		searchWidget.allowAutoJump = function() { return false; };
		
		$("patientName").focus();
		noPatientsFound = "<a href='#' class='searchHit' onclick='document.location=getHref()'>No Patients Found.  Select to add a new Patient</a>";
		patientsFound = "<a href='#' class='searchHit' onclick='document.location=getHref()'>Add New Patient</a>";
		
	});
	
	var getHref = function() {
		return "newPatient.form?name=" + patientName + "&birthyear=" + birthyear + "&gender=" + gender + "&age=" + age;
	}
	
	function cancel() {
		window.location = "${pageContext.request.contextPath}/formentry";
	}
	
</script>

<style>
	tr th#patientGender, tr th#patientAge, .patientGender, .patientAge {
		text-align: center;
	}
	
	.openmrsSearchDiv {
		position: absolute;
		margin-top: 6em;
		margin-left: -5em;
		margin-right: 1em;
		z-index: 100;
	}
</style>

<h2><spring:message code="Patient.title"/></h2>

<form method="get" action="" onSubmit="return search(patientName, null, false, 0);" id="patientForm">
	<table>
		<tr>
			<td><spring:message code="Patient.name"/></td>
			<td>
				<div dojoType="PatientSearch" widgetId="pSearch" inputId="patientName" inputName="patientName" tableHeight="600" allowAutoList="false"></div>
			</td>
		</tr>
		<tr>
			<td><spring:message code="Patient.birthyear"/></td>
			<td>
				<input type="text" name="birthyear" id="birthyear" size="5" value="" onChange="inputChanged=true;" onFocus="searchWidget.exitNumberMode()" />
				<spring:message code="Patient.age.or"/>
				<input type="text" name="age" id="age" size="5" value="" onChange="inputChanged=true;" onFocus="searchWidget.exitNumberMode()" />
			</td>
		</tr>
		<tr>
			<td><spring:message code="Patient.gender"/></td>
			<td>
				<openmrs:forEachRecord name="gender">
					<input type="radio" name="gender" id="${record.key}" value="${record.key}" <c:if test="${record.key == status.value}">checked</c:if> onclick="inputChanged=true" onFocus="searchWidget.exitNumberMode()" /><label for="${record.key}"> <spring:message code="Patient.gender.${record.value}"/> </label>
				</openmrs:forEachRecord>
			</td>
		</tr>
	</table>
	
	<input type="button" value="<spring:message code="general.continue"/>" onClick="searchWidget.search(event)"/> &nbsp; &nbsp; 
	<input type="button" value="<spring:message code="general.cancel" />" name="action" id="cancelButton" onClick="return cancel()">
	
	<br />
	<br />
	
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>