<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:htmlInclude file="/scripts/easyAjax.js" />

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
		<div id="encounters">
			<div class="boxHeader">Encounters</div>
			<div class="box">
				<table cellspacing="0" cellpadding="2" class="patientEncounters">
					<c:if test="${fn:length(model.patientEncounters) > 0}">
						<tr>
							<th colspan="6" class="tableTitle">All Encounters</th>
						</tr>
						<tr>
							<th> <spring:message code="Encounter.datetime"/> </th>
							<th> <spring:message code="Encounter.type"/>     </th>
							<th> <spring:message code="Encounter.provider"/> </th>
							<th> <spring:message code="Encounter.form"/>     </th>
							<th> <spring:message code="Encounter.location"/> </th>
							<th> <spring:message code="Encounter.enterer"/>  </th>
						</tr>
						<openmrs:forEachEncounter encounters="${model.patientEncounters}" sortBy="encounterDatetime" descending="true" var="enc">
							<tr>
								<c:set var="showLink" value="${fn:length(enc.obs) > 0}"/>	
								<td>
									<openmrs:hasPrivilege privilege="Edit Encounters">
										<a href="${pageContext.request.contextPath}/admin/encounters/encounter.form?encounterId=${enc.encounterId}">
											<img src="${pageContext.request.contextPath}/images/edit.gif" title="<spring:message code="general.edit"/>" border="0" align="top" />
										</a>
									</openmrs:hasPrivilege>
									<c:if test="${showLink}">
										<a href="#encounterId=${enc.encounterId}" onClick="handleGetObservations('${enc.encounterId}'); return false;">
									</c:if>
										<openmrs:formatDate date="${enc.encounterDatetime}" type="small" />
									<c:if test="${showLink}">
										</a>
									</c:if>
								</td>
							 	<td>${enc.encounterType.name}</td>
							 	<td>${enc.provider.firstName} ${enc.provider.lastName}</td>
							 	<td>${enc.form.name}</td>
							 	<td>${enc.location.name}</td>
							 	<td>${enc.creator.firstName} ${enc.creator.lastName}</td>
							</tr>
						</openmrs:forEachEncounter>
					</c:if>
					<c:if test="${fn:length(encounters) == 0}">
						<tr>
							<th colspan="6" class="tableTitle"><spring:message code="FormEntry.no.last.encounters"/></th>
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
	<script>
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
					var formWindow = window.open('encounterDisplay.list?encounterId=' + encounterId, '${enc.encounterId}', 'toolbar=no,width=800,height=600,resizable=yes,scrollbars=yes');
					formWindow.focus();
				</c:when>
				<c:when test="${viewEncounterWhere == 'oneNewWindow'}">
					var formWindow = window.open('encounterDisplay.list?encounterId=' + encounterId, 'formWindow', 'toolbar=no,width=800,height=600,resizable=yes,scrollbars=yes');
					formWindow.focus();
				</c:when>
				<c:otherwise>
					window.location = 'encounterDisplay.list?encounterId=' + encounterId;
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
