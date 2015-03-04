<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Encounters" redirect="/encounters/encounterDisplay.htm" otherwise="/login.htm" />
<openmrs:message var="pageTitle" code="encounter.title" scope="page"/>
<%@ include file="/WEB-INF/template/headerMinimal.jsp" %>

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


<openmrs:extensionPoint pointId="org.openmrs.encounters.encounterListTop" type="html" parameters="encounterId=${model.encounter.encounterId}">
	<openmrs:hasPrivilege privilege="${extension.requiredPrivilege}">
		<div class="boxHeader" style="font-weight: bold;"><openmrs:message code="${extension.title}" /></div>
		<div class="box" style="padding: 0px 0px 5px;"><openmrs:message code="${extension.content}" />
			<c:if test="${extension.portletUrl != null}">
				<openmrs:portlet url="${extension.portletUrl}" moduleId="${extension.moduleId}" id="${extension.portletUrl}" encounterId="${model.encounter.encounterId}" parameters="allowEdits=true"/>
			</c:if>
		</div>
		<br />
	</openmrs:hasPrivilege>
</openmrs:extensionPoint>


<div style="float: right">
	<openmrs:hasPrivilege privilege="Edit Encounters">
		<a href="javascript:void(0)" onClick="window.parent.location = '${pageContext.request.contextPath}/admin/encounters/encounter.form?encounterId=${model.encounter.encounterId}'; return false;">[ <openmrs:message code="Encounter.edit"/> ]</a>
	</openmrs:hasPrivilege>
</div>

<c:forEach var="pageEntry" items="${model.pages}">
	<c:set var="thisPage" value="${pageEntry.value}" />
	<c:set var="pageNumber" value="${pageEntry.key}" />
	<div id="page_${pageNumber}">
		<table class="encounterFormTable" cellpadding="0" cellspacing="0">
			<c:if test="${model.usePages}">
				<tr><td align="center" colspan="2" style="background-color: black; color: white;"><openmrs:message code="Encounter.page" arguments="${pageNumber}"/></td></tr>
			</c:if>
		
		<%-- The alternating odd/even status of the current row --%>	
		<c:set var="rowStatus" value="false"/>
		
		<%-- Loop over the rows on this page --%>
		<c:forEach var="fieldHolder" items="${thisPage}" varStatus="varStatus">
			<c:if test="${ showEmptyFields || not empty fieldHolder.obs }">
				<tr valign="top" class='${rowStatus == true ? "evenRow" : "oddRow" }'>
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
											<c:forEach var="obsList" items="${obsRow}">
												<td>
													<span class="encounterViewObsGroup">
														<c:forEach var="obs" items="${obsList}" varStatus="obsVarStatus">
															<openmrs:format obsValue="${obs}" />
															<c:if test="${!obsVarStatus.last}">--</c:if>
														</c:forEach>
													</span>
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
										<tr <c:if test="${varStatusInner.count > 1}"> class='${rowStatus == true ? "evenRow" : "oddRow" }' </c:if>>
											<td class="encounterViewObsConcept"><openmrs_tag:concept conceptId="${obs.concept.conceptId}"/>:</td>
											<td class="encounterViewObsAnswer">
										<span class="encounterViewObsValue"><openmrs:format obsValue="${obs}" /></span>
										<c:if test="${not empty obs.obsDatetime && obs.obsDatetime != model.encounter.encounterDatetime}">
											<span class="encounterViewObsDatetime">
												<openmrs:message code="general.onDate"/>
												<openmrs:formatDate date="${obs.obsDatetime}"/>
											</span>
										</c:if>
										<br/>
										<c:if test="${varStatusInner.last}" />
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