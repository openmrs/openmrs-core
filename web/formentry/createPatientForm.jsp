<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Form Entry" otherwise="/login.jsp" />

<%@ include file="/WEB-INF/template/header.jsp"%>

<h2>Create Patient</h2>

<br>
<form method="post" action="createPatient">
	<div class="patientIdentifier">
		Identifier
			<input type="text" name="identifier" id="identifier" value="${identifier.identifier}" />
		Type
			<select name="identifierType">
				<openmrs:forEachRecord name="PatientIdentifierType">
					<option value="${record.patientIdentifierTypeId}">
						${record.name}
					</option>
				</openmrs:forEachRecord>
			</select>
		Location
			<select name="identifierLocation">
				<openmrs:forEachRecord name="Location">
					<option value="${record.locationId}">
						${record.name}
					</option>
				</openmrs:forEachRecord>
			</select>
	</div>
	<br />
	<div class="patientName">
		<table>
			<tr>
				<td>Preferred</td>
				<td><input type="checkbox" name="preferred" id="preferred" /></td>
			</tr>
			<tr>
				<td>Given Name</td>
				<td><input type="text" name="givenName" id="givenname" /></td>
			</tr>
			<tr>
				<td>Middle Name</td>
				<td><input type="text" name="middleName" id="middleName" /></td>
			</tr>
			<tr>
				<td>Family Name</td>
				<td>
					<input type="text" name="familyNamePrefix" id="familyNamePrefix" size="10"/>
					<input type="text" name="familyName" id="familyname" />
					<input type="text" name="familyname2" id="familyname2" />
					<input type="text" name="familyNameSuffix" id="familyNameSuffix" size="10"/>
				</td>
			</tr>	
			<tr>
				<td>Degree</td>
				<td><input type="text" name="degree" id="degree" /></td>
			</tr>
		</table>
	</div>
	<br />
	<div class="patientAddress">
		<table>
			<tr>
				<td>Address</td>
				<td><input type="text" name="address1" id="address1" /></td>
			</tr>
			<tr>
				<td>Address2</td>
				<td><input type="text" name="address2" id="address2" /></td>
			</tr>
			<tr>
				<td>City/Village</td>
				<td><input type="text" name="cityVillage" id="cityVillage" /></td>
				<td>State/Province</td>
				<td><input type="text" name="stateProvince" id="stateProvince" size="10" /></td>
				<td>Country</td>
				<td><input type="text" name="country" id="country" size="15" /></td>
			</tr>
			<tr>
				<td>Latitude</td>
				<td><input type="text" name="latitute" id="latitude" /></td>
				<td>Longitude</td>
				<td><input type="text" name="longitude" id="longitude" /></td>
			</tr>
		</table>
	</div>
	<br />
	<div class="patientInformation">
		<table>
			<tr>
				<td>Gender</td>
				<td>
					<select name="gender">
						<option value="M">Male</option>
						<option value="F">Female</option>
					</select>
				</td>
			</tr>
			<tr>
				<td>Race</td>
				<td><input type="text" name="race" id="race" size="10" /></td>
			</tr>
			<tr>
				<td>Birthdate</td>
				<td colspan="3">
					<input type="text" name="birthdate" id="birthdate" size="10" />
					Estimated
					<input type="checkbox" name="birthdateEstimated" id="birthdateEstimated" value="true" />
				</td>
			</tr>
			<tr>
				<td>Birthplace</td>
				<td><input type="text" name="birthplace" id="birthplace" /></td>
			<tr>
				<td>Tribe</td>
				<td>
					<select name="tribe">
							<openmrs:forEachRecord name="Tribe">
								<option value="${record.tribeId}">
									${record.name}
								</option>
							</openmrs:forEachRecord>
					</select>
				</td>
			</tr>
			<tr>
				<td>Citizenship</td>
				<td><input type="text" name="citizenship" id="citizenship" /></td>
			</tr>
			<tr>
				<td>Mother's Name</td>
				<td><input type="text" name="mothersName" id="mothersName" /></td>
			</tr>
			<tr>
				<td>Civil Status</td>
				<td>
					<select name="civilStatus">
						<option value="0">Single</option>
						<option value="1">Married</option>
						<option value="2">Divorced</option>
						<option value="3">Widowed</option>
					</select>
				</td>
			</tr>
			<tr>
				<td>Death Date</td>
				<td><input type="text" name="deathDate" id="deathDate" size="10" /></td>
				<td>Cause of Death</td>
				<td><input type="text" name="causeOfDeath" id="causeOfDeath" /></td>
			</tr>
			<tr>
				<td>Health District</td>
				<td><input type="text" name="healthDistrict" id="healthDistrict" /></td>
			</tr>
			<tr>
				<td>Health Center</td>
				<td><input type="text" name="healthCenter" id="healthCenter" /></td>
			</tr>
		</table>
	</div>

<input type="submit" value="Save Patient">
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
