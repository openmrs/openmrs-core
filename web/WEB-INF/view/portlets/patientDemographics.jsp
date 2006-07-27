<%@ include file="/WEB-INF/template/include.jsp" %>
	<table>
		<thead>
			<tr><th><spring:message code="Patient.addresses"/></th></tr>
		</thead>
		<tbody>
			<c:forEach var="address" items="${model.patient.addresses}" varStatus="status">
				<c:if test="${address.address1 != null}"><tr><td>${address.address1}</td></tr></c:if>
				<c:if test="${address.address2 != null}"><tr><td>${address.address2}</td></tr></c:if>
				<c:if test="${address.cityVillage != null}"><tr><td>${address.cityVillage}</td></tr></c:if>
				<c:if test="${address.stateProvince != null}"><tr><td>${address.stateProvince}</td></tr></c:if>
				<c:if test="${address.postalCode != null}"><tr><td>${address.postalCode}</td></tr></c:if>
				<c:if test="${address.country != null}"><tr><td>${address.country}</td></tr></c:if>
				<c:if test="${address.latitude != null}"><tr><td>${address.latitude}</td></tr></c:if>
				<c:if test="${address.longitude != null}"><tr><td>${address.longitude}</td></tr></c:if>
				<tr><td>&nbsp;</td></tr>
			</c:forEach>
		</tbody>
	</table>
	<table>
		<tr>
			<td>Additional patient names:</td>
			<td>
				<c:forEach var="name" items="${model.patient.names}" varStatus="status">
					<c:if test="${name != model.patient.patientName}">${name.givenName} ${name.middleName} ${name.familyName}</c:if>
				</c:forEach>
			</td>
		</tr>
		<tr>
			<td><spring:message code="Patient.race"/>:</td>
			<td>${model.patient.race}</td>
		</tr>
		<tr>
			<td><spring:message code="Patient.birthplace"/>:</td>
			<td>${model.patient.birthplace}</td>
		</tr>
		<tr>
			<td><spring:message code="Patient.mothersName"/>:</td>
			<td>${model.patient.mothersName}</td>
		</tr>
		<tr>
			<td><spring:message code="Patient.civilStatus"/>:</td>
			<td>${model.patient.civilStatus}</td>
		</tr>
	</table>
	