<table>
	<tr>
		<td>Gender</td>
		<td><spring:bind path="patient.gender">
			<select name="gender">
				<option value="M" <c:if test="${status.value == \"M\"}">selected</c:if>>Male</option>
				<option value="F" <c:if test="${status.value == \"F\"}">selected</c:if>>Female</option>
			</select>
			<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Race</td>
		<td>
			<spring:bind path="patient.race">
				<input type="text" name="race" id="race" size="10" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Birthdate</td>
		<td colspan="3">
			<spring:bind path="patient.birthdate">			
				<input type="text" name="birthdate" id="birthdate" size="10" 
					   value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
			<spring:bind path="patient.birthdateEstimated">
				Estimated
				<input type="hidden" name="_birthdateEstimated">
				<input type="checkbox" name="birthdateEstimated" 
					   id="birthdateEstimated" value="true" 
					   <c:if test="${status.value == true}">checked</c:if> />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Birthplace</td>
		<td>
			<spring:bind path="patient.birthplace">
				<input type="text" name="birthplace" id="birthplace" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	<tr>
		<td>Tribe</td>
		<td>
			<spring:bind path="patient.tribe">
				<select name="tribe">
					<openmrs:forEachRecord name="tribe" select="${status.value}">
						<option value="${record.tribeId}" ${selected}>
							${record.name}
						</option>
					</openmrs:forEachRecord>
				</select>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Citizenship</td>
		<td>
			<spring:bind path="patient.citizenship">
				<input type="text" name="citizenship" id="citizenship" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Mother's Name</td>
		<td>
			<spring:bind path="patient.mothersName">
				<input type="text" name="mothersName" id="mothersName" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Civil Status</td>
		<td>
			<spring:bind path="patient.civilStatus">
				<!-- TODO put this hashmap somewhere more central (in db?) -->
				<select name="civilStatus">
					<openmrs:forEachRecord name="civilStatus" select="${status.value}">
						<option value="${record.key}" ${selected}>
							${record.value}
						</option>
					</openmrs:forEachRecord>
				</select>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Death Date</td>
		<td>
			<spring:bind path="patient.deathDate">
				<input type="text" name="deathDate" id="deathDate" size="10" 
					value="<openmrs:formatDate date="${status.value}" type="textbox"/>"/>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
		<td>Cause of Death</td>
		<td>
			<spring:bind path="patient.causeOfDeath">
				<input type="text" name="causeOfDeath" id="causeOfDeath" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Health District</td>
		<td>
			<spring:bind path="patient.healthDistrict">
				<input type="text" name="healthDistrict" id="healthDistrict" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Health Center</td>
		<td>
			<!-- TODO make this list of locations> -->
			<spring:bind path="patient.healthCenter">
				<input type="text" name="healthCenter" id="healthCenter" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
</table>