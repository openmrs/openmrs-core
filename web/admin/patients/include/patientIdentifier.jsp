<table>
	<tr>
		<td>Identifier</td>
		<td>
			<spring:bind path="identifier">
				<input type="text" name="${status.expression}" id="identifier" value="${status.value}" />
				<span class="error">${status.errorMessage}</span>
			</spring:bind>
			</td>
	</tr>
	<tr>
		<td>Type</td>
		<td>
			<spring:bind path="identifierType">
				<select name="${status.expression}" onclick="modifyTab(this, this.options[this.selectedIndex].text, 0)">
					<openmrs:forEachRecord name="patientIdentifierType" select="${status.value}">
						<option value="${record.patientIdentifierTypeId}" ${selected}>
							${record.name}
						</option>
					</openmrs:forEachRecord>
				</select>
				<span class="error">${status.errorMessage}</span>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Location</td>
		<td>
			<spring:bind path="location">
				<select name="${status.expression}">
					<openmrs:forEachRecord name="location" select="${status.value}">
						<option value="${record.locationId}" ${selected}>
							${record.name}
						</option>
					</openmrs:forEachRecord>
				</select>
				<span class="error">${status.errorMessage}</span>
			</spring:bind>
		</td>
	</tr>
	<c:if test="${!(identifier.creator == null)}" >
		<tr>
			<td>Creator</td>
			<td>
				<spring:bind path="creator">
					${identifier.creator.username}
					<input type="hidden" name="${status.expression}" value="${status.value}">
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td>Date Created</td>
			<td>
				<spring:bind path="dateCreated">
					<openmrs:formatDate date="${identifier.dateCreated}" type="long"/>
					<input type="hidden" name="${status.expression}" value="${status.value}">
				</spring:bind>
			</td>
		</tr>
	</c:if>
</table>