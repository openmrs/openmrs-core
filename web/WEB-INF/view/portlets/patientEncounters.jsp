<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:htmlInclude file="/scripts/easyAjax.js" />

<openmrs:htmlInclude file="/scripts/jquery/jquery-1.3.2.min.js" />
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables.css" />
<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js" />

<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui-1.7.2.custom.min.js" />
<openmrs:htmlInclude file="/scripts/jquery-ui/css/redmond/jquery-ui-1.7.2.custom.css" />

<script type="text/javascript">
	var $j = jQuery.noConflict(); 
</script>

<openmrs:globalProperty key="dashboard.encounters.showViewLink" var="showViewLink" defaultValue="true"/>
<openmrs:globalProperty key="dashboard.encounters.showEditLink" var="showEditLink" defaultValue="true"/>
		

<c:if test="${model.showPagination == 'true'}">
<script type="text/javascript">
	$j(document).ready(function() {
		$j('#portlet${model.portletUUID} #patientEncountersTable').dataTable({
			"sPaginationType": "two_button",
			"bAutoWidth": false,
			"bFilter": false,
			"aaSorting": [[3,'desc']], // perform first pass sort on initialisation on encounter.encounterDatetime column
			"iDisplayLength": 20,
			"aoColumns": [
				{ "bVisible": false, "sType": "numeric" },
				{ "bVisible": ${showEditLink}, "iDataSort": 0 }, // sort this column by using the previous invisible column for encounterIds,
				{ "bVisible": ${showViewLink}, "iDataSort": 0 }, // sort this column by using the first invisible column for encounterIds,
            	{ "iDataSort": 3 }, // sort the date in this column by using the next invisible column for time in milliseconds
            	{ "bVisible": false, "sType": "numeric" },
            	null,
            	null,
            	null,
            	null,
            	null
        	],
			"oLanguage": {
					"sLengthMenu": 'Show <select><option value="20">20</option><option value="50">50</option><option value="100">100</option></select> entries',
					"sZeroRecords": '<spring:message code="Encounter.no.previous"/>'
			}
		} );
	} );
</script>
</c:if>

<%--
Parameters
	model.num == \d  limits the number of encounters shown to the value given
	model.showPagination == 'true' lists off the encounters in a paginated table
--%>

<div id="portlet${model.portletUUID}">
<div id="encounterPortlet">

	<openmrs:globalProperty var="viewEncounterWhere" key="dashboard.encounters.viewWhere" defaultValue="newWindow"/>
	<openmrs:globalProperty var="enableFormEntry" key="dashboard.encounters.enableFormEntry" defaultValue="true"/>

	<c:if test="${enableFormEntry}">
		<openmrs:hasPrivilege privilege="Form Entry">
			<div id="formEntryDialog" class="jqmWindow">
				<table id="formEntryTable">
					<thead>
						<tr>
							<th><spring:message code="general.name"/></th>
							<th><spring:message code="Form.version"/></th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="entry" items="${model.formToEntryUrlMap}">
							<openmrs:hasPrivilege privilege="${entry.value.requiredPrivilege}">
								<c:url var="formUrl" value="${entry.value.formEntryUrl}">
									<c:param name="personId" value="${model.personId}"/>
									<c:param name="patientId" value="${model.patientId}"/>
									<c:param name="formId" value="${entry.key.formId}"/>
								</c:url>
								<tr>
									<td>
										<a href="${formUrl}">${entry.key.name}</a>
									</td>
									<td>
										${entry.key.version}
										<c:if test="${!entry.key.published}"><i>(<spring:message code="Form.unpublished"/>)</i></c:if>
									</td>
								</tr>
							</openmrs:hasPrivilege>
						</c:forEach>
					</tbody>
				</table>			
			</div>

			<button class="showFormEntryDialog" style="margin-left: 2em; margin-bottom: 0.5em"><spring:message code="FormEntry.fillOutForm"/></button>
			
			<script type="text/javascript">
				$j(document).ready(function() {
					$j("#formEntryDialog").dialog({
						title: '<spring:message code="FormEntry.fillOutForm" javaScriptEscape="true"/>',
						autoOpen: false,
						draggable: false,
						resizable: false,
						width: '90%',
						modal: true
					});
					$j("#formEntryTable").dataTable({
						"bPaginate": false,
						"bSort": false,
						"bAutoWidth": false
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
			<div class="boxHeader${model.patientVariation}"><c:choose><c:when test="${empty model.title}"><spring:message code="Encounter.header"/></c:when><c:otherwise><spring:message code="${model.title}"/></c:otherwise></c:choose></div>
			<div class="box${model.patientVariation}">
				<div>
					<table cellspacing="0" cellpadding="2" id="patientEncountersTable">
						<thead>
							<tr>
								<th> hidden Encounter id </th>
								<th class="encounterEdit" align="center"><c:if test="${showEditLink == 'true'}">
									<spring:message code="general.edit"/>
								</c:if></th>
								<th class="encounterView" align="center"><c:if test="${showViewLink == 'true'}">
								 	<spring:message code="general.view"/>
								</c:if></th>
								<th class="encounterDatetimeHeader"> <spring:message code="Encounter.datetime"/> </th>
								<th> hidden Encounter.datetime </th>
								<th class="encounterTypeHeader"> <spring:message code="Encounter.type"/>     </th>
								<th class="encounterProviderHeader"> <spring:message code="Encounter.provider"/> </th>
								<th class="encounterFormHeader"> <spring:message code="Encounter.form"/>     </th>
								<th class="encounterLocationHeader"> <spring:message code="Encounter.location"/> </th>
								<th class="encounterEntererHeader"> <spring:message code="Encounter.enterer"/>  </th>
							</tr>
						</thead>
						<tbody>
							<openmrs:forEachEncounter encounters="${model.patientEncounters}" sortBy="encounterDatetime" descending="true" var="enc" num="${model.num}">
								<tr class="<c:choose><c:when test="${count % 2 == 0}">evenRow</c:when><c:otherwise>oddRow</c:otherwise></c:choose>">
									<td>
										<%--  this column contains the encounter id and will be used for sorting in the dataTable's encounter edit column --%>
										${enc.encounterId}
									</td>
									<td class="encounterEdit" align="center">
										<c:if test="${showEditLink == 'true'}">
											<openmrs:hasPrivilege privilege="Edit Encounters">
												<c:set var="editUrl" value="${pageContext.request.contextPath}/admin/encounters/encounter.form?encounterId=${enc.encounterId}"/>
												<c:if test="${ model.formToEditUrlMap[enc.form] != null }">
													<c:url var="editUrl" value="${model.formToEditUrlMap[enc.form]}">
														<c:param name="encounterId" value="${enc.encounterId}"/>
													</c:url>
												</c:if>
												<a href="${editUrl}">
													<img src="${pageContext.request.contextPath}/images/edit.gif" title="<spring:message code="general.edit"/>" border="0" align="top" />
												</a>
											</openmrs:hasPrivilege>
										</c:if>
									</td>
									<td class="encounterView" align="center">
										<c:if test="${showViewLink}">
											<c:choose>
												<c:when test="${ model.formToViewUrlMap[enc.form] != null }">
													<a href="${model.formToViewUrlMap[enc.form]}?encounterId=${enc.encounterId}">
														<img src="${pageContext.request.contextPath}/images/file.gif" title="<spring:message code="general.view"/>" border="0" align="top" />
													</a>
												</c:when>
												<c:otherwise>
													<c:if test="${fn:length(enc.allObs) > 0}">
														<a href="#encounterId=${enc.encounterId}" onClick="handleGetObservations('${enc.encounterId}'); return false;">
															<img src="${pageContext.request.contextPath}/images/file.gif" title="<spring:message code="general.view"/>" border="0" align="top" />
														</a>
													</c:if>
												</c:otherwise>
											</c:choose>
										</c:if>
									</td>
									<td class="encounterDatetime">
										<openmrs:formatDate date="${enc.encounterDatetime}" type="small" />
									</td>
									<td>
									<%--  this column contains milliseconds and will be used for sorting in the dataTable's encounterDatetime column --%>
										<openmrs:formatDate date="${enc.encounterDatetime}" type="milliseconds" />
									</td>
					 				<td class="encounterType">${enc.encounterType.name}</td>
					 				<td class="encounterProvider">${enc.provider.personName}</td>
					 				<td class="encounterForm">${enc.form.name}</td>
					 				<td class="encounterLocation">${enc.location.name}</td>
					 				<td class="encounterEnterer">${enc.creator.personName}</td>
								</tr>
							</openmrs:forEachEncounter>
						</tbody>
					</table>
				</div>
			</div>
		</div>
	</openmrs:hasPrivilege>
	
	<openmrs:htmlInclude file="/dwr/interface/DWRObsService.js" />
	<openmrs:htmlInclude file="/dwr/interface/DWRPatientService.js" />
	<openmrs:htmlInclude file="/dwr/engine.js" />
	<openmrs:htmlInclude file="/dwr/util.js" />
	<script type="text/javascript">
		<!-- // begin

		<%--
		var obsTableCellFunctions = [
			function(data) { return "" + data.encounter; },
			function(data) { return "" + data.conceptName; },
			function(data) { return "" + data.value; },
			function(data) { return "" + data.datetime; }
		];
		--%>


		function handleGetObservations(encounterId) { 
			<%--
			DWRObsService.getObservations(encounterId, handleRefreshObsData);
			document.getElementById("encounterId").value = encounterId;
			--%>
			<c:choose>
				<c:when test="${viewEncounterWhere == 'newWindow'}">
					var formWindow = window.open('${pageContext.request.contextPath}/admin/encounters/encounterDisplay.list?encounterId=' + encounterId, '${enc.encounterId}', 'toolbar=no,width=800,height=600,resizable=yes,scrollbars=yes');
					formWindow.focus();
				</c:when>
				<c:when test="${viewEncounterWhere == 'oneNewWindow'}">
					var formWindow = window.open('${pageContext.request.contextPath}/admin/encounters/encounterDisplay.list?encounterId=' + encounterId, 'formWindow', 'toolbar=no,width=800,height=600,resizable=yes,scrollbars=yes');
					formWindow.focus();
				</c:when>
				<c:otherwise>
					window.location = '${pageContext.request.contextPath}/admin/encounters/encounterDisplay.list?encounterId=' + encounterId;
				</c:otherwise>
			</c:choose>
		}

		<%--
		function handleRefreshObsData(data) {
  			handleRefreshTable('obsTable', data, obsTableCellFunctions);
		}
		--%>

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
			var patientId = ${model.patient.patientId};			
			//alert("Adding obs for encounter (" + encounterId + "): " + conceptId + " = " + valueText + " " + obsDate);  
			DWRObsService.createObs(patientId, encounterId, conceptId, valueText, obsDate);
			handleGetObservations(encounterId);
		}
			
	
		//refreshObsTable();

		// end -->
		
	</script>
</div>
</div>
