<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/admin/patients/addPatient.htm"/>

<%@ include file="/WEB-INF/template/header.jsp" %>

<c:choose>
	<c:when test="${empty param.name}">
		
		<h2><spring:message code="Patient.find"/></h2>
		<openmrs:portlet id="createPatient" url="addPatientForm" parameters="postURL=${param.postURL}" />
		<script type="text/javascript">
			document.getElementById("patientName").focus();
		</script>
	</c:when>
	<c:otherwise>
		
		<openmrs:globalProperty key="use_patient_attribute.tribe" defaultValue="false" var="showTribe"/>
		<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />
		
		<script type='text/javascript' src='/amrs/dwr/interface/DWRPatientService.js'></script>
		
		<script type="text/javascript">
			dojo.require("dojo.widget.openmrs.PatientSearch");
			
			dojo.addOnLoad( function() {
		
				searchWidget = dojo.widget.manager.getWidgetById("pSearch");			
				
				dojo.event.topic.subscribe("pSearch/select", 
					function(msg) {
						document.location = "${pageContext.request.contextPath}/patientDashboard.form?patientId=" + msg.objs[0].patientId;
					}
				);
				
				searchWidget.allowNewSearch = function() {
					return false;
				};
				
				var patientName = "${param.name}";
				var birthyear = "${param.birthyear}";
				var age = "${param.age}";
				var gender = "${param.gndr}";
				DWRPatientService.getSimilarPatients(searchWidget.simpleClosure(searchWidget, "doObjectsFound"), patientName, birthyear, age, gender);
				
				searchWidget.allowAutoJump = function() { return false; };
			});
			
			function onContinueClick() {
				var href = "newPatient.form?name=${param.name}&birthyear=${param.birthyear}&gndr=${param.gndr}&age=${param.age}";
				document.location = href;
			}
		</script>
		
		<style type="text/css">
			#openmrsSearchTable th {
				text-align: left;
			}
			#pSearchInput {
				display: none;
			}
		</style>
		
		<h2><spring:message code="Patient.search.similarPatients"/></h2>
		<b id="similarPatientsIinstructions"><spring:message code="Patient.search.similarPatients.instructions"/></b>
		
		<br/><br/>
		
		<div dojoType="PatientSearch" widgetId="pSearch" inputId="pSearchInput" showAddPatientLink='false'></div>
		
		<br/>
		
		<input type="button" value='<spring:message code="Patient.search.similarPatients.notOnList"/>' onClick="return onContinueClick()" />
		&nbsp;
		<input type="button" value='<spring:message code="general.back"/>' onClick="history.go(-1)" />
		
		<br/><br/>
		
	</c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/template/footer.jsp" %>