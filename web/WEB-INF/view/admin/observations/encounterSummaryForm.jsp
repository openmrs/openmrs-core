<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Encounters" otherwise="/login.htm" redirect="/admin/observations/encounterSummary.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<style>
	#table th {
		text-align: left;
	}
</style>

<h2><spring:message code="Encounter.title"/></h2>

<b class="boxHeader"><spring:message code="Encounter.summary"/></b>
<div class="box">
	<table id="encounter">
		<tr>
			<th><spring:message code="Encounter.type"/></th>
			<td>${encounter.encounterType.name}</td>
		</tr>
		<tr>
			<th><spring:message code="Encounter.patient"/></th>
			<td>
				${encounter.patient.patientName.givenName} ${encounter.patient.patientName.middleName} ${encounter.patient.patientName.familyName}
			</td>
		</tr>
		<tr>
			<th><spring:message code="Encounter.provider"/></th>
			<td>
				${encounter.provider.firstName} ${encounter.provider.lastName}
			</td>
		</tr>
		<tr>
			<th><spring:message code="Encounter.location"/></th>
			<td>${encounter.location.name}</td>
		</tr>
		<tr>
			<th><spring:message code="Encounter.form"/></th>
			<td>${encounter.form.name}</td>
		</tr>
		<tr>
			<th><spring:message code="Encounter.datetime"/></th>
			<td>${encounter.encounterDatetime}</td>
		</tr>
		<c:if test="${!(encounter.creator == null)}">
			<tr>
				<th><spring:message code="general.createdBy" /></th>
				<td>
					${encounter.creator.firstName} ${encounter.creator.lastName} -
					<openmrs:formatDate date="${encounter.dateCreated}" type="long" />
				</td>
			</tr>
		</c:if>
	</table>
</div>
<br />

<b class="boxHeader"><spring:message code="Encounter.observations"/></b>
<div class="box">
<table cellspacing="0" cellpadding="2" width="98%">
	<tr>
		<th><spring:message code="Obs.concept"/></th>
		<th><spring:message code="Obs.datetime"/></th>
		<th><spring:message code="Obs.location"/></th>
		<th><spring:message code="Obs.comment"/></th>
		<th><spring:message code="general.creator"/></th>
	</tr>
	<c:forEach items="${encounter.obs}" var="obs" varStatus="status">
		<tr <c:if test="${status.index % 2 == 0}">class="evenRow"</c:if>>
			<td><a href="obs.form?obsId=${obs.obsId}"><%= ((org.openmrs.Obs)pageContext.getAttribute("obs")).getConcept().getName(request.getLocale()) %></a></td>
			<td><openmrs:formatDate date="${obs.obsDatetime}" type="short" /></td>
			<td>${obs.location.name}</td>
			<td>${obs.comment}</td>
			<td>
				${obs.creator.firstName} ${obs.creator.lastName} -
				<openmrs:formatDate date="${obs.dateCreated}" type="medium" />
			</td>
		</tr>
	</c:forEach>
</table>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>