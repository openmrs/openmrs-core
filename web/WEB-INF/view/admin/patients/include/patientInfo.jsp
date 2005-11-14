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
				<input type="text" name="race" size="10" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Birthdate</td>
		<td colspan="3">
			<spring:bind path="patient.birthdate">			
				<input type="text" name="birthdate" size="10" 
					   value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
			<spring:bind path="patient.birthdateEstimated">
				Estimated
				<input type="hidden" name="_birthdateEstimated">
				<input type="checkbox" name="birthdateEstimated" value="true" 
					   <c:if test="${status.value == true}">checked</c:if> />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Birthplace</td>
		<td>
			<spring:bind path="patient.birthplace">
				<input type="text" name="birthplace" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Tribe</td>
		<td>
			<spring:bind path="patient.tribe">
				<select name="tribe">
					<openmrs:forEachRecord name="tribe">
						<option value="${record.tribeId}" <c:if test="${record.tribeId == status.value}">selected</c:if>>
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
				<input type="text" name="citizenship" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Mother's Name</td>
		<td>
			<spring:bind path="patient.mothersName">
				<input type="text" name="mothersName" value="${status.value}" />
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
			<input type="text" name="deathDate" size="10" 
				   value="${status.value}" />
			<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
		<td>Cause of Death</td>
		<td>
			<spring:bind path="patient.causeOfDeath">
				<input type="text" name="causeOfDeath" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Health District</td>
		<td>
			<spring:bind path="patient.healthDistrict">
				<input type="text" name="healthDistrict" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>Health Center</td>
		<td>
			<!-- TODO make this list of locations> -->
			<spring:bind path="patient.healthCenter">
				<input type="text" name="healthCenter" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<c:if test="${!(patient.creator == null)}" >
		<tr>
			<td><spring:message code="general.creator"/></td>
			<td>
				<spring:bind path="patient.creator">
					${patient.creator.username}
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td><spring:message code="general.dateCreated"/></td>
			<td>
				<spring:bind path="patient.dateCreated">
					<openmrs:formatDate date="${patient.dateCreated}" type="long"/>
				</spring:bind>
			</td>
		</tr>
	</c:if>
	<c:if test="${!(patient.changedBy == null)}" >
		<tr>
			<td><spring:message code="general.changedBy"/></td>
			<td>
				<spring:bind path="patient.changedBy">
					${patient.changedBy.username}
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td><spring:message code="general.dateChanged"/></td>
			<td>
				<spring:bind path="patient.dateChanged">
					<openmrs:formatDate date="${patient.dateChanged}" type="long"/>
				</spring:bind>
			</td>
		</tr>
	</c:if>
	<tr>
		<td><spring:message code="general.voided"/></td>
		<td>
			<spring:bind path="patient.voided">
				<input type="hidden" name="_${status.expression}"/>
				<input type="checkbox" name="${status.expression}" 
					   <c:if test="${status.value == true}">checked</c:if> 
					   onClick="voidedBoxClick(this)"
				/>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="general.voidReason"/></td>
		<spring:bind path="patient.voidReason">
			<td>
				<input type="text" name="${status.expression}" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</td>
		</spring:bind>
	</tr>
	<c:if test="${!(patient.voidedBy == null)}" >
		<tr>
			<td><spring:message code="general.voidedBy"/></td>
			<td>
				<spring:bind path="patient.voidedBy">
					${patient.voidedBy.username}
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td><spring:message code="general.dateVoided"/></td>
			<td>
				<spring:bind path="patient.dateVoided">
					<openmrs:formatDate date="${patient.dateVoided}" type="long"/>
				</spring:bind>
			</td>
		</tr>
	</c:if>
</table>