<%@ include file="/WEB-INF/template/include.jsp" %>
<%--
Parameters
	model.showUnpublishedForms == 'true' means allow users to enter forms that haven't been published yet
	model.goBackOnEntry == 'true' means have the browser go back to the find patient page after starting to enter a form
--%>

<openmrs:hasPrivilege privilege="View Encounters">
	<openmrs:portlet url="patientEncounters" id="patientDashboardEncounters" patientId="${patient.patientId}" parameters="num=3|hideHeader=true|title=FormEntry.last.encounters" />
	<br/>
</openmrs:hasPrivilege>

<openmrs:hasPrivilege privilege="Form Entry">
	<openmrs:htmlInclude file="/scripts/dojoConfig.js"></openmrs:htmlInclude>
	<openmrs:htmlInclude file="/scripts/dojo/dojo.js"></openmrs:htmlInclude>
	<openmrs:htmlInclude file="/dwr/interface/DWRFormService.js"></openmrs:htmlInclude>
	
	<script type="text/javascript">
	
		dojo.require("dojo.widget.openmrs.OpenmrsSearch");
		
		var searchWidget;
		
		dojo.addOnLoad( function() {
			
			searchWidget = dojo.widget.manager.getWidgetById("fSearch");			
			
			searchWidget.doFindObjects = function() {
				this.doObjectsFound(this.allObjectsFound);
			};
			
			searchWidget.getCellContent = function(form) {
				var s = '<span onMouseOver="window.status=\'formId=' + form.formId + '\'">';
				s += form.name + " (v." + form.version + ")";
				if (form.published == false)
					s += ' <i>(<spring:message code="formentry.unpublished"/>)</i>';
					
				s += "</span>";
				return s;
			};
			
			dojo.event.topic.subscribe("fSearch/select", 
				function(msg) {
					document.location = "${pageContext.request.contextPath}/formDownload?target=formEntry&patientId=${patient.patientId}&formId=" + msg.objs[0].formId;
					startDownloading();
				}
			);
			
			DWRFormService.getForms(function(obj) {searchWidget.doObjectsFound(obj); searchWidget.showHighlight();} , '${model.showUnpublishedForms}');
		});
		
		
		//set up delayed post back
		var timeOut = null;
	
		function startDownloading() {
			<c:if test="${model.goBackOnEntry == 'true'}">
				timeOut = setTimeout("goBack()", 30000);
			</c:if>
		}
		
		function goBack() {
			document.location='findPatient.htm';
		}
		
		function switchPatient() {
			document.location='findPatient.htm?phrase=${param.phrase}&autoJump=false';
		}
		
		function cancelTimeout() {
			if (timeOut != null)
				clearTimeout(timeOut);
		}
	</script>
	
	<div id="selectFormHeader" class="boxHeader">Forms</div>
	<div id="selectForm" class="box">
		<div dojoType="OpenmrsSearch" widgetId="fSearch" ></div>
	</div>
	
	<script type="text/javascript">	
		addEvent(window, 'load', function() {
			var widget = dojo.widget.manager.getWidgetById("fSearch");
			widget.inputNode.focus();
		});
	</script>
	
</openmrs:hasPrivilege>