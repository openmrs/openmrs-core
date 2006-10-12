<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/admin/patients/addPatient.htm"/>

<%@ include file="/WEB-INF/template/header.jsp" %>

<c:choose>
	<c:when test="${empty param.name}">
		
		<h2><spring:message code="Patient.find"/></h2>
		<span class="instructions"><spring:message code="Patient.search.instructions" /></span>
		<openmrs:portlet id="createPatient" url="addPatientForm" parameters="postURL=" />
		
	</c:when>
	<c:otherwise>
		
		<openmrs:globalProperty key="use_patient_attribute.tribe" defaultValue="false" var="showTribe"/>
		
		<script type="text/javascript">
			function rowMouseOver(tr) {
				if (tr.className.indexOf("searchHighlight") == -1)
					tr.className = "searchHighlight " + tr.className;
			};
			
			function rowMouseOut(tr) { 
				var c = tr.className;
				tr.className = c.substring(c.indexOf(" ") + 1, c.length);
			};
			
			function onMouseClick(patientId) {
				document.location = "${pageContext.request.contextPath}/patientDashboard.form?patientId=" + patientId;
			}
			
			function onContinueClick() {
				var href = "newPatient.form?name=${param.name}&birthyear=${param.birthyear}&gender=${param.gender}&age=${param.age}";
				document.location = href;
			}
		</script>
		
		<style type="text/css">
			#openmrsSearchTable th {
				text-align: left;
			}
		</style>
		
		<h2><spring:message code="Patient.search.similarPatient"/></h2>
		<b id="similarPatientsIinstructions"><spring:message code="Patient.search.similarPatients.instructions"/></b>
		
		<br/><br/>
		
		<table class="openmrsSearchTable" style="width: 100%;" cellpadding="2" cellspacing="0">
			<thead>
				<tr>
					<th><spring:message code="PatientIdentifier.identifier"/></th>
					<th><spring:message code="PatientName.givenName"/></th>
					<th><spring:message code="PatientName.middleName"/></th>
					<th><spring:message code="PatientName.familyName"/></th>
					<th><spring:message code="Patient.age"/></th>
					<th><spring:message code="Patient.gender"/></th>
					<c:if test="${showTribe == 'true'}">
						<th><spring:message code="Patient.tribe"/></th>
					</c:if>
					<th></th>
					<th><spring:message code="Patient.birthdate"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="patient" items="${patients}" varStatus="status">
					<tr onMouseOver="rowMouseOver(this)" onMouseOut="rowMouseOut(this)" onClick="onMouseClick(${patient.patientId});" class="<c:choose><c:when test="${status.count % 2 == 0}">evenRow</c:when><c:otherwise>oddRow</c:otherwise></c:choose>">
						<td class="patientIdentifier"><a>${patient.identifier} </a></td>
						<td>${patient.givenName} </td>
						<td>${patient.middleName} </td>
						<td>${patient.familyName} </td>
						<td class="patientAge">${patient.age} </td>
						<td class="patientGender"><img src="/amrs/images/<c:if test="${patient.gender == 'F'}">fe</c:if>male.gif"></td>
						<c:if test="${showTribe}">
							<td>${patient.tribe}</td>
						</c:if>
						<td><c:if test="${patient.birthdateEstimated}">~</c:if></td>
						<td><openmrs:formatDate date="${patient.birthdate}" type="textbox"/></td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
		
		<br/>
		<b id="similarPatientsNotFound"><spring:message code="Patient.search.similarPatients.notFound"/></b>
		<br/><br/>
		
		<input type="button" value='<spring:message code="general.continue"/>' onClick="return onContinueClick()" />
		&nbsp;
		<input type="button" value='<spring:message code="general.cancel"/>' onClick="history.go(-1)" />
		
		<br/><br/>
		
	</c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/template/footer.jsp" %>