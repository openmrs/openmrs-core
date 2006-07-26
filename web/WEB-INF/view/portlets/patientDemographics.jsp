<%@ include file="/WEB-INF/template/include.jsp" %>

	<h3><spring:message code="Patient.names"/></h3>
	<c:forEach var="name" items="${model.patient.names}" varStatus="status">
		${name.givenName} ${name.middleName} ${name.familyName}
		${name.preferred}
	</c:forEach>
	
	<table>
		<thead>
			<tr>
				<th>Patient attributes</th>
				<th>Current value</th>
				<th>New value</th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<td><spring:message code="Patient.gender"/>:</td>
				<td>
					<c:if test="${model.patient.gender == 'M'}"><spring:message code="Patient.gender.male"/></c:if>
					<c:if test="${model.patient.gender == 'F'}"><spring:message code="Patient.gender.female"/></c:if>
				</td>
				<td>Input field here</td>
			</tr>
			<tr>
				<td><spring:message code="Patient.race"/>:</td>
				<td>${model.patient.race}</td>
				<td>Input field here</td>
			</tr>
			<tr>
				<td><spring:message code="Patient.birthdate"/>:</td>
				<td><openmrs:formatDate date="${model.patient.birthdate}" />, Estimated: ${model.patient.birthdateEstimated}</td>
				<td>Input field here</td>
			</tr>
			<tr>
				<td><spring:message code="Patient.birthplace"/>:</td>
				<td>${model.patient.birthplace}</td>
				<td>Input field here</td>
			</tr>
			<tr>
				<td><spring:message code="Patient.mothersName"/>:</td>
				<td>${model.patient.mothersName}</td>
				<td>Input field here</td>
			</tr>
			<tr>
				<td><spring:message code="Patient.civilStatus"/>:</td>
				<td>${model.patient.civilStatus}</td>
				<td>Input field here</td>
			</tr>
			<tr>
				<td><spring:message code="Patient.dead"/>:</td>
				<td>${model.patient.dead} (<openmrs:formatDate date="${model.patient.deathDate}" />) - ${model.patient.causeOfDeath}</td>
				<td>Input field here</td>
			</tr>
			<tr>
				<td><spring:message code="Patient.healthDistrict"/>:</td>
				<td>${model.patient.healthDistrict}</td>
				<td>Input field here</td>
			</tr>
			<tr>
				<td><spring:message code="Patient.healthCenter"/>:</td>
				<td>${model.patient.healthCenter}</td>
				<td>Input field here</td>
			</tr>
		</tbody>
	</table>
	
	<h3><spring:message code="Patient.identifiers"/></h3>
	<c:forEach var="id" items="${model.patient.identifiers}" varStatus="status">
		${id.identifierType.name}: ${id.identifier}, ${id.location} Preferred: ${id.preferred}
	</c:forEach>
	
	<h3><spring:message code="Patient.addresses"/></h3>
	<c:forEach var="address" items="${model.patient.addresses}" varStatus="status">
		${address.patientAddressId}
		${address.address1}
		${address.address2}
		${address.cityVillage}
		${address.stateProvince}
		${address.country}
		${address.postalCode}
		${address.latitude}
		${address.longitude}
		${address.preferred}
	</c:forEach>