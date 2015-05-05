<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="View Patients" otherwise="/login.htm"
	redirect="/index.htm" />

<openmrs:globalProperty key="visits.enabled" var="visitsEnabled" />

<c:if test="${model.patientVariation == 'Exited'}">
	<div class="retiredMessage">
		<div>
			<openmrs:message code="Patient.patientExitedCare"/>
			&nbsp;&nbsp;&nbsp;&nbsp;
			<openmrs:message code="Patient.patientCareExitDate"/>: <openmrs:formatDate date="${model.patientReasonForExit.obsDatetime}"/>
			&nbsp;&nbsp;&nbsp;&nbsp;
			<openmrs:message code="Patient.patientCareExitReason"/>: <openmrs:format concept="${model.patientReasonForExit.valueCoded}"/>
		</div>
	</div>
</c:if>

<%-- Header showing preferred name, id, and treatment status --%>
<div id="patientHeader" class="boxHeader${model.patientVariation}">
<div id="patientHeaderPatientName"><c:out value="${model.patient.personName}" /></div>
<div id="patientHeaderPreferredIdentifier">
	<c:if test="${fn:length(model.patient.activeIdentifiers) > 0}">
		<c:forEach var="identifier" items="${model.patient.activeIdentifiers}"
			begin="0" end="0">
			<span class="patientHeaderPatientIdentifier"><span
				id="patientHeaderPatientIdentifierType"><c:out value="${identifier.identifierType.name}" /><openmrs:extensionPoint
						pointId="org.openmrs.patientDashboard.afterPatientHeaderPatientIdentifierType"
						type="html"
						parameters="identifierLocation=${identifier.location.name}" />:
			</span> <c:out value="${identifier.identifier}" /></span>
		</c:forEach>
	</c:if>
</div>
<table id="patientHeaderGeneralInfo">
	<tr class="patientHeaderGeneralInfoRow">
		<td id="patientHeaderPatientGender"><c:if
				test="${model.patient.gender == 'M'}">
				<img src="${pageContext.request.contextPath}/images/male.gif"
					alt='<openmrs:message code="Person.gender.male"/>'
					id="maleGenderIcon" />
			</c:if> <c:if test="${model.patient.gender == 'F'}">
				<img src="${pageContext.request.contextPath}/images/female.gif"
					alt='<openmrs:message code="Person.gender.female"/>'
					id="femaleGenderIcon" />
			</c:if></td>
		<td id="patientHeaderPatientAge"><openmrs:extensionPoint
				pointId="org.openmrs.patientDashboard.beforePatientHeaderPatientAge"
				type="html" parameters="patientId=${model.patient.patientId}" /> <c:if
				test="${model.patient.age > 0}">${model.patient.age} <openmrs:message
					code="Person.age.years" />
			</c:if> <c:if test="${model.patient.age == 0}">< 1 <openmrs:message
					code="Person.age.year" />
			</c:if> <span id="patientHeaderPatientBirthdate"><c:if
					test="${not empty model.patient.birthdate}">(<c:if
						test="${model.patient.birthdateEstimated}">~</c:if>
					<openmrs:formatDate date="${model.patient.birthdate}" type="medium" />)</c:if>
				<c:if test="${empty model.patient.birthdate}">
					<openmrs:message code="Person.age.unknown" />
				</c:if></span></td>

		<%-- Display selected person attributes from the manage person attributes page --%>
		<openmrs:forEachDisplayAttributeType personType="patient"
			displayType="header" var="attrType">
			<td class="patientHeaderPersonAttribute"><openmrs:message
					code="PersonAttributeType.${fn:replace(attrType.name, ' ', '')}"
					text="${attrType.name}" />: <b>${model.patient.attributeMap[attrType.name]}</b>
			</td>
		</openmrs:forEachDisplayAttributeType>

		<%-- The following is kept for backward compatibility. --%>
		<td id="patientHeaderPatientTribe"><openmrs:extensionPoint
				pointId="org.openmrs.patientDashboard.afterPatientHeaderPatientTribe"
				type="html" parameters="patientId=${model.patient.patientId}" /></td>
		<openmrs:globalProperty key="use_patient_attribute.healthCenter"
			defaultValue="false" var="showHealthCenter" />
		<c:if
			test="${showHealthCenter && not empty model.patient.attributeMap['Health Center']}">
			<td id="patientHeaderHealthCenter"><openmrs:message
					code="PersonAttributeType.HealthCenter" />: <b>${model.patient.attributeMap['Health Center']}</b></td>
		</c:if>
		<td id="patientDashboardHeaderExtension"><openmrs:extensionPoint
				pointId="org.openmrs.patientDashboard.Header" type="html"
				parameters="patientId=${model.patient.patientId}" /></td>
		<td style="width: 100%;" class="patientHeaderEmptyData">&nbsp;</td>
		<td id="patientHeaderOtherIdentifiers"><c:if
				test="${fn:length(model.patient.activeIdentifiers) > 1}">
				<c:forEach var="identifier"
					items="${model.patient.activeIdentifiers}" begin="1" end="1">
					<span class="patientHeaderPatientIdentifier"><c:out value="${identifier.identifierType.name}" /><openmrs:extensionPoint
							pointId="org.openmrs.patientDashboard.afterPatientHeaderPatientIdentifierType"
							type="html"
							parameters="identifierLocation=${identifier.location.name}" />:
						<c:out value="${identifier.identifier}" />
					</span>
				</c:forEach>
			</c:if> <c:if test="${fn:length(model.patient.activeIdentifiers) > 2}">
				<div id="patientHeaderMoreIdentifiers">
					<c:forEach var="identifier"
						items="${model.patient.activeIdentifiers}" begin="2">
						<span class="patientHeaderPatientIdentifier"><c:out value="${identifier.identifierType.name}" /><openmrs:extensionPoint
								pointId="org.openmrs.patientDashboard.afterPatientHeaderPatientIdentifierType"
								type="html"
								parameters="identifierLocation=${identifier.location.name}" />:
							<c:out value="${identifier.identifier}" />
						</span>
					</c:forEach>
				</div>
			</c:if></td>
		<c:if test="${fn:length(model.patient.activeIdentifiers) > 2}">
			<td width="32" class="patientHeaderShowMoreIdentifiersData"><small><a
					id="patientHeaderShowMoreIdentifiers"
					onclick="return showMoreIdentifiers()"
					title='<openmrs:message code="patientDashboard.showMoreIdentifers"/>'><openmrs:message
							code="general.nMore"
							arguments="${fn:length(model.patient.activeIdentifiers) - 2}" /></a></small>
			</td>
		</c:if>
	</tr>
</table>
</div><%-- Closing div for the patientHeader box --%>
<div id="patientSubheader" class="box">

<openmrs:globalProperty var="programIdsToShow"
	key="dashboard.header.programs_to_show" listSeparator="," />
<%--
			Clever(?) hack: because there's no JSTL function for array membership I'm going to add a comma before
			and after the already-comma-separated list, so I can search for the substring ",ID,"
		--%>
<openmrs:globalProperty var="workflowsToShow"
	key="dashboard.header.workflows_to_show" />
<c:set var="workflowsToShow" value=",${workflowsToShow}," />

<c:forEach var="programNameOrId" items="${programIdsToShow}">
	<c:forEach var="programEnrollment"
		items="${model.patientCurrentPrograms}">
		<c:if
			test="${ programEnrollment.program.programId == programNameOrId || programEnrollment.program.name == programNameOrId }">
			<table class="programEnrollmentTable">
				<tr>
					<th class="programEnrollmentNameHeader">${
						programEnrollment.program.name }</th>
					<td class="programEnrollmentBarData">|</td>
					<td class="programEnrollmentName"><openmrs:message
							code="Program.enrolled" />:</td>
					<th class="programEnrollmentDateHeader"><openmrs:formatDate
							date="${programEnrollment.dateEnrolled}" type="medium" /></th>
					<c:forEach items="${programEnrollment.currentStates}"
						var="patientState">
						<c:set var="temp"
							value=",${patientState.state.programWorkflow.programWorkflowId}," />
						<c:if test="${ fn:contains(workflowsToShow, temp) }">
							<td class="programEnrollmentBarData">|</td>
							<td class="patientStateProgramWorkflowNameData"><c:out value="${patientState.state.programWorkflow.concept.name}" />:</td>
							<th class="patientStateConceptNameHeader"><c:out value="${patientState.state.concept.name}" /></th>
						</c:if>
					</c:forEach>
				</tr>
			</table>
		</c:if>
	</c:forEach>
</c:forEach>

<table id="patientHeaderObs">
	<openmrs:globalProperty key="concept.weight" var="weightConceptId" />
	<openmrs:globalProperty key="concept.height" var="heightConceptId" />
	<openmrs:globalProperty key="dashboard.header.showConcept" var="conceptIds" listSeparator="," />

	<tr class="patientObsRow">
		<th id="patientHeaderObsWeight"><openmrs:message
				code="Patient.bmi" />: ${model.patientBmiAsString}</th>
		<th class="patientHeaderObsWeightHeightHeader"><small> (
				<openmrs:message code="Patient.weight" />: <openmrs_tag:mostRecentObs
					observations="${model.patientObs}" concept="${weightConceptId}"
					showUnits="true" locale="${model.locale}" showDate="false" /> , <openmrs:message
					code="Patient.height" />: <openmrs_tag:mostRecentObs
					observations="${model.patientObs}" concept="${heightConceptId}"
					showUnits="true" locale="${model.locale}" showDate="false" /> )
		</small></th>

        <c:forEach items="${conceptIds}" var="conceptId">
            <td class="patientRecentObsConfigured">
                <openmrs:concept conceptId="${conceptId}" var="c" nameVar="n" numericVar="num" shortestNameVar="sn">
                    <span title="${n.description}">${sn}:</span>
                </openmrs:concept>
                <openmrs_tag:mostRecentObs
                    observations="${model.patientObs}" concept="${conceptId}"
                    showUnits="true" locale="${model.locale}" showDate="false" />
            </td>
        </c:forEach>

		<td id="patientHeaderObsRegimen"><openmrs:message
				code="Patient.regimen" />: <span id="patientHeaderRegimen">
				<c:forEach items="${model.currentDrugOrders}" var="drugOrder"
					varStatus="drugOrderStatus">
					<c:if test="${!empty drugOrder.drug}"><c:out value="${drugOrder.drug.name}" /></c:if>
					<c:if test="${empty drugOrder.drug}"><c:out value="${drugOrder.concept.name.name}" /></c:if>
					<c:if test="${!drugOrderStatus.last}">, </c:if>
				</c:forEach>
		</span></td>
	</tr>
</table>

<c:if test="${not visitsEnabled}">
	<div class="column">
		<div class="box noBorder">
			<table class="patientLastEncounterTable">
				<tr class="patientLastEncounterRow">
					<td class="patientLastEncounterData"><openmrs:message
							code="Patient.lastEncounter" />:</td>
					<th><c:forEach
							items='${openmrs:sort(model.patientEncounters, "encounterDatetime", true)}'
							var="lastEncounter" varStatus="lastEncounterStatus" end="0">
								<c:out value="${lastEncounter.encounterType.name}" /> @ <c:out value="${lastEncounter.location.name}" />, <openmrs:formatDate
								date="${lastEncounter.encounterDatetime}" type="medium" />
						</c:forEach> <c:if test="${fn:length(model.patientEncounters) == 0}">
							<openmrs:message code="Encounter.no.previous" />
						</c:if></th>
				</tr>
			</table>
		</div>
	</div>
</c:if>


<c:if test="${visitsEnabled }">
<c:choose>
<c:when test="${model.patient.dead==false}">
	<openmrs:hasPrivilege privilege="Add Visits">
		<c:if test="${empty model.activeVisits}">
			<div id="patientVisitsSubheader" class="box" style="margin-top: 2px">
				<input type="button" value="<openmrs:message code="Visit.start"/>"
					onclick="window.location='<openmrs:contextPath />/admin/visits/visit.form?patientId=<c:out value="${model.patient.patientId}" />&startNow=true'" />
			</div>
		</c:if>
	</openmrs:hasPrivilege>
	</c:when>
	</c:choose>
	<openmrs:hasPrivilege privilege="View Visits, View Encounters">	
		<openmrs:htmlInclude file="/scripts/timepicker/timepicker.js" />
		
		<div id="patientHeader-endvisit-dialog" title="<openmrs:message code="Visit.end"/>">
		    <form:form action="${pageContext.request.contextPath}/admin/visits/endVisit.form" method="post" modelAttribute="visit">
		       <table cellpadding="3" cellspacing="3" align="center">
					<tr>
						<td>
							<input type="hidden" name="visitId" value="" />
							<openmrs:message code="Visit.enterEndDate"/>
							<jsp:useBean id="now" class="java.util.Date" scope="page" />
							<input type="text" id="enddate_visit" size="20" name="stopDate" value="<openmrs:formatDate date="${now}" format="dd/MM/yyyy HH:mm"/>" onClick="showDateTimePicker(this)" readonly="readonly"/></br>&nbsp;&nbsp;
						</td>
					</tr>
					<tr height="20"></tr>
					<tr>
						<td colspan="2" style="text-align: center">
							<input type="submit" value="<openmrs:message code="Visit.end"/>" />
							&nbsp;
							<input id="patientHeader-close-endvisit-dialog" type="button" value="<openmrs:message code="general.cancel"/>" /> 
						</td>
					</tr>
				</table>
			</form:form>
		</div>
		<script type="text/javascript">	
			$j(document).ready( function() {
				$j("#patientHeader-endvisit-dialog").dialog({
					autoOpen: false,
					resizable: false,
					width:'auto',
					height:'auto',
					modal: true
				});
				$j('#patientHeader-close-endvisit-dialog').click(function() {
					$j('#patientHeader-endvisit-dialog').dialog('close')
				});
			});
			function patientHeaderEndVisit(visitId, stopVisitDate) {
				$j("input[name=visitId]", "#patientHeader-endvisit-dialog").val(visitId);
				if (stopVisitDate) {
					$j("input[name=stopDate]", "#patientHeader-endvisit-dialog").val(stopVisitDate);
				}
				$j('#patientHeader-endvisit-dialog').dialog('open');
			}
		</script>
	
		<c:forEach var="visit" items="${model.activeVisits}">
			<div id="patientVisitsSubheader" class="box" style="margin-top: 2px">
				&nbsp;<strong><openmrs:message code="Visit.active.label" />: <a
					href="<openmrs:contextPath />/admin/visits/visit.form?visitId=${ visit.visitId }&patientId=<c:out value="${model.patient.patientId}" />"><openmrs:format
							visitType="${ visit.visitType }" /></a></strong>
				<c:if test="${ not empty visit.location }">
					<openmrs:message code="general.atLocation" />
					<strong><openmrs:format location="${ visit.location }" /></strong></c:if>
				<openmrs:message code="general.fromDate" />
				<openmrs:formatDate date="${ visit.startDatetime }" showTodayOrYesterday="true" />
				<c:if test="${not empty visit.stopDatetime }">
					<openmrs:message code="general.toDate" />
					<openmrs:formatDate date="${ visit.stopDatetime }" showTodayOrYesterday="true" />
				</c:if>
				<openmrs:hasPrivilege privilege="Edit Visits">
					<input type="button" value="<openmrs:message code="Visit.edit"/>"
						onclick="window.location='<openmrs:contextPath />/admin/visits/visit.form?visitId=${ visit.visitId }&patientId=<c:out value="${model.patient.patientId}" />'" />
					<input type="button" value="<openmrs:message code="Visit.end"/>" onclick="patientHeaderEndVisit('${visit.visitId}', '<openmrs:formatDate date="${visit.stopDatetime}" format="dd/MM/yyyy HH:mm" />');" />
				</openmrs:hasPrivilege>
				<br />&nbsp;
				<c:if test="${empty visit.nonVoidedEncounters}">
					<i><openmrs:message code="Encounter.noEncounters" /></i>
				</c:if>
				<c:forEach var="encounter" items="${visit.nonVoidedEncounters}" varStatus="status">
					<c:set var="viewEncounterUrl" value="${pageContext.request.contextPath}/admin/encounters/encounter.form?encounterId=${encounter.encounterId}"/>
					<c:choose>
						<c:when test="${ model.formToViewUrlMap[encounter.form] != null }">
							<c:url var="viewEncounterUrl" value="${model.formToViewUrlMap[encounter.form]}">
								<c:param name="encounterId" value="${encounter.encounterId}"/>
							</c:url>
						</c:when>
						<c:when test="${ model.formToEditUrlMap[encounter.form] != null }">
							<c:url var="viewEncounterUrl" value="${model.formToEditUrlMap[encounter.form]}">
								<c:param name="encounterId" value="${encounter.encounterId}"/>
							</c:url>
						</c:when>
					</c:choose>
					<a href="${viewEncounterUrl}">
						<openmrs:format encounterType="${encounter.encounterType}" /></a><c:if test="${not status.last}">,</c:if>
				</c:forEach>
				
				<openmrs:hasPrivilege privilege="Add Encounters">
				
					<c:if test="${not empty allAddEncounterToVisitLinks}">
						<div id="patientHeaderAddEncounterToVisit${visit.visitId}Popup">
							<openmrs:format visitType="${ visit.visitType }" var="visitType"/>
							<openmrs:message code="Visit.addEncounterToVisit" arguments="${visitType}"/>
							<c:forEach items="${allAddEncounterToVisitLinks}" var="link">
								<c:url var="linkUrl" value="${link.url}">
									<c:param name="patientId" value="${model.patientId}"/>
									<c:param name="returnUrl" value="${model.returnUrl}"/>
									<c:param name="visitId" value="${visit.visitId}"/>
								</c:url>
								<p><a href="${linkUrl}">${link.label}</a></p>
							</c:forEach>
							
						</div>
						
						<script type="text/javascript">
							$j(document).ready(function() {
								$j('#patientHeaderAddEncounterToVisit${visit.visitId}Popup').dialog({
										title: '<openmrs:message code="Visit.addEncounter"/>',
										autoOpen: false,
										draggable: false,
										resizable: false,
										width: '50%'
								});
							});
							
							function patientHeaderShowAddEncounterToVisit${visit.visitId}Popup() {
								$j('#patientHeaderAddEncounterToVisit${visit.visitId}Popup')
								.dialog('option', 'height', $j(window).height() - 100) 
								.dialog('open');
							}
						</script>
						
						<input type="button" value="<openmrs:message code="Visit.addEncounter"/>"
							onclick="patientHeaderShowAddEncounterToVisit${visit.visitId}Popup()" />
					</c:if>
					
				</openmrs:hasPrivilege>
			</div>
		</c:forEach>
	</openmrs:hasPrivilege>
</c:if>

<div class="column">
	<div class="box noBorder">
		<openmrs:extensionPoint
			pointId="org.openmrs.patientDashboard.afterLastEncounter"
			type="html" parameters="patientId=${model.patient.patientId}" />
	</div>
</div>

<div class="columnEnd"></div>

</div> <%-- Closing div for the patientSubheader box --%>

<script type="text/javascript">
	function showMoreIdentifiers() {
		if (identifierElement.style.display == '') {
			linkElement.innerHTML = '<openmrs:message code="general.nMore" arguments="${fn:length(model.patient.activeIdentifiers) - 2}"/>';
			identifierElement.style.display = "none";
		} else {
			linkElement.innerHTML = '<openmrs:message code="general.nLess" arguments="${fn:length(model.patient.activeIdentifiers) - 2}"/>';
			identifierElement.style.display = "";
		}
	}

	var identifierElement = document
			.getElementById("patientHeaderMoreIdentifiers");
	var linkElement = document
			.getElementById("patientHeaderShowMoreIdentifiers");
	if (identifierElement)
		identifierElement.style.display = "none";
</script>
