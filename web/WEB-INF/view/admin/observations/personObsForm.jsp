<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Observations" otherwise="/login.htm" redirect="/admin/observations/obs.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:authentication/>

<h3>
	<c:set var="linkUrl" value="../../personDashboard.form?personId=${command.person.personId}"/>
	<c:if test="${command.person.patient}">
		<c:set var="linkUrl" value="../../patientDashboard.form?patientId=${command.person.personId}"/>
	</c:if>
	<a href="${linkUrl}">${command.person.personName}</a>
	&nbsp;-&nbsp;
	<c:if test="${empty command.concept}">
		<spring:message code="Person.allObservations"/>
	</c:if>
	<c:if test="${not empty command.concept}">
		<spring:message code="Person.allObservationsForConcept" arguments="${command.concept.name.name}"/>
		&nbsp;&nbsp;&nbsp;<small><a href="?personId=${command.person.personId}"><spring:message code="general.showAll"/></a></small>
	</c:if>
</h3>

<table cellspacing="0" cellpadding="2" border="1">
	<tr>
		<c:if test="${empty command.concept}">
			<th><spring:message code="Obs.concept"/></th>
		</c:if>
		<th>
			<spring:message code="Obs.value"/>
		</th>
		<th>
			<spring:message code="Obs.datetime"/>
		</th>
		<th>
			<spring:message code="general.creator"/>
			<spring:message code="general.dateCreated"/>
		</th>
		<th><spring:message code="Obs.encounter"/></th>
		<th><spring:message code="general.voidInfo"/></th>
	</tr>
	<c:forEach var="obs" items="${command.observations}">
		<tr <c:if test="${obs.voided}">style="background-color: #f0f0f0"</c:if>>
			<c:if test="${empty command.concept}">
				<td>
					<c:if test="${obs.voided}"><strike></c:if>
						<openmrs:format concept="${obs.concept}"/>
					<c:if test="${obs.voided}"></strike></c:if>
				</td>
			</c:if>
			<td>
				<a target="new" href="obs.form?obsId=${obs.obsId}">
					<c:if test="${obs.voided}"><strike></c:if>
						<openmrs:format obsValue="${obs}"/>
					<c:if test="${obs.voided}"></strike></c:if>
				</a>
			</td>
			<td>
				<c:if test="${obs.voided}"><strike></c:if>
					<openmrs:formatDate date="${obs.obsDatetime}"/>
				<c:if test="${obs.voided}"></strike></c:if>
			</td>
			<td>
				<c:if test="${obs.voided}"><strike></c:if>
					<openmrs:format user="${obs.creator}"/>
					<openmrs:formatDate date="${obs.dateCreated}"/>
				<c:if test="${obs.voided}"></strike></c:if>
			</td>
			<td>
				<c:if test="${not empty obs.encounter}">
					<a target="new" href="../encounters/encounter.form?encounterId=${obs.encounter.encounterId}">
						${obs.encounter.encounterType.name}
						<openmrs:formatDate date="${obs.encounter.encounterDatetime}"/>
					</a>
					<c:if test="${obs.dateCreated != obs.encounter.dateCreated}">
						<br/>
						<spring:message code="general.byPerson"/> <openmrs:format user="${obs.encounter.creator}"/>
						<spring:message code="general.onDate"/> <openmrs:formatDate date="${obs.encounter.dateCreated}"/>
					</c:if>
				</c:if>
			</td>
			<td>
				<c:if test="${obs.voided}">
					<openmrs:format user="${obs.voidedBy}"/><br/>
					<spring:message code="general.onDate"/>
						<openmrs:formatDate date="${obs.dateVoided}"/><br/>
					<spring:message code="general.reason"/>: ${obs.voidReason}
				</c:if>
			</td>
		</tr>
	</c:forEach>
</table>

<%@ include file="/WEB-INF/template/footer.jsp" %>