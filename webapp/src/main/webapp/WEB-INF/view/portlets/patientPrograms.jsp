<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<openmrs:htmlInclude file="/scripts/easyAjax.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRProgramWorkflowService.js" />
<openmrs:htmlInclude file="/dwr/engine.js" />
<openmrs:htmlInclude file="/dwr/util.js" />

<script type="text/javascript">
	function getDateString(d) {
		var str = '';
		if (d != null) {
			var date = d.getDate();
			if (date < 10)
				str += "0";
			str += date;
			str += '-';
			var month = d.getMonth() + 1;
			if (month < 10)
				str += "0";
			str += month;
			str += '-';
			str += (d.getYear() + 1900);
		}
		return str;
	}
	
	function isEmpty(o) {
		return o == null || o == '';
	}
	
	<%-- TODO: FORMATDATE AND PARSEDATE ARE TERRIBLE HACKS --%>
	function formatDate(ymd) {
		if (ymd == null || ymd == '')
			return '';
		<c:choose>
			<c:when test="${model.locale == 'fr' || model.locale == 'en_GB'}">
				return ymd.substring(8, 10) + '/' + ymd.substring(5, 7) + '/' + ymd.substring(0, 4);
			</c:when>
			<c:otherwise>
				return ymd.substring(5, 7) + '/' + ymd.substring(8, 10) + '/' + ymd.substring(0, 4);
			</c:otherwise>
		</c:choose>
	}
	
	function parseDate(date) {
		if (date == null || date == '')
			return '';
		<c:choose>
			<c:when test="${model.locale == 'fr' || model.locale == 'en_GB'}">
				// dd/mm/yyyy 01/34/6789
				return date.substring(6,10) + '-' + date.substring(3,5) + '-' + date.substring(0,2);
			</c:when>
			<c:otherwise>
				// mm/dd/yyyy 01/34/6789
				return date.substring(6,10) + '-' + date.substring(0,2) + '-' + date.substring(3,5);
			</c:otherwise>
		</c:choose>
	}
	
	var currentProgramBeingEdited = null;
	var currentWorkflowBeingEdited = null;
	var patientProgramForWorkflowEdited = null;

	function handleSaveProgram() {
		if (currentProgramBeingEdited == null)
			return;
		var idToSave = currentProgramBeingEdited;
		var startDate = parseDate($('enrollmentDateElement').value);
		var endDate = parseDate($('completionDateElement').value);
		var locationId = $('programLocationElement').value;
		currentProgramBeingEdited = null;
		DWRProgramWorkflowService.updatePatientProgram(idToSave, startDate, endDate, locationId, function() {
				hideLayer('editPatientProgramPopup');
				refreshPage();
			});
	}
	
	function handleDeleteProgram() {
		if (currentProgramBeingEdited == null)
			return;
		var idToDelete = currentProgramBeingEdited;
		var voidReason = document.getElementById("voidReason_PatientProgram").value;
		DWRProgramWorkflowService.deletePatientProgram(idToDelete, voidReason , function() {
				hideLayer('editPatientProgramPopup');
				refreshPage();
			});
	}
	
	function handleChangeWorkflowState() {
		if (currentWorkflowBeingEdited == null)
			return;
		
		var ppId = patientProgramForWorkflowEdited;
		var wfId = currentWorkflowBeingEdited;
		var stateId = DWRUtil.getValue('changeToState');
		var onDate = parseDate(DWRUtil.getValue('changeStateOnDate'));
		DWRProgramWorkflowService.changeToState(ppId, wfId, stateId, onDate, function() {
				currentWorkflowBeingEdited = null;
				refreshPage();
			});
	}
	
	function handleVoidLastState() {
		var patientProgramId = patientProgramForWorkflowEdited;
		var programWorkflowId = currentWorkflowBeingEdited;
		DWRProgramWorkflowService.voidLastState(patientProgramId, programWorkflowId, '', function() {
				currentWorkflowBeingEdited = null;
				patientProgramForWorkflowEdited = null;
				refreshPage();
			});
	}
	
	function showEditWorkflowPopup(wfName, patientProgramId, programWorkflowId) {
		hideLayer('editPatientProgramPopup');
		currentWorkflowBeingEdited = programWorkflowId;
		patientProgramForWorkflowEdited = patientProgramId;
		showLayer('editWorkflowPopup');
		$('workflowPopupTitle').innerHTML = wfName;
		dwr.util.removeAllRows('workflowTable');
		dwr.util.addRows('workflowTable', ['<spring:message code="general.loading" javaScriptEscape="true"/>'], [ function(s) { return s; } ], { escapeHtml:false });
		dwr.util.removeAllOptions('changeToState');
		dwr.util.addOptions('changeToState', ['<spring:message code="general.loading" javaScriptEscape="true"/>']);
		$('changeStateOnDate').value = '';
		DWRProgramWorkflowService.getPatientStates(patientProgramId, programWorkflowId, function(states) {
				dwr.util.removeAllRows('workflowTable');
				var count = 0;
				var goUntil = states.length;
				dwr.util.addRows('workflowTable', states, [
						function(state) { return state.stateName; },
						function(state) {
							++count;
							var str = '';
							if (!isEmpty(state.startDate)) str += ' <spring:message code="general.fromDate" javaScriptEscape="true"/> ' + getDateString(state.startDate);
							if (!isEmpty(state.endDate)) str += ' <spring:message code="general.toDate" javaScriptEscape="true" /> ' + getDateString(state.endDate);
							if (count == goUntil)
								str += ' <a href="javascript:handleVoidLastState()" style="color: red">[x]</a>';
							return str;
						},
						function(state) {
							var str = '';
							str += '<small>&nbsp;&nbsp;';
							str += '<spring:message code="general.createdBy" javaScriptEscape="true" />&nbsp;';
							str += state.creator;
							str += '&nbsp;<spring:message code="general.onDate" javaScriptEscape="true" />&nbsp;';
							str += getDateString(state.dateCreated);
							str += '</small>';
							return str;
						}
					], { escapeHtml:false });
			});
		DWRProgramWorkflowService.getPossibleNextStates(patientProgramId, programWorkflowId, function(items) {
				dwr.util.removeAllOptions('changeToState');
				dwr.util.addOptions('changeToState', {'': '<spring:message code="State.select" javaScriptEscape="true"/>' });
				dwr.util.addOptions('changeToState', items, 'id', 'name');
			});
	}
	
	function setEditPatientProgramPopupSelectedLocation(locationId) {
		locationSelect = document.getElementById("programLocationElement");

		for (i=0;i<=locationSelect.length-1;i++) {
			if (locationSelect.options[i].value == locationId) {
				locationSelect.selectedIndex = i;
				break;
			}	
		}
	}
	
	function showEditPatientProgramPopup(patientProgramId) {
		hideLayer('editWorkflowPopup');
		hideLayer('changedByTR');
		currentProgramBeingEdited = patientProgramId;
		$('programNameElement').innerHTML = '<spring:message code="general.loading" javaScriptEscape="true"/>';
		$('enrollmentDateElement').value = '';
		$('completionDateElement').value = '';
		showLayer('editPatientProgramPopup');
		DWRProgramWorkflowService.getPatientProgram(patientProgramId, function(program) {
				$('programNameElement').innerHTML = program.name;
				$('enrollmentDateElement').value = formatDate(program.dateEnrolledAsYmd);
				$('completionDateElement').value = formatDate(program.dateCompletedAsYmd);
				
				setEditPatientProgramPopupSelectedLocation(program.location.locationId);
				
				$('createdByElement').innerHTML = program.creator;//program.creator is just a String object, not User class
				$('dateCreatedElement').innerHTML = getDateString(program.dateCreated);
				//show changedBy and date_changed only if changedBy is not empty
				if(!isEmpty(program.changedBy)){
					$('changedByElement').innerHTML = program.changedBy;//program.creator is just a String object, not User class
					$('dateChangedElement').innerHTML = getDateString(program.dateChanged);
					showLayer('changedByTR');
				}
			});
	}
</script>

<div id="editPatientProgramPopup" style="position: absolute; background-color: #e0e0e0; z-index: 5; padding: 10px; border: 1px black dashed; display: none">
	<table>
		<tr>
			<td><spring:message code="Program.program"/>:</td>
			<td><b><span id="programNameElement"></span></b></td>
		</tr>
		<tr>
			<td><spring:message code="Program.location"/>:</td>
			<td>
				<select name="locationId" id="programLocationElement">
					<option value=""><spring:message code="Program.location.choose"/></option>
					<c:forEach var="location" items="${model.locations}">
						<c:if test="${!location.retired}">						
							<option value="${location.locationId}">${location.displayString}</option>						
						</c:if>
					</c:forEach>
					<c:forEach var="location" items="${model.locations}">
						<c:if test="${location.retired}">						
							<option value="${location.locationId}">${location.displayString} (<spring:message code="general.retired"/>)</option>						
						</c:if>
					</c:forEach>
				</select>
			</td>
		</tr>
		<tr>
			<td><spring:message code="Program.dateEnrolled"/>:</td>
			<td><input type="text" id="enrollmentDateElement" size="10" onClick="showCalendar(this)" /></td>
		</tr>
		<tr>
			<td><spring:message code="Program.dateCompleted"/>:</td>
			<td><input type="text" id="completionDateElement" size="10" onClick="showCalendar(this)" /></td>
		</tr>
		<tr>
			<td><spring:message code="general.createdBy" />:</td><td><span id="createdByElement"></span>&nbsp;<spring:message code="general.onDate" />&nbsp;<span id="dateCreatedElement"></span></td>
		</tr>
		<tr id="changedByTR" style="display:none;">
			<td><spring:message code="general.changedBy" />:</td><td><span id="changedByElement"></span>&nbsp;<spring:message code="general.onDate" />&nbsp;<span id="dateChangedElement"></span></td>
		</tr>
	</table>
	<table width="400">
		<tr>
			<td align="center">
				<input type="button" value="<spring:message code="general.save"/>" onClick="handleSaveProgram()" />
			</td>
			<td align="center">
				<input type="button" value="<spring:message code="general.cancel"/>" onClick="currentProgramBeingEdited = null; hideLayer('editPatientProgramPopup')" />
			</td>
			<td align="center">
				<!-- <input type="button" value="<spring:message code="general.delete"/>" onClick="handleDeleteProgram()" />	 -->	
				<span style="position: relative">
				    <input type="button" id="deletePatientProgramButton" value="<spring:message code="general.delete"/>" onClick="showDiv('deletePatientProgramDiv')" />
					<div id="deletePatientProgramDiv" style="position: absolute; padding: 1em; bottom: -5px; left: 0px; z-index: 9; width: 250px; border: 1px black solid; background-color: #E0E0F0; display: none">
					    <spring:message code="general.voidReasonQuestion"/>:&nbsp;&nbsp;<input type="text" id="voidReason_PatientProgram" size="15" />
						<br/><br/>
						<div align="center">
							<input type="button" value="<spring:message code="general.delete"/>" onclick="handleDeleteProgram()"/>
							&nbsp; &nbsp; &nbsp;
							<input type="button" value="<spring:message code="general.cancel" />" onClick="hideDiv('deletePatientProgramDiv')"/>
						</div>
					</div>
				</span>
			</td>
		</tr>
		
	</table>
</div>
					<div id="editWorkflowPopup" style="position: absolute; background-color: #e0e0e0; z-index: 5; padding: 10px; border: 1px black dashed; display: none">
						<b><u><span id="workflowPopupTitle"></span></u></b>
						<table id="workflowTable">
						</table>
						
						Change to 
							<select id="changeToState"><option value=""><spring:message code="general.loading"/></option></select>
						on 
							<input type="text" id="changeStateOnDate" size="10" onClick="showCalendar(this)" />
			
						<input type="button" value="<spring:message code="general.change"/>" onClick="handleChangeWorkflowState()" />
						<input type="button" value="<spring:message code="general.close"/>" onClick="currentWorkflowBeingEdited = null; hideLayer('editWorkflowPopup')" />
					</div>						
	
<c:choose>
	<c:when test="${fn:length(model.patientPrograms) == 0}">
		<spring:message code="Program.notEnrolledInAny"/><br/><br/>
	</c:when>
	<c:otherwise>

		<table width="100%" border="0">
			<tr bgcolor="whitesmoke">
				<td><spring:message code="Program.program"/></td>
				<td><spring:message code="Program.dateEnrolled"/></td>
				<td><spring:message code="Program.location"/></td>
				<td><spring:message code="Program.dateCompleted"/></td>
				<td><spring:message code="Program.state"/></td>
			</tr>
			<c:set var="bgColor" value="whitesmoke" />
			<c:forEach var="program" items="${model.patientPrograms}">
				<c:if test="${!program.voided}">
					<c:choose>
						<c:when test="${bgColor == 'white'}"><c:set var="bgColor" value="whitesmoke" /></c:when>
						<c:otherwise><c:set var="bgColor" value="white" /></c:otherwise>
					</c:choose>
					<tr style="background-color: ${bgColor}">
						<td valign="top">
							<c:if test="${program.dateCompleted != null}">
								<small><i>[<spring:message code="Program.completed"/>]</i></small>
							</c:if>
							<a href="javascript:showEditPatientProgramPopup(${program.patientProgramId})">
							<openmrs:format program="${program.program}"/>
							</a>
						</td>
						<td align="left" valign="top">
							<openmrs:formatDate date="${program.dateEnrolled}" type="medium" />
						</td>
						<td align="left" valign="top">
							${program.location}
						</td>
						<td align="left" valign="top">
							
							<c:choose>
								<c:when test="${not empty program.dateCompleted}">
									<openmrs:formatDate date="${program.dateCompleted}" type="medium" />
								</c:when>
								<c:otherwise>
									<i><spring:message code="Program.stillEnrolled"/></i>
								</c:otherwise>								
							</c:choose>
						</td>
						<td>
							<table width="100%">
								<c:forEach var="workflow" items="${program.program.workflows}">
									<tr>
										<td style="" valign="top">
										
											<small><openmrs:format concept="${workflow.concept}"/>:</small>
											<br/>
											
											<c:set var="stateId" value="" />
											<c:set var="stateStart" value="" />
											<c:forEach var="state" items="${program.states}">
												<c:if test="${!state.voided && state.state.programWorkflow.programWorkflowId == workflow.programWorkflowId && state.active}">
													<c:set var="stateId" value="${state.state.concept.conceptId}" />
													<c:set var="stateStart" value="${state.startDate}" />
												</c:if>
											</c:forEach>
											<c:choose>
												<c:when test="${not empty stateId}">
													<b><openmrs:format conceptId="${stateId}"/></b>
													<i>(<spring:message code="general.since" /> 
													<openmrs:formatDate date="${stateStart}" type="medium" />)</i>
												</c:when>
												<c:otherwise>
													<i>(<spring:message code="general.none" />)</i>
												</c:otherwise>
											</c:choose>

											<a href="javascript:showEditWorkflowPopup('<openmrs:concept conceptId="${workflow.concept.conceptId}" nameVar="n" var="v" numericVar="nv">${n.name}</openmrs:concept>', ${program.patientProgramId}, ${workflow.programWorkflowId})">[<spring:message code="general.edit"/>]</a>
										</td>
									</tr>
								</c:forEach>
							</table>
						</td>
					</tr>
				</c:if>
			</c:forEach>
	</c:otherwise>
</c:choose>

</table>

<script type="text/javascript">
	$j(document).ready(function() {

		$j('#addProgramLink').click(function(event){ 
			$j('#enrollInProgramDialog').dialog('open');
		});

		$j('#programSelector').change(function(event){
			$j(".workflowSection").hide();
			$j("#initialStateSection").hide();
			var pId = $j(this).val();
			if (pId && pId != null && pId != '') {
				$j("#initialStateSection").show();
				$j("#workflowSection"+pId).show();
			}
		});
		
		$j('#enrollInProgramDialog').dialog({
			position: 'top',
			autoOpen: false,
			modal: true,
			title: '<spring:message code="Program.add" javaScriptEscape="true"/>',
			width: '90%',
			zIndex: 100,
			buttons: { '<spring:message code="Program.enrollButton"/>': function() { handleEnrollInProgram(); },
					   '<spring:message code="general.cancel"/>': function() { $j(this).dialog("close"); }
			}
		});
	});

	function handleEnrollInProgram() {
		$j('#enrollForm').submit();
	}
</script>

<div id="enrollInProgramDialog" style="display:none;">
	<br/>
	<div id="enrollError" class="error" style="display:none;"></div>
	<form id="enrollForm" name="enrollForm" method="post" action="${pageContext.request.contextPath}/admin/programs/patientProgram.form">
		<input type="hidden" name="method" value="enroll"/>
		<input type="hidden" name="patientId" value="${model.patientId}"/>
		<input type="hidden" name="returnPage" value="${pageContext.request.contextPath}/patientDashboard.form?patientId=${model.patientId}"/>
		<table style="margin: 0px 0px 1em 2em;">
			<tr>
				<td nowrap><spring:message code="Program.program" javaScriptEscape="true"/>:</td>
				<td>
					<select id="programSelector" name="programId">
						<option value=""><spring:message code="Program.choose"/></option>
						<c:forEach var="program" items="${model.programs}">
							<c:if test="${!program.retired}">
							  <option id="programOption${program.programId}" value="${program.programId}"><openmrs:format program="${program}"/></option>
							</c:if>
						</c:forEach>
					</select>
				</td>
			</tr>
			<tr>
				<td nowrap><spring:message code="Program.dateEnrolled"/>:</td>
				<td><openmrs_tag:dateField formFieldName="dateEnrolled" startValue="" /></td>
			</tr>
			<tr>
				<td nowrap><spring:message code="Program.location"/>:</td>
				<td>
					<select name="locationId">
						<option value=""><spring:message code="Program.location.choose"/></option>
						<c:forEach var="location" items="${model.locations}">
							<c:if test="${!location.retired}">
							  <option value="${location.locationId}">${location.displayString}</option>
							</c:if>
						</c:forEach>
						<c:forEach var="location" items="${model.locations}">
							<c:if test="${location.retired}">						
								<option value="${location.locationId}">${location.displayString} (<spring:message code="general.retired"/>)</option>						
							</c:if>
						</c:forEach>
					</select>				
				</td>
			</tr>
			<tr><td colspan="2">&nbsp;</td></tr>
			<tr id="initialStateSection" style="display:none;">
				<td valign="top"><spring:message code="Program.initialStates"/><br/>(<spring:message code="general.optional"/>)</td>
				<td>
					<c:forEach items="${model.programs}" var="p">
						<table id="workflowSection${p.programId}" style="display:none;" class="workflowSection">
							<c:forEach items="${p.allWorkflows}" var="wf">
								<tr>
									<th align="left"><openmrs:format concept="${wf.concept}"/></th>
									<td>
										<select name="initialState.${wf.programWorkflowId}">
											<option value=""></option>
											<c:forEach items="${wf.sortedStates}" var="wfState">
												<c:if test="${wfState.initial}">
													<option value="${wfState.programWorkflowStateId}"><openmrs:format concept="${wfState.concept}"/></option>
												</c:if>
											</c:forEach>
										</select>
									</td>
								</tr>
							</c:forEach>
						</table>
					</c:forEach>
				</td>
			</tr>
		</table>
	</form>
</div>
			
<c:if test="${model.allowEdits == 'true' && fn:length(model.programs) > 0}">
	<openmrs:hasPrivilege privilege="Edit Patient Programs">
		<a href="#" id="addProgramLink"><spring:message code="Program.add"/></a>
	</openmrs:hasPrivilege>
</c:if>