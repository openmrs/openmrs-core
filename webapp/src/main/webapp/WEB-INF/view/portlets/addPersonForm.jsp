<%@ include file="/WEB-INF/template/include.jsp" %>

<c:if test="${model.authenticatedUser != null}">

	<openmrs:require privilege="Add Patients" otherwise="/login.htm" redirect="/index.htm" />
	
	<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

	<div id="createPatient">
		<b class="boxHeader">
			<c:choose>
				<c:when test="${model.personType != null && model.personType != ''}">
					<openmrs:message code="Person.create.${model.personType}" />
				</c:when>
				<c:otherwise>
					<openmrs:message code="Person.create"/>
				</c:otherwise>
			</c:choose>
		</b>
		<div class="box">
			<openmrs:message code="Person.search.instructions"/> <br/>
			
			<form method="get" action="${model.postURL}" onSubmit="return validateForm()">
				
				<table>
					<tr>
						<td><openmrs:message code="Person.name"/><span class="required">*</span></td>
						<td>
							<input type="text" name="addName" id="personName" size="40" onKeyUp="hideError('nameError'); hideError('invalidNameError');" />
							<span class="error" id="nameError"><openmrs:message code="Person.name.required"/></span>
							<span class="error" id="invalidNameError"><openmrs:message code="Person.name.invalid"/></span>
						</td>
					</tr>
					<tr>
						<td><openmrs:message code="Person.birthdate"/><span class="required">*</span><br/><i style="font-weight: normal; font-size: 0.8em;">(<openmrs:message code="general.format"/>: <openmrs:datePattern />)</i></td>
						<td valign="top">
							<input type="text" name="addBirthdate" id="birthdate" size="11" value="" onfocus="showCalendar(this,60)" onChange="hideError('birthdateError')" />
							<openmrs:message code="Person.age.or"/>
							<input type="text" name="addAge" id="age" size="5" value="" onKeyUp="hideError('birthdateError')" />
							<span class="error" id="birthdateError"><openmrs:message code="Person.birthdate.required"/></span>
						</td>
					</tr>
					<tr>
						<td><openmrs:message code="Person.gender"/><span class="required">*</span></td>
						<td>
							<openmrs:forEachRecord name="gender">
								<input type="radio" name="addGender" id="gender-${record.key}" value="${record.key}"  onClick="hideError('genderError')" /><label for="gender-${record.key}"> <openmrs:message code="Person.gender.${record.value}"/> </label>
							</openmrs:forEachRecord>
							<span class="error" id="genderError"><openmrs:message code="Person.gender.required"/></span>
						</td>
					</tr>
					<tr>
						<td></td>
						<td>
							<input type="submit" value='<openmrs:message code="Person.create"/>'/>
						</td>
					</tr>
				</table>
				<input type="hidden" name="personType" value="${model.personType}"/>
				<input type="hidden" name="viewType" value="${model.viewType}"/>
			</form>
		</div>
		
		<script type="text/javascript"><!--
			hideError("nameError");
			hideError("invalidNameError");
			hideError("birthdateError");
			hideError("genderError");
			
			function validateForm() {
				var name = document.getElementById("personName");
				var birthdate = document.getElementById("birthdate");
				var birthyear = (birthdate == null || birthdate.value == "") ? "" : birthdate.value.substr(6, 4);
				var age = document.getElementById("age");
				var male = document.getElementById("gender-M");
				var female = document.getElementById("gender-F");
				var year = new Date().getFullYear();
				var nameValidatorRegexGP = "<openmrs:globalProperty key='patient.nameValidationRegex' defaultValue='.*'/>";
				if (nameValidatorRegexGP == "")
					nameValidatorRegexGP = ".*";
				var nameValidatorRegex = new RegExp(nameValidatorRegexGP);
				
				var result = true;
				if (name.value == "") {
					showError("nameError"); 
					result = false;
				}
				else {
					if (!(name.value.match(nameValidatorRegex))) {
						showError("invalidNameError");
						result = false;	
					}
				}
				
				if (birthdate.value == "" && age.value == "") {
					showError("birthdateError");
					result = false;
				}
				else {
					if (birthdate.value != "") {
						if (birthyear.length < 4 || birthyear < (year-120) || isFutureDate(birthdate.value)) {
							showError("birthdateError");
							result = false;
						}
					}
					else if (age.value != "") {
						if (isInteger(age.value) == false) {
							showError("birthdateError");
							result = false;
						} else if (age.value < 0 || age.value > 120) {
							showError("birthdateError");
							result = false;
						}
					}
				}
		
				if (male.checked == false && female.checked == false) {
					showError("genderError");
					result = false;
				} 
				
				return result;
			}
			
			function isFutureDate(birthdate) {
				if (birthdate == "")
					return false;
				
				var currentTime = new Date().getTime();
				
				var datePattern = '<openmrs:datePattern />';
				var datePatternStart = datePattern.substr(0,1).toLowerCase();
				
				var enteredTime = new Date();
				var year, month, day;
				if (datePatternStart == 'm') { /* M-D-Y */
					year = birthdate.substr(6, 4);
					month = birthdate.substr(0, 2);
					day = birthdate.substr(3, 2);
				}
				else if (datePatternStart == 'y') { /* Y-M-D */
					year = birthdate.substr(0, 4);
					month = birthdate.substr(5, 2);
					day = birthdate.substr(8, 2);
				}
				else { /* (datePatternStart == 'd') D-M-Y */
					year = birthdate.substr(6, 4);
					month = birthdate.substr(3, 2);
					day = birthdate.substr(0, 2);
				}
				
				/* alert("year: " + year + " month: " + month + " day " + day); */
				
				enteredTime.setYear(year);
				enteredTime.setMonth(month - 1);
				enteredTime.setDate(day);
				
				return enteredTime.getTime() > currentTime;
				
			}
			
			function isInteger(val)
			{
			    if(val==null) {
			        return false;
			    }
			    if (val.length==0) {
			        return false;
			    }
			    for (var i = 0; i < val.length; i++) {
			        var ch = val.charAt(i);
			        if (ch < "0" || ch > "9") return false;
			    }
			    return true;
			}
			
		--></script>
		
	</div>

</c:if>
