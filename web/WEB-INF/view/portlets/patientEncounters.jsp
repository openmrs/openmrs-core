<%@ include file="/WEB-INF/template/include.jsp" %>
<br/>
<table class="box" cellspacing="0" cellpadding="2">
	<c:if test="${fn:length(model.patientEncounters) > 0}">
		<tr>
			<th colspan="6" class="tableTitle">All Encounters</th>
		</tr>
		<tr>
			<th> <spring:message code="Encounter.datetime"/> </th>
			<th> <spring:message code="Encounter.type"/>     </th>
			<th> <spring:message code="Encounter.provider"/> </th>
			<th> <spring:message code="Encounter.form"/>     </th>
			<th> <spring:message code="Encounter.location"/> </th>
			<th> <spring:message code="Encounter.enterer"/>  </th>
		</tr>
		<openmrs:forEachEncounter encounters="${model.patientEncounters}" sortBy="encounterDatetime" descending="true" var="enc">
			<tr>
				<td><openmrs:formatDate date="${enc.encounterDatetime}" type="small" /></td>
			 	<td>${enc.encounterType.name}</td>
			 	<td>${enc.provider.firstName} ${enc.provider.lastName}</td>
			 	<td>${enc.form.name}</td>
			 	<td>${enc.location.name}</td>
			 	<td>${enc.creator.firstName} ${enc.creator.lastName}</td>
			</tr>
		</openmrs:forEachEncounter>
	</c:if>
	<c:if test="${fn:length(encounters) == 0}">
		<tr>
			<th colspan="6" class="tableTitle"><spring:message code="FormEntry.no.last.encounters"/></th>
		</tr>
	</c:if>
</table>