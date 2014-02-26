<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:htmlInclude file="/scripts/easyAjax.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRRelationshipService.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRPatientService.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRObsService.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRConceptService.js" />
<openmrs:htmlInclude file="/dwr/engine.js" />
<openmrs:htmlInclude file="/dwr/util.js" />

<openmrs:globalProperty var="importantIdentifiers" key="patient_identifier.importantTypes" />
<openmrs:globalProperty key="use_patient_attribute.healthCenter" defaultValue="false" var="showHealthCenter"/>

<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.overviewBox" type="html" parameters="patientId=${model.patient.patientId}" requiredClass="org.openmrs.module.web.extension.BoxExt">
	<openmrs:hasPrivilege privilege="${extension.requiredPrivilege}">
		<div class="boxHeader${model.patientVariation}"><openmrs:message code="${extension.title}" /></div>
		<div class="box${model.patientVariation}"><openmrs:message code="${extension.content}" />
  			<c:if test="${extension.portletUrl != null}">
   				<openmrs:portlet url="${extension.portletUrl}" moduleId="${extension.moduleId}" id="${extension.portletUrl}" patientId="${patient.patientId}" parameters="allowEdits=true"/>
 			</c:if>
		</div>
		<br />
	</openmrs:hasPrivilege>
</openmrs:extensionPoint>

<openmrs:hasPrivilege privilege="Patient Overview - View Patient Actions">
<div id="patientActionsBoxHeader" class="boxHeader${model.patientVariation}"><openmrs:message code="Patient.actions" /></div>
<div id="patientActionsBox" class="box${model.patientVariation}">
	<table id="patientActions">
		<tr class="patientActionsRow">
			<td id="patientActionsPatientSummary">
				<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.patientSummary" parameters="patientId=${model.patient.patientId}">
					<a href="javascript:window.open('<openmrs_tag:url value="${extension.url}"/>?patientId=<c:out value="${model.patient.patientId}" />', 'summaryWindow', 'toolbar=no,width=660,height=600,resizable=yes,scrollbars=yes').focus()"><c:out value="${extension.label}" /></a>
				</openmrs:extensionPoint>
			</td>
		</tr>
	</table>
	<table id="patientActions">
		<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.patientActionsContent" type="html" parameters="patientId=${model.patient.patientId}"/> 
		<tr class="patientActionsRow">
		<openmrs:globalProperty key="concept.reasonExitedCare" var="reasonExitedCare" />
		<c:if test="${empty model.patientReasonForExit && !empty reasonExitedCare}">
			<td id="patientActionsOutcome">
				<div id="patientActionsOutcomeLink">					
					<button id="cancelExitButton" onClick="return showExitForm();"><openmrs:message code="Patient.outcome.exitFromCare"/></button>
				</div>
				<div id="patientActionsOutcomeForm" style="display:none; padding: 3px; border: 1px black dashed">
					<form method="post" id="exitForm">
						<table id="outcomeFormTable">
							<tr class="patientOutcomeRow">
								<td id="patientActionsOutcomeReason">
									<span id="patientOutcomeTextReason"><openmrs:message code="Patient.outcome.exitType" /></span>
									<openmrs:fieldGen type="org.openmrs.Patient.exitReason" formFieldName="reasonForExit" val="" parameters="optionHeader=[blank]|globalProp=concept.reasonExitedCare|onChange=updateCauseField()" />
								</td>
								<td id="patientActionsCauseOfDeath" style="display:none;">
									<span id="patientOutcomeTextDeathCause"><openmrs:message code="Person.causeOfDeath"/></span>
									<openmrs:globalProperty key="concept.causeOfDeath" var="conceptCauseOfDeath" />
									<openmrs:globalProperty key="concept.otherNonCoded" var="conceptOther" />
									<openmrs:fieldGen type="org.openmrs.Concept" formFieldName="causeOfDeath" val="${status.value}" parameters="showAnswers=${conceptCauseOfDeath}|showOther=${conceptOther}|otherValue=${causeOfDeathOther}" />
								</td>
							</tr>
							<tr class="patientOutcomeRow">
								<td id="patientActionsOutcomeDate">
									<span id="patientOutcomeTextExitDate"><openmrs:message code="Patient.outcome.exitDate" /></span>
									<openmrs:fieldGen type="java.util.Date" formFieldName="dateOfExit" val="" parameters="noBind=true" />
								</td>
								<td id="patientActionsOutcomeSave">
									<input type="button" onClick="javascript:exitFormValidate();" value="<openmrs:message code="general.save" />" />
									<input type="button" onClick="javascript:hideExitForm();" value="<openmrs:message code="general.cancel" />" />
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
								alert("<openmrs:message code="Patient.outcome.error.noType" />");
								return;
							}

							if ( outcomeDate == '' ) {
								alert("<openmrs:message code="Patient.outcome.error.noDate" />");
								return;
							}
							
							if ( outcomeType == '${conceptPatientDied}' && outcomeCauseOfDeath == '' ) {
								alert("<openmrs:message code="Patient.outcome.error.noCauseOfDeath" />");
								return
							}
							
							if ( outcomeType && outcomeDate ) {
								var exitTypeSelect = document.getElementById("reasonForExit");
								var exitTypeText = exitTypeSelect[exitTypeSelect.selectedIndex].text;
								var answer = confirm("<openmrs:message code="Patient.outcome.readyToSubmit" />" + "\n<openmrs:message code="Patient.outcome.exitType" />: " + exitTypeText + "\n<openmrs:message code="Patient.outcome.exitDate" />: " + outcomeDate);
								if ( answer ) {
									DWRPatientService.exitPatientFromCare( <c:out value="${model.patient.patientId}" />, outcomeType, outcomeDate, outcomeCauseOfDeath, outcomeCauseOther, confirmExit );
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
				<openmrs:message code="general.voidReasonQuestion" var="reasonText"/>
				function handleCancelExit() {
					var reason = dwr.util.getValue('cancelExitReason');
					if (reason == '') {
						alert("<openmrs:message code="Patient.outcome.resumeCareReason.required" arguments="${reasonText}"/>");
						return;
					} else {
						DWRObsService.voidObservation(${model.patientReasonForExit.obsId}, reason,
							function() { window.location.reload(); } );
					}
				}
			</script>
			<td id="patientActionsOutcome">
				<span id="reasonForExit"><openmrs:message code="Patient.outcome.exitType" />: 
					<b><openmrs_tag:concept conceptId="${model.patientReasonForExit.valueCoded.conceptId}"/> 
					(<openmrs:formatDate date="${model.patientReasonForExit.obsDatetime}"/>)</b>
				</span>
				
				<input type="button" id="cancelExitButton" value="<openmrs:message code="Patient.outcome.resumeCare"/>" onClick="showDiv('cancelExit'); hideDiv('cancelExitButton');"/>

				<br/><br/>
				<div id="cancelExit" style="display: none; padding: 3px; border: 1px black dashed">
					<openmrs:message code="Patient.outcome.exit.cancelReason"/>:
					<input type="text" id="cancelExitReason" name="cancelExitReason" value="<openmrs:message code="general.dataEntryError"/>"/>
					<input type="button" value="<openmrs:message code="general.save"/>" onClick="handleCancelExit()" />
					<input type="button" value="<openmrs:message code="general.cancel"/>" onClick="hideDiv('cancelExit'); showDiv('cancelExitButton')"/>
				</div>
			</td>
		</c:if>
		</tr>
	</table>
</div>
<br />

<c:if test="${not empty importantIdentifiers}">
	<div id="patientIdentifiersBoxHeader" class="boxHeader${model.patientVariation}"><openmrs:message code="Patient.identifiers" /></div>
	<div id="patientIdentifiersBox" class="box${model.patientVariation}">
		<openmrs:portlet url="patientIdentifiers" size="normal" patientId="${model.patientId}" parameters="showIfSet=true|showIfMissing=true|highlightIfMissing=false" />
	</div>
	<p>
</c:if>
</openmrs:hasPrivilege>

<openmrs:hasPrivilege privilege="Patient Overview - View Programs">
	<div id="patientProgramsBoxHeader" class="boxHeader${model.patientVariation}"><openmrs:message code="Program.title"/></div>
	<div id="patientProgramsBox" class="box${model.patientVariation}">
		<openmrs:portlet url="patientPrograms" id="patientPrograms" patientId="${patient.patientId}" parameters="allowEdits=true"/>
	</div>
	<br/>

<openmrs:globalProperty var="conceptIdsToUse" key="dashboard.overview.showConcepts" />
<c:if test="${not empty conceptIdsToUse}">
	<div id="patientMostRecentObsBoxHeader" class="boxHeader${model.patientVariation}"><openmrs:message code="patientDashboard.mostRecentObs"/></div>
	<div id="patientMostRecentObsBox" class="box${model.patientVariation}">
		<openmrs:portlet url="customMostRecentObs" size="normal" patientId="${patient.patientId}" parameters="conceptIds=${conceptIdsToUse}|allowNew=true" />
	</div>
	<br/>
</c:if>
</openmrs:hasPrivilege>

<openmrs:hasPrivilege privilege="Patient Overview - View Relationships">
	<div id="patientRelationshipsBoxHeader" class="boxHeader${model.patientVariation}"><openmrs:message code="Relationship.relationships" /></div>
	<div id="patientRelationshipsBox" class="box${model.patientVariation}">
		<openmrs:portlet url="personRelationships" size="normal" patientId="${patient.patientId}" />
	</div>
	<br/>
</openmrs:hasPrivilege>

<openmrs:hasPrivilege privilege="Patient Overview - View Allergies">
	<div id="patientActiveListsAllergyBoxHeader" class="boxHeader${model.patientVariation}"><openmrs:message code="ActiveLists.allergy.title" /></div>
	<div id="patientActiveListsAllergyBox" class="box${model.patientVariation}">
		<openmrs:portlet url="activeListAllergy" patientId="${patient.patientId}" parameters="type=allergy"/>
	</div>
	<br/>
</openmrs:hasPrivilege>

<openmrs:hasPrivilege privilege="Patient Overview - View Problem List">
	<div id="patientActiveListsProblemBoxHeader" class="boxHeader${model.patientVariation}"><openmrs:message code="ActiveLists.problem.title" /></div>
	<div id="patientActiveListsProblemBox" class="box${model.patientVariation}">
		<openmrs:portlet url="activeListProblem" patientId="${patient.patientId}" parameters="type=problem"/>
	</div>
	<br/>
</openmrs:hasPrivilege>
