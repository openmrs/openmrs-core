<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:globalProperty key="use_patient_attribute.tribe" defaultValue="false" var="showTribe"/>

<div class="boxHeader"><spring:message code="Patient.title"/></div>
<div class="box">
	<table class="patientAddress">
		<thead>
			<tr>
				<th><spring:message code="Patient.names"/></th>
				<th><spring:message code="Patient.mothersName"/></th>
				<th><spring:message code="Patient.civilStatus"/></th>
				<th><spring:message code="Patient.race"/></th>
				<c:if test="${showTribe == 'true'}">
					<th><spring:message code="Patient.tribe"/></th>
				</c:if>
				<th><spring:message code="Patient.birthplace"/></th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<td>
					<c:forEach var="name" items="${model.patient.names}" varStatus="status">
						<c:if test="${name == model.patient.patientName}">*</c:if>
						${name.givenName} ${name.middleName} ${name.familyName}<br/>
					</c:forEach>
				</td>
				<td>${model.patient.mothersName}</td>
				<td>
					<c:if test='${model.patient.civilStatus != null}'>
						<openmrs:concept conceptId="${model.patient.civilStatus.conceptId}" nameVar="n" var="v" numericVar="nv">${n.name}</openmrs:concept>
					</c:if>
				</td>
				<td>${model.patient.race}</td>
				<c:if test="${showTribe == 'true'}">
					<td>${model.patient.tribe.name}</td>
				</c:if>
				<td><c:if test='${model.patient.birthplace != null}'>${model.patient.birthplace}</c:if></td>
			</tr>
		</tbody>
	</table>
</div>

<br/>

<div class="boxHeader"><spring:message code="Patient.addresses"/></div>
<div class="box">
	<table class="patientAddress">
		<thead>
			<openmrs:portlet url="address" id="addressPortlet" size="columnHeaders" parameters="addressShowTable=false|addressShowExtended=true" />
		</thead>
		<tbody>
			<c:forEach var="address" items="${model.patient.addresses}" varStatus="status">
				<% request.setAttribute("address", pageContext.getAttribute("address")); %>
				<spring:nestedPath path="address">
					<openmrs:portlet url="address" id="addressPortlet" size="inOneRow" parameters="addressShowTable=false|addressShowExtended=true" />
				</spring:nestedPath>
			</c:forEach>
		</tbody>
	</table>
</div>

<br/>
<br/>

<div id="patientDemographicsEdit">
	<openmrs:hasPrivilege privilege="Edit Patients">
		<a href="<%= request.getContextPath() %>/admin/patients/patient.form?patientId=${model.patient.patientId}"><spring:message code="Patient.edit"/></a><br />
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Edit Patients" inverse="true">
		<openmrs:hasPrivilege privilege="Add Patients">
			<a href="<%= request.getContextPath() %>/admin/patients/newPatient.form?pId=${model.patient.patientId}"><spring:message code="Patient.edit"/></a><br />
		</openmrs:hasPrivilege>
	</openmrs:hasPrivilege>
</div>
