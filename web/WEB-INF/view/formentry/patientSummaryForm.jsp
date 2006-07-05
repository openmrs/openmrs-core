<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/formentry/patientSummary.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<h3><spring:message code="formentry.title"/></h3>

<script type="text/javascript">
	
	var timeOut = null;

	function startDownloading() {
		timeOut = setTimeout("goBack()", 30000);
		
	}
	
	function goBack() {
		document.location='index.htm';
	}
	
	function switchPatient() {
		document.location='index.htm?phrase=${param.phrase}&autoJump=false';
	}
	
	function cancelTimeout() {
		if (timeOut != null)
			clearTimeout(timeOut);
	}
	
	//window.onfocus=cancelTimeout;
	
</script>

<style>
	.formLink {
		line-height: 1.5em;
		font-size: 1.2em;
	}
</style>

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
		<a href="#switch" onClick="switchPatient()" style='float:right; clear:right'>
			<spring:message code="formentry.patient.switch"/>
		</a>

		<table>
			<tr>
				<td><spring:message code="Patient.gender"/>: </td>
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
						<td valign="top"><spring:message code="Patient.other.names"/>: </td>
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
						<td valign="top"><spring:message code="Patient.other.identifiers"/>: </td>
						<td>
							<c:forEach var="identifier" items="${patient.identifiers}" varStatus="status">
								<c:if test="${status.index != 0}"><b>${identifier.identifier}</b> (${identifier.identifierType.name}) <br/></c:if>
							</c:forEach>
						</td>
					</tr>
				</c:if>
			</c:forEach>
			<c:forEach var="name" items="${patient.addresses}" varStatus="status">
				<c:if test="${status.index == 1}">
					<tr>
						<td valign="top"><spring:message code="Patient.addresses"/>: </td>
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
				<td valign="top"><spring:message code="Patient.birthdate"/>: </td>
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
				<td valign="top"><spring:message code="Patient.tribe"/>: </td>
				<td id="tribe"><b>${patient.tribe.name}</b></td>
			</tr>
		</table>
		
		<center>
			<table class="box" cellspacing="0" cellpadding="2">
				<c:if test="${fn:length(encounters) > 0}">
					<tr>
						<th colspan="6" class="tableTitle"><spring:message code="FormEntry.last.encounters"/></th>
					</tr>
					<tr>
						<th> <spring:message code="Encounter.type"/>     </th>
						<th> <spring:message code="Encounter.provider"/> </th>
						<th> <spring:message code="Encounter.form"/>     </th>
						<th> <spring:message code="Encounter.location"/> </th>
						<th> <spring:message code="Encounter.datetime"/> </th>
						<th> <spring:message code="Encounter.enterer"/>  </th>
					</tr>
					<c:forEach items="${encounters}" var="enc" end="2">
						<tr>
						 	<td>${enc.encounterType.name}</td>
						 	<td>${enc.provider.firstName} ${enc.provider.lastName}</td>
						 	<td>${enc.form.name}</td>
						 	<td>${enc.location.name}</td>
						 	<td><openmrs:formatDate date="${enc.encounterDatetime}" type="small" /></td>
						 	<td>${enc.creator.firstName} ${enc.creator.lastName}</td>
						</tr>
					</c:forEach>
				</c:if>
				<c:if test="${fn:length(encounters) == 0}">
					<tr>
						<th colspan="6" class="tableTitle"><spring:message code="FormEntry.no.last.encounters"/></th>
					</tr>
				</c:if>
			</table>
		</center>
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
		<c:forEach items="${forms}" var="form">
			<a href="${pageContext.request.contextPath}/formDownload?target=formEntry&formId=${form.formId}&patientId=${patient.patientId}" onclick="startDownloading()" class="formLink">
				${form.name} 
			</a> (v.${form.version})
			
			<br />
		</c:forEach>
	</form>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>
