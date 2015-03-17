<%@ include file="/WEB-INF/template/include.jsp" %>

<div class="boxHeader${model.patientVariation}"><openmrs:message code="Patient.title"/></div>
<div class="box${model.patientVariation}">
	<table class="personName">
		<thead>
			<tr class="patientDemographicsHeaderRow">
				<th class="patientDemographicsPersonNameHeader"><openmrs:message code="Person.names"/></th>
				<openmrs:forEachDisplayAttributeType personType="patient" displayType="viewing" var="attrType">
					<th class="patientDemographicsPersonAttTypeHeader"><openmrs:message code="PersonAttributeType.${fn:replace(attrType.name, ' ', '')}" text="${attrType.name}"/></th>
				</openmrs:forEachDisplayAttributeType>
			</tr>
		</thead>
		<tbody>
			<tr class="patientDemographicsRow">
				<td valign="top" class="patientDemographicsData" >
					<c:forEach var="name" items="${model.patient.names}" varStatus="status">
						<c:if test="${!name.voided}">
							<% request.setAttribute("name", pageContext.getAttribute("name")); %>
							<spring:nestedPath path="name">
								<openmrs:portlet url="nameLayout" id="namePortlet" size="quickView" parameters="layoutShowExtended=true" />
							</spring:nestedPath>
						</c:if>
					</c:forEach>
				</td>
				<openmrs:forEachDisplayAttributeType personType="patient" displayType="viewing" var="attrType">
					<td valign="top" class="patientDemographicsAttrName">${model.patient.attributeMap[attrType.name]}</td>
				</openmrs:forEachDisplayAttributeType>
			</tr>
		</tbody>
	</table>
</div>

<br/>

<div class="boxHeader${model.patientVariation}"><openmrs:message code="Person.addresses"/></div>
<div class="box${model.patientVariation}">
	<table class="personAddress">
		<thead>
			<openmrs:portlet url="addressLayout" id="addressPortlet" size="columnHeaders" parameters="layoutShowTable=false|layoutShowExtended=true" />
		</thead>
		<tbody>
			<c:forEach var="address" items="${model.patient.addresses}" varStatus="status">
				<c:if test="${!address.voided}">
				<% request.setAttribute("address", pageContext.getAttribute("address")); %>
				<spring:nestedPath path="address">
					<openmrs:portlet url="addressLayout" id="addressPortlet" size="inOneRow" parameters="layoutMode=view|layoutShowTable=false|layoutShowExtended=true" />
				</spring:nestedPath>
				</c:if>
			</c:forEach>
		</tbody>
	</table>
</div>

<br/>
<br/>

<div id="patientDemographicsEdit">
	<openmrs:hasPrivilege privilege="Edit Patients">
		<a href="${pageContext.request.contextPath}/admin/patients/patient.form?patientId=<c:out value="${model.patient.patientId}" />"><openmrs:message code="Patient.edit"/></a> |
		<a href="${pageContext.request.contextPath}/admin/patients/shortPatientForm.form?patientId=<c:out value="${model.patient.patientId}" />"><openmrs:message code="Patient.edit.short"/></a><br /><br />
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Edit Patients" inverse="true">
		<openmrs:hasPrivilege privilege="Add Patients">
			<a href="${pageContext.request.contextPath}/admin/patients/shortPatientForm.form"><openmrs:message code="Patient.addNew"/></a><br />
		</openmrs:hasPrivilege>
	</openmrs:hasPrivilege>
</div>
