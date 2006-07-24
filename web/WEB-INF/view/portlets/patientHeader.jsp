<%@ include file="/WEB-INF/template/include.jsp" %>

<c:if test="${model.authenticatedUser != null}">

	<%-- Header showing preferred name, id, and treatment status --%>
	<div id="patientHeader" class="boxHeader">
		<span class="patientName">
			<c:forEach var="name" items="${model.patient.names}" varStatus="status">
				<c:if test="${name.preferred}">${name.givenName} ${name.middleName} ${name.familyName}</c:if>
			</c:forEach>
		</span>
		<c:if test="${model.patient.gender == 'M'}"><spring:message code="Patient.gender.male"/></c:if>
		<c:if test="${model.patient.gender == 'F'}"><spring:message code="Patient.gender.female"/></c:if>
		&nbsp;|&nbsp;
		<c:if test="${model.patient.age > 0}">${model.patient.age} <spring:message code="Patient.age.years"/></c:if>
		<c:if test="${model.patient.age == 0}">< 1 <spring:message code="Patient.age.year"/></c:if>
		<spring:bind path="patient.birthdate">(${status.value})</spring:bind>
		&nbsp;|&nbsp;
		<spring:message code="Patient.healthCenter"/>: TBD
		&nbsp;|&nbsp;
		Accompagnateur: TBD
		&nbsp;|&nbsp;
		Current Status: TBD
		<br/>
		<c:forEach var="identifier" items="${model.patient.identifiers}" varStatus="status">
			${identifier.identifierType.name}: ${identifier.identifier}
			<c:if test="${!status.last}">&nbsp;&nbsp;|&nbsp;&nbsp;</c:if>
		</c:forEach>
	</div>
	<div id="patientTreatmentHeader" class="box">
	
		<table class="patientRecentObs">
			<thead>
				<tr>
					<th colspan="2">Latest Observations</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td><openmrs:concept conceptId="5497" var="c" nameVar="n">${n.name}:</openmrs:concept></td>
					<td>
						<openmrs:recentObs obs="${model.patientObs}" conceptId="5089" var="o">
							${o.valueNumeric} (<openmrs:formatDate date="${o.obsDatetime}" />)
						</openmrs:recentObs>
					</td>
				</tr>
				<tr>
					<td><openmrs:concept conceptId="5089" var="c" nameVar="n">${n.name}:</openmrs:concept></td>
					<td>
						<openmrs:recentObs obs="${model.patientObs}" conceptId="5497" var="cd4">
							${cd4.valueNumeric} (<openmrs:formatDate date="${cd4.obsDatetime}" />)
						</openmrs:recentObs>
					</td>
				</tr>
			</tbody>
		</table>

		<table class="patientTreatmentPrograms">
			<thead>
				<tr>
					<th colspan="4">Treatment Programs</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td>HIV+</td>
					<td>Group 1</td>
					<td>Triomune-30 (1 Co, 2/j)</td>
					<td>10/09/2005</td>
				</tr>
				<tr>
					<td>TB Active</td>
					<td>Group 4</td>
					<td>RHEZ (3 Co, 1/j)</td>
					<td>07/11/2005</td>
				</tr>
			</tbody>
		</table>
	</div>

</c:if>