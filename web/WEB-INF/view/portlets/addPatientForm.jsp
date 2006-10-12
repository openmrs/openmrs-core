<%@ include file="/WEB-INF/template/include.jsp" %>

<c:if test="${model.authenticatedUser != null}">

	<openmrs:require privilege="Add Patients" otherwise="/login.htm" redirect="/index.htm" />
	
	<div id="createPatient">
		<b class="boxHeader"><spring:message code="Patient.create"/></b>
		<div class="box">
			<spring:message code="Patient.search.instructions"/> <br/>
			
			<form method="get" action="${model.postURL}">
				<table>
					<tr>
						<td><spring:message code="Patient.name"/></td>
						<td>
							<input type="text" name="name" id="patientName" size="40" />
						</td>
					</tr>
					<tr>
						<td><spring:message code="Patient.birthyear"/></td>
						<td>
							<input type="text" name="birthyear" id="birthyear" size="5" value="" />
							<spring:message code="Patient.age.or"/>
							<input type="text" name="age" id="age" size="5" value="" onFocus="searchWidget.exitNumberMode()" />
						</td>
					</tr>
					<tr>
						<td><spring:message code="Patient.gender"/></td>
						<td>
							<openmrs:forEachRecord name="gender">
								<input type="radio" name="gender" id="${record.key}" value="${record.key}" /><label for="${record.key}"> <spring:message code="Patient.gender.${record.value}"/> </label>
							</openmrs:forEachRecord>
						</td>
					</tr>
				</table>
				
				<br/>
				<input type="submit" value="<spring:message code="general.continue" />"/>
				
			</form>
		</div>
	</div>

</c:if>
