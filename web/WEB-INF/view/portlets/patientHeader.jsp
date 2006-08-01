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
			<spring:bind path="patient.birthdate">(<c:if test="${model.patient.birthdateEstimated}">~</c:if>${status.value})</spring:bind>
			&nbsp;|&nbsp;
			<spring:message code="Patient.healthCenter"/>:
			<openmrs:forEachEncounter encounters="${encounters}" num="1" sortBy="encounterDatetime" descending="true" var="enc">
				${enc.location.name}
			</openmrs:forEachEncounter>
		</div>
		<div>
			<c:forEach var="identifier" items="${model.patient.identifiers}" varStatus="status">
				${identifier.identifierType.name}: ${identifier.identifier}
				<c:if test="${!status.last}">&nbsp;&nbsp;|&nbsp;&nbsp;</c:if>
			</c:forEach>
		</div>
		<div>
			Accompagnateur: TBD
			&nbsp;|&nbsp;
			Current Status: TBD
		</div>
	</div>
	<div id="patientTreatmentHeader" class="box">
		<table><tr>
			<td>
				<table class="patientRecentObs">
					<thead>
						<tr>
							<th colspan="2"><spring:message code="Obs.list.title"/></th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td><openmrs:concept conceptId="5089" var="c" nameVar="n">${n.name}:</openmrs:concept></td>
							<td>
								<openmrs:forEachObs obs="${model.patientObs}" conceptId="5089" var="o" num="1" descending="true">
									${o.valueNumeric} (<openmrs:formatDate date="${o.obsDatetime}" />)
								</openmrs:forEachObs>
							</td>
						</tr>
						<tr>
							<td><openmrs:concept conceptId="5497" var="c" nameVar="n">${n.name}:</openmrs:concept></td>
							<td>
								<openmrs:forEachObs obs="${model.patientObs}" conceptId="5497" var="cd4" num="1" descending="true">
									${cd4.valueNumeric} (<openmrs:formatDate date="${cd4.obsDatetime}" />)
								</openmrs:forEachObs>
							</td>
						</tr>
					</tbody>
				</table>
			</td>
			<td>
				<table class="patientTreatmentPrograms">
					<thead>
						<tr>
							<th colspan="2">Current Regimen</th>
							<!-- 
							<th>Treatment</th>
							<th>Group</th>
							<th>Regimen</th>
							<th>Start Date</th>
							 -->
						</tr>
					</thead>
					<tbody>
						<tr>
							<td>ARV:</td>
							<td>
								<openmrs:forEachObs obs="${model.patientObs}" conceptId="1088" var="arv">
									<openmrs:concept conceptId="${arv.valueCoded.conceptId}" var="c" nameVar="n">${n.name}, </openmrs:concept>
								</openmrs:forEachObs>
							</td>
							<!-- 
							<td>HIV+</td>
							<td>Group 1</td>
							<td>Triomune-30 (1 Co, 2/j)</td>
							<td>10/09/2005</td> -->
						</tr>
						<!-- 
						<tr>
							<td>TB Active</td>
							<td>Group 4</td>
							<td>RHEZ (3 Co, 1/j)</td>
							<td>07/11/2005</td>
						</tr>
						 -->
					</tbody>
				</table>
			</td>
		</tr></table>
	</div>