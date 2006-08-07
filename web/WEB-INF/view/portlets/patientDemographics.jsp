<%@ include file="/WEB-INF/template/include.jsp" %>
<div class="box">
	<table class="patientAddress">
		<thead>
			<tr>
				<th><spring:message code="Patient.names"/></th>
				<th><spring:message code="Patient.mothersName"/></th>
				<th><spring:message code="Patient.civilStatus"/></th>
				<th><spring:message code="Patient.race"/></th>
				<th><spring:message code="Patient.birthplace"/></th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<td>
					<c:forEach var="name" items="${model.patient.names}" varStatus="status">
						<c:if test="${name == model.patient.patientName}">*</c:if>
						${name.givenName} ${name.middleName} ${name.familyName}<br/>
					</c:forEach>
				</td>
				<td>${model.patient.mothersName}</td>
				<td>${model.patient.civilStatus}</td>
				<td>${model.patient.race}</td>
				<td>${model.patient.birthplace}</td>
			</tr>
		</tbody>
	</table>
</div>
<br/>
<div class="box">
	<table class="patientAddress">
		<thead>
			<tr><th colspan="9" class="tableTitle"><spring:message code="Patient.addresses"/></th><tr>
			<tr>
				<th><spring:message code="general.preferred"/></th>
				<th><spring:message code="Location.address1"/></th>
				<th><spring:message code="Location.address2"/></th>
				<th><spring:message code="Location.cityVillage"/></th>
				<th><spring:message code="Location.stateProvince"/></th>
				<th><spring:message code="Location.postalCode"/></th>
				<th><spring:message code="Location.country"/></th>
				<th><spring:message code="Location.latitude"/></th>
				<th><spring:message code="Location.longitude"/></th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<c:forEach var="address" items="${model.patient.addresses}" varStatus="status">
					<td><c:if test="${address.preferred}">*</c:if></td>
					<td>${address.address1}</td>
					<td>${address.address2}</td>
					<td>${address.cityVillage}</td>
					<td>${address.stateProvince}</td>
					<td>${address.postalCode}</td>
					<td>${address.country}</td>
					<td>${address.latitude}</td>
					<td>${address.longitude}</td>
				</c:forEach>
			</tr>
		</tbody>
	</table>
</div>
<br><br>
Example using openmrs:forEachObs - 

<openmrs:forEachObs obs="${model.patientObs}" conceptId="5089" var="o" num="1" descending="true">
	${o.valueNumeric} (<openmrs:formatDate date="${o.obsDatetime}" />)
</openmrs:forEachObs>

<br><br><br>

Example using c:forEach tag, openmrs:sort function, openmrs:filterObsByConcept function -

<c:forEach items="${openmrs:sort(openmrs:filterObsByConcept(model.patientObs, 5089), 'obsDatetime', true)}" var="o" end="0">
	${o.valueNumeric} (<openmrs:formatDate date="${o.obsDatetime}" />)
</c:forEach>

<br><br><br>

Example using mostRecentObs tag file, which includes c:forEach tag, openmrs:sort function, openmrs:filterObsByConcept function -

<openmrs_tag:mostRecentObs observations="${model.patientObs}" concept="5089" />

<br><br><br>

Example using lastNObs tag file:

<openmrs_tag:lastNObs observations="${model.patientObs}" concept="5089" n="3" separator=", " />

