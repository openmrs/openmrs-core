<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/formentry/patientSummary.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<h3><spring:message code="formentry.title"/></h3>

<div id="patientSummary">
	<b class='boxHeader'>
		<c:forEach var="name" items="${patient.names}" varStatus="status">
			<c:if test="${status.index == 0}">${name.givenName} ${name.middleName} ${name.familyName}</c:if>
		</c:forEach>
		(
		<c:forEach var="identifier" items="${patient.identifiers}" varStatus="status">
					<c:if test="${status.index == 0}">${identifier.identifier}</c:if>
		</c:forEach>
		)
	</b>

	<div class="box">	
		<a href='<%= request.getContextPath() %>/admin/patients/newPatient.form?pId=${patient.patientId}' style='float:right'><spring:message code="Patient.edit"/></a>
		<a href="#switch" onClick="document.location='index.htm?phrase=${param.phrase}&autoJump=false'" style='float:right; clear:right'>
			<spring:message code="formentry.patient.switch"/>
		</a>

		<table>
			<tr>
				<td><spring:message code="Patient.gender"/></td>
				<td id="gender">
					<c:if test="${patient.gender == 'M'}">
						<img src="${pageContext.request.contextPath}/images/male.gif" />
					</c:if>
					<c:if test="${patient.gender == 'F'}">
						<img src="${pageContext.request.contextPath}/images/female.gif" />
					</c:if>
				</td>
			</tr>
			<c:forEach var="name" items="${patient.names}" varStatus="status">
				<c:if test="${status.index == 1}">
					<tr>
						<td valign="top"><spring:message code="Patient.other.names"/></td>
						<td>
							<c:forEach var="name" items="${patient.names}" varStatus="status">
								<c:if test="${status.index != 0}"><b>${name.givenName} ${name.middleName} ${name.familyName}</b><br/></c:if>
							</c:forEach>
						</td>
					</tr>
				</c:if>
			</c:forEach>
			<c:forEach var="name" items="${patient.identifiers}" varStatus="status">
				<c:if test="${status.index == 1}">
					<tr>
						<td valign="top"><spring:message code="Patient.other.identifiers"/></td>
						<td>
							<c:forEach var="identifier" items="${patient.identifiers}" varStatus="status">
								<c:if test="${status.index != 0}"><b>${identifier.identifier}</b><br/></c:if>
							</c:forEach>
						</td>
					</tr>
				</c:if>
			</c:forEach>
			<c:forEach var="name" items="${patient.addresses}" varStatus="status">
				<c:if test="${status.index == 1}">
					<tr>
						<td valign="top"><spring:message code="Patient.addresses"/></td>
						<td>
						<c:forEach var="address" items="${patient.addresses}" varStatus="status">
							<b>
								${address.address1} <br/>
								${address.address2} <br/>
							</b>
							<br/>
						</c:forEach>
					</td>
				</tr>
				</c:if>
			</c:forEach>
			<tr>
				<td valign="top"><spring:message code="Patient.birthdate"/></td>
				<td id="birthdate">
					<b><spring:bind path="patient.birthdate">${status.value}</spring:bind></b>
					<c:if test="${patient.age > 0}">
						( ${patient.age} <spring:message code="Patient.age.years"/> )
					</c:if>
					<c:if test="${patient.age == 0}">
						( < 1 <spring:message code="Patient.age.year"/> )
					</c:if>
				</td>
			</tr>
			<tr>
				<td valign="top"><spring:message code="Patient.tribe"/></td>
				<td id="tribe"><b>${patient.tribe.name}</b></td>
			</tr>
		</table>
	</div>	
</div>

<br />

<b class="boxHeader" style="clear: right">
	<spring:message code="formentry.step2"/>
	<c:forEach var="name" items="${patient.names}" varStatus="status">
		<c:if test="${status.index == 0}">${name.givenName} ${name.middleName} ${name.familyName}</c:if>
	</c:forEach>
	(
	<c:forEach var="identifier" items="${patient.identifiers}" varStatus="status">
				<c:if test="${status.index == 0}">${identifier.identifier}</c:if>
	</c:forEach>
	)
</b>

<div id="selectForm" class="box">
	<form id="selectFormForm" method="post" action="<%= request.getContextPath() %>/formDownload">
		<table>
			<c:forEach items="${forms}" var="form">
				<tr>
					<td><input type="radio" name="formId" value="${form.formId}" id="${form.formId}"></td>
					<td><label for="${form.formId}">${form.name} (v.${form.version}<c:if test="${form.build != null}">-${form.build}</c:if>)</label></td>
				</tr>
			</c:forEach>
		</table>
		<input type="hidden" name="patientId" id="patientId" value="${patient.patientId}">
		<input type="submit" value="<spring:message code="formentry.download.form"/>">
	</form>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>
