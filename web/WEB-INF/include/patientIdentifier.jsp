<table>
	<tr>
		<td>Identifier</td>
		<td><input type="text" name="identifier" id="identifier" value="${identifier.identifier}" /></td>
	</tr>
	<tr>
		<td>Type</td>
		<td>
			<select name="identifierType" onchange="modifyTab(this, this.options[this.selectedIndex].text, 0)">
				<openmrs:forEachRecord name="PatientIdentifierType" select="${identifier.identifierType}">
					<option value="${record.patientIdentifierTypeId}" ${selected}>
						${record.name}
					</option>
				</openmrs:forEachRecord>
			</select>
		</td>
	</tr>
	<tr>
		<td>Location</td>
		<td>
			<select name="identifierLocation">
				<openmrs:forEachRecord name="Location" select="${identifier.location}">
					<option value="${record.locationId}" ${selected}>
						${record.name}
					</option>
				</openmrs:forEachRecord>
			</select>
		</td>
	</tr>
	<c:if test="${!(identifier.creator == null)}" >
		<tr>
			<td>Creator</td>
			<td>${identifier.creator.username}</td>
		</tr>
		<tr>
			<td>Date Created</td>
			<td><%= org.openmrs.web.Util.formatDate(identifier.getDateCreated()) %></td>
		</tr>
	</c:if>
</table>