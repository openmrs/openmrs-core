<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:htmlInclude file="/scripts/easyAjax.js" />

<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables.css" />
<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js" />

<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui-1.7.2.custom.min.js" />
<link href="<openmrs:contextPath/>/scripts/jquery-ui/css/<spring:theme code='jqueryui.theme.name' />/jquery-ui.custom.css" type="text/css" rel="stylesheet" />

<openmrs:globalProperty key="dashboard.visits.showViewLink" var="showViewLink" defaultValue="true"/>
<openmrs:globalProperty key="dashboard.visits.showEditLink" var="showEditLink" defaultValue="true"/>

<div id="portlet${model.portletUUID}">
<div id="visitPortlet">

	<openmrs:hasPrivilege privilege="View Visits">
		<div id="visits">
			<div class="boxHeader${model.patientVariation}"><c:choose><c:when test="${empty model.title}"><spring:message code="Visit.header"/></c:when><c:otherwise><spring:message code="${model.title}"/></c:otherwise></c:choose></div>
			<div class="box${model.patientVariation}">
				<div>
					<table cellspacing="0" cellpadding="2" id="patientVisitsTable">
						<thead>
							<tr>
								<th class="hidden"> hidden Visit id </th>
								<th class="visitEdit" align="center"><c:if test="${showEditLink == 'true'}">
									<spring:message code="general.edit"/>
								</c:if></th>
								<th class="visitView" align="center"><c:if test="${showViewLink == 'true'}">
								 	<spring:message code="general.view"/>
								</c:if></th>
								<th class="startDatetimeHeader"> <spring:message code="Visit.datetime"/> </th>
								<th class="hidden"> hidden Visit.datetime </th>
								<th class="visitTypeHeader"> <spring:message code="Visit.type"/>     </th>
								<th class="visitLocationHeader"> <spring:message code="Visit.location"/> </th>
								<th class="visitEntererHeader"> <spring:message code="Visit.enterer"/>  </th>
							</tr>
						</thead>
						<tbody>
							<openmrs:forEachVisit visits="${model.patientVisits}" sortBy="startDatetime" descending="true" var="visit" num="${model.num}">
								<tr class='${status.index % 2 == 0 ? "evenRow" : "oddRow"}'>
									<td class="hidden">
										<%--  this column contains the visit id and will be used for sorting in the dataTable's visit edit column --%>
										${visit.visitId}
									</td>
									<td class="visitEdit" align="center">
										<c:if test="${showEditLink == 'true'}">
											<openmrs:hasPrivilege privilege="Edit Visits">
												<c:set var="editUrl" value="${pageContext.request.contextPath}/admin/visits/visit.form?visitId=${visit.visitId}"/>
												<c:if test="${ model.formToEditUrlMap[visit.form] != null }">
													<c:url var="editUrl" value="${model.formToEditUrlMap[visit.form]}">
														<c:param name="visitId" value="${visit.visitId}"/>
													</c:url>
												</c:if>
												<a href="${editUrl}">
													<img src="${pageContext.request.contextPath}/images/edit.gif" title="<spring:message code="general.edit"/>" border="0" />
												</a>
											</openmrs:hasPrivilege>
										</c:if>
									</td>
									<td class="visitView" align="center">
										<c:if test="${showViewLink}">
											<c:set var="viewVisitUrl" value="${pageContext.request.contextPath}/admin/visits/visitDisplay.list?visitId=${visit.visitId}"/>
											<c:if test="${ model.formToViewUrlMap[visit.form] != null }">
												<c:url var="viewVisitUrl" value="${model.formToViewUrlMap[visit.form]}">
													<c:param name="visitId" value="${visit.visitId}"/>
													<c:param name="inPopup" value="true"/>
												</c:url>
											</c:if>
											<a href="javascript:void(0)" onClick="loadUrlIntoVisitPopup('<openmrs:format visit="${visit}" javaScriptEscape="true"/>', '${viewVisitUrl}'); return false;">
												<img src="${pageContext.request.contextPath}/images/file.gif" title="<spring:message code="general.view"/>" border="0" />
											</a>
										</c:if>
									</td>
									<td class="startDatetime">
										<openmrs:formatDate date="${visit.startDatetime}" type="small" />
									</td>
									<td class="hidden">
									<%--  this column contains milliseconds and will be used for sorting in the dataTable's startDatetime column --%>
										<openmrs:formatDate date="${visit.startDatetime}" type="milliseconds" />
									</td>
					 				<td class="visitType"><openmrs:format visitType="${visit.visitType}"/></td>
					 				<td class="visitLocation"><openmrs:format location="${visit.location}"/></td>
					 				<td class="visitEnterer">${visit.creator.personName}</td>
								</tr>
							</openmrs:forEachVisit>
						</tbody>
					</table>
				</div>
			</div>
		</div>
		
		<c:if test="${model.showPagination != 'true'}">
			<script type="text/javascript">
				// hide the columns in the above table if datatable isn't doing it already 
				$j(".hidden").hide();
			</script>
		</c:if>
	</openmrs:hasPrivilege>
	
</div>
</div>