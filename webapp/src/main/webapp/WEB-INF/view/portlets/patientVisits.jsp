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

<div id="visitsPortletVisitInfoPopup">
	<b><span id="visitsPortletType"></span></b><br /> @ <span
		id="visitsPortletLocation"></span><br />
	<spring:message code="Visit.from" />
	: <span id="visitsPortletFrom"></span><br />
	<spring:message code="Visit.to" />
	: <span id="visitsPortletTo"></span><br />
	<spring:message code="Visit.indication" />
	: <span id="visitsPortletIndication"></span>
</div>

<style>
#visitsPortletVisitInfoPopup {
	border: 1px solid #1aac9b;
	background: #ffffff;
	padding: 5px 5px 5px 5px;
	display: none;
	position: absolute;
}
</style>

<script type="text/javascript">
	function visitsPortletShowVisitInfoPopup(href, active, type, location,
			from, to, indication) {
		var popup = $j('#visitsPortletVisitInfoPopup');
		if (active == 'true') {
			$j('#visitsPortletType', popup).html(
					'<spring:message code="Visit.active.label" />: ' + type);
		} else {
			$j('#visitsPortletType', popup).html(
					'<spring:message code="Visit" />: ' + type);
		}
		$j('#visitsPortletLocation', popup).html(location);
		$j('#visitsPortletFrom', popup).html(from);
		$j('#visitsPortletTo', popup).html(to);
		$j('#visitsPortletIndication', popup).html(indication);

		$j(popup).show();

		$j(href).mousemove(function(e) {
			$j('#visitsPortletVisitInfoPopup').css({
				"left" : e.pageX + 10,
				"top" : e.pageY + 20
			});
		});

		$j(href).mouseleave(function() {
			$j('#visitsPortletVisitInfoPopup').hide();
		});
	}
</script>

<style>
div.visit {
	border: 1px solid #1aac9b;
	border-radius: 10px;
	padding: 5px;
	background-color: #e0e0f0;
	color: #000000;
	white-space: nowrap;
}

a.visitLink {
	text-decoration: none;
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
											"sAjaxSource" : "${pageContext.request.contextPath}/admin/visits/datatable.list?patient=${model.patient.patientId}",
											"bLengthChange" : false,
											iDisplayLength:2,
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

													var viewImg = '<img src="${pageContext.request.contextPath}/images/file.gif" title="<spring:message code="general.view"/>" />';
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
																	'<i><spring:message code="Encounter.noEncounters" /></i>');
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
												
												pageInfoElement.innerHTML = '<b><spring:message code="patientDashboard.visits.showing.pages"/>'.
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

															var method = "visitsPortletShowVisitInfoPopup(this, '"
																	+ active
																	+ "', '"
																	+ type
																	+ "', '"
																	+ location
																	+ "', '"
																	+ from
																	+ "', '"
																	+ to
																	+ "', '"
																	+ indication
																	+ "')";
															var editImg = ' <img src="${pageContext.request.contextPath}/images/info.gif" />';
															var visit = type
																	+ editImg;
															visit = '<div class="visit">'
																	+ visit
																	+ ' </div>';
															visit = '<a href="${pageContext.request.contextPath}/admin/visits/visit.form?visitId='
																	+ visitId
																	+ '&patientId=${model.patient.patientId}" onmouseover="'
																	+ method
																	+ '" class="visitLink">'
																	+ visit
																	+ '</a>';
															$j('td:eq(0)',
																	trs[i])
																	.html(visit);
														}

														lastVisitId = visitId;
													} else {
														$j('td:eq(0)', trs[i])
																.html('');
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
		<openmrs:hasPrivilege privilege="View Visits, View Encounters">
			<openmrs:hasPrivilege privilege="Add Visits">
				&nbsp;<a
					href="<openmrs:contextPath />/admin/visits/visit.form?patientId=${model.patient.patientId}"><spring:message
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
							<th class="visitTypeHeader"><spring:message code="Visit" /></th>
							<th class="visitLocationHeader"></th>
							<th class="visitFromHeader"></th>
							<th class="visitToHeader"></th>
							<th class="visitIndicationHeader"></th>
							<th class="encounterFirstHeader"></th>
							<th class="encounterLastHeader"></th>
							<th class="encounterIdHeader"><spring:message
									code="general.view" /></th>
							<th class="encounterDateHeader"><spring:message
									code="Encounter.datetime" /></th>
							<th class="encounterTypeHeader"><spring:message
									code="Encounter.type" /></th>
							<th class="encounterProvidersHeader"><spring:message
									code="Encounter.providers" /></th>
							<th class="encounterLocationHeader"><spring:message
									code="Encounter.location" /></th>
							<th class="encounterEntererHeader"><spring:message
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