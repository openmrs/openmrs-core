<%@ include file="/WEB-INF/template/include.jsp" %>

<c:if test="${model.authenticatedUser != null}">
	<c:choose>
		<c:when test="${model.size == 'compact'}">
			<form method=get action="${model.postURL}">
				<spring:message code="Navigation.findPatient" />
				<input type="text" name="phrase" value="<request:parameter name="phrase"/>"/>
				<input type="submit" value="<spring:message code="general.searchButton" />" />
			</form>
		</c:when>
		<c:when test="${model.size == 'full'}">
			
			<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/index.htm" />

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
					
					<c:if test="${empty hideAddNewPatient}">
						searchWidget.addPatientLink = '<a href="#" onClick="return jumpToAddNew()"><spring:message javaScriptEscape="true" code="Patient.add.new"/> &dArr;</a>';
					</c:if>
					searchWidget.inputNode.select();
					changeClassProperty("description", "display", "none");
				});
				
			</script>
			
			<div id="findPatient">
				<b class="boxHeader"><spring:message code="Patient.find"/></b>
				<div class="box">
					<div dojoType="PatientSearch" widgetId="pSearch" <c:if test="${model.showIncludeVoided == 'true'}">showIncludeVoided="true"</c:if> searchLabel="<spring:message code="formentry.searchBox" htmlEscape="true"/>" showVerboseListing="true" patientId='<request:parameter name="patientId"/>' searchPhrase='<request:parameter name="phrase"/>' <c:if test="${not empty hideAddNewPatient}">showAddPatientLink='false'</c:if>></div>
				</div>
			</div>
			
			<c:if test="${empty hideAddNewPatient}">
				<openmrs:hasPrivilege privilege="Add Patients">
					<br/> &nbsp; <spring:message code="general.or"/><br/><br/>
					<openmrs:portlet id="addPatient" url="addPatientForm" parameters="postURL=admin/patients/addPatient.htm" />
				</openmrs:hasPrivilege>
				
				<script type="text/javascript">
					function jumpToAddNew() {
						var searchWidget = dojo.widget.manager.getWidgetById("pSearch");
						searchWidget.clearSearch();
						document.getElementById("patientName").focus();
						return false;
					}
				</script>
			</c:if>
			
		</c:when>
		<c:otherwise>
			ERROR! unknown size '${model.size}' in FindOnePatientWidget
		</c:otherwise>
	</c:choose>
</c:if>
