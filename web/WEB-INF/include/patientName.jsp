<table>
	<tr>
		<td>Preferred</td>
		<td><input type="checkbox" name="preferred" id="preferred" <%= name.isPreferred().booleanValue() ? "checked" : "" %>/></td>
	</tr>
	<tr>
		<td>Given Name</td>
		<td><input type="text" name="givenName" id="givenName" value="${name.givenName}" onKeyUp="modifyTab(this, this.value, 0);" /></td>
	</tr>
	<tr>
		<td>Middle Name</td>
		<td><input type="text" name="middleName" id="middleName" value="${name.middleName}" /></td>
	</tr>
	<tr>
		<td>Family Name</td>
		<td>
			<input type="text" name="familyNamePrefix" id="familyNamePrefix" size="10" value="${name.familyNamePrefix}" />
			<input type="text" name="familyName" id="familyName" value="${name.familyName}" onKeyUp="modifyTab(this, this.value, 2);" />
			<input type="text" name="familyname2" id="familyName2" value="${name.familyName2}" />
			<input type="text" name="familyNameSuffix" id="familyNameSuffix" size="10" value="${name.familyNameSuffix}" />
		</td>
	</tr>	
	<tr>
		<td>Degree</td>
		<td><input type="text" name="degree" id="degree" value="${name.degree}" /></td>
	</tr>
	<c:if test="${!(name.creator == null)}" >
		<tr>
			<td>Creator</td>
			<td>${name.creator.username}</td>
		</tr>
		<tr>
			<td>Date Created</td>
			<td><%= org.openmrs.web.Util.formatDate(name.getDateCreated()) %></td>
		</tr>
	</c:if>
</table>
