<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Users" otherwise="/openmrs/login.jsp" />

<%@ include file="/WEB-INF/template/header.jsp"%>

<h2>Create Patient</h2>

<br>
<form method="post" action="">
	<div class="patientIdentifier">
		Identifier
			<input type="text" name="identifier" id="identifier" />
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
	<div class="patientName">
		Preferred
			<input type="checkbox" name="preferred" id="preferred" />
		Given Name
			<input type="text" name="givenName" id="givenname" />
		Middle Name
			<input type="text" name="middleName" id="middleName" />
		Family Name Prefix
			<input type="text" name="familyNamePrefix" id="familyNamePrefix" />
		Family Name
			<input type="text" name="familyName" id="familyname" />
		Family Name 2
			<input type="text" name="familyname2" id="familyname2" />
		Family Name Suffix
			<input type="text" name="familyNameSuffix" id="familyNameSuffix" />
		Degree
			<input type="text" name="degree" id="degree" />
	</div>
	<div class="patientAddress">
		Address
			<input type="text" name="address1" id="address1" />
		Address2
			<input type="text" name="address2" id="address2" />
		City/Village
			<input type="text" name="cityVillage" id="cityVillage" />
		State/Province
			<input type="text" name="stateProvince" id="stateProvince" />
		Country
			<input type="text" name="country" id="country" />
		Latitude
			<input type="text" name="latitute" id="latitude" />
		Longitude
			<input type="text" name="longitude" id="longitude" />
	</div>
	<div class="patientInformation">
		Gender
			<select name="gender">
				<option value="M">Male</option>
				<option value="F">Female</option>
			</select>
		Race
			<input type="text" name="race" id="race" />
		Birthdate
			<input type="text" name="birthdate" id="birthdate" />
		Estimated
			<input type="checkbox" name="birthdateEstimated" id="birthdateEstimated" />
		Birthplace
			<input type="text" name="birthplace" id="birthplace" />
		Tribe
			<select name="identifierLocation">
					<openmrs:forEachRecord name="Tribe">
						<option value="${record.tribeId}">
							${record.name}
						</option>
					</openmrs:forEachRecord>
			</select>
		Citizenship
			<input type="text" name="citizenship" id="citizenship" />
		Mother's Name
			<input type="text" name="mothersName" id="mothersName" />
		Civil Status
			<select name="civilStatus">
				<option value="0">Single</option>
				<option value="1">Married</option>
				<option value="2">Divorced</option>
				<option value="3">Widowed</option>
			</select>
		Death Date
			<input type="text" name="deathDate" id="deathDate" />
		Cause of Death
			<input type="text" name="causeOfDeath" id="causeOfDeath" />
		Health District
			<input type="text" name="healthDistrict" id="healthDistrict" />
	</div>

<input type="submit" value="Save Patient">
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
