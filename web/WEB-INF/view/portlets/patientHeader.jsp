<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/formentry/index.htm" />

	<%-- Header showing preferred name, id, and treatment status --%>
	<table id="patientHeader" class="boxHeader">
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
		</tr>
	</table>
	<div id="patientSubheader" class="box">
		<c:forEach items="${model.patientCurrentPrograms}" var="p" varStatus="s">
			<c:if test="${p.program.concept.conceptId == 0}">
				<table><tr>
					<td><spring:message code="Program.hiv"/></td>
					<td>|</td>
					<td><spring:message code="Program.enrolled"/>: <openmrs:formatDate date="${p.dateEnrolled}" type="medium" /></td>
					<td>|</td>
					<td><spring:message code="Program.group"/>: <openmrs_tag:mostRecentObs observations="${model.patientObs}" concept="1377" locale="${model.locale}" /></td>
					<td>|</td>
					<td><openmrs:portlet id="headerAccompagnateur" url="patientRelationships" size="normal" patientId="${patient.patientId}" parameters="allowEditShownTypes=false|allowAddShownTypes=false|allowAddOtherTypes=false|allowVoid=true|showFrom=false|showTo=true|showTypes=Accompagnateur|showOtherTypes=false"/></td>
				</tr></table>
			</c:if>
		</c:forEach>
		<c:forEach items="${model.patientCurrentPrograms}" var="p" varStatus="s">
			<c:if test="${p.program.concept.conceptId == 0}">
				<table><tr>
					<td><spring:message code="Program.tb"/></td>
					<td>|</td>
					<td><spring:message code="Program.enrolled"/>: <openmrs:formatDate date="${p.dateEnrolled}" type="medium" /></td>
					<td>|</td>
					<td><spring:message code="Program.group"/>: <openmrs_tag:mostRecentObs observations="${model.patientObs}" concept="1378" locale="${model.locale}" /></td>
				</tr></table>
			</c:if>
		</c:forEach>
		<table><tr>
			<td>
				<spring:message code="Patient.weight"/>:
				<openmrs_tag:mostRecentObs observations="${model.patientObs}" concept="5089" showUnits="true" locale="${model.locale}" showDate="true" />
			</td>
			<td>|</td>
			<td>
				<spring:message code="Patient.cd4"/>:
				<openmrs_tag:mostRecentObs observations="${model.patientObs}" concept="5497" locale="${model.locale}" />
			</td>
			<td>|</td>
			<td>
				<spring:message code="Patient.regimen" />:
				<c:forEach items="${model.patientDrugOrders}" var="drugOrder" varStatus="drugOrderStatus">
					<c:if test="${drugOrder.current}">
						${drugOrder.drug.name}
					</c:if>
				</c:forEach>
			</td>
		</tr></table>
		<table><tr>
			<td>
				<spring:message code="Patient.lastEncounter"/>:
				<c:forEach items='${openmrs:sort(encounters, "encounterDatetime", true)}' var="lastEncounter" varStatus="lastEncounterStatus" end="0">
					${lastEncounter.encounterType.name} @ ${lastEncounter.location.name}, <openmrs:formatDate date="${lastEncounter.encounterDatetime}" type="medium" />
				</c:forEach>
			</td>
		</tr></table>
	</div>