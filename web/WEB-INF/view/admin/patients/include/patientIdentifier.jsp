<table>
	<tr>
		<td>Identifier</td>
		<td>
			<spring:bind path="identifier">
				<input type="text" name="${status.expression}" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
			</td>
	</tr>
	<tr>
		<td>Type</td>
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
		<td>Location</td>
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
	<c:if test="${!(identifier.creator == null)}" >
		<tr>
			<td>Creator</td>
			<td>
				<spring:bind path="creator">
					${identifier.creator.username}
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td>Date Created</td>
			<td>
				<spring:bind path="dateCreated">
					<openmrs:formatDate date="${identifier.dateCreated}" type="long"/>
				</spring:bind>
			</td>
		</tr>
	</c:if>
</table>