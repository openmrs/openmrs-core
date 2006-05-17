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
				<option value="$!{fn.getPatient().getPatientId()}">&nbsp; <spring:message code="Patient.id" /></option>
			
				<option value="$!{fn.getPatient().getPatientName().getGivenName()}">&nbsp; <spring:message code="PatientName.givenName" /></option>
				<option value="$!{fn.getPatient().getPatientName().getMiddleName()}">&nbsp; <spring:message code="PatientName.middleName" /></option>
				<option value="$!{fn.getPatient().getPatientName().getFamilyName()}">&nbsp; <spring:message code="PatientName.familyName" /></option>
				<option value="$!{fn.getPatient().getPatientName().getDegree()}">&nbsp; <spring:message code="PatientName.degree" /></option>
			
				<option value="$!{fn.getPatient().getPatientAddress().getAddress1()}">&nbsp; <spring:message code="PatientAddress.address1" /></option>
				<option value="$!{fn.getPatient().getPatientAddress().getAddress2()}">&nbsp; <spring:message code="PatientAddress.address2" /></option>
				<option value="$!{fn.getPatient().getPatientAddress().getCityVillage()}">&nbsp; <spring:message code="PatientAddress.cityVillage" /></option>
				<option value="$!{fn.getPatient().getPatientAddress().getStateProvince()}">&nbsp; <spring:message code="PatientAddress.stateProvince" /></option>
				<option value="$!{fn.getPatient().getPatientAddress().getCountry()}">&nbsp; <spring:message code="PatientAddress.country" /></option>
				<option value="$!{fn.getPatient().getPatientAddress().getPostalCode()}">&nbsp; <spring:message code="PatientAddress.postalCode" /></option>
			
				<option value="$!{fn.getPatient().getPatientIdentifier().getIdentifier()}">&nbsp; <spring:message code="PatientIdentifier.identifier" /></option>
				<option value="$!{fn.getPatient().getPatientIdentifier().getIdentifierType().getName()}">&nbsp; <spring:message code="PatientIdentifier.identifierType" /></option>
				<option value="$!{fn.getPatient().getPatientIdentifier().getLocation().getName()}">&nbsp; <spring:message code="PatientIdentifier.location" /></option>
			
				<option value="$!{fn.getPatient().getGender()}">&nbsp; <spring:message code="Patient.gender" /></option>
				<option value="$!{fn.formatDate('short', $fn.getPatient().getBirthdate())}">&nbsp; <spring:message code="Patient.birthdate" /></option>
				<option value="$!{fn.getPatient().getBirthdateEstimated()}">&nbsp; <spring:message code="Patient.birthdateEstimated" /></option>
				<option value="$!{fn.getPatient().getRace()}">&nbsp; <spring:message code="Patient.race" /></option>
				<option value="$!{fn.getPatient().getTribe().getName()}">&nbsp; <spring:message code="Tribe.name" /></option>
				<option value="$!{fn.formatDate('short', $fn.getPatient().getDeathDate())}">&nbsp; <spring:message code="Patient.deathDate" /></option>
			
				<option disabled> </option>
				<option disabled><spring:message code="DataExport.simpleEncounter"/></option>
				<option value="$!{fn.getLastEncounter().getEncounterId()}">&nbsp; <spring:message code="Encounter.id" /></option>
				<option value="$!{fn.getLastEncounter().getEncounterType().getName()}">&nbsp; <spring:message code="Encounter.type" /></option>
				<option value="$!{fn.getLastEncounter().getForm().getName()}">&nbsp; <spring:message code="Encounter.form" /></option>
				<option value="$!{fn.getLastEncounter().getLocation().getName())}">&nbsp; <spring:message code="Encounter.location" /></option>
				<option value="$!{fn.formatDate('short', $fn.getLastEncounter().getEncounterDatetime())}">&nbsp; <spring:message code="Encounter.datetime" /></option>
				<option value="$!{fn.getLastEncounter().getProvider().getFirstName()} $!{fn.getLastEncounter().getProvider().getLastName()}">&nbsp; <spring:message code="Encounter.provider" /></option>
			</select>
			
		</td>
	</tr>
</table>
<br />