
<!-- Assumes spring:nestedPath already set on PatientName object -->
<table>
	<tr>
		<td>Preferred</td>
		<td>
			<spring:bind path="preferred">
				<input type="hidden" name="_${status.expression}">
				<input type="checkbox" name="preferred" id="preferred" <c:if test="${status.value == true}">checked</c:if> />
				${status.errorMessage}
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Given Name</td>
		<td>
			<spring:bind path="givenName">
				<input type="text" name="givenName" id="givenName" value="${status.value}" onKeyUp="modifyTab(this, this.value, 0);" />
				${status.errorMessage}
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Middle Name</td>
		<td>
			<spring:bind path="middleName">
				<input type="text" name="middleName" id="middleName" value="${status.value}" />
				${status.errorMessage}
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Family Name</td>
		<td>
			<spring:bind path="familyNamePrefix">
				<input type="text" name="familyNamePrefix" id="familyNamePrefix" size="10" value="${status.value}" />
				${status.errorMessage}
			</spring:bind>
			<spring:bind path="familyName">
				<input type="text" name="familyName" id="familyName" value="${status.value}" onKeyUp="modifyTab(this, this.value, 2);" />
				${status.errorMessage}
			</spring:bind>
			<spring:bind path="familyName2">
				<input type="text" name="familyName2" id="familyName2" value="${status.value}" />
				${status.errorMessage}
			</spring:bind>
			<spring:bind path="familyNameSuffix">
				<input type="text" name="familyNameSuffix" id="familyNameSuffix" size="10" value="${status.value}" />
				${status.errorMessage}
			</spring:bind>
		</td>
	</tr>	
	<tr>
		<td>Degree</td>
		<td>
			<spring:bind path="degree">
				<input type="text" name="degree" id="degree" value="${degree}" />
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
