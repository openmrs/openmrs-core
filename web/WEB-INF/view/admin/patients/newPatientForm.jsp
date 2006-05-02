<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Add Patients" otherwise="/login.htm" redirect="/admin/patients/newPatient.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<script src="<%= request.getContextPath() %>/scripts/calendar/calendar.js"></script>

<script type="text/javascript">
	function addIdentifier() {
		var table = document.getElementById('identifiers');
		var row = document.getElementById('newIdentifierRow');
		var newrow = row.cloneNode(true);
		newrow.style.display = "";
		newrow.id = table.childNodes.length;
		table.appendChild(newrow);
	}
	
	function updateAge() {
		var birthdateBox = document.getElementById('birthdate');
		var ageBox = document.getElementById('age');
		try {
			var birthdate = new Date(birthdateBox.value);
			var age = getAge(birthdate);
			if (age > 0)
				ageBox.innerHTML = "(" + age + ' <spring:message code="Patient.age.years"/>)';
			else if (age == 1)
				ageBox.innerHTML = '(1 <spring:message code="Patient.age.year"/>)';
			else if (age == 0)
				ageBox.innerHTML = '( < 1 <spring:message code="Patient.age.year"/>)';
			else
				ageBox.innerHTML = '( ? )';
			ageBox.style.display = "";
		} catch (err) {
			ageBox.innerHTML = "";
			ageBox.style.display = "none";
		}
	}
	
	// age function borrowed from http://anotherdan.com/2006/02/simple-javascript-age-function/
	function getAge(d, now) {
		var age = -1;
		if (typeof(now) == 'undefined') now = new Date();
		while (now >= d) {
			age++;
			d.setFullYear(d.getFullYear() + 1);
		}
		return age;
	}

	
</script>

<style>
	th {
		text-align: left;
	}
	#newIdentifierRow {
		display: none;
	}
</style>

<spring:hasBindErrors name="patient">
	<div class="error">Please fix all errors</div>
	<div class="error">
		<c:forEach items="${errors.allErrors}" var="error">
			<spring:message code="${error.code}" text="${error.code}"/><br/><!-- ${error} -->
		</c:forEach>
	</div>
</spring:hasBindErrors>

<form method="post" action="newPatient.form">
	<c:if test="${patient.patientId == null}"><h2><spring:message code="Patient.create"/></h2></c:if>
	<c:if test="${patient.patientId != null}"><h2><spring:message code="Patient.edit"/></h2></c:if>
	
	<table cellspacing="2">
			<tr>
				<th><spring:message code="PatientName.givenName"/></th>
				<th><spring:message code="PatientName.middleName"/></th>
				<th><spring:message code="PatientName.familyName"/></th>
			</tr>
			<tr>
			<td valign="top">
				<spring:bind path="patient.givenName">
					<input type="text" name="${status.expression}" value="${status.value}" size="30" />
					<c:if test="${status.errorMessage != ''}"><br/><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
			<td valign="top">
				<spring:bind path="patient.middleName">
					<input type="text" name="${status.expression}" value="${status.value}" size="30" />
					<c:if test="${status.errorMessage != ''}"><br/><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
			<td valign="top">
				<spring:bind path="patient.familyName">
					<input type="text" name="${status.expression}" value="${status.value}" size="30" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
	</table>
	<br/>
	<table id="identifiers" cellspacing="2">
		<tr>
			<th><spring:message code="PatientIdentifier.identifier"/></th>
			<th><spring:message code="PatientIdentifier.identifierType"/></th>
			<th><spring:message code="PatientIdentifier.location.identifier"/></th>
		</tr>
		<c:forEach items="${identifiers}" var="id">
			<tr>
				<td>${id.identifier}</td>
				<td>${id.identifierType.name}</td>
				<td>${id.location.name}</td>
			</tr>
		</c:forEach>
		<tr id="newIdentifierRow">
			<td valign="top">
				<input type="text" size="30"
						name="newIdentifier" 
						value="" 
						onBlur="return true; validateIdentifier(this, 'addButton', '<spring:message code="error.identifier"/>');"/>
			</td>
			<td valign="top">
				<select name="newIdentifierType">
					<openmrs:forEachRecord name="patientIdentifierType">
						<option value="${record.patientIdentifierTypeId}">
							${record.name}
						</option>
					</openmrs:forEachRecord>
				</select>
			</td>
			<td valign="top">
				<select name="newLocation">
					<openmrs:forEachRecord name="location">
						<option value="${record.locationId}">
							${record.name}
						</option>
					</openmrs:forEachRecord>
				</select>
			</td>
		</tr>
	</table>
	<input type="button" class="smallButton" onclick="addIdentifier()" value="<spring:message code="PatientIdentifier.add" />" hidefocus />
	<br/><br/>
	<table>
		<tr>
			<th>
				<spring:message code="Patient.birthdate"/> <br/>
				<i style="font-weight: normal; font-size: .8em;">(<spring:message code="general.format"/>: ${datePattern})</i>
			</th>
			<td colspan="3" valign="top">
				<spring:bind path="patient.birthdate">			
					<input type="text" id="birthdate" name="birthdate" size="10" value="${status.value}" 
							onClick="showCalendar(this)" onChange="updateAge()" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
				</spring:bind>
				<span id="age"></span> &nbsp; 
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
			<th><spring:message code="Patient.gender"/></th>
			<td>
				<spring:bind path="patient.gender">
						<openmrs:forEachRecord name="gender">
							<input type="radio" name="gender" id="${record.key}" value="${record.key}" <c:if test="${record.key == status.value}">checked</c:if> />
								<label for="${record.key}"> <spring:message code="Patient.gender.${record.value}"/> </label>
						</openmrs:forEachRecord>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th><spring:message code="Patient.tribe"/></th>
			<td>
				<spring:bind path="patient.tribe">
					<select name="tribe">
						<option value=""></option>
						<openmrs:forEachRecord name="tribe">
							<option value="${record.tribeId}" <c:catch><c:if test="${record.name == status.value || status.value == record.tribeId}">selected</c:if></c:catch>>
								${record.name}
							</option>
						</openmrs:forEachRecord>
					</select>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th><spring:message code="PatientAddress.address1"/></th>
			<td>
				<spring:bind path="patient.address1">
					<input type="text" name="${status.expression}" value="${status.value}" size="45" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th><spring:message code="PatientAddress.address2"/></th>
			<td>
				<spring:bind path="patient.address2">
					<input type="text" name="${status.expression}" value="${status.value}" size="45" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th><spring:message code="Patient.mothersName"/></th>
			<td>
				<spring:bind path="patient.mothersName">
					<input type="text" name="mothersName" value="${status.value}" size="45" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
	</table>
	
	<input type="hidden" name="pId" value="${param.pId}" />
	
	<br />
	<input type="submit" value="<spring:message code="general.save" />" name="action" id="addButton"> &nbsp; &nbsp; 
	<input type="button" value="<spring:message code="general.cancel" />" onclick="history.go(-1);">
</form>

<script type="text/javascript">
	document.forms[0].elements[0].focus();
	addIdentifier();
	updateAge();
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>
