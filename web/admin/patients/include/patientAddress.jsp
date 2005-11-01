<table>
	<tr>
		<td>Address</td>
		<td>
			<spring:bind path="address1">
				<input type="text" name="address1" id="address1" value="${status.value}"/>
				${status.errorMessage}
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Address2</td>
		<td>
			<spring:bind path="address2">
				<input type="text" name="address2" id="address2" value="${status.value}" />
				${status.errorMessage}
			</spring:bind>
		</td>
			
	</tr>
	<tr>
		<td>City/Village</td>
			<spring:bind path="cityVillage">
				<td>
					<input type="text" name="cityVillage" id="cityVillage" value="${status.value}" onKeyUp="modifyTab(this, this.value, 0);"/>
				</td>
				${status.errorMessage}
			</spring:bind>
		<td>State/Province</td>
			<spring:bind path="stateProvince">
				<td>
					<input type="text" name="stateProvince" id="stateProvince" size="10" value="${status.value}" />
				</td>
				${status.errorMessage}
			</spring:bind>
		<td>Country</td>
			<spring:bind path="country">
				<td>
					<input type="text" name="country" id="country" size="15" value="${status.value}" />
				</td>
				${status.errorMessage}
			</spring:bind>

	</tr>
	<tr>
		<td>Latitude</td>
			<spring:bind path="latitude">
				<td>
					<input type="text" name="latitude" id="latitude" value="${status.value}" />
				</td>
				${status.errorMessage}
			</spring:bind>
		<td>Longitude</td>
			<spring:bind path="longitude">
				<td>
					<input type="text" name="longitude" id="longitude" value="${status.value}" />
				</td>
				${status.errorMessage}
			</spring:bind>
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