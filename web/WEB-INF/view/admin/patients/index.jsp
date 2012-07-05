<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/admin/patients/index.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.PatientSearch");
	
	dojo.addOnLoad( function() {
		
		searchWidget = dojo.widget.manager.getWidgetById("pSearch");			
		
		dojo.event.topic.subscribe("pSearch/select", 
			function(msg) {
				document.location = "patient.form?patientId=" + msg.objs[0].patientId;
			}
		);
		
		searchWidget.inputNode.select();
		changeClassProperty("description", "display", "none");
		
		searchWidget.addPatientLink =  "<a href='${pageContext.request.contextPath}/admin/person/addPerson.htm?postURL=patient.form'><openmrs:message javaScriptEscape="true" code="Patient.addNew"/></a>";
		
	});
	
</script>

<h2><openmrs:message code="Patient.title"/></h2>

<a href="${pageContext.request.contextPath}/admin/person/addPerson.htm?personType=patient&viewType=edit"><openmrs:message code="Patient.create"/></a><br/><br/>

<div id="findPatient">
	<b class="boxHeader"><openmrs:message code="Patient.find"/></b>
	<div class="box">
		<div dojoType="PatientSearch" widgetId="pSearch" inputName="patientName" searchLabel='<openmrs:message code="Patient.searchBox"/>' patientId='<request:parameter name="patientId" />' showIncludeVoided='true'></div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>