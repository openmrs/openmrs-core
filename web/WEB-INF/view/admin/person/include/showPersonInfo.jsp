<table>
	<tr>
		<th align="left"><spring:message code="general.id"/></th>
		<td>${patient.patientId}</td>
	</tr>
	<tr>
		<th align="left"><spring:message code="Person.gender"/></th>
		<td>
			<c:choose>
				<c:when test="${patient.gender == 'M'}">
					<img src="${pageContext.request.contextPath}/images/male.gif" />
				</c:when>
				<c:when test="${patient.gender == 'F'}">
					<img src="${pageContext.request.contextPath}/images/female.gif" />
				</c:when>
				<c:otherwise>${patient.gender}</c:otherwise>
			</c:choose>
		</td>
	</tr>
	<tr>
		<th align="left"><spring:message code="Person.birthdate"/></th>
		<td><openmrs:formatDate date="${patient.birthdate}" type="short" /></td>
	</tr>
	<openmrs:forEachDisplayAttributeType personType="patient" displayType="viewing" var="attrType">
		<tr>
			<th align="left"><spring:message code="PersonAttributeType.${fn:replace(attrType.name, ' ', '')}" text="${attrType.name}"/></th>
			<td>${patient.attributeMap[attrType.name].hydratedObject}</td>
		</tr>
	</openmrs:forEachDisplayAttributeType>
	<tr>
		<th align="left"><spring:message code="Person.deathDate"/></th>
		<td><openmrs:formatDate date="${patient.deathDate}" type="short" /></td>
	</tr>
	<tr>
		<th align="left"><spring:message code="general.createdBy" /></th>
		<td>
			${patient.creator.personName} -
			<openmrs:formatDate date="${patient.dateCreated}" type="long" />
		</td>
	</tr>
	<tr>
		<th align="left"><spring:message code="general.changedBy" /></th>
		<td>
			${patient.changedBy.personName} -
			<openmrs:formatDate date="${patient.dateChanged}" type="long" />
		</td>
	</tr>
	<tr>
		<th align="left"><spring:message code="general.voided"/></th>
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