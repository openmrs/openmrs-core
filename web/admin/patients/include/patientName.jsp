
<!-- Assumes spring:nestedPath already set on PatientName object -->
<table>
	<tr>
		<td><spring:message code="PatientName.preferred"/></td>
		<td>
			<spring:bind path="preferred">
				<input type="hidden" name="_${status.expression}">
				<input type="checkbox" name="${status.expression}" id="preferred" <c:if test="${status.value == true}">checked</c:if> />
				${status.errorMessage}
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="PatientName.givenName"/></td>
		<td>
			<spring:bind path="givenName">
				<input type="text" name="${status.expression}" id="givenName" value="${status.value}" onKeyUp="modifyTab(this, this.value, 0);" />
				${status.errorMessage}
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="PatientName.middleName"/></td>
		<td>
			<spring:bind path="middleName">
				<input type="text" name="${status.expression}" id="middleName" value="${status.value}" />
				${status.errorMessage}
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="PatientName.familyName"/></td>
		<td>
			<spring:bind path="familyNamePrefix">
				<input type="text" name="${status.expression}" id="familyNamePrefix" size="10" value="${status.value}" />
				${status.errorMessage}
			</spring:bind>
			<spring:bind path="familyName">
				<input type="text" name="${status.expression}" id="familyName" value="${status.value}" onKeyUp="modifyTab(this, this.value, 2);" />
				${status.errorMessage}
			</spring:bind>
			<spring:bind path="familyName2">
				<input type="text" name="${status.expression}" id="familyName2" value="${status.value}" />
				${status.errorMessage}
			</spring:bind>
			<spring:bind path="familyNameSuffix">
				<input type="text" name="${status.expression}" id="familyNameSuffix" size="10" value="${status.value}" />
				${status.errorMessage}
			</spring:bind>
		</td>
	</tr>	
	<tr>
		<td><spring:message code="PatientName.degree"/></td>
		<td>
			<spring:bind path="degree">
				<input type="text" name="${status.expression}" id="degree" value="${status.value}" />
				${status.errorMessage}
			</spring:bind>
		</td>
	</tr>
	<c:if test="${!(address.creator == null)}" >
		<tr>
			<td><spring:message code="general.creator"/></td>
			<td>
				<spring:bind path="creator">
					${address.creator.username}
					<input type="hidden" name="${status.expression}" value="${status.value}"/>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td><spring:message code="general.dateCreated"/></td>
			<td>
				<spring:bind path="dateCreated">
					<openmrs:formatDate date="${address.dateCreated}" type="long"/>
					<input type="hidden" name="${status.expression}" value="${status.value}">
				</spring:bind>
			</td>
		</tr>
		<spring:bind path="patientAddressId">
			<input type="text" name="${status.expression}" value="${status.value}">
		</spring:bind>
	</c:if>
</table>
