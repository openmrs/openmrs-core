<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:htmlInclude file="/scripts/easyAjax.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRRelationshipService.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRPatientService.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRObsService.js" />
<openmrs:htmlInclude file="/dwr/engine.js" />
<openmrs:htmlInclude file="/dwr/util.js" />

<openmrs:globalProperty var="importantIdentifiers" key="patient_identifier.importantTypes" />
<openmrs:globalProperty key="use_patient_attribute.healthCenter" defaultValue="false" var="showHealthCenter"/>

<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.overviewBox" type="html" parameters="patientId=${model.patient.patientId}">
	<openmrs:hasPrivilege privilege="${extension.requiredPrivilege}">
		<div class="boxHeader${model.patientVariation}"><spring:message code="${extension.title}" /></div>
		<div class="box${model.patientVariation}"><spring:message code="${extension.content}" />
  			<c:if test="${extension.portletUrl != null}">
   				<openmrs:portlet url="${extension.portletUrl}" moduleId="${extension.moduleId}" id="${extension.portletUrl}" patientId="${patient.patientId}" parameters="allowEdits=true"/>
 			</c:if>
		</div>
		<br />
	</openmrs:hasPrivilege>
</openmrs:extensionPoint>

<div id="patientActionsBoxHeader" class="boxHeader${model.patientVariation}"><spring:message code="Patient.actions" /></div>
<div id="patientActionsBox" class="box${model.patientVariation}">
	<table id="patientActions">
		<tr>
			<td id="patientActionsPatientSummary">
				<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.patientSummary">
					<a href="javascript:window.open('${extension.url}?patientId=${model.patient.patientId}', 'summaryWindow', 'toolbar=no,width=660,height=600,resizable=yes,scrollbars=yes').focus()">${extension.label}</a>
				</openmrs:extensionPoint>
			</td>
		</tr>
	</table>
	<table id="patientActions">
		<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.patientActionsContent" type="html" parameters="patientId=${model.patient.patientId}"/> 
		<tr>
		<c:if test="${empty model.patientReasonForExit}">
			<td id="patientActionsOutcome">
				<div id="patientActionsOutcomeLink">					
					<button id="cancelExitButton" onClick="return showExitForm();"><spring:message code="Patient.outcome.exitFromCare"/></button>
				</div>
				<div id="patientActionsOutcomeForm" style="display:none; padding: 3px; border: 1px black dashed">
					<form method="post" id="exitForm">
						<table id="outcomeFormTable">
							<tr>
								<td id="patientActionsOutcomeReason">
									<span id="patientOutcomeTextReason"><spring:message code="Patient.outcome.exitType" /></span>
									<openmrs:fieldGen type="org.openmrs.Patient.exitReason" formFieldName="reasonForExit" val="" parameters="optionHeader=[blank]|globalProp=concept.reasonExitedCare|onChange=updateCauseField()" />
								</td>
								<td id="patientActionsCauseOfDeath" style="display:none;">
									<span id="patientOutcomeTextDeathCause"><spring:message code="Person.causeOfDeath"/></span>
									<openmrs:globalProperty key="concept.causeOfDeath" var="conceptCauseOfDeath" />
									<openmrs:globalProperty key="concept.otherNonCoded" var="conceptOther" />
									<openmrs:fieldGen type="org.openmrs.Concept" formFieldName="causeOfDeath" val="${status.value}" parameters="showAnswers=${conceptCauseOfDeath}|showOther=${conceptOther}|otherValue=${causeOfDeathOther}" />
								</td>
							<tr>
							<tr>
								<td id="patientActionsOutcomeDate">
									<span id="patientOutcomeTextExitDate"><spring:message code="Patient.outcome.exitDate" /></span>
									<openmrs:fieldGen type="java.util.Date" formFieldName="dateOfExit" val="" parameters="noBind=true" />
								</td>
								<td id="patientActionsOutcomeSave">
									<input type="button" onClick="javascript:exitFormValidate();" value="<spring:message code="general.save" />" />
									<input type="button" onClick="javascript:hideExitForm();" value="<spring:message code="general.cancel" />" />
								</td>
							</tr>
						</table>
					</form>
				</div>
				<script>
					<!--
						
						function updateCauseField() {
							var outcomeType = dwr.util.getValue("reasonForExit");
							<openmrs:globalProperty key="concept.patientDied" var="conceptPatientDied" />

							if ( outcomeType == '${conceptPatientDied}' ) {
								showDiv("patientActionsCauseOfDeath");
							} else {
								hideDiv("patientActionsCauseOfDeath");
							}
						}
					
						function showExitForm() {
							showDiv("patientActionsOutcomeForm");
							hideDiv("patientActionsOutcomeLink");
						}
					
						function hideExitForm() {
							showDiv("patientActionsOutcomeLink");
							hideDiv("patientActionsOutcomeForm");
						}
					
						function exitFormValidate() {
							var outcomeType = dwr.util.getValue("reasonForExit");
							var outcomeDate = dwr.util.getValue("dateOfExit");
							var outcomeCauseOfDeath = dwr.util.getValue("causeOfDeath");
							var outcomeCauseOther = dwr.util.getValue("causeOfDeath_other");
							
							if ( outcomeType == '' ) {
								alert("<spring:message code="Patient.outcome.error.noType" />");
								return;
							}

							if ( outcomeDate == '' ) {
								alert("<spring:message code="Patient.outcome.error.noDate" />");
								return;
							}
							
							if ( outcomeType == '${conceptPatientDied}' && outcomeCauseOfDeath == '' ) {
								alert("<spring:message code="Patient.outcome.error.noCauseOfDeath" />");
								return
							}
							
							if ( outcomeType && outcomeDate ) {
								var exitTypeSelect = document.getElementById("reasonForExit");
								var exitTypeText = exitTypeSelect[exitTypeSelect.selectedIndex].text;
								var answer = confirm("<spring:message code="Patient.outcome.readyToSubmit" />" + "\n<spring:message code="Patient.outcome.exitType" />: " + exitTypeText + "\n<spring:message code="Patient.outcome.exitDate" />: " + outcomeDate);
								if ( answer ) {
									DWRPatientService.exitPatientFromCare( ${model.patient.patientId}, outcomeType, outcomeDate, outcomeCauseOfDeath, outcomeCauseOther, confirmExit );
								}
							}
						}
						
						function confirmExit(message) {
							if ( message == '' ) {
								// patient has been exited, let's refresh the page
								window.location.reload();
							} else {
								alert(message);
							}
						}
					-->
				</script>
			</td>
		</c:if>
		<c:if test="${not empty model.patientReasonForExit}">
			<script type="text/javascript">
				<spring:message code="general.voidReasonQuestion" var="reasonText"/>
				function handleCancelExit() {
					var reason = dwr.util.getValue('cancelExitReason');
					if (reason == '') {
						alert("<spring:message code="Patient.outcome.resumeCareReason.required" arguments="${reasonText}"/>");
						return;
					} else {
						DWRObsService.voidObservation(${model.patientReasonForExit.obsId}, reason,
							function() { window.location.reload(); } );
					}
				}
			</script>
			<td id="patientActionsOutcome">
				<span id="reasonForExit"><spring:message code="Patient.outcome.exitType" />: 
					<b><openmrs_tag:concept conceptId="${model.patientReasonForExit.valueCoded.conceptId}"/> 
					(<openmrs:formatDate date="${model.patientReasonForExit.obsDatetime}"/>)</b>
				</span>
				
				<input type="button" id="cancelExitButton" value="<spring:message code="Patient.outcome.resumeCare"/>" onClick="showDiv('cancelExit'); hideDiv('cancelExitButton');"/>

				<br/><br/>
				<div id="cancelExit" style="display: none; padding: 3px; border: 1px black dashed">
					<spring:message code="Patient.outcome.exit.cancelReason"/>:
					<input type="text" id="cancelExitReason" name="cancelExitReason" value="<spring:message code="general.dataEntryError"/>"/>
					<input type="button" value="<spring:message code="general.save"/>" onClick="handleCancelExit()" />
					<input type="button" value="<spring:message code="general.cancel"/>" onClick="hideDiv('cancelExit'); showDiv('cancelExitButton')"/>
				</div>
			</td>
		</c:if>
		</tr>
	</table>
</div>
<br />

<c:if test="${not empty importantIdentifiers}">
	<div id="patientIdentifiersBoxHeader" class="boxHeader${model.patientVariation}"><spring:message code="Patient.identifiers" /></div>
	<div id="patientIdentifiersBox" class="box${model.patientVariation}">
		<openmrs:portlet url="patientIdentifiers" size="normal" patientId="${model.patientId}" parameters="showIfSet=true|showIfMissing=true|highlightIfMissing=false" />
	</div>
	<p>
</c:if>

<openmrs:hasPrivilege privilege="View Patient Programs">
	<div id="patientProgramsBoxHeader" class="boxHeader${model.patientVariation}"><spring:message code="Program.title"/></div>
	<div id="patientProgramsBox" class="box${model.patientVariation}">
		<openmrs:portlet url="patientPrograms" id="patientPrograms" patientId="${patient.patientId}" parameters="allowEdits=true"/>
	</div>
	<br/>
</openmrs:hasPrivilege>

<openmrs:globalProperty var="conceptIdsToUse" key="dashboard.overview.showConcepts" />
<c:if test="${not empty conceptIdsToUse}">
	<div id="patientMostRecentObsBoxHeader" class="boxHeader${model.patientVariation}"><spring:message code="patientDashboard.mostRecentObs"/></div>
	<div id="patientMostRecentObsBox" class="box${model.patientVariation}">
		<openmrs:portlet url="customMostRecentObs" size="normal" patientId="${patient.patientId}" parameters="conceptIds=${conceptIdsToUse}|allowNew=true" />
	</div>
	
	<br/>
</c:if>

<openmrs:hasPrivilege privilege="View Relationships">
<div id="patientRelationshipsBoxHeader" class="boxHeader${model.patientVariation}"><spring:message code="Relationship.relationships" /></div>
<div id="patientRelationshipsBox" class="box${model.patientVariation}">
	<openmrs:portlet url="personRelationships" size="normal" patientId="${patient.patientId}" />
</div>
<br/>
</openmrs:hasPrivilege>