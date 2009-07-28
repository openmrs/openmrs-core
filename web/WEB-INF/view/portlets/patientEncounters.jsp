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

<%--
Parameters
	model.num == \d  limits the number of encounters shown to the value given
	model.hideHeader == 'true' hides the 'All Encounter' header above the table listing
	model.hideFormEntry == 'true' does not show the "Enter Forms" popup no matter what the gp has
	model.formEntryReturnUrl == what URL to return to when a form has been cancelled or successfully filled out
--%>

<div id="encounterPortlet">

	<openmrs:globalProperty var="enableFormEntryInEncounters" key="FormEntry.enableOnEncounterTab" defaultValue="false"/>
	
	<div id="displayEncounterPopup"> 
 		        <div id="displayEncounterPopupLoading"><spring:message code="general.loading"/></div> 
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
				$j("#displayEncounterPopupIframe").load(function() { $j('#displayEncounterPopupLoading').hide(); });
			}); 
 		 
			function loadUrlIntoEncounterPopup(title, urlToLoad) { 
				$j("#displayEncounterPopupIframe").attr("src", urlToLoad); 
				$j('#displayEncounterPopup') 
					.dialog('option', 'title', title) 
					.dialog('option', 'height', $j(window).height() - 50)  
					.dialog('open'); 
			}
 	</script>

	<c:if test="${enableFormEntryInEncounters && !model.hideFormEntry}">
		<openmrs:hasPrivilege privilege="Form Entry">
			<div id="formEntryDialog">
				<openmrs:portlet url="personFormEntry" personId="${patient.personId}" id="encounterTabFormEntryPopup" parameters="showLastThreeEncounters=false|returnUrl=${model.formEntryReturnUrl}"/>
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
					})
					$j("#displayEncounterPopupIframe").load(function() { $j('#displayEncounterPopupLoading').hide(); });
				});

				$j('button.showFormEntryDialog').click(function() {
					$j('#formEntryDialog').dialog('open');
				});
			</script>

 		</openmrs:hasPrivilege>
 	</c:if>
	
	<openmrs:hasPrivilege privilege="View Encounters">
		<openmrs:globalProperty key="dashboard.encounters.showViewLink" var="showViewLink" defaultValue="false"/>
		<openmrs:globalProperty key="dashboard.encounters.showEditLink" var="showEditLink" defaultValue="false"/>
		<div id="encounters">
			<div class="boxHeader${model.patientVariation}"><c:choose><c:when test="${empty model.title}"><spring:message code="Encounter.header"/></c:when><c:otherwise><spring:message code="${model.title}"/></c:otherwise></c:choose></div>
			<div class="box${model.patientVariation}">
				<table cellspacing="0" cellpadding="2" class="patientEncounters">
					<c:if test="${fn:length(model.patientEncounters) > 0}">
						<c:if test="${empty model.hideHeader}">
							<tr>
								<th colspan="8" class="tableTitle"><spring:message code="Portlet.patientEncounter.allEncounter"/></th>
							</tr>
						</c:if>
						<tr>
							<th class="encounterEdit" align="center"><c:if test="${showEditLink == 'true'}">
								<spring:message code="general.edit"/>
							</c:if></th>
							<th class="encounterView" align="center"><c:if test="${showViewLink == 'true'}">
								 <spring:message code="general.view"/>
							</c:if></th>
							<th class="encounterDatetimeHeader"> <spring:message code="Encounter.datetime"/> </th>
							<th class="encounterTypeHeader"> <spring:message code="Encounter.type"/>     </th>
							<th class="encounterProviderHeader"> <spring:message code="Encounter.provider"/> </th>
							<th class="encounterFormHeader"> <spring:message code="Encounter.form"/>     </th>
							<th class="encounterLocationHeader"> <spring:message code="Encounter.location"/> </th>
							<th class="encounterEntererHeader"> <spring:message code="Encounter.enterer"/>  </th>
						</tr>
						<openmrs:forEachEncounter encounters="${model.patientEncounters}" sortBy="encounterDatetime" descending="true" var="enc" num="${model.num}">
							<tr class="<c:choose><c:when test="${count % 2 == 0}">evenRow</c:when><c:otherwise>oddRow</c:otherwise></c:choose>">
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
										<c:set var="viewEncounterUrl" value="${pageContext.request.contextPath}/admin/encounters/encounterDisplay.list?encounterId=${enc.encounterId}"/>
										<c:if test="${ model.formToViewUrlMap[enc.form] != null }">
											<c:url var="viewEncounterUrl" value="${model.formToViewUrlMap[enc.form]}">
												<c:param name="encounterId" value="${enc.encounterId}"/>
												<c:param name="inPopup" value="true"/>
											</c:url>
										</c:if>
										<a href="javascript:void(0)" onClick="loadUrlIntoEncounterPopup('<openmrs:format encounter="${enc}"/>', '${viewEncounterUrl}'); return false;">
											<img src="${pageContext.request.contextPath}/images/file.gif" title="<spring:message code="general.view"/>" border="0" align="top" />
										</a>
									</c:if>
								</td>
								<td class="encounterDatetime">
									<openmrs:formatDate date="${enc.encounterDatetime}" type="small" />
								</td>
							 	<td class="encounterType">${enc.encounterType.name}</td>
							 	<td class="encounterProvider">${enc.provider.personName}</td>
							 	<td class="encounterForm">${enc.form.name}</td>
							 	<td class="encounterLocation">${enc.location.name}</td>
							 	<td class="encounterEnterer">${enc.creator.personName}</td>
							</tr>
						</openmrs:forEachEncounter>
					</c:if>
					<c:if test="${fn:length(encounters) == 0}">
						<tr>
							<th colspan="6" class="tableTitle"><spring:message code="Encounter.no.previous"/></th>
						</tr>
					</c:if>	
				</table>
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
			--%>
			loadUrlIntoEncounterPopup('Test title', '${pageContext.request.contextPath}/admin/encounters/encounterDisplay.list?encounterId=' + encounterId);
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
