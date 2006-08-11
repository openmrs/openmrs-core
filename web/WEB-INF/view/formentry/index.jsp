<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/formentry/index.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/dojo/dojo.js"></script>

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.PatientSearch");
	
	dojo.addOnLoad( function() {
		
		searchWidget = dojo.widget.manager.getWidgetById("pSearch");			
		
		dojo.event.topic.subscribe("pSearch/select", 
			function(msg) {
				if (msg.objs[0].patientId)
					document.location = "patientDashboard.form?patientId=" + msg.objs[0].patientId + "&phrase=" + searchWidget.text;
				else if (msg.objs[0].href)
					document.location = arr[0].href;
			}
		);
		
		searchWidget.inputNode.select();
		changeClassProperty("description", "display", "none");
	});
	
</script>

<openmrs:portlet url="patientSet" id="patientSetHeaderBox" size="compact" parameters="linkUrl=patientDashboard.form|allowRemove=true|allowClear=true|mutable=true|droppable=true"/>

<h3><spring:message code="formentry.title"/></h3>

<div id="findPatient">
	<b class="boxHeader"><spring:message code="formentry.step1"/></b>
	<div class="box">
		<div dojoType="PatientSearch" widgetId="pSearch" searchLabel='<spring:message code="formentry.searchBox"/>' showVerboseListing="true" patientId='<request:parameter name="patientId"/>' searchPhrase='<request:parameter name="phrase"/>'></div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>