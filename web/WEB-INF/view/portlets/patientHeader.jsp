<%@ include file="/WEB-INF/template/include.jsp" %>

<c:set var="HIV_PROGRAM_CONCEPT_ID" value="1482" />
<c:set var="TB_PROGRAM_CONCEPT_ID" value="1483" />

<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/index.htm" />

	<%-- Header showing preferred name, id, and treatment status --%>
	<c:if test="${empty model.patientReasonForExit}">
		<div id="patientHeader" class="boxHeader">
	</c:if>
	<c:if test="${not empty model.patientReasonForExit}">
		<div id="patientHeader" class="boxHeaderRed">
	</c:if>
		<div id="patientHeaderPatientName">${model.patient.personName}</div>
		<div id="patientHeaderPreferredIdentifier">
			<c:if test="${fn:length(model.patient.activeIdentifiers) > 0}">
				<c:forEach var="identifier" items="${model.patient.activeIdentifiers}" begin="0" end="0">
					<span class="patientHeaderPatientIdentifier"><span id="patientHeaderPatientIdentifierType">${identifier.identifierType.name}:</span> ${identifier.identifier}</span>
				</c:forEach>
			</c:if>
		</div>
		<table id="patientHeaderGeneralInfo">
			<tr>
				<td id="patientHeaderPatientGender">
					<c:if test="${model.patient.gender == 'M'}"><img src="${pageContext.request.contextPath}/images/male.gif" alt='<spring:message code="Person.gender.male"/>'/></c:if>
					<c:if test="${model.patient.gender == 'F'}"><img src="${pageContext.request.contextPath}/images/female.gif" alt='<spring:message code="Person.gender.female"/>'/></c:if>
				</td>
				<td id="patientHeaderPatientAge">
					<c:if test="${model.patient.age > 0}">${model.patient.age} <spring:message code="Person.age.years"/></c:if>
					<c:if test="${model.patient.age == 0}">< 1 <spring:message code="Person.age.year"/></c:if>
					<span id="patientHeaderPatientBirthdate"><c:if test="${not empty model.patient.birthdate}">(<c:if test="${model.patient.birthdateEstimated}">~</c:if><openmrs:formatDate date="${model.patient.birthdate}" type="medium" />)</c:if><c:if test="${empty model.patient.birthdate}"><spring:message code="general.unknown"/></c:if></span>
				</td>
				<openmrs:globalProperty key="use_patient_attribute.tribe" defaultValue="false" var="showTribe"/>
				<c:if test="${showTribe}">
					<td id="patientHeaderPatientTribe">
						<spring:message code="Patient.tribe"/>:
						<b>${model.patient.tribe.name}</b>
					</td>
				</c:if>
				<c:if test="${not empty model.patient.attributeMap['Health Center']}">
					<td id="patientHeaderHealthCenter">
						<spring:message code="PersonAttributeType.HealthCenter"/>:
						<b>${model.patient.attributeMap['Health Center'].hydratedObject}</b>
					</td>
				</c:if>
				<td id="patientHeaderPatientSummary">
					<a class="offColor" href="javascript:window.open('patientSummary.htm?patientId=${model.patientId}', 'summaryWindow', 'toolbar=no,width=800,height=600,resizable=yes,scrollbars=yes').focus()">Summary</a>
				</td>
				<c:if test="${empty model.patientReasonForExit}">
					<td id="patientHeaderOutcome">
						<div id="patientHeaderOutcomeLink">
							<a class="offColor" href="javascript:showExitForm();"><spring:message code="Patient.outcome.exitFromCare" /></a>
						</div>
						<div id="patientHeaderOutcomeForm" style="display:none;">
							<form method="post" id="exitForm">
								<table id="outcomeFormTable">
									<tr>
										<td id="patientHeaderOutcomeReason">
											<span id="patientOutcomeTextReason"><spring:message code="Patient.outcome.exitType" /></span>
											<openmrs:fieldGen type="org.openmrs.Patient.exitReason" formFieldName="reasonForExit" val="" parameters="optionHeader=[blank]|globalProp=concept.reasonExitedCare|onChange=updateCauseField()" />
										</td>
										<td id="patientHeaderOutcomeDate">
											<span id="patientOutcomeTextReason"><spring:message code="Patient.outcome.exitDate" /></span>
											<openmrs:fieldGen type="java.util.Date" formFieldName="dateOfExit" val="" parameters="noBind=true" />
										</td>
										<td id="patientHeaderCauseOfDeath" style="display:none;">
											<span id="patientOutcomeTextDeathCause"><spring:message code="Person.deathDate"/></span>
											<openmrs:globalProperty key="concept.causeOfDeath" var="conceptCauseOfDeath" />
											<openmrs:globalProperty key="concept.otherNonCoded" var="conceptOther" />
											<openmrs:fieldGen type="org.openmrs.Concept" formFieldName="causeOfDeath" val="${status.value}" parameters="showAnswers=${conceptCauseOfDeath}|showOther=${conceptOther}|otherValue=${causeOfDeathOther}" />
										</td>
										<td id="patientHeaderOutcomeSave">
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
										showDiv("patientHeaderCauseOfDeath");
									} else {
										hideDiv("patientHeaderCauseOfDeath");
									}
								}
							
								function showExitForm() {
									showDiv("patientHeaderOutcomeForm");
									hideDiv("patientHeaderOutcomeLink");
								}
							
								function hideExitForm() {
									showDiv("patientHeaderOutcomeLink");
									hideDiv("patientHeaderOutcomeForm");
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
					<td id="patientHeaderOutcome">
						<span id="reasonForExit"><spring:message code="Patient.outcome.exitType" />: <b>${model.patientReasonForExit} (${model.patientDateOfExit})</b></span>
					</td>
				</c:if>
				<td id="patientDashboardHeaderExtension">
					<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.Header" type="html" parameters="patientId=${model.patient.patientId}" />
				</td>
				<td style="width: 100%;">&nbsp;</td>
				<td id="patientHeaderOtherIdentifiers">
					<c:if test="${fn:length(model.patient.activeIdentifiers) > 1}">
						<c:forEach var="identifier" items="${model.patient.activeIdentifiers}" begin="1" end="1">
							<span class="patientHeaderPatientIdentifier">${identifier.identifierType.name}: ${identifier.identifier}</span>
						</c:forEach>
					</c:if>
					<c:if test="${fn:length(model.patient.activeIdentifiers) > 2}">
						<div id="patientHeaderMoreIdentifiers">
							<c:forEach var="identifier" items="${model.patient.activeIdentifiers}" begin="2">
								<span class="patientHeaderPatientIdentifier">${identifier.identifierType.name}: ${identifier.identifier}</span>
							</c:forEach>
						</div>
					</c:if>
				</td>
				<c:if test="${fn:length(model.patient.activeIdentifiers) > 2}">
					<td width="32">
						<small><a id="patientHeaderShowMoreIdentifiers" onclick="return showMoreIdentifiers()" title='<spring:message code="patientDashboard.showMoreIdentifers"/>'><spring:message code="general.nMore" arguments="${fn:length(model.patient.activeIdentifiers) - 2}"/></a></small>
					</td>
				</c:if>
			</tr>
		</table>
	</div>
	<c:if test="${empty model.patientReasonForExit}">
		<div id="patientSubheader" class="box">
	</c:if>
	<c:if test="${not empty model.patientReasonForExit}">
		<div id="patientSubheaderExited" class="boxRed">
	</c:if>
		<c:forEach items="${model.patientCurrentPrograms}" var="p" varStatus="s">
			<c:if test="${p.program.concept.conceptId == HIV_PROGRAM_CONCEPT_ID}">
				<table><tr>
					<th><spring:message code="Program.hiv"/></th>
					<td>|</td>
					<td><spring:message code="Program.enrolled"/>:</td>
					<th><openmrs:formatDate date="${p.dateEnrolled}" type="medium" /></th>
					<td>|</td>
					<td><spring:message code="Program.group"/>:</td>
					<th><openmrs_tag:mostRecentObs observations="${model.patientObs}" concept="1377" locale="${model.locale}" /></th>
					<td>|</td>
					<td><spring:message code="Program.agent"/>:</td>
					<th>
						<c:forEach items="${model.patientRelationships}" var="r" varStatus="s">
							<c:if test="${r.relationshipType.relationshipTypeId == 1}">
								<c:if test="${accompFound}">, </c:if>
								${r.personA.personName}
								<c:set var="accompFound" value="true"/>
							</c:if>
						</c:forEach>
					</th>
				</tr></table>
			</c:if>
		</c:forEach>
		<c:forEach items="${model.patientCurrentPrograms}" var="p" varStatus="s">
			<c:if test="${p.program.concept.conceptId == TB_PROGRAM_CONCEPT_ID}">
				<table><tr>
					<th><spring:message code="Program.tb"/></th>
					<td>|</td>
					<td><spring:message code="Program.enrolled"/>:</td>
					<th><openmrs:formatDate date="${p.dateEnrolled}" type="medium" /></th>
					<td>|</td>
					<td><spring:message code="Program.group"/>:</td>
					<th><openmrs_tag:mostRecentObs observations="${model.patientObs}" concept="1378" locale="${model.locale}" /></th>
				</tr></table>
			</c:if>
		</c:forEach>
		<table id="patientHeaderObs">
			<tr>
				<td id="patientHeaderObsWeight">
					<spring:message code="Patient.weight"/>:
					<openmrs_tag:mostRecentObs observations="${model.patientObs}" concept="5089" showUnits="true" locale="${model.locale}" showDate="true" />
				</td>
				<td id="patientHeaderObsCD4">
					<spring:message code="Patient.cd4"/>:
					<openmrs_tag:mostRecentObs observations="${model.patientObs}" concept="5497" locale="${model.locale}" />
				</td>
				<td id="patientHeaderObsReturnVisit">
					<spring:message code="Patient.returnVisit"/>:
					<openmrs_tag:mostRecentObs observations="${model.patientObs}" concept="5096" locale="${model.locale}" />
				</td>
				<td id="patientHeaderObsRegimen">
					<spring:message code="Patient.regimen" />:
					<span id="patientHeaderRegimen">
						<c:forEach items="${model.currentDrugOrders}" var="drugOrder" varStatus="drugOrderStatus">
							${drugOrder.drug.name}
							<c:if test="${!drugOrderStatus.last}">, </c:if>
						</c:forEach>
					</span>
				</td>
			</tr>
		</table>
		<table><tr>
			<td><spring:message code="Patient.lastEncounter"/>:</td>
			<th>
				<c:forEach items='${openmrs:sort(model.patientEncounters, "encounterDatetime", true)}' var="lastEncounter" varStatus="lastEncounterStatus" end="0">
					${lastEncounter.encounterType.name} @ ${lastEncounter.location.name}, <openmrs:formatDate date="${lastEncounter.encounterDatetime}" type="medium" />
				</c:forEach>
				<c:if test="${fn:length(encounters) == 0}">
					<spring:message code="Encounter.no.previous"/>
				</c:if>	
			</th>
		</tr></table>
	</div>
	
	<script type="text/javascript">
		function showMoreIdentifiers() {
			if (identifierElement.style.display == '') {
				linkElement.innerHTML = '<spring:message code="general.nMore" arguments="${fn:length(model.patient.activeIdentifiers) - 2}"/>';
				identifierElement.style.display = "none";
			}
			else {
				linkElement.innerHTML = '<spring:message code="general.nLess" arguments="${fn:length(model.patient.activeIdentifiers) - 2}"/>';
				identifierElement.style.display = "";
			}
		}
		
		var identifierElement = document.getElementById("patientHeaderMoreIdentifiers");
		var linkElement = document.getElementById("patientHeaderShowMoreIdentifiers");
		if (identifierElement)
			identifierElement.style.display = "none";
		
	</script>
	