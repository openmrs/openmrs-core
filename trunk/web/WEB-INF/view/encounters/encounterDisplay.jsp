<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Encounters" redirect="/encounters/encounterDisplay.htm" otherwise="/login.htm" />

<%@ include file="/WEB-INF/template/headerMinimal.jsp" %>

<openmrs:globalProperty var="viewEncounterWhere" key="dashboard.encounters.viewWhere" defaultValue="newWindow"/>
<openmrs:globalProperty var="showEmptyFields" key="dashboard.encounters.showEmptyFields" defaultValue="true"/>

<script type="text/javascript">
	var pageIds = new Array();
	<c:forEach var="pageNumber" items="${model.pageNumbers}">
		pageIds.push('page_${pageNumber}');
	</c:forEach>
	
	function showPage(pg) {
		for (var i = 0; i < pageIds.length; ++i) {
			hideLayer(pageIds[i]);
		}
		if (('' + pg).indexOf('page_') < 0)
			pg = 'page_' + pg;
		showLayer(pg);
	}

</script>

<div class="boxHeader">
	<div style="border-bottom: 1px white solid; color: white">
		<c:set var="patient" value="${model.encounter.patient}"/>
		<center>
			<b>
				${patient.personName.givenName} ${patient.personName.middleName} ${patient.personName.familyName}
			</b>
			|
			<c:if test="${patient.age > 0}">${patient.age} <spring:message code="Person.age.years"/></c:if>
			<c:if test="${patient.age == 0}">< 1 <spring:message code="Person.age.year"/></c:if>
			<c:forEach var="identifier" items="${patient.identifiers}">
				|
				${identifier.identifierType.name}: ${identifier.identifier}
			</c:forEach>

		</center>
	</div>
	<div style="float: right">
		<c:if test="${viewEncounterWhere == 'newWindow' || viewEncounterWhere == 'oneNewWindow'}">
			<a href="javascript:self.close();">[ <spring:message code="general.closeWindow" /> ]</a>
		</c:if>
		<c:if test="${viewEncounterWhere != 'newWindow' && viewEncounterWhere != 'oneNewWindow'}">
			<a href="javascript:window.history.back()">[ <spring:message code="general.navigateBack" /> ]</a>
		</c:if>
		<openmrs:hasPrivilege privilege="Edit Encounters">
			<br/>
			<c:choose>
				<c:when test="${viewEncounterWhere == 'newWindow' || viewEncounterWhere == 'oneNewWindow'}">
					<a href="javascript:window.opener.location = '${pageContext.request.contextPath}/admin/encounters/encounter.form?encounterId=${model.encounter.encounterId}'; window.parent.focus(); window.close();">[ <spring:message code="general.edit"/> ]</a>
				</c:when>
				<c:otherwise>
					<a href="${pageContext.request.contextPath}/admin/encounters/encounter.form?encounterId=${model.encounter.encounterId}">[ <spring:message code="general.edit"/> ]</a>
				</c:otherwise>
			</c:choose>
		</openmrs:hasPrivilege>
	</div>
	<spring:message code="Encounter.title"/>: <b>${model.encounter.encounterType.name}<b>
		<spring:message code="general.onDate"/> <b><openmrs:formatDate date="${model.encounter.encounterDatetime}"/></b>
		<spring:message code="general.atLocation"/> <b>${model.encounter.location}</b>
	<br/>
	<spring:message code="Encounter.form"/>: <b>${model.encounter.form.name}</b>
	<c:if test="${model.usePages}">
		<br/>
		<spring:message code="FormField.pageNumber"/>
		<c:forEach var="pageNumber" items="${model.pageNumbers}">
			<%-- TODO: get rid of
				style="color: white"
			--%>
			&nbsp;&nbsp;
			<a style="color: white" href="javascript:showPage(${pageNumber})">${pageNumber}</a>
			&nbsp;&nbsp;
		</c:forEach>
	</c:if>
</div>

<c:forEach var="pageEntry" items="${model.pages}">
	<c:set var="thisPage" value="${pageEntry.value}" />
	<c:set var="pageNumber" value="${pageEntry.key}" />
	<div id="page_${pageNumber}">
		<table class="encounterFormTable" cellpadding="0" cellspacing="0">
			<c:if test="${model.usePages}">
				<tr><td align="center" colspan="2" style="background-color: black; color: white;">Page ${pageNumber}</td></tr>
			</c:if>
		
		<%-- The alternating odd/even status of the current row --%>	
		<c:set var="rowStatus" value="false"/>
		
		<%-- Loop over the rows on this page --%>
		<c:forEach var="fieldHolder" items="${thisPage}" varStatus="varStatus">
			<c:if test="${ showEmptyFields || not empty fieldHolder.obs }">
				<tr valign="top" class='<c:choose><c:when test="${rowStatus == true}">evenRow<c:set var="rowStatus" value="false"/></c:when><c:otherwise>oddRow<c:set var="rowStatus" value="true"/></c:otherwise></c:choose>'>
					<th class="encounterViewLabel">${fieldHolder.label}</th>
					<td>
						<c:choose>
							<c:when test="${fieldHolder.obsGrouping}">
								<table class="borderedTable">
									<tr>
										<th class="smallHeader obsGroupLabel" colspan="${fn:length(fieldHolder.groupMemberConcepts)}">
											<openmrs_tag:concept conceptId="${fieldHolder.obs[0].concept.conceptId}"/>
										</th>
									</tr>
									<tr>
										<c:forEach var="conceptColumn" items="${fieldHolder.groupMemberConcepts}">
											<th class="smallHeader"><openmrs_tag:concept conceptId="${conceptColumn.conceptId}"/></th>
										</c:forEach>
									</tr>
									<c:forEach var="matrixEntry" items="${fieldHolder.obsGroupMatrix}">
										<c:set var="obsGrouper" value="${matrixEntry.key}"/>
										<c:set var="obsRow" value="${matrixEntry.value}"/>
										<tr>
											<c:forEach var="obs" items="${obsRow}">
												<td>
													<span class="encounterViewObsGroup">${obs.valueAsString[model.locale]}</span>
												</td>
											</c:forEach>
										</tr>
									</c:forEach>
								</table>
							</c:when>
							<c:otherwise>
								<%-- Just loop over the obs that have been grouped together by concept --%>
								<table class="encounterFormInnerTable" cellspacing="0" cellpadding="4">
								<c:forEach var="obs" items="${fieldHolder.obs}" varStatus="varStatusInner">
									<c:if test="${varStatusInner.first}">
										<tr <c:if test="${varStatusInner.count > 1}">class='<c:choose><c:when test="${rowStatus == true}">evenRow<c:set var="rowStatus" value="false"/></c:when><c:otherwise>oddRow<c:set var="rowStatus" value="true"/></c:otherwise></c:choose>'</c:if>>
										<td class="encounterViewObsConcept"><openmrs_tag:concept conceptId="${obs.concept.conceptId}"/>:</td>
										<td class="encounterViewObsAnswer">
									</c:if>
											<span class="encounterViewObsValue">${obs.valueAsString[model.locale]}</span>
											<c:if test="${not empty obs.obsDatetime && obs.obsDatetime != model.encounter.encounterDatetime}">
												<span class="encounterViewObsDatetime">
													<spring:message code="general.onDate"/>
													<openmrs:formatDate date="${obs.obsDatetime}"/>
												</span>
											</c:if>
											<br/>
									<c:if test="${varStatusInner.last}">
										</td>
										</tr>
									</c:if>
								</c:forEach>
								</table>
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
			</c:if>
		</c:forEach>
		</table>
	</div>
	<script type="text/javascript">
		hideLayer('page_${pageNumber}');
	</script>
</c:forEach>

<script type="text/javascript">
	showPage(pageIds[0]);
</script>

<%@ include file="/WEB-INF/template/footerMinimal.jsp" %>