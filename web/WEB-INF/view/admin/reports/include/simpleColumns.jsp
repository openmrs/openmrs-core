<openmrs:globalProperty key="use_patient_attribute.tribe" defaultValue="false" var="showTribe"/>
<openmrs:globalProperty key="use_patient_attribute.healthCenter" defaultValue="false" var="showHealthCenter"/>

<br />
<table>
	<tr>
		<td><spring:message code="DataExport.columnName"/></td>
		<td><input type="text" name="simpleName" size="30"/></td>
	</tr>
	<tr>
		<td><spring:message code="DataExport.columnValue"/></td>
		<td>
			<input type="text" name="simpleValue" size="50"/>
		
			<==
			
			<select name="simplePatient" onclick="updateSimpleColumn(this)">
				
				<option value=""> </option>
				<option disabled><spring:message code="DataExport.simplePatient"/></option>
				<option value="$!{fn.patientId}">&nbsp; <spring:message code="Patient.id" /></option>
			
				<option value="$!{fn.getPatientAttr('PersonName', 'givenName')}">&nbsp; <spring:message code="PersonName.givenName" /></option>
				<option value="$!{fn.getPatientAttr('PersonName', 'middleName')}">&nbsp; <spring:message code="PersonName.middleName" /></option>
				<option value="$!{fn.getPatientAttr('PersonName', 'familyName')}">&nbsp; <spring:message code="PersonName.familyName" /></option>
				<option value="$!{fn.getPatientAttr('PersonName', 'degree')}">&nbsp; <spring:message code="PersonName.degree" /></option>
				<option disabled> </option>
				<c:forEach items="${addressTemplate.lines}" var="line">
					<c:forEach items="${line}" var="token">
						<c:if test="${token.isToken == addressTemplate.layoutToken}">
							<option value="$!{fn.getPatientAttr('PersonAddress', '${token.codeName}')}">&nbsp; <spring:message code="${token.displayText}" /></option>
						</c:if>
					</c:forEach>
				</c:forEach>	

<%--
				<option value="$!{fn.getPatientAttr('PersonAddress', 'address1')}">&nbsp; <spring:message code="PersonAddress.address1" /></option>
				<option value="$!{fn.getPatientAttr('PersonAddress', 'address2')}">&nbsp; <spring:message code="PersonAddress.address2" /></option>
				<option value="$!{fn.getPatientAttr('PersonAddress', 'cityVillage')}">&nbsp; <spring:message code="PersonAddress.cityVillage" /></option>
				<option value="$!{fn.getPatientAttr('PersonAddress', 'stateProvince')}">&nbsp; <spring:message code="PersonAddress.stateProvince" /></option>
				<option value="$!{fn.getPatientAttr('PersonAddress', 'country')}">&nbsp; <spring:message code="PersonAddress.country" /></option>
				<option value="$!{fn.getPatientAttr('PersonAddress', 'postalCode')}">&nbsp; <spring:message code="PersonAddress.postalCode" /></option>
--%>
				<option disabled> </option>
				<option value="$!{fn.getPatientAttr('PatientIdentifier', 'identifier')}">&nbsp; <spring:message code="PatientIdentifier.identifier" /></option>
				<option value="$!{fn.getPatientAttr('PatientIdentifier', 'identifierType').getName()}">&nbsp; <spring:message code="PatientIdentifier.identifierType" /></option>
				<option value="$!{fn.getPatientAttr('PatientIdentifier', 'location').getName()}">&nbsp; <spring:message code="PatientIdentifier.location" /></option>
				<option disabled> </option>
				<option value="$!{fn.getPatientAttr('Person', 'gender')}">&nbsp; <spring:message code="Patient.gender" /></option>
				<option value="$!{fn.calculateAge($fn.getPatientAttr('Person', 'birthdate'))}">&nbsp; <spring:message code="Person.age" /></option>
				<option value="$!{fn.formatDate('short', $fn.getPatientAttr('Person', 'birthdate'))}">&nbsp; <spring:message code="Person.birthdate" /></option>
				<option value="$!{fn.getPatientAttr('Person', 'birthdateEstimated')}">&nbsp; <spring:message code="Person.birthdateEstimated" /></option>
				<c:if test="${showHealthCenter == 'true'}">
					<option value="$!{fn.getPersonAttribute('Health Center', 'Location', 'locationId', 'name', false)}">&nbsp; <spring:message code="PersonAttributeType.HealthCenter" /></option>
				</c:if>
				<c:if test="${showTribe == 'true'}">
					<option value="$!{fn.getPersonAttribute('Race')}">&nbsp; <spring:message code="PersonAttributeType.Race" /></option>
					<option value="$!{fn.getPatientAttr('Patient', 'tribe').getName()}">&nbsp; <spring:message code="Tribe.name" /></option>
				</c:if>
                <option value="$!{fn.getPatientAttr('Person', 'dead')}">&nbsp; <spring:message code="Person.dead" /></option>
				<option value="$!{fn.formatDate('short', $fn.getPatientAttr('Person', 'deathDate'))}">&nbsp; <spring:message code="Person.deathDate" /></option>
				<option value="$!{fn.getPatientAttr('Person', 'causeOfDeath')}">&nbsp; <spring:message code="Person.causeOfDeath" /></option>
				<option value=""> </option>
				<option disabled><spring:message code="DataExport.simpleLastEncounter"/></option>
				<option value="$!{fn.getLastEncounterAttr([''], 'encounterType').getName()}">&nbsp; <spring:message code="Encounter.type" /></option>
				<option value="$!{fn.getLastEncounter('').getProvider().getFirstName()} $!{fn.getLastEncounter('').getProvider().getLastName()}">&nbsp; <spring:message code="Encounter.provider" /></option>
				<option value="$!{fn.getLastEncounterAttr([''], 'location').getName()}">&nbsp; <spring:message code="Encounter.location" /></option>
				<option value="$!{fn.getLastEncounterAttr([''], 'form').getName()}">&nbsp; <spring:message code="Encounter.form" /></option>
				<option value="$!{fn.formatDate('short', $!{fn.getLastEncounterAttr([''], 'encounterDatetime')})}">&nbsp; <spring:message code="Encounter.datetime" /></option>
				<option value=""> </option>
				<option disabled><spring:message code="DataExport.simpleFirstEncounter"/></option>
				<option value="$!{fn.getFirstEncounter('').getEncounterType().getName()}">&nbsp; <spring:message code="Encounter.type" /></option>
				<option value="$!{fn.getFirstEncounter('').getProvider().getFirstName()} $!{fn.getFirstEncounter('').getProvider().getLastName()}">&nbsp; <spring:message code="Encounter.provider" /></option>
				<option value="$!{fn.getFirstEncounterAttr([''], 'location').getName()}">&nbsp; <spring:message code="Encounter.location" /></option>
				<option value="$!{fn.getFirstEncounterAttr([''], 'form').getName()}">&nbsp; <spring:message code="Encounter.form" /></option>
				<option value="$!{fn.formatDate('short', $!{fn.getFirstEncounterAttr([''], 'encounterDatetime')})}">&nbsp; <spring:message code="Encounter.datetime" /></option>
				<option value=""> </option>
				<option value="$!{fn.getRelationshipNames('')}">&nbsp; <spring:message code="All Relationships" /></option>
				<option value="$!{fn.getRelationshipNames('Accompagnateur')}">&nbsp; <spring:message code="provider.chw.names" /></option>
				<option value="$!{fn.getRelationshipIds('Accompagnateur')}">&nbsp; <spring:message code="provider.chw.id" /></option>
				<option value="$!{fn.getRelationshipIdentifiers('Mother')}">&nbsp; <spring:message code="RelationshipType.mother" /></option>
				<option value=""> </option>
				<option value="$!{fn.formatDate('ymd', $fn.getProgram('HIV PROGRAM').getDateEnrolled())}">&nbsp; <spring:message code="HIV Program enrollment date" /></option>
				<option value="$!{fn.formatDate('ymd', $fn.getProgram('TUBERCULOSIS PROGRAM').getDateEnrolled())}">&nbsp; <spring:message code="TB Program enrollment date" /></option>
				<option value="$!{fn.getProgram('HIV PROGRAM').getCurrentState($fn.getProgram('HIV PROGRAM').getProgram().getWorkflowByName('ANTIRETROVIRAL TREATMENT GROUP')).getState().getConcept().getName()}">&nbsp; <spring:message code="ARV Treatment Group" /></option>
				<option value="$!{fn.getProgram('HIV PROGRAM').getCurrentState($fn.getProgram('TB PROGRAM').getProgram().getWorkflowByName('TUBERCULOSIS TREATMENT GROUP')).getState().getConcept().getName()}">&nbsp; <spring:message code="TB Treatment Group" /></option>
				<option value=""> </option>
				<option value="$!{fn.getCurrentDrugNames('ANTIRETROVIRAL DRUGS')}">&nbsp; <spring:message code="Current ARVs" /></option>
				<option value="$!{fn.getCurrentDrugNames('TUBERCULOSIS TREATMENT DRUGS')}">&nbsp; <spring:message code="Current TB meds" /></option>
				<option value="$!{fn.formatDate('ymd', $fn.getEarliestDrugStart('ANTIRETROVIRAL DRUGS'))}">&nbsp; <spring:message code="Earliest ARV start" /></option>
				<option value="$!{fn.formatDate('ymd', $fn.getEarliestDrugStart('TUBERCULOSIS TREATMENT DRUGS'))}">&nbsp; <spring:message code="Earliest TB med start" /></option>
			</select>
			
		</td>
	</tr>
</table>
<br />