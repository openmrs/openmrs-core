<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Encounters" otherwise="/login.htm" />

<link href="<%= request.getContextPath() %>/openmrs.css" type="text/css" rel="stylesheet" />
<link href="<%= request.getContextPath() %>/style.css" type="text/css" rel="stylesheet" />
<openmrs:htmlInclude file="/openmrs.js" />

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
	Encounter: <b>${model.encounter.encounterType.name}<b>
		on <b>${model.encounter.encounterDatetime}</b>
		at <b>${model.encounter.location}</b>
	<br/>
	Form: <b>${model.form.name}</b>
	<br/>
	<c:forEach var="pn" items="${model.pageNumbers}">
		<a href="javascript:showPage(${pn})">${pn}</a>
	</c:forEach>
</div>

<c:forEach var="pageNumber" items="${model.pageNumbers}">

	<div id="page_${pageNumber}">
		<h4><u>Page ${pageNumber}</u></h4>

		<table class="encounterFormTable">
		<c:forEach var="fieldHolder" items="${model.data}">
			<c:if test="${fieldHolder.label.pageNumber == pageNumber && (model.showBlankFields || not empty fieldHolder.observations)}">
				<tr valign="top">
					<th>${fieldHolder.label}</th>
					<td>
						<table>
						<c:forEach var="obsEntry" items="${fieldHolder.observations}">
							<tr>
								<td><small><openmrs_tag:concept conceptId="${obsEntry.key.conceptId}"/>:</small></td>
								<td>
									<c:forEach var="obs" items="${obsEntry.value}">
										<b>${obs.valueAsString[model.locale]}</b>
										<c:if test="${not empty obs.obsDatetime && obs.obsDatetime != model.encounter.encounterDatetime}">
											<small>
												<spring:message code="general.onDate"/>
												<openmrs:formatDate date="${obs.obsDatetime}"/>
											</small>
										</c:if>
										<br/>
									</c:forEach>
								</td>
							</tr>
						</c:forEach>
						</table>
						<c:if test="${not empty fieldHolder.obsGroups}">
							<table border="1">
								<tr>
									<c:forEach var="conc" items="${fieldHolder.obsGroupConcepts}">
										<td><small><openmrs_tag:concept conceptId="${conc.conceptId}"/></small></td>
									</c:forEach>
								</tr>
								<c:forEach var="groupEntry" items="${fieldHolder.obsGroups}">
								<tr>
									<c:forEach var="obsList" items="${groupEntry.value.observationsByConcepts}">
										<td>
										<c:forEach var="obs" items="${obsList}">
											<b>${obs.valueAsString[model.locale]}</b>
										</c:forEach>
										</td>
									</c:forEach>
								</tr>
								</c:forEach>
							</table>
						</c:if>
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