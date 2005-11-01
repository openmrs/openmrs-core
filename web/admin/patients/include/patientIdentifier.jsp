<table>
	<tr>
		<td>Identifier</td>
		<td>
			<spring:bind path="identifier">
				<input type="text" name="identifier" id="identifier" value="${status.value}" />
				${status.errorMessage}
			</spring:bind>
			</td>
	</tr>
	<tr>
		<td>Type</td>
		<td>
			<spring:bind path="identifierType">
				<select name="identifierType" onclick="modifyTab(this, this.options[this.selectedIndex].text, 0)">
					<openmrs:forEachRecord name="patientIdentifierType" select="${status.value}">
						<option value="${record.patientIdentifierTypeId}" ${selected}>
							${record.name}
						</option>
					</openmrs:forEachRecord>
				</select>
				${status.errorMessage}
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Location</td>
		<td>
			<spring:bind path="location">
				<select name="location">
					<openmrs:forEachRecord name="location" select="${status.value}">
						<option value="${record.locationId}" ${selected}>
							${record.name}
						</option>
					</openmrs:forEachRecord>
				</select>
				${status.errorMessage}
			</spring:bind>
		</td>
	</tr>
	<c:if test="${creator ne null}" >
		<tr>
			<td>Creator</td>
			<td>${creator.username}</td>
		</tr>
		<tr>
			<td>Date Created</td>
			<td><openmrs:formatDate date="${dateCreated}" type="long"/></td>
		</tr>
	</c:if>
</table>