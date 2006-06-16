<table>
	<tr>
		<td><spring:message code="Patient.gender"/></td>
		<td><spring:bind path="patient.gender">
				<openmrs:forEachRecord name="gender">
					<input type="radio" name="gender" id="${record.key}" value="${record.key}" <c:if test="${record.key == status.value}">checked</c:if> />
						<label for="${record.key}"> <spring:message code="Patient.gender.${record.value}"/> </label>
				</openmrs:forEachRecord>
			<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Patient.race"/></td>
		<td>
			<spring:bind path="patient.race">
				<input type="text" name="race" size="10" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Patient.birthdate"/></td>
		<td colspan="3">
			<spring:bind path="patient.birthdate">			
				<input type="text" name="birthdate" size="10" 
					   value="${status.value}" onClick="showCalendar(this)" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
			(<spring:message code="general.format"/>: ${datePattern})
			<spring:bind path="patient.birthdateEstimated">
				<spring:message code="Patient.birthdateEstimated"/>
				<input type="hidden" name="_birthdateEstimated">
				<input type="checkbox" name="birthdateEstimated" value="true" 
					   <c:if test="${status.value == true}">checked</c:if> />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Patient.birthplace"/></td>
		<td>
			<spring:bind path="patient.birthplace">
				<input type="text" name="birthplace" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Patient.tribe"/></td>
		<td>
			<spring:bind path="patient.tribe">
				<select name="tribe">
					<option value=""></option>
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
		<td><spring:message code="Patient.citizenship"/></td>
		<td>
			<spring:bind path="patient.citizenship">
				<input type="text" name="citizenship" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Patient.mothersName"/></td>
		<td>
			<spring:bind path="patient.mothersName">
				<input type="text" name="mothersName" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Patient.civilStatus"/></td>
		<td>
			<spring:bind path="patient.civilStatus">
				<select name="civilStatus">
					<option value=""></option>
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
		<td><spring:message code="Patient.dead"/></td>
		<td>
			<spring:bind path="patient.dead">
				<input type="hidden" name="_${status.expression}"/>
				<input type="checkbox" name="${status.expression}" 
					   <c:if test="${status.value == true}">checked</c:if>
					   onclick="patientDeadClicked(this)" id="patientDead"
				/>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
			<script type="text/javascript">
				function patientDeadClicked(input) {
					if (input.checked) {
						document.getElementById("deathInformation").style.display = "";
					}
					else {
						document.getElementById("deathInformation").style.display = "none";
						document.getElementById("deathDate").value = "";
						document.getElementById("causeOfDeath").value = "";
					}
				}
			</script>
		</td>
	</tr>
	<tr id="deathInformation">
		<td><spring:message code="Patient.deathDate"/></td>
		<td>
			<spring:bind path="patient.deathDate">
				<input type="text" name="deathDate" size="10" 
					   value="${status.value}" onClick="showCalendar(this)" 
					   id="deathDate" />
				(<spring:message code="general.format"/>: ${datePattern})
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
			&nbsp; &nbsp; 
			<spring:message code="Patient.causeOfDeath"/>
			<spring:bind path="patient.causeOfDeath">
				<input type="text" name="causeOfDeath" value="${status.value}" id="causeOfDeath"/>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
			<script type="text/javascript">				
				//set up death info fields
				patientDeadClicked(document.getElementById("patientDead"));
			</script>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Patient.healthDistrict"/></td>
		<td>
			<spring:bind path="patient.healthDistrict">
				<input type="text" name="healthDistrict" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Patient.healthCenter"/></td>
		<td>
			<!-- TODO make this list of locations> -->
			<spring:bind path="patient.healthCenter">
				<input type="text" name="healthCenter" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<c:if test="${!(patient.creator == null)}">
		<tr>
			<td><spring:message code="general.createdBy" /></td>
			<td>
				${patient.creator.firstName} ${patient.creator.lastName} -
				<openmrs:formatDate date="${patient.dateCreated}" type="short" />
			</td>
		</tr>
	</c:if>
	<c:if test="${!(patient.changedBy == null)}">
		<tr>
			<td><spring:message code="general.changedBy" /></td>
			<td colspan="2">
				${patient.changedBy.firstName} ${patient.changedBy.lastName} -
				<openmrs:formatDate date="${patient.dateChanged}" type="short" />
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
				<input type="text" name="${status.expression}" value="${status.value}" size="35"/>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</td>
		</spring:bind>
	</tr>
	<c:if test="${patient.voided}" >
		<tr>
			<td><spring:message code="general.voidedBy"/></td>
			<td>
				${patient.voidedBy.firstName} ${patient.voidedBy.lastName} -
				<openmrs:formatDate date="${patient.dateVoided}" type="short" />
			</td>
		</tr>
	</c:if>
</table>