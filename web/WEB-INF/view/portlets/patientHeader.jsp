<%@ include file="/WEB-INF/template/include.jsp" %>

<c:set var="HIV_PROGRAM_CONCEPT_ID" value="1482" />
<c:set var="TB_PROGRAM_CONCEPT_ID" value="1483" />

<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/index.htm" />

	<%-- Header showing preferred name, id, and treatment status --%>
	<div id="patientHeader" class="boxHeader">
		<table>
			<tr>
				<td class="patientName">${model.patient.patientName.givenName} ${model.patient.patientName.middleName} ${model.patient.patientName.familyName}&nbsp;&nbsp</td>
				<td>
					<c:if test="${model.patient.gender == 'M'}"><spring:message code="Patient.gender.male"/></c:if>
					<c:if test="${model.patient.gender == 'F'}"><spring:message code="Patient.gender.female"/></c:if>
				</td>
				<td>|</td>
				<td>
					<c:if test="${model.patient.age > 0}">${model.patient.age} <spring:message code="Patient.age.years"/></c:if>
					<c:if test="${model.patient.age == 0}">< 1 <spring:message code="Patient.age.year"/></c:if>
					(<c:if test="${model.patient.birthdateEstimated}">~</c:if><openmrs:formatDate date="${model.patient.birthdate}" type="medium" />)
				</td>
				<td>|</td>
				<td>
					<c:forEach var="identifier" items="${model.patient.identifiers}" varStatus="status">
						${identifier.identifierType.name}: ${identifier.identifier}
						<c:if test="${!status.last}">&nbsp;&nbsp;|&nbsp;&nbsp;</c:if>
					</c:forEach>
				</td>
				<td>&nbsp;</td>
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
		<table><tr>
			<td><spring:message code="Patient.weight"/>:</td>
			<th><openmrs_tag:mostRecentObs observations="${model.patientObs}" concept="5089" showUnits="true" locale="${model.locale}" showDate="true" /></th>
			<td>|</td>
			<td><spring:message code="Patient.cd4"/>:</td>
			<th><openmrs_tag:mostRecentObs observations="${model.patientObs}" concept="5497" locale="${model.locale}" /></th>
			<td>|</td>
			<td><spring:message code="Patient.regimen" />:</td>
			<th>
				<c:forEach items="${model.patientDrugOrders}" var="drugOrder" varStatus="drugOrderStatus">
					<c:if test="${drugOrder.current}">
						${drugOrder.drug.name}
					</c:if>
				</c:forEach>
			</th>
		</tr></table>
		<table><tr>
			<td><spring:message code="Patient.lastEncounter"/>:</td>
			<th>
				<c:forEach items='${openmrs:sort(encounters, "encounterDatetime", true)}' var="lastEncounter" varStatus="lastEncounterStatus" end="0">
					${lastEncounter.encounterType.name} @ ${lastEncounter.location.name}, <openmrs:formatDate date="${lastEncounter.encounterDatetime}" type="medium" />
				</c:forEach>
			</th>
		</tr></table>
	</div>