<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:htmlInclude
	file="/scripts/jquery/dataTables/css/dataTables.css" />
<openmrs:htmlInclude
	file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js" />

<openmrs:htmlInclude
	file="/scripts/jquery-ui/js/jquery-ui-1.7.2.custom.min.js" />
<link
	href="<openmrs:contextPath/>/scripts/jquery-ui/css/<spring:theme code='jqueryui.theme.name' />/jquery-ui.custom.css"
	type="text/css" rel="stylesheet" />

<style>
.dimmedDates {
	font-size: small;
	color: grey;
	white-space: nowrap;
}
tr.encounter-in-visit td {
	border-width: 0px;
	border-color: #1aac9b;
	border-style: solid;
	padding: 5px;
}

tr.encounter-in-visit td:first-child {
	border-left-width: 1px;
}

tr.encounter-in-visit td:last-child {
	border-right-width: 1px;
}

tr.top-encounter-in-visit td {
	border-top-width: 1px;
}

tr.top-encounter-in-visit td:first-child {
	border-left-width: 1px;
	border-top-left-radius: 10px;
}

tr.top-encounter-in-visit td:last-child {
	border-right-width: 1px;
	border-top-right-radius: 10px;
}

tr.bottom-encounter-in-visit td {
	border-bottom-width: 1px;
}

tr.bottom-encounter-in-visit td:first-child {
	border-left-width: 1px;
	border-bottom-left-radius: 10px;
}

tr.bottom-encounter-in-visit td:last-child {
	border-right-width: 1px;
	border-bottom-right-radius: 10px;
}
</style>

<script type="text/javascript">
	$j(document)
			.ready(
					function() {
						$j('#patientVisitsTable')
								.dataTable(
										{
											"bProcessing" : true,
											"bServerSide" : true,
											"sAjaxSource" : "${pageContext.request.contextPath}/admin/visits/datatable.list?patient=<c:out value="${model.patient.patientId}" />",
											"bLengthChange" : false,
											"oLanguage": {
												"sInfo": ""//hack to hide the text but keep the element to maintain the UI
											},
											"aoColumns" : [ {
												"bVisible" : false
											}, {
												"bVisible" : false
											}, null, {
												"bVisible" : false
											}, {
												"bVisible" : false
											}, {
												"bVisible" : false
											}, {
												"bVisible" : false
											}, {
												"bVisible" : false
											}, {
												"bVisible" : false
											}, null, null, null, null, null, null, {
												"bVisible" : false
											} ],
											"fnRowCallback" : function(nRow,
													aData, iDisplayIndex) {
												var encounterId = aData[9];
												if (encounterId != '') {
													//Changes the action column
													var actions = '';
													var encounterURL = aData[15];

													var viewImg = '<img src="${pageContext.request.contextPath}/images/file.gif" title="<openmrs:message code="general.view"/>" />';
													actions = actions
															+ ' <a href="'
															+ encounterURL
															+ '">'
															+ viewImg + '</a>';

													$j('td:eq(1)', nRow).html(
															actions);
												} else {
													$j('td:eq(2)', nRow)
															.html(
																	'<i><openmrs:message code="Encounter.noEncounters" /></i>');
												}
												return nRow;
											},
											"fnDrawCallback" : function(
													oSettings) {
												if (oSettings.aiDisplay.length == 0) {
													return;
												}

												sInfoElement = document.getElementById('patientVisitsTable_info');
												pageCount = Math.ceil(oSettings._iRecordsTotal/oSettings._iDisplayLength);
												currentPage = Math.ceil(oSettings._iDisplayStart/oSettings._iDisplayLength) + 1;
												pageInfoElement = document.getElementById('pageInfo');
												if(!pageInfoElement){
													pageInfoElement = document.createElement('div');
													pageInfoElement.id = 'pageInfo';
													$j(pageInfoElement).css('float','left');
													//vertically position the element just like 'sInfo'
													$j(pageInfoElement).css('padding-top', $j(sInfoElement).css('padding-top'));
												}
												
												pageInfoElement.innerHTML = '<b><openmrs:message code="patientDashboard.visits.showing.pages"/>'.
													replace('{0}', currentPage).replace('{1}', pageCount)+'</b>';
												sInfoElement.parentNode.insertBefore(pageInfoElement, sInfoElement);

												var trs = $j('#patientVisitsTable tbody tr');
												var colspan = trs[0]
														.getElementsByTagName('td').length;
												var lastVisitId = "";
												for ( var i = 0; i < trs.length; i++) {
													//Groups visits

													var iDisplayIndex = i;
													var aoData = oSettings.aoData[oSettings.aiDisplay[iDisplayIndex]];
													var visitId = aoData._aData[0];
													var first = aoData._aData[7];
													var last = aoData._aData[8];

													if (visitId != '') {
														$j(trs[i])
																.addClass(
																		'encounter-in-visit');
													}

													if (first == 'true') {
														$j(trs[i])
																.addClass(
																		'top-encounter-in-visit');
													}
													if (last == 'true') {
														$j(trs[i])
																.addClass(
																		'bottom-encounter-in-visit');

														//Adds a whitespace between groups
														var trGroup = document
																.createElement('tr');
														var tdGroup = document
																.createElement('td');
														tdGroup.colSpan = colspan;
														trGroup
																.appendChild(tdGroup);
														trs[i].parentNode
																.insertBefore(
																		trGroup,
																		trs[i + 1]);
													}

													if (visitId != lastVisitId) {
														//Adds info about a visit to the first displayed row
														if (visitId != '') {
															var active = aoData._aData[1];
															var type = visitsPortletEscapeHtml(aoData._aData[2]);
															var location = visitsPortletEscapeHtml(aoData._aData[3]);
															var from = aoData._aData[4];
															var to = aoData._aData[5];
															var indication = visitsPortletEscapeHtml(aoData._aData[6]);

															visit = '<a href="${pageContext.request.contextPath}/admin/visits/visit.form?visitId='
																	+ visitId
																	+ '&patientId=<c:out value="${model.patient.patientId}" />">'
																	+ type
																	+ '</a><br/><span class="dimmedDates"><openmrs:message javaScriptEscape="true" code="general.fromDate" /> '
																	+ from;
															if (to != '') {
																visit = visit + ' <openmrs:message javaScriptEscape="true" code="general.toDate" /> ' + to;
															}
															visit = visit + '</span>';
															$j('td:eq(0)',
																	trs[i])
																	.html(visit);
														}

														lastVisitId = visitId;
													}else if (visitId == lastVisitId){
														$j('td:eq(0)', trs[i]).html('');
													}
													
													if (visitId == '') {
														$j('td:eq(0)', trs[i]).html('<openmrs:message code="general.none" />');
													}
												}
											}
										});
					});

	function visitsPortletEscapeHtml(text) {
		return $j('<div/>').text(text).html();
	}
</script>

<div id="portlet${model.portletUUID}">

	<div id="visitPortlet">
		<c:if test="${ not empty model.showDisclaimer and model.showDisclaimer }">
			<span class="error">
				<openmrs:message code="EncounterType.privilege.disclaimer"/>
			</span>
			<br/><br/>
		</c:if>
		<openmrs:hasPrivilege privilege="View Visits, View Encounters">
			<openmrs:hasPrivilege privilege="Add Visits">
				&nbsp;<a
					href="<openmrs:contextPath />/admin/visits/visit.form?patientId=<c:out value="${model.patient.patientId}" />"><openmrs:message
						code="Visit.add" /></a>
				<br />
				<br />
			</openmrs:hasPrivilege>
			<div>
				<table id="patientVisitsTable" style="border-spacing: 0px;">
					<thead>
						<tr>
							<th class="visitIdHeader"></th>
							<th class="visitActiveHeader"></th>
							<th class="visitTypeHeader"><openmrs:message code="Visit" /></th>
							<th class="visitLocationHeader"></th>
							<th class="visitFromHeader"></th>
							<th class="visitToHeader"></th>
							<th class="visitIndicationHeader"></th>
							<th class="encounterFirstHeader"></th>
							<th class="encounterLastHeader"></th>
							<th class="encounterIdHeader"><openmrs:message
									code="general.view" /></th>
							<th class="encounterDateHeader"><openmrs:message
									code="Encounter.datetime" /></th>
							<th class="encounterTypeHeader"><openmrs:message
									code="Encounter.type" /></th>
							<th class="encounterProvidersHeader"><openmrs:message
									code="Encounter.providers" /></th>
							<th class="encounterLocationHeader"><openmrs:message
									code="Encounter.location" /></th>
							<th class="encounterEntererHeader"><openmrs:message
									code="Encounter.enterer" /></th>
							<th class="encounterViewURLHeader"></th>
						</tr>
					</thead>
					<tbody>
					</tbody>
				</table>
			</div>
		</openmrs:hasPrivilege>

	</div>
</div>