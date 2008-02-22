<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:htmlInclude file="/scripts/easyAjax.js" />

<%--
Parameters
	model.num == \d  limits the number of encounters shown to the value given
	model.hideHeader == 'true' hides the 'All Encounter' header above the table listing
--%>

<div id="encounterPortlet">

	<openmrs:globalProperty var="viewEncounterWhere" key="dashboard.encounters.viewWhere" defaultValue="newWindow"/>

	<%--
	<openmrs:hasPrivilege privilege="Add Forms">
		<div id="encounterForms">
			<div class="boxHeader">Forms</div>
			<div class="box">
				<openmrs:portlet url="patientFormsSelect" id="patientDashboardFormsSelect" patientId="${patient.patientId}"/>
				<br/>
			</div>
		</div>
	</openmrs:hasPrivilege>
	--%>
	
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
								<th colspan="8" class="tableTitle">All Encounters</th>
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
								<c:set var="showLink" value="${fn:length(enc.obs) > 0 && showViewLink == 'true'}"/>	
								<td class="encounterEdit" align="center">
									<c:if test="${showEditLink == 'true'}">
										<openmrs:hasPrivilege privilege="Edit Encounters">
											<a href="${pageContext.request.contextPath}/admin/encounters/encounter.form?encounterId=${enc.encounterId}">
												<img src="${pageContext.request.contextPath}/images/edit.gif" title="<spring:message code="general.edit"/>" border="0" align="top" />
											</a>
										</openmrs:hasPrivilege>
									</c:if>
								</td>
								<td class="encounterView" align="center">
									<c:if test="${showLink}">
										<a href="#encounterId=${enc.encounterId}" onClick="handleGetObservations('${enc.encounterId}'); return false;">
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
	
	<%--
	<openmrs:hasPrivilege privilege="View Observations">
		<div id="encounterListObs" >
			<div class="boxHeader"><spring:message code="Obs" /></div>		
			<div class="box">
				<table  cellspacing="0" cellpadding="2" class="patientEncounters">
					<thead>
						<tr>
							<th>Encounter</th>
							<th>Concept</th>
							<th>Value</th>
							<th>Obs Datetime</th>
						</tr>
					
					</thead>
					<tbody id="obsTable">
					</tbody>
				</table>			
			</div>
		</div>
	</openmrs:hasPrivilege>
	--%>

	<%--
	<openmrs:hasPrivilege privilege="Add Observations,Edit Observations">
		<div id="encounterAddObs" >
			<div class="boxHeader"><spring:message code="Obs.add" /></div>		
			<div class="box">
				<form method="post" id="obsForm">
					<input type="hidden" id="patientId" name="patientId" value="${model.patientId}" />
					<table  cellspacing="0" cellpadding="2" class="patientEncounters">
	
						<tr>
							<td>
								<spring:message code="Obs.encounter"/>
								<select id="encounterId" name="encounterId">
									<openmrs:forEachEncounter encounters="${model.patientEncounters}" sortBy="encounterDatetime" descending="true" var="enc">
										<option value='${enc.encounterId}'>
											Encounter #${enc.encounterId} 
											(
											<openmrs:formatDate date="${enc.encounterDatetime}" type="small" />
									 		@ ${enc.location.name}
									 		)
									 	</option>
									</openmrs:forEachEncounter>							
								</select>
							</td>
							<td>
								<spring:message code="Obs.concept"/>
								<% -- <openmrs:fieldGen type="org.openmrs.Concept" formFieldName="conceptId" val="" parameters="noBind=true|fieldLength=12" /> -- %>
								<select id="conceptId" name="conceptId">
									<option value="<openmrs:globalProperty key="concept.weight" defaultValue="" />"><spring:message code="Patient.weight"/></option>
									<option value="<openmrs:globalProperty key="concept.cd4_count" defaultValue="" />"><spring:message code="Patient.cd4"/></option>
								</select>
							</td>
							<td>
								<spring:message code="general.value"/>
								<openmrs:fieldGen type="java.lang.String" formFieldName="valueText" val="" parameters="noBind=true|fieldLength=12" />
							</td>							
							<td>
								<spring:message code="Obs.datetime"/>
								<openmrs:fieldGen type="java.util.Date" formFieldName="obsDate" val="" parameters="noBind=true" />
							</td>							
							<td><input type="button" value="<spring:message code="general.add"/>" onClick="handleAddObs('encounterId', 'conceptId', 'valueText', 'obsDate')"></td>
						</tr>									
					</table>
				</form>
			</div>
		</div>
	</openmrs:hasPrivilege>
	--%>
	
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
			DWRUtil.removeAllRows(id);
			DWRUtil.addRows(id, data, func, {
				cellCreator:function(options) {
				    var td = document.createElement("td");
				    return td;
				}
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
			var encounterId = DWRUtil.getValue($(encounterField));
			var conceptId = DWRUtil.getValue($(conceptField));
			var valueText = DWRUtil.getValue($(valueTextField));
			var obsDate = DWRUtil.getValue($(obsDateField));
			var patientId = ${model.patient.patientId};			
			//alert("Adding obs for encounter (" + encounterId + "): " + conceptId + " = " + valueText + " " + obsDate);  
			DWRObsService.createObs(patientId, encounterId, conceptId, valueText, obsDate);
			handleGetObservations(encounterId);
		}
			
	
		//refreshObsTable();

		// end -->
		
	</script>
</div>
