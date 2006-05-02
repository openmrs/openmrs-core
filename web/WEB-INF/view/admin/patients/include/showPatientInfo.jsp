<table>
	<tr>
		<td><spring:message code="general.id"/></td>
		<td>${patient.patientId}</td>
	</tr>
	<tr>
		<td><spring:message code="Patient.gender"/></td>
		<td>
			<c:choose>
				<c:when test="${patient.gender == 'M'}">
					<img src="${pageContext.request.contextPath}/images/male.gif" />
				</c:when>
				<c:when test="${patient.gender == 'F'}">
					<img src="${pageContext.request.contextPath}/images/female.gif" />
				</c:when>
				<c:otherwise>"${patient.gender}"</c:otherwise>
			</c:choose>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Patient.race"/></td>
		<td>${patient.race}</td>
	</tr>
	<tr>
		<td><spring:message code="Patient.birthdate"/></td>
		<td><openmrs:formatDate date="${patient.birthdate}" type="short" /></td>
	</tr>
	<tr>
		<td><spring:message code="Patient.birthplace"/></td>
		<td>${patient.birthplace}</td>
	</tr>
	<tr>
		<td><spring:message code="Patient.tribe"/></td>
		<td>${patient.tribe.name}</td>
	</tr>
	<tr>
		<td><spring:message code="Patient.citizenship"/></td>
		<td>${patient.citizenship}</td>
	</tr>
	<tr>
		<td><spring:message code="Patient.mothersName"/></td>
		<td>${patient.mothersName}</td>
	</tr>
	<tr>
		<td><spring:message code="Patient.civilStatus"/></td>
		<td>
			<c:if test="${patient.civilStatus != null}">
				<openmrs:forEachRecord name="civilStatus" select="${status.value}">
					<c:if test="${record.key == patient.civilStatus.conceptId}">
						${record.value}
					</c:if>
				</openmrs:forEachRecord>
			</c:if>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Patient.deathDate"/></td>
		<td><openmrs:formatDate date="${patient.deathDate}" type="short" /></td>
	</tr>
	<tr>
		<td><spring:message code="Patient.healthDistrict"/></td>
		<td>${patient.healthDistrict}</td>
	</tr>
	<tr>
		<td><spring:message code="Patient.healthCenter"/></td>
		<td>${patient.healthCenter}</td>
	</tr>
	<tr>
		<td><spring:message code="general.createdBy" /></td>
		<td>
			${patient.creator.firstName} ${patient.creator.lastName} -
			<openmrs:formatDate date="${patient.dateCreated}" type="long" />
		</td>
	</tr>
	<tr>
		<td><spring:message code="general.changedBy" /></td>
		<td>
			${patient.changedBy.firstName} ${patient.changedBy.lastName} -
			<openmrs:formatDate date="${patient.dateChanged}" type="long" />
		</td>
	</tr>
	<tr>
		<td><spring:message code="general.voided"/></td>
		<td>
			<c:choose>
				<c:when test="${patient.voided}">
					<spring:message code="general.yes"/>
				</c:when>
				<c:otherwise>
					<spring:message code="general.no"/>
				</c:otherwise>
			</c:choose>
		</td>
	</tr>
</table>