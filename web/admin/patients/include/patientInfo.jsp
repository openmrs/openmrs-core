<table>
	<tr>
		<td>Gender</td>
		<td><spring:bind path="patient.gender">
			<select name="${status.expression}">
				<option value="M" <c:if test="${status.value == \"M\"}">selected</c:if>>Male</option>
				<option value="F" <c:if test="${status.value == \"F\"}">selected</c:if>>Female</option>
			</select>
			${status.errorMessage}
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Race</td>
		<td>
			<spring:bind path="patient.race">
				<input type="text" name="${status.expression}" id="${status.expression}" size="10" value="${status.value}" />
				${status.errorMessage}
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Birthdate</td>
		<td colspan="3">
			<spring:bind path="patient.birthdate">			
				<input type="text" name="${status.expression}" id="${status.expression}" size="10" 
					   value="${status.value}" />
				${status.errorMessage} 
			</spring:bind>
			<spring:bind path="patient.birthdateEstimated">
				Estimated
				<input type="checkbox" name="${status.expression}" 
					   id="${status.expression}" value="true" 
					   <c:if test="${status.value == true}">checked</c:if> />
				${status.errorMessage}
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Birthplace</td>
		<td>
			<spring:bind path="patient.birthplace">
				<input type="text" name="${status.expression}" id="${status.expression}" value="${status.value}" />
				${status.errorMessage}
			</spring:bind>
		</td>
	<tr>
		<td>Tribe</td>
		<td>
			<spring:bind path="patient.tribe">
				<select name="${status.expression}">
					<openmrs:forEachRecord name="tribe" select="${status.value}">
						<option value="${record.tribeId}" ${selected}>
							${record.name}
						</option>
					</openmrs:forEachRecord>
				</select>
				${status.errorMessage}
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Citizenship</td>
		<td>
			<spring:bind path="patient.citizenship">
				<input type="text" name="${status.expression}" id="${status.expression}" value="${status.value}" />
				${status.errorMessage}
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Mother's Name</td>
		<td>
			<spring:bind path="patient.mothersName">
				<input type="text" name="${status.expression}" id="${status.expression}" value="${status.value}" />
				${status.errorMessage}
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Civil Status</td>
		<td>
			<spring:bind path="patient.civilStatus">
				<!-- TODO put this hashmap somewhere more central (in db?) -->
				<select name="${status.expression}">
					<openmrs:forEachRecord name="civilStatus" select="${status.value}">
						<option value="${record.key}" ${selected}>
							${record.value}
						</option>
					</openmrs:forEachRecord>
				</select>
				${status.errorMessage}
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Death Date</td>
		<td>
			<spring:bind path="patient.deathDate">
				<input type="text" name="deathDate" id="${status.expression}" size="10" 
					value="<openmrs:formatDate date="${status.value}" type="textbox"/>"/>
				${status.errorMessage}
			</spring:bind>
		</td>
		<td>Cause of Death</td>
		<td>
			<spring:bind path="patient.causeOfDeath">
				<input type="text" name="causeOfDeath" id="${status.expression}" value="${status.value}" />
				${status.errorMessage}
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Health District</td>
		<td>
			<spring:bind path="patient.healthDistrict">
				<input type="text" name="healthDistrict" id="${status.expression}" value="${status.value}" />
				${status.errorMessage}
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Health Center</td>
		<td>
			<!-- TODO make this list of locations> -->
			<spring:bind path="patient.healthCenter">
				<input type="text" name="healthCenter" id="${status.expression}" value="${status.value}" />
				${status.errorMessage}
			</spring:bind>
		</td>
	</tr>
</table>