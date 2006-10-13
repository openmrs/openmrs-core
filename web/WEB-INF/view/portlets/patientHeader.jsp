<%@ include file="/WEB-INF/template/include.jsp" %>

<c:set var="HIV_PROGRAM_CONCEPT_ID" value="1482" />
<c:set var="TB_PROGRAM_CONCEPT_ID" value="1483" />

<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/index.htm" />

	<%-- Header showing preferred name, id, and treatment status --%>
	<div id="patientHeader" class="boxHeader">
		<div id="patientHeaderPatientName">${model.patient.patientName.givenName} ${model.patient.patientName.middleName} ${model.patient.patientName.familyName}</div>
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
					<c:if test="${model.patient.gender == 'M'}"><img src="${pageContext.request.contextPath}/images/male.gif" alt='<spring:message code="Patient.gender.male"/>'/></c:if>
					<c:if test="${model.patient.gender == 'F'}"><img src="${pageContext.request.contextPath}/images/female.gif" alt='<spring:message code="Patient.gender.female"/>'/></c:if>
				</td>
				<td id="patientHeaderPatientAge">
					<c:if test="${model.patient.age > 0}">${model.patient.age} <spring:message code="Patient.age.years"/></c:if>
					<c:if test="${model.patient.age == 0}">< 1 <spring:message code="Patient.age.year"/></c:if>
					<span id="patientHeaderPatientBirthdate"><c:if test="${not empty model.patient.birthdate}">(<c:if test="${model.patient.birthdateEstimated}">~</c:if><openmrs:formatDate date="${model.patient.birthdate}" type="medium" />)</c:if><c:if test="${empty model.patient.birthdate}"><spring:message code="general.unknown"/></c:if></span>
				</td>
				<openmrs:globalProperty key="use_patient_attribute.tribe" defaultValue="false" var="showTribe"/>
				<c:if test="${showTribe}">
					<td id="patientHeaderPatientTribe">
						<spring:message code="Patient.tribe"/>:
						<b>${model.patient.tribe.name}</b>
					</td>
				</c:if>
				<openmrs:globalProperty key="use_patient_attribute.healthCenter" defaultValue="false" var="showHealthCenter"/>
				<c:if test="${showHealthCenter && not empty model.patient.healthCenter}">
					<td id="patientHeaderHealthCenter">
						<spring:message code="Patient.healthCenter"/>:
						<b>${model.patient.healthCenter.name}</b>
					</td>
				</c:if>
				<td id="patientHeaderPatientSummary">
					<a class="offColor" href="javascript:window.open('patientSummary.htm?patientId=${model.patientId}', 'summaryWindow', 'toolbar=no,width=800,height=600,resizable=yes,scrollbars=yes').focus()">Summary</a>
				</td>
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
						<small><a id="patientHeaderShowMoreIdentifiers" onclick="return showMoreIdentifiers()" title='<spring:message code="patientDashboard.showMoreIdentifers"/>'><spring:message code="general.nMore" arguments="${fn:length(model.patient.activeIdentifiers) - 2}"/> <span id="patientHeaderMoreIdentifiersArrow">&dArr;</span></a></small>
					</td>
				</c:if>
			</tr>
		</table>
	</div>
	<div id="patientSubheader" class="box">
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
							<c:if test="${r.relationship.relationshipTypeId == 1}">
								<c:if test="${accompFound}">, </c:if>
								<c:if test="${r.person.patient != null}">
									${r.person.patient.patientName.givenName} ${r.person.patient.patientName.middleName} ${r.person.patient.patientName.familyName}
								</c:if>
								<c:if test="${r.person.user != null}">
									${r.person.user.firstName} ${r.person.user.lastName} 
								</c:if>
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
					<spring:message code="FormEntry.no.last.encounters"/>
				</c:if>	
			</th>
		</tr></table>
	</div>
	
	<script type="text/javascript">
		function showMoreIdentifiers() {
			if (identifierElement.style.display == '') {
				identifierArrow.innerHTML = "&dArr";
				identifierElement.style.display = "none";
			}
			else {
				identifierArrow.innerHTML = "&uArr";
				identifierElement.style.display = "";
			}
		}
		
		var identifierElement = document.getElementById("patientHeaderMoreIdentifiers");
		var identifierArrow = document.getElementById("patientHeaderMoreIdentifiersArrow");
		if (identifierElement)
			identifierElement.style.display = "none";
		
	</script>
	