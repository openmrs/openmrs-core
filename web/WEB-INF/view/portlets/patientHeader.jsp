<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/formentry/index.htm" />

	<%-- Header showing preferred name, id, and treatment status --%>
	<div id="patientHeader" class="boxHeader">
		<div>
			<span class="patientName">${model.patient.patientName.givenName} ${model.patient.patientName.middleName} ${model.patient.patientName.familyName}</span>
			<c:if test="${model.patient.gender == 'M'}"><spring:message code="Patient.gender.male"/></c:if>
			<c:if test="${model.patient.gender == 'F'}"><spring:message code="Patient.gender.female"/></c:if>
			&nbsp;|&nbsp;
			<c:if test="${model.patient.age > 0}">${model.patient.age} <spring:message code="Patient.age.years"/></c:if>
			<c:if test="${model.patient.age == 0}">< 1 <spring:message code="Patient.age.year"/></c:if>
			(<c:if test="${model.patient.birthdateEstimated}">~</c:if><openmrs:formatDate date="${model.patient.birthdate}" type="medium" />)
			&nbsp;|&nbsp;
			<c:forEach var="identifier" items="${model.patient.identifiers}" varStatus="status">
				${identifier.identifierType.name}: ${identifier.identifier}
				<c:if test="${!status.last}">&nbsp;&nbsp;|&nbsp;&nbsp;</c:if>
			</c:forEach>
		</div>
		<div>
			<c:forEach items="${model.patientCurrentPrograms}" var="p" varStatus="s">
				<c:if test="${p.program.concept.conceptId == 0}">
					<spring:message code="Program.hiv"/>
					&nbsp;|&nbsp;
					<spring:message code="Program.enrolled"/>: <openmrs:formatDate date="${p.dateEnrolled}" type="medium" />
					&nbsp;|&nbsp;
					<spring:message code="Program.group"/>: <openmrs_tag:mostRecentObs observations="${model.patientObs}" concept="1377" locale="${model.locale}" />
					&nbsp;|&nbsp;
					<spring:message code="Program.agent"/>: 
				</c:if>
			</c:forEach>
		</div>
		<div>
			<c:forEach items="${model.patientCurrentPrograms}" var="p" varStatus="s">
				<c:if test="${p.program.concept.conceptId == 0}">
					<spring:message code="Program.tb"/>
					&nbsp;|&nbsp;
					<spring:message code="Program.enrolled"/>: <openmrs:formatDate date="${p.dateEnrolled}" type="medium" />
					&nbsp;|&nbsp;
					<spring:message code="Program.group"/>: <openmrs_tag:mostRecentObs observations="${model.patientObs}" concept="1378" locale="${model.locale}" />
				</c:if>
			</c:forEach>
		</div>
		<div>
			<spring:message code="Patient.weight"/>:
			<openmrs_tag:mostRecentObs observations="${model.patientObs}" concept="5089" showUnits="true" locale="${model.locale}" showDate="true" />
			&nbsp;|&nbsp;
			<spring:message code="Patient.cd4"/>:
			<openmrs_tag:mostRecentObs observations="${model.patientObs}" concept="5497" locale="${model.locale}" />
			&nbsp;|&nbsp;
			<spring:message code="Patient.regimen" />:
			<c:forEach items='${openmrs:filterObsByConcept(model.patientObs, "1088")}' var="arv" varStatus="arvStatus">
				<openmrs:concept conceptId="${arv.valueCoded.conceptId}" var="c" nameVar="n" numericVar="num">
					${n.name}<c:if test="${!arvStatus.last}">, </c:if>
				</openmrs:concept>
			</c:forEach>
		</div>
		<div>
			<spring:message code="Patient.lastEncounter"/>:
			<c:forEach items='${openmrs:sort(encounters, "encounterDatetime", true)}' var="lastEncounter" varStatus="lastEncounterStatus" end="0">
				${lastEncounter.encounterType.name} @ ${lastEncounter.location.name}, <openmrs:formatDate date="${lastEncounter.encounterDatetime}" type="medium" />
			</c:forEach>
		</div>
	</div>