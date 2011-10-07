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

<div id="visitsPortletdisplayEncounterPopup">
	<div id="visitsPortletdisplayEncounterPopupLoading"><spring:message code="general.loading"/></div>
	<iframe id="visitsPortletdisplayEncounterPopupIframe" width="100%" height="100%" marginWidth="0" marginHeight="0" frameBorder="0" scrolling="auto"></iframe>
</div>

<script type="text/javascript">
	$j(document).ready(function() {
		$j('#visitsPortletdisplayEncounterPopup').dialog({
				title: 'dynamic',
				autoOpen: false,
				draggable: false,
				resizable: false,
				width: '95%',
				modal: true,
				open: function(a, b) { $j('#visitsPortletdisplayEncounterPopupLoading').show(); }
		});
		$j("#visitsPortletdisplayEncounterPopupIframe").load(function() { $j('#visitsPortletdisplayEncounterPopupLoading').hide(); });
	});

	function visitsPortletLoadUrlIntoEncounterPopup(title, urlToLoad) {
		$j("#visitsPortletdisplayEncounterPopupIframe").attr("src", urlToLoad);
		$j('#visitsPortletdisplayEncounterPopup')
			.dialog('option', 'title', title)
			.dialog('option', 'height', $j(window).height() - 50) 
			.dialog('open');
	}
</script>

<script type="text/javascript">
	var $j = jQuery.noConflict();
	
	var popupHoverBox;
	
	var previousVisitId;

	$j(document).ready(function() {
		$j('#patientVisitsTable').dataTable({
			"bProcessing" : true,
			"bServerSide" : true,
			"sAjaxSource" : "${pageContext.request.contextPath}/admin/visits/datatable.list?patient=${model.patient.patientId}",
			"bLengthChange": false,
			"aoColumns": [ 
				{ "bVisible": false }, { "bVisible": false }, { "bVisible": false }, { "bVisible": false },
				{ "bVisible": false }, { "bVisible": false }, { "bVisible": false },
				null, null, null, null, null, null, { "bVisible": false }, { "bVisible": false }
			],
			"fnRowCallback": function( nRow, aData, iDisplayIndex ) {
				var encounterId = aData[7];
				if (encounterId != '') {
					var actions = '';
					var img;
					var url;
					<openmrs:hasPrivilege privilege="Edit Encounters">
						img = '<img src="${pageContext.request.contextPath}/images/edit.gif" title="<spring:message code="general.edit"/>" />';
						actions = actions + ' <a href="'+ aData[14] + '">' + img + '</a>';
					</openmrs:hasPrivilege>
					<openmrs:hasPrivilege privilege="View Encounters">
						url =  $j('<div/>').text(aData[13] + "&inPopup=true").html();
						var method = "visitsPortletLoadUrlIntoEncounterPopup('Encounter Preview', '" + url + "')";
						img = '<img src="${pageContext.request.contextPath}/images/file.gif" title="<spring:message code="general.view"/>" />';
						actions = actions + ' <a href="javascript:;" onclick="' + method + '">' + img + '</a>';
					</openmrs:hasPrivilege>
					$j('td:eq(0)', nRow).html(actions);
				}
				return nRow;
			},
			"fnDrawCallback": function ( oSettings ) {
				if ( oSettings.aiDisplay.length == 0 ) {
					return;
				}
				
				var nTrs = $j('#patientVisitsTable tbody tr');
				var iColspan = nTrs[0].getElementsByTagName('td').length;
				var iLastVisitId = "";
				for ( var i=0 ; i<nTrs.length ; i++ ) {
					var iDisplayIndex = i;
					var aoData = oSettings.aoData[ oSettings.aiDisplay[iDisplayIndex] ];
					var iVisidId = aoData._aData[0];
					if ( iVisidId != iLastVisitId ) {
						var nGroup = document.createElement( 'tr' );
						var nCell = document.createElement( 'td' );
						
						var active = aoData._aData[1];
						var type = $j('<div/>').text(aoData._aData[2]).html();
						var location = $j('<div/>').text(aoData._aData[3]).html();
						var from = $j('<div/>').text(aoData._aData[4]).html();
						var to = $j('<div/>').text(aoData._aData[5]).html();
						var indication = $j('<div/>').text(aoData._aData[6]).html();
						var encounterId = aoData._aData[7];

						if (active == 'true') {
							nCell.className = "tableGroup highlighted";
						} else {
							nCell.className = "tableGroup";
						}
						
						if (iVisidId != '') {
							var img = ' <img src="${pageContext.request.contextPath}/images/edit.gif" title="<spring:message code="general.edit"/>" />';
							var editLink = '<a href="${pageContext.request.contextPath}/admin/visits/visit.form?visitId=' + iVisidId + '&patientId=${model.patient.patientId}">' + img + '</a>';
							nCell.innerHTML = editLink;
						}
						nGroup.appendChild( nCell );
						
						nCell = document.createElement('td');
						nCell.colSpan = iColspan - 1;
						
						if (active == 'true') {
							nCell.className = "tableGroup highlighted";
						} else {
							nCell.className = "tableGroup";
						}
						if (iVisidId != '') {
							var sGroup = '';
							if (active == 'true') {
								sGroup = sGroup + '<spring:message code="Visit.active.label" />:';
							} else {
								sGroup = sGroup + '<spring:message code="Visit" />:';
							}
							sGroup = sGroup + " <strong>" + type + "</strong>";
							if (indication != '') {
								sGroup = sGroup + " <spring:message code="general.with" /> <strong>" + indication + "</strong> <spring:message code="Visit.indication" /> ";
							}
							sGroup = sGroup + " <spring:message code="general.at" />  " + location + " <spring:message code="general.startedAt" /> " + from;
							if (to != '') {
								sGroup = sGroup + " <spring:message code="general.completedAt" /> " + to;
							}
							nCell.innerHTML = sGroup;
						} else {
							nCell.innerHTML = "<spring:message code="Visit.noVisitAssigned" /> ";
						}
						nGroup.appendChild( nCell );
						nTrs[i].parentNode.insertBefore( nGroup, nTrs[i] );
						
						if (encounterId == '') {
							nGroup = document.createElement('tr');
							nCell = document.createElement('td');
							
							nCell.colSpan = iColspan;
							nCell.innerHTML = "<spring:message code="Encounter.noEncounters" />";
							nGroup.appendChild(nCell);
							nTrs[i+1].parentNode.insertBefore(nGroup, nTrs[i+1]);
						}
						
						iLastVisitId = iVisidId;
					}
				}
			}
		});
	});
</script>

<div id="portlet${model.portletUUID}">

	<div id="visitPortlet">
		<openmrs:hasPrivilege privilege="View Visits, View Encounters">
			<div id="visits">
				<div class="boxHeader${model.patientVariation}">
					<c:choose>
						<c:when test="${empty model.title}">
							<spring:message code="Visit.header" />
						</c:when>
						<c:otherwise>
							<spring:message code="${model.title}" />
						</c:otherwise>
					</c:choose>
				</div>
				<div class="box${model.patientVariation}">
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
									<th class="visitTypeHeader"></th>
									<th class="visitLocationHeader"></th>
									<th class="visitFromHeader"></th>
									<th class="visitToHeader"></th>
									<th class="visitIndicationHeader"></th>
									<th class="encounterIdHeader">Actions</th>
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
									<th class="encounterEditURLHeader"></th>
								</tr>
							</thead>
							<tbody>
							</tbody>
						</table>
					</div>
				</div>
			</div>
		</openmrs:hasPrivilege>

	</div>
</div>