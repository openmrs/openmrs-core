<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/formentry/patientSummary.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<h2><spring:message code="formentry.title"/></h2>

<div id="patientSummary">
	<b class='boxHeader'><spring:message code="formentry.patient.info"/></b>
	<a href='<%= request.getContextPath() %>/admin/patients/newPatient.form?pId=${patient.patientId}' style='float:right'><spring:message code="Patient.edit"/></a>
	<div id="otherInfo" class="sideNote" style="width: 200px; float: right; clear: right">
		<b><spring:message code="Patient.identifiers"/></b><br/>
			<c:forEach var="identifier" items="${patient.identifiers}" varStatus="status">
				<c:if test="${status.index != 0}">${identifier.identifier}<br/></c:if>
			</c:forEach>
		<br/>
		<b><spring:message code="Patient.names"/></b><br/>
			<c:forEach var="name" items="${patient.names}" varStatus="status">
				<c:if test="${status.index != 0}">${name.familyName}, ${name.givenName} ${name.middleName}<br/></c:if>
			</c:forEach>
		<br/>
		<b><spring:message code="Patient.addresses"/></b><br/>
			<c:forEach var="address" items="${patient.addresses}" varStatus="status">
				<c:if test="${status.index != 0}">${address.address1} - ${address.address2}<br/></c:if>
			</c:forEach>
	</div>
	<table>
		<tr>
			<td valign="top"><b><spring:message code="Patient.identifier"/></b></td>
			<td id="identifier">
				<c:forEach var="identifier" items="${patient.identifiers}" varStatus="status">
					<c:if test="${status.index == 0}">${identifier.identifier}</c:if>
				</c:forEach>
			</td>
		</tr>
		<tr>
			<td><b><spring:message code="general.name"/></b></td>
			<td id="name">
				<c:forEach var="name" items="${patient.names}" varStatus="status">
					<c:if test="${status.index == 0}"><b>${name.familyName}</b>, ${name.givenName} ${name.middleName}</c:if>
				</c:forEach>
			</td>
		</tr>
		<tr>
			<td><b><spring:message code="Patient.gender"/></b></td>
			<td id="gender">${patient.gender}</td>
		</tr>
		<c:forEach var="address" items="${patient.addresses}" varStatus="status">
			<c:if test="${status.index == 0}">
				<tr><td valign="top"><b><spring:message code="PatientAddress.address1"/></b><td id="address1">${address.address1}</td></tr>
				<tr><td valign="top"><b><spring:message code="PatientAddress.address2"/></b><td id="address2">${address.address2}</td></tr>
			</c:if>
		</c:forEach>
		<tr><td valign="top"><b><spring:message code="Tribe.name"/></b><td id="tribe">${patient.tribe.name}</td></tr>
		<tr><td valign="top"><b><spring:message code="Patient.birthdate"/></b><td id="birthdate"><spring:bind path="patient.birthdate">${status.value}</spring:bind></td></tr>
		<tr><td valign="top"><b><spring:message code="Patient.mothersName"/></b><td id="mothersName">${patient.mothersName}</td></tr>
	</table>
	<br /><input type='button' value='<spring:message code="formentry.patient.switch"/>' onClick="document.location='index.htm?phrase=${param.phrase}'" />
</div>

<br />

<b class="boxHeader"><spring:message code="formentry.step2"/></b>
<div id="selectForm" class="box">
	<form id="selectFormForm" method="post" action="<%= request.getContextPath() %>/formDownload">
		<table>
			<tr>
				<td><input type="radio" name="formType" value="adultInitial" id="adultInitial"></td>
				<td><strike><label for="adultInitial">Adult Initial</label></strike></td>
			</tr>
			<tr>
				<td><input type="radio" name="formType" value="adultReturn" id="adultReturn"></td>
				<td><label for="adultReturn">Adult Return</label></td>
			</tr>
			<tr>
				<td><input type="radio" name="formType" value="adultReturn_local" id="adultReturn_local"></td>
				<td><label for="adultReturn_local">Adult Return (Localhost)</label></td>
			</tr>
			<tr>
				<td><input type="radio" name="formType" value="pedInitial" id="pedInitial"></td>
				<td><strike><label for="pedInitial">Ped Initial</label></strike></td>
			</tr>
			<tr>
				<td><input type="radio" name="formType" value="pedReturn" id="pedReturn"></td>
				<td><strike><label for="pedReturn">Ped Return</label></strike></td>
			</tr>
		</table>
		<input type="hidden" name="patientId" id="patientId" value="${patient.patientId}">
		<input type="submit" value="<spring:message code="formentry.download.form"/>">
	</form>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>
