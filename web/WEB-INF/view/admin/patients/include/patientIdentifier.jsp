<table>
	<tr>
		<td><spring:message code="PatientIdentifier.identifier"/></td>
		<td>
			<spring:bind path="identifier">
				<input type="text" 
						name="${status.expression}" 
						value="${status.value}" 
						onKeyUp="return true; validateIdentifier(this, 'saveButton', '<spring:message code="error.identifier"/>');"
						onChange="return true; validateIdentifier(this, 'saveButton', '<spring:message code="error.identifier"/>');"/>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
			</td>
	</tr>
	<tr>
		<td><spring:message code="PatientIdentifier.identifierType"/></td>
		<td>
			<spring:bind path="identifierType">
				<select name="${status.expression}" onclick="modifyTab(this, this.options[this.selectedIndex].text, 0)">
					<openmrs:forEachRecord name="patientIdentifierType">
						<option value="${record.patientIdentifierTypeId}" <c:if test="${record.patientIdentifierTypeId == status.value}">selected</c:if>>
							${record.name}
						</option>
					</openmrs:forEachRecord>
				</select>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="PatientIdentifier.location"/></td>
		<td>
			<spring:bind path="location">
				<select name="${status.expression}">
					<openmrs:forEachRecord name="location">
						<option value="${record.locationId}" <c:if test="${record.locationId == status.value}">selected</c:if>>
							${record.name}
						</option>
					</openmrs:forEachRecord>
				</select>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<spring:bind path="creator">
		<c:if test="${!(status.value == null)}">
			<tr>
				<td><spring:message code="general.createdBy" /></td>
				<td>
					${status.value.firstName} ${status.value.lastName} -
					<spring:bind path="dateCreated">
						${status.value}
					</spring:bind>
				</td>
			</tr>
		</c:if>
	</spring:bind>
</table>