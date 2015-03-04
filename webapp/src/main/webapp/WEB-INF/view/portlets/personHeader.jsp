<%@ include file="/WEB-INF/template/include.jsp" %>

<div id="personHeader" class="boxHeader">
	<div id="patientHeaderPatientName"><c:out value="${model.person.personName}" /></div>
	<table id="patientHeaderGeneralInfo">
		<tr>
			<td id="patientHeaderPatientGender">
				<c:if test="${model.person.gender == 'M'}"><img src="${pageContext.request.contextPath}/images/male.gif" alt='<openmrs:message code="Person.gender.male"/>'/></c:if>
				<c:if test="${model.person.gender == 'F'}"><img src="${pageContext.request.contextPath}/images/female.gif" alt='<openmrs:message code="Person.gender.female"/>'/></c:if>
			</td>
			<td id="patientHeaderPatientAge">
				<c:if test="${model.person.age > 0}">${model.person.age} <openmrs:message code="Person.age.years"/></c:if>
				<c:if test="${model.person.age == 0}">< 1 <openmrs:message code="Person.age.year"/></c:if>
				<span id="patientHeaderPatientBirthdate"><c:if test="${not empty model.person.birthdate}">(<c:if test="${model.person.birthdateEstimated}">~</c:if><openmrs:formatDate date="${model.person.birthdate}" type="medium" />)</c:if><c:if test="${empty model.person.birthdate}"><openmrs:message code="general.unknown"/></c:if></span>
			</td>
			<td></td>
		</tr>
	</table>
</div>
