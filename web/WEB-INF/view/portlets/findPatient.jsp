<%@ include file="/WEB-INF/template/include.jsp" %>

<c:if test="${model.authenticatedUser != null}">
	<c:choose>
		<c:when test="${model.size == 'compact'}">
			<form method=get action="${model.postURL}">
				<spring:message code="Widget.findOnePatient.find" />
				<input type="text" name="phrase" />
				<input type="submit" value="<spring:message code="general.searchButton" />" />
			</form>
		</c:when>
		<c:when test="${model.size == 'full'}">
			
			<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/formentry/index.htm" />

			<openmrs:htmlInclude file="/scripts/dojoConfig.js"></openmrs:htmlInclude>
			<openmrs:htmlInclude file="/scripts/dojo/dojo.js"></openmrs:htmlInclude>

			<script type="text/javascript">
				dojo.require("dojo.widget.openmrs.PatientSearch");
				
				dojo.addOnLoad( function() {
					
					searchWidget = dojo.widget.manager.getWidgetById("pSearch");			
					
					dojo.event.topic.subscribe("pSearch/select", 
						function(msg) {
							if (msg.objs[0].patientId)
								document.location = "${model.postURL}?patientId=" + msg.objs[0].patientId + "&phrase=" + searchWidget.text;
							else if (msg.objs[0].href)
								document.location = msg.objs[0].href;
						}
					);
					
					searchWidget.inputNode.select();
					changeClassProperty("description", "display", "none");
				});
				
			</script>
			
			<h3><spring:message code="Patient.find"/></h3>
			
			<div id="findPatient">
				<b class="boxHeader"><spring:message code="formentry.step1"/></b>
				<div class="box">
					<div dojoType="PatientSearch" widgetId="pSearch" searchLabel='<spring:message code="formentry.searchBox"/>' showVerboseListing="true" patientId='<request:parameter name="patientId"/>' searchPhrase='<request:parameter name="phrase"/>'></div>
				</div>
			</div>
			
		</c:when>
		<c:otherwise>
			ERROR! unknown size '${model.size}' in FindOnePatientWidget
		</c:otherwise>
	</c:choose>
</c:if>