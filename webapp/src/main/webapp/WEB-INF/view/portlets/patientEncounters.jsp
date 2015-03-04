<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:htmlInclude file="/scripts/easyAjax.js" />

<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables.css" />
<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js" />

<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui-1.7.2.custom.min.js" />
<link href="<openmrs:contextPath/>/scripts/jquery-ui/css/<spring:theme code='jqueryui.theme.name' />/jquery-ui.custom.css" type="text/css" rel="stylesheet" />

<openmrs:globalProperty key="dashboard.encounters.showViewLink" var="showViewLink" defaultValue="true"/>
<openmrs:globalProperty key="dashboard.encounters.showEditLink" var="showEditLink" defaultValue="true"/>

<div id="displayEncounterPopup">
	<div id="displayEncounterPopupLoading"><openmrs:message code="general.loading"/></div>
	<iframe id="displayEncounterPopupIframe" width="100%" height="100%" marginWidth="0" marginHeight="0" frameBorder="0" scrolling="auto"></iframe>
</div>

<script type="text/javascript">
	$j(document).ready(function() {
		$j('#displayEncounterPopup').dialog({
				title: 'dynamic',
				autoOpen: false,
				draggable: false,
				resizable: false,
				width: '95%',
				modal: true,
				open: function(a, b) { $j('#displayEncounterPopupLoading').show(); }
		});
	});

	function loadUrlIntoEncounterPopup(title, urlToLoad) {
		$j("#displayEncounterPopupIframe").attr("src", urlToLoad);
		$j('#displayEncounterPopup')
			.dialog('option', 'title', title)
			.dialog('option', 'height', $j(window).height() - 50) 
			.dialog('open');
	}
</script>

<c:if test="${model.showPagination == 'true'}">
<script type="text/javascript">
	$j(document).ready(function() {
		$j('#portlet${model.portletUUID} #patientEncountersTable').dataTable({
			"sPaginationType": "two_button",
			"bAutoWidth": false,
			"bFilter": false,
			"aaSorting": [[3,'asc']], // initial sorting uses the samer order given by ForEachEncounter (Encounter.datetime by default)
			"iDisplayLength": 20,
			"aoColumns": [
				{ "bVisible": false, "sType": "numeric" },
				{ "bVisible": ${showViewLink}, "iDataSort": 0 }, // sort this column by using the first invisible column for encounterIds,
            	{ "iDataSort": 3 }, // sort the date in this column by using the next invisible column for time in milliseconds
            	{ "bVisible": false, "sType": "numeric" },
            	null,
            	null,
            	null,
            	null,
            	null,
            	null
        	],
			"oLanguage": {
					"sLengthMenu": 'Show <select><option value="20">20</option><option value="50">50</option><option value="100">100</option></select> entries',
					"sZeroRecords": '<openmrs:message code="Encounter.no.previous"/>'
			}
		} );
		$j("#displayEncounterPopupIframe").load(function() { $j('#displayEncounterPopupLoading').hide(); });
	} );
</script>
</c:if>

<%--
Parameters
	model.num == \d  limits the number of encounters shown to the value given
	model.showPagination == 'true' lists off the encounters in a paginated table
	model.hideHeader == 'true' hides the 'All Encounter' header above the table listing
	model.hideFormEntry == 'true' does not show the "Enter Forms" popup no matter what the gp has
	model.formEntryReturnUrl == what URL to return to when a form has been cancelled or successfully filled out
--%>

<div id="portlet${model.portletUUID}">
<div id="encounterPortlet">
	<c:if test="${ not empty model.showDisclaimer and model.showDisclaimer }">
		<span class="error">
			<openmrs:message code="EncounterType.privilege.disclaimer"/>
		</span>
		<br/><br/>
	</c:if>
	
	<openmrs:globalProperty var="enableFormEntryInEncounters" key="FormEntry.enableOnEncounterTab" defaultValue="false"/>

	<c:if test="${enableFormEntryInEncounters && !model.hideFormEntry}">
		<openmrs:hasPrivilege privilege="Form Entry">
			<div id="formEntryDialog">
				<openmrs:portlet url="personFormEntry" personId="${patient.personId}" id="encounterTabFormEntryPopup" parameters="showDefinedNumberOfEncounters=false|returnUrl=${model.formEntryReturnUrl}"/>
			</div>

			<button class="showFormEntryDialog" style="margin-left: 2em; margin-bottom: 0.5em"><openmrs:message code="FormEntry.fillOutForm"/></button>
			
			<script type="text/javascript">
				$j(document).ready(function() {
					$j("#formEntryDialog").dialog({
						title: '<openmrs:message code="FormEntry.fillOutForm" javaScriptEscape="true"/>',
						autoOpen: false,
						draggable: false,
						resizable: false,
						width: '90%',
						modal: true
					});
					$j('button.showFormEntryDialog').click(function() {
						$j('#formEntryDialog').dialog('open');
					});
				});
			</script>

		</openmrs:hasPrivilege>
	</c:if>

	<openmrs:hasPrivilege privilege="View Encounters">
		<div id="encounters">
			<div class="boxHeader${model.patientVariation}"><c:choose><c:when test="${empty model.title}"><openmrs:message code="Encounter.header"/></c:when><c:otherwise><openmrs:message code="${model.title}" arguments="${model.num}"/></c:otherwise></c:choose></div>
			<div class="box${model.patientVariation}">
				<div>
					<table cellspacing="0" cellpadding="2" id="patientEncountersTable">
						<thead>
							<tr>
								<th class="hidden"> hidden Encounter id </th>
								<th class="encounterView" align="center"><c:if test="${showViewLink == 'true'}">
								 	<openmrs:message code="general.view"/>
								</c:if></th>
								<th class="encounterDatetimeHeader"> <openmrs:message code="Encounter.datetime"/> </th>
								<th class="hidden"> hidden Sorting Order (by Encounter.datetime) </th>
								<th class="encounterTypeHeader"> <openmrs:message code="Encounter.type"/>     </th>
								<th class="encounterVisitHeader"><openmrs:message code="Encounter.visit"/></th>
								<th class="encounterProviderHeader"> <openmrs:message code="Encounter.provider"/> </th>
								<th class="encounterFormHeader"> <openmrs:message code="Encounter.form"/>     </th>
								<th class="encounterLocationHeader"> <openmrs:message code="Encounter.location"/> </th>
								<th class="encounterEntererHeader"> <openmrs:message code="Encounter.enterer"/>  </th>
							</tr>
						</thead>
						<tbody>
							<%-- WARNING: if sortBy="encounterDatetime" is changed, update the hidden Sorting Order column, in order to sort the encounterDatetime column too --%>
							<openmrs:forEachEncounter encounters="${model.patientEncounters}" sortBy="encounterDatetime" descending="true" var="enc" num="${model.num}">
								<tr class='${status.index % 2 == 0 ? "evenRow" : "oddRow"}'>
									<td class="hidden">
										<%--  this column contains the encounter id and will be used for sorting in the dataTable's encounter edit column --%>
										${enc.encounterId}
									</td>
									<td class="encounterView" align="center">
										<c:if test="${showViewLink}">
											<c:set var="viewEncounterUrl" value="${pageContext.request.contextPath}/admin/encounters/encounter.form?encounterId=${enc.encounterId}"/>
											<c:choose>
												<c:when test="${ model.formToViewUrlMap[enc.form] != null }">
													<c:url var="viewEncounterUrl" value="${model.formToViewUrlMap[enc.form]}">
														<c:param name="encounterId" value="${enc.encounterId}"/>
													</c:url>
												</c:when>
												<c:when test="${ model.formToEditUrlMap[enc.form] != null }">
													<c:url var="viewEncounterUrl" value="${model.formToEditUrlMap[enc.form]}">
														<c:param name="encounterId" value="${enc.encounterId}"/>
													</c:url>
												</c:when>
											</c:choose>
											<a href="${viewEncounterUrl}">
												<img src="${pageContext.request.contextPath}/images/file.gif" title="<openmrs:message code="general.view"/>" border="0" />
											</a>
										</c:if>
									</td>
									<td class="encounterDatetime">
										<openmrs:formatDate date="${enc.encounterDatetime}" type="small" />
									</td>
									<td class="hidden">
									<%--  this column contains the sorting order provided by ForEachEncounterTag (by encounterDatetime) --%>
									<%--  and will be used for the initial sorting and sorting in the dataTable's encounterDatetime column --%>
										${count}
									</td>
					 				<td class="encounterType"><openmrs:format encounterType="${enc.encounterType}"/></td>
					 				<td class="encounterVisit">
					 					<c:if test="${enc.visit != null}"><openmrs:format visitType="${enc.visit.visitType}"/></c:if>
					 				</td>
					 				<td class="encounterProvider"><openmrs:format encounterProviders="${enc.providersByRoles}"/></td>
					 				<td class="encounterForm">${enc.form.name}</td>
					 				<td class="encounterLocation"><openmrs:format location="${enc.location}"/></td>
					 				<td class="encounterEnterer"><c:out value="${enc.creator.personName}" /></td>
								</tr>
							</openmrs:forEachEncounter>
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
	
	<openmrs:htmlInclude file="/dwr/interface/DWRObsService.js" />
	<openmrs:htmlInclude file="/dwr/interface/DWRPatientService.js" />
	<openmrs:htmlInclude file="/dwr/engine.js" />
	<openmrs:htmlInclude file="/dwr/util.js" />
	<script type="text/javascript">
		<!-- // begin

		function handleGetObservations(encounterId) { 
			loadUrlIntoEncounterPopup('Test title', '${pageContext.request.contextPath}/admin/encounters/encounterDisplay.list?encounterId=' + encounterId);
		}

		function handleRefreshTable(id, data, func) {
			dwr.util.removeAllRows(id);
			dwr.util.addRows(id, data, func, {
				cellCreator:function(options) {
				    var td = document.createElement("td");
				    return td;
				},
				escapeHtml:false
			});
		}

		function showHideDiv(divId) {
			var div = document.getElementById(divId);
			if ( div ) {
				if (div.style.display != "") { 
					div.style.display = "";
				} else { 
					div.style.display = "none";
				}				
			}
		}
		
		function handleAddObs(encounterField, conceptField, valueTextField, obsDateField) {
			var encounterId = dwr.util.getValue($(encounterField));
			var conceptId = dwr.util.getValue($(conceptField));
			var valueText = dwr.util.getValue($(valueTextField));
			var obsDate = dwr.util.getValue($(obsDateField));
			var patientId = <c:out value="${model.patient.patientId}" />;
			DWRObsService.createObs(patientId, encounterId, conceptId, valueText, obsDate);
			handleGetObservations(encounterId);
		}

		// end -->

	</script>
</div>
</div>
