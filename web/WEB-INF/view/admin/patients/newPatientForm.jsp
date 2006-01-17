<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/admin/patients/newPatient.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<style>
	th {
		text-align: left;
	}
</style>

<form method="post" action="newPatient.form">
	<h2><spring:message code="Patient.create"/></h2>
	
	<table>
			<tr>
				<th><spring:message code="PatientName.givenName"/></th>
				<th><spring:message code="PatientName.middleName"/></th>
				<th><spring:message code="PatientName.familyName"/></th>
			</tr>
			<tr>
				<td>
					<spring:bind path="patient.givenName">
						<input type="text" name="${status.expression}" value="${status.value}" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
			<td>
				<spring:bind path="patient.middleName">
					<input type="text" name="${status.expression}" value="${status.value}" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
			<td>
				<spring:bind path="patient.familyName">
					<input type="text" name="${status.expression}" value="${status.value}" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr><td><br/></td><td><br/></td></tr>
		<tr>
			<th><spring:message code="PatientIdentifier.identifier"/></th>
			<th><spring:message code="PatientIdentifier.identifierType"/></th>
			<th><spring:message code="PatientIdentifier.location"/></th>
		</tr>
		<c:forEach items="${identifiers}" var="id">
			<tr>
				<td>${id.identifier}</td>
				<td>${id.identifierType.name}</td>
				<td>${id.location.name}</td>
			</tr>
		</c:forEach>
		<tr>
			<td>
				<spring:bind path="patient.identifier">
					<input type="text" 
							name="${status.expression}" 
							value="" 
							onBlur="return true; validateIdentifier(this, 'addButton', '<spring:message code="error.identifier"/>');"/>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
			<td>
				<select name="identifierType">
					<openmrs:forEachRecord name="patientIdentifierType">
						<option value="${record.patientIdentifierTypeId}">
							${record.name}
						</option>
					</openmrs:forEachRecord>
				</select>
			</td>
			<td>
				<select name="location">
					<openmrs:forEachRecord name="location">
						<option value="${record.locationId}">
							${record.name}
						</option>
					</openmrs:forEachRecord>
				</select>
			</td>
		</tr>
	</table>
	<br/>
	<table>
		<tr>
			<th><spring:message code="Patient.gender"/></th>
			<td>
				<spring:bind path="patient.gender">
					<select name="gender" id="gender">
						<openmrs:forEachRecord name="gender">
							<option value="${record.key}" <c:if test="${record.key == status.value}">selected</c:if>><spring:message code="Patient.gender.${record.value}"/></option>
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
					<input type="text" name="${status.expression}" value="${status.value}"/>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th><spring:message code="PatientAddress.address2"/></th>
			<td>
				<spring:bind path="patient.address2">
					<input type="text" name="${status.expression}" value="${status.value}" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th><spring:message code="Patient.birthdate"/></th>
			<td colspan="3">
				<spring:bind path="patient.birthdate">			
					<input type="text" name="birthdate" size="10" 
						   value="${status.value}" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
				</spring:bind>
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
			<th><spring:message code="Patient.tribe"/></th>
			<td>
				<spring:bind path="patient.tribe">
					<select name="tribe">
						<openmrs:forEachRecord name="tribe">
							<option value="${record.tribeId}" <c:if test="${record.name == status.value}">selected</c:if>>
								${record.name}
							</option>
						</openmrs:forEachRecord>
					</select>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th><spring:message code="Patient.mothersName"/></th>
			<td>
				<spring:bind path="patient.mothersName">
					<input type="text" name="mothersName" value="${status.value}" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
	</table>
	
	<br />
	<input type="submit" value="<spring:message code="general.save" />" id="addButton">
</form>

<script type="text/javascript">
	document.forms[0].elements[0].focus();
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>
