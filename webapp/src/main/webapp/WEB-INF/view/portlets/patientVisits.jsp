<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables.css" />
<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js" />

<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui-1.7.2.custom.min.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRVisitService.js"/>
<link href="<openmrs:contextPath/>/scripts/jquery-ui/css/<spring:theme code='jqueryui.theme.name' />/jquery-ui.custom.css" type="text/css" rel="stylesheet" />

<script type="text/javascript">
var selectedVisitRow;
function updateEncounters(visitId){
	//user has selected the same visit as the one before
	if(selectedVisitRow && $j(selectedVisitRow).attr('id') == visitId)
		return;
	
	//clear the table
	$j("#visitEncountersTable > tbody > tr").remove();
	if(selectedVisitRow){
		$j(selectedVisitRow).removeClass('selected-visit');
	}
	selectedVisitRow = $j("#"+visitId);
	$j(selectedVisitRow).addClass('selected-visit');
	
	DWRVisitService.findEncountersByVisit(visitId, function(encounters) {
		if(encounters){
			if(encounters.length > 0){
				for(var i in encounters){
					var e = encounters[i];
					$j('#visitEncountersTable tbody:last').append('<tr>'+
						'<td>'+e.encounterDateString+'</td>'+
						'<td>'+e.encounterType+'</td>'+
						'<td>'+e.providerName+'</td>'+
						'<td>'+((e.formName) ? e.formName:"")+'</td>'+
						'<td>'+e.location+'</td>'+
					'</tr>');
				}
			}
		}else{
			alert('<spring:message code="Visit.find.encounters.error"/>');
		}
	});
}
</script>

<style type="text/css">
.visitRow{
	cursor: pointer;
}
.visitRow:hover{
	background: #F0E68C;
}
.selected-visit{
	background-color: #A8D0F7; color: white; font-weight: bold;
}
</style>

<div id="portlet${model.portletUUID}">
<div id="visitPortlet">

	<openmrs:hasPrivilege privilege="View Visits">
		<div id="visits">
			<div class="boxHeader${model.patientVariation}"><c:choose><c:when test="${empty model.title}"><spring:message code="Visit.header"/></c:when><c:otherwise><spring:message code="${model.title}"/></c:otherwise></c:choose></div>
			<div class="box${model.patientVariation}">
				<openmrs:hasPrivilege privilege="Add Visits">
				&nbsp;<a href="<openmrs:contextPath />/admin/visits/visit.form?patientId=${model.patient.patientId}"><spring:message code="Visit.add"/></a>
				<br/><br/>
				</openmrs:hasPrivilege>
				<div>
					<table cellspacing="0" cellpadding="2" id="patientVisitsTable">
						<thead>
							<tr>
								<th class="visitEdit" align="center">
									<spring:message code="general.edit"/>
								</th>
								<th class="visitTypeHeader"> <spring:message code="Visit.type"/>     </th>
								<th class="visitLocationHeader"> <spring:message code="Visit.location"/> </th>
								<th class="startDatetimeHeader"> <spring:message code="Visit.startDatetime"/> </th>
								<th class="stopDatetimeHeader"> <spring:message code="Visit.stopDatetime"/> </th>
								<th class="indicationHeader"> <spring:message code="Visit.indication"/> </th>
							</tr>
						</thead>
						<tbody>
							<openmrs:forEachVisit visits="${model.patientVisits}" sortBy="startDatetime" descending="true" var="visit" num="${model.num}">
								<tr id="${visit.visitId}" class='visitRow ${status.index % 2 == 0 ? "evenRow" : "oddRow"}'
									onclick="updateEncounters(${visit.visitId})">
									<td class="visitEdit" align="center">
										<openmrs:hasPrivilege privilege="Edit Visits">
											<a href="${pageContext.request.contextPath}/admin/visits/visit.form?visitId=${visit.visitId}">
												<img src="${pageContext.request.contextPath}/images/edit.gif" title="<spring:message code="general.edit"/>" border="0" />
											</a>
										</openmrs:hasPrivilege>
									</td>
					 				<td class="visitType"><openmrs:format visitType="${visit.visitType}"/></td>
					 				<td class="visitLocation"><openmrs:format location="${visit.location}"/></td>
					 				<td class="startDatetime">
										<openmrs:formatDate date="${visit.startDatetime}" type="small" />
									</td>
									<td class="stopDatetime">
										<openmrs:formatDate date="${visit.stopDatetime}" type="small" />
									</td>
									<td class="indication"><openmrs:format concept="${visit.indication}"/></td>
								</tr>
							</openmrs:forEachVisit>
						</tbody>
					</table>
				</div>
			</div>
		</div>
		<br />
		<div class="boxHeader"><spring:message code="Visit.encounters.selectedVisit"/></div>
		<div class="box">
			<table cellspacing="0" cellpadding="2" id="visitEncountersTable">
				<thead>
				 <tr>
					<th> <spring:message code="Encounter.datetime"/></th>
					<th> <spring:message code="Encounter.type"/></th>
					<th> <spring:message code="Encounter.provider"/></th>
					<th> <spring:message code="Encounter.form"/></th>
					<th> <spring:message code="Encounter.location"/></th>
				 </tr>
				<thead>
				<tbody></tbody>
			</table>
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