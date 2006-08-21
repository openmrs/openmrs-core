<%@ include file="/WEB-INF/template/include.jsp" %>

<div class="boxHeader"><spring:message code="Patient.title"/></div>
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
				<td><openmrs:concept conceptId="${model.patient.civilStatus.conceptId}" nameVar="n" var="v" numericVar="nv">${n.name}</openmrs:concept></td>
				<td>${model.patient.race}</td>
				<td><c:if test='${model.patient.birthplace != "null"}'>${model.patient.birthplace}</c:if></td>
			</tr>
		</tbody>
	</table>
</div>
<br/>
<div class="boxHeader"><spring:message code="Patient.addresses"/></div>
<div class="box">
	<table class="patientAddress">
		<thead>
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
<div id="patientDemographicsEdit">
	<openmrs:hasPrivilege privilege="Edit Patients">
		<a href="<%= request.getContextPath() %>/admin/patients/patient.form?patientId=${model.patient.patientId}"><spring:message code="Patient.edit"/></a><br />
	</openmrs:hasPrivilege>
</div>