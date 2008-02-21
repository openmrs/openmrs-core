<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:htmlInclude file="/scripts/easyAjax.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRRelationshipService.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRPatientService.js" />
<openmrs:htmlInclude file="/dwr/engine.js" />
<openmrs:htmlInclude file="/dwr/util.js" />

<openmrs:globalProperty var="importantIdentifiers" key="patient_identifier.importantTypes" />
<openmrs:globalProperty key="use_patient_attribute.healthCenter" defaultValue="false" var="showHealthCenter"/>

<div class="boxHeader${model.patientVariation}"><spring:message code="Patient.actions" /></div>
<div class="box${model.patientVariation}">
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
		<tr>
		<c:if test="${empty model.patientReasonForExit}">
			<td id="patientActionsOutcome">
				<div id="patientActionsOutcomeLink">
					<a href="javascript:showExitForm();"><spring:message code="Patient.outcome.exitFromCare" /></a>
				</div>
				<div id="patientActionsOutcomeForm" style="display:none;">
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
							var outcomeType = DWRUtil.getValue("reasonForExit");
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
							var outcomeType = DWRUtil.getValue("reasonForExit");
							var outcomeDate = DWRUtil.getValue("dateOfExit");
							var outcomeCauseOfDeath = DWRUtil.getValue("causeOfDeath");
							var outcomeCauseOther = DWRUtil.getValue("causeOfDeath_other");
							
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
			<td id="patientActionsOutcome">
				<span id="reasonForExit"><spring:message code="Patient.outcome.exitType" />: <b>${model.patientReasonForExit} (${model.patientDateOfExit})</b></span>
			</td>
		</c:if>
		</tr>
	</table>
</div>
<br />

<c:if test="${not empty importantIdentifiers}">
	<div class="boxHeader${model.patientVariation}"><spring:message code="Patient.identifiers" /></div>
	<div class="box${model.patientVariation}">
		<openmrs:portlet url="patientIdentifiers" size="normal" patientId="${model.patientId}" parameters="showIfSet=true|showIfMissing=true|highlightIfMissing=false" />
	</div>
	<p>
</c:if>

<div class="boxHeader${model.patientVariation}"><spring:message code="Program.title"/></div>
<div class="box${model.patientVariation}">
	<openmrs:portlet url="patientPrograms" id="patientPrograms" patientId="${patient.patientId}" parameters="allowEdits=true"/>
</div>
<br/>

<openmrs:globalProperty var="conceptIdsToUse" key="dashboard.overview.showConcepts" />
<c:if test="${not empty conceptIdsToUse}">
	<div class="boxHeader${model.patientVariation}"><spring:message code="patientDashboard.mostRecentObs"/></div>
	<div class="box${model.patientVariation}">
		<openmrs:portlet url="customMostRecentObs" size="normal" patientId="${patient.patientId}" parameters="conceptIds=${conceptIdsToUse}|allowNew=true" />
	</div>
	
	<br/>
</c:if>

<div class="boxHeader${model.patientVariation}"><spring:message code="Relationship.relationships" /></div>
<div class="box${model.patientVariation}">
	<openmrs:portlet url="personRelationships" size="normal" patientId="${patient.patientId}" />
</div>

