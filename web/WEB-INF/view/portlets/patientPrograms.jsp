<%@ include file="/WEB-INF/template/include.jsp" %>

<script src="<%= request.getContextPath() %>/scripts/calendar/calendar.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/easyAjax.js"></script>

<script type="text/javascript" src="<%= request.getContextPath() %>/dwr/interface/DWRProgramWorkflowService.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/dwr/engine.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/dwr/util.js"></script>


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
			<c:when test="${model.locale == 'fr'}">
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
			<c:when test="${model.locale == 'fr'}">
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
		currentProgramBeingEdited = null;
		DWRProgramWorkflowService.updatePatientProgram(idToSave, startDate, endDate, function() {
				hideLayer('editPatientProgramPopup');
				refreshPage();
			});
	}
	
	function handleDeleteProgram() {
		if (currentProgramBeingEdited == null)
			return;
		var idToDelete = currentProgramBeingEdited;
		DWRProgramWorkflowService.deletePatientProgram(idToDelete, '', function() {
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
		DWRUtil.removeAllRows('workflowTable');
		DWRUtil.addRows('workflowTable', ['<spring:message code="general.loading"/>'], [ function(s) { return s; } ]);
		DWRUtil.removeAllOptions('changeToState');
		DWRUtil.addOptions('changeToState', ['<spring:message code="general.loading"/>']);
		$('changeStateOnDate').value = '';
		DWRProgramWorkflowService.getPatientStates(patientProgramId, programWorkflowId, function(states) {
				DWRUtil.removeAllRows('workflowTable');
				var count = 0;
				var goUntil = states.length;
				DWRUtil.addRows('workflowTable', states, [
						function(state) { return state.stateName; },
						function(state) {
							++count;
							var str = '';
							if (!isEmpty(state.startDate)) str += ' <spring:message code="general.fromDate"/> ' + getDateString(state.startDate);
							if (!isEmpty(state.endDate)) str += ' <spring:message code="general.toDate"/> ' + getDateString(state.endDate);
							if (count == goUntil)
								str += ' <a href="javascript:handleVoidLastState()" style="color: red">[x]</a>';
							return str;
						}
					]);
			});
		DWRProgramWorkflowService.getPossibleNextStates(patientProgramId, programWorkflowId, function(items) {
				DWRUtil.removeAllOptions('changeToState');
				DWRUtil.addOptions('changeToState', {'': '<spring:message code="State.select"/>' });
				DWRUtil.addOptions('changeToState', items, 'id', 'name');
			});
	}
	
	function showEditPatientProgramPopup(patientProgramId) {
		hideLayer('editWorkflowPopup');
		currentProgramBeingEdited = patientProgramId;
		$('programNameElement').innerHTML = '<spring:message code="general.loading"/>';
		$('enrollmentDateElement').value = '';
		$('completionDateElement').value = '';
		showLayer('editPatientProgramPopup');
		DWRProgramWorkflowService.getPatientProgram(patientProgramId, function(program) {
				$('programNameElement').innerHTML = program.name;
				$('enrollmentDateElement').value = formatDate(program.dateEnrolledAsYmd);
				$('completionDateElement').value = formatDate(program.dateCompletedAsYmd);
			});
	}
</script>

<div id="editPatientProgramPopup" style="position: absolute; background-color: #e0e0e0; z-index: 5; border: 2px black solid; display: none">
	<table>
	<tr>
		<td><spring:message code="Program.program"/>:</td>
		<td><b><span id="programNameElement"></span></b></td>
	</tr>
	<tr>
		<td><spring:message code="Program.dateEnrolled"/>:</td>
		<td><input type="text" id="enrollmentDateElement" size="10" onClick="showCalendar(this)" />
	</tr>
	<tr>
		<td><spring:message code="Program.dateCompleted"/>:</td>
		<td><input type="text" id="completionDateElement" size="10" onClick="showCalendar(this)" />
	</tr>
	</table>
	<table width="400"><tr>
		<td align="center">
			<input type="button" value="<spring:message code="general.save"/>" onClick="handleSaveProgram()" />
		</td>
		<td align="center">
			<input type="button" value="<spring:message code="general.cancel"/>" onClick="currentProgramBeingEdited = null; hideLayer('editPatientProgramPopup')" />
		</td>
		<td align="center">
			<input type="button" value="<spring:message code="general.delete"/>" onClick="handleDeleteProgram()" />
		</td>
	</tr></table>
</div>

<c:choose>
	<c:when test="${fn:length(model.patientPrograms) == 0}">
		<spring:message code="Program.notEnrolledInAny"/>
	</c:when>
	<c:otherwise>
		<table>
		<tr>
			<td><spring:message code="Program.program"/></td>
			<td><spring:message code="Program.dateEnrolled"/></td>
			<td><spring:message code="Program.dateCompleted"/></td>
			<td>
				<spring:message code="Program.states"/>
				<div id="editWorkflowPopup" style="position: absolute; background-color: #e0e0e0; z-index: 5; border: 2px black solid; display: none">
					<b><u><span id="workflowPopupTitle"></span></u></b>
					<table id="workflowTable">
					</table>
					Change to
						<select id="changeToState"><option value=""><spring:message code="general.loading"/></option></select>
					on
						<input type="text" id="changeStateOnDate" size="10" onClick="showCalendar(this)" />
					&nbsp;&nbsp;&nbsp;
					<input type="button" value="<spring:message code="general.change"/>" onClick="handleChangeWorkflowState()" />
					<br/>
					<input type="button" value="<spring:message code="general.close"/>" onClick="currentWorkflowBeingEdited = null; hideLayer('editWorkflowPopup')" />
				</div>		
			</td>
		</tr>
		<c:forEach var="program" items="${model.patientPrograms}">
			<c:if test="${!program.voided}">
				<tr>
					<td>
						<c:if test="${program.dateCompleted != null}">
							<small><i>[<spring:message code="Program.completed"/>]</i></small>
						</c:if>
						<a href="javascript:showEditPatientProgramPopup(${program.patientProgramId})">
						<openmrs_tag:concept conceptId="${program.program.concept.conceptId}"/>
						</a>
					</td>
					<td align="center">
						<openmrs:formatDate date="${program.dateEnrolled}"/>
					</td>
					<td align="center">
						<openmrs:formatDate date="${program.dateCompleted}"/>
					</td>
					<td>				
						<c:forEach var="workflow" items="${program.program.workflows}">
							<small><openmrs_tag:concept conceptId="${workflow.concept.conceptId}"/>:</small>
							<c:forEach var="state" items="${program.states}">
								<c:if test="${!state.voided && state.state.programWorkflow.programWorkflowId == workflow.programWorkflowId && state.active}">
									<b><openmrs_tag:concept conceptId="${state.state.concept.conceptId}"/></b>
								</c:if>
							</c:forEach>
							&nbsp;&nbsp;&nbsp;&nbsp;
							<a href="javascript:showEditWorkflowPopup('<openmrs:concept conceptId="${workflow.concept.conceptId}" nameVar="n" var="v" numericVar="nv">${n.name}</openmrs:concept>', ${program.patientProgramId}, ${workflow.programWorkflowId})">[<spring:message code="general.edit"/>]</a>
							<br/>
						</c:forEach>
					</td>
				</tr>
			</c:if>
		</c:forEach>
		</table>			
	</c:otherwise>
</c:choose>

<c:if test="${model.allowEdits == 'true' && fn:length(model.programs) > 0}">
	<openmrs:hasPrivilege privilege="Manage Patient Programs">
		<div id="newProgramEnroll">
		<form method="post" action="patientProgram.form">
			<input type="hidden" name="method" value="enroll"/>
			<input type="hidden" name="patientId" value="${model.patientId}"/>
			<input type="hidden" name="returnPage" value="patientDashboard.form?patientId=${model.patientId}"/>
			
			<spring:message code="Program.enrollIn"/>
			<select name="programId">
				<option value=""><spring:message code="Program.choose"/></option>
				<c:forEach var="program" items="${model.programs}">
					<option value="${program.programId}"><openmrs_tag:concept conceptId="${program.concept.conceptId}"/></option>
				</c:forEach>
			</select>
			<spring:message code="general.onDate"/>
			<input type="text" id="programDateEnrolled" name="dateEnrolled" size="10" onClick="showCalendar(this)" />
		
			<input type="submit" value="<spring:message code="Program.enrollButton"/>"/>
		</form>
		</div>
	</openmrs:hasPrivilege>
</c:if>