<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<openmrs:htmlInclude file="/scripts/easyAjax.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRProgramWorkflowService.js" />
<openmrs:htmlInclude file="/dwr/engine.js" />
<openmrs:htmlInclude file="/dwr/util.js" />

<script type="text/javascript">
	function isEmpty(o) {
		return o == null || o == '';
	}
	
	/** 
	 * Parses a string of a given format into a js date object.
	 * see web/openmrs.js parseSimpleDate
	 * 
	 * @param date
	 *		string object to parse
	 * @param format
	 *		if not given a default format is applied
	 * @return a js date object or null if string can't be parsed
	 */
	function parseDate(date, format) {
		if (date == null || date == '')
			return null;

		if (format == null || typeof (format) == 'undefined')
			format = '<openmrs:datePattern />';
		format = format.toLowerCase();
		return parseSimpleDate(date, format);
	}


	/** 
	 * Formats a js date object into a string of a given format.
	 * Format should contain:
	 * 'yyyy' for a year, 'mm' for a month, 'dd' for a day
	 * 
	 * @param date
	 *		js date object to parse
	 * @param format
	 *		if not given a default format is applied
	 * @return string representation of date
	 */
	function formatDate(date, format) {
		if (date == null || date == '')
			return '';
		if (format == null || typeof (format) == 'undefined')
			format = '<openmrs:datePattern />';
		format = format.toLowerCase();

		var yyyy = date.getFullYear();
		var mm = date.getMonth() + 1;
		if (mm < 10)
			mm = "0" + mm;
		var dd = date.getDate();
		if (dd < 10)
			dd = "0" + dd;

		format = format.replace('yyyy', yyyy);
		format = format.replace('mm', mm);
		format = format.replace('dd', dd);

		return format;
	}

	var currentProgramBeingEdited = null;
	var currentWorkflowBeingEdited = null;
	var patientProgramForWorkflowEdited = null;

	function handleSaveProgram() {
		if (currentProgramBeingEdited == null)
			return;
		var idToSave = currentProgramBeingEdited;
		var startDate = parseDate(jQuery('#enrollmentDateElement').val());
		var endDate = parseDate(jQuery('#completionDateElement').val());
		var locationId = jQuery('#programLocationElement').val();
		var outcomeId = jQuery('#programOutcomeConceptElement').val();
        if (!isEmpty(endDate) && !$j('#editProgramOutcomeRow').is(':hidden')
				&& outcomeId == '') {
			alert("<openmrs:message code="PatientProgram.error.outcomeRequired" />");
		} else if (!isEmpty(startDate) && startDate > endDate && !isEmpty(endDate)) {
			alert('<openmrs:message code="Program.error.invalidDate" javaScriptEscape="true"/>');
		} else {
			currentProgramBeingEdited = null;
			DWRProgramWorkflowService.updatePatientProgram(idToSave, formatDate(startDate, 'yyyy-mm-dd'),
					formatDate(endDate, 'yyyy-mm-dd'), locationId, outcomeId, function() {
						hideLayer('editPatientProgramPopup');
						refreshPage();
					});
		}
	}

	function handleDeleteProgram() {
		if (currentProgramBeingEdited == null)
			return;
		var idToDelete = currentProgramBeingEdited;
		var voidReason = document.getElementById("voidReason_PatientProgram").value;
		DWRProgramWorkflowService.deletePatientProgram(idToDelete, voidReason,
				function() {
					hideLayer('editPatientProgramPopup');
					refreshPage();
				});
	}

	function handleChangeWorkflowState() {
		if (currentWorkflowBeingEdited == null)
			return;

		var ppId = patientProgramForWorkflowEdited;
		var wfId = currentWorkflowBeingEdited;
		var stateId = jQuery('#changeToState').val();
		var onDate = parseDate(jQuery('#changeStateOnDate').val());
		var lastStateStartDate = parseDate(jQuery('#lastStateStartDate').val());
		var lastStateEndDate = parseDate(jQuery('#lastStateEndDate').val());
		var lastState = jQuery('#lastState').val();

		if (isEmpty(stateId)) {
			alert('<openmrs:message code="State.error.noState" javaScriptEscape="true"/>');
			return;
		}
		if (!isEmpty(lastState)) {
			if (isEmpty(onDate)) {
				alert('<openmrs:message code="State.error.noDate" javaScriptEscape="true"/>');
				return;
			}
			if (!isEmpty(lastStateStartDate) && lastStateStartDate > onDate) {
				alert('<openmrs:message code="State.error.invalidDate" javaScriptEscape="true"/>');
				return;
			}
			if (!isEmpty(lastStateEndDate)) {
				alert('<openmrs:message code="State.error.invalidChangeState" javaScriptEscape="true"/>');
				return;
			}
		}

		DWRProgramWorkflowService.changeToState(ppId, wfId, stateId,
				formatDate(onDate, 'yyyy-mm-dd'), function() {
					currentWorkflowBeingEdited = null;
					refreshPage();
				});
	}

	function handleVoidLastState() {
		var patientProgramId = patientProgramForWorkflowEdited;
		var programWorkflowId = currentWorkflowBeingEdited;
		DWRProgramWorkflowService.voidLastState(patientProgramId,
				programWorkflowId, '', function() {
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
		jQuery('#workflowPopupTitle').html(wfName);
		dwr.util.removeAllRows('workflowTable');
		dwr.util
				.addRows(
						'workflowTable',
						[ '<openmrs:message code="general.loading" javaScriptEscape="true"/>' ],
						[ function(s) {
							return s;
						} ], {
							escapeHtml : false
						});
		dwr.util.removeAllOptions('changeToState');
		dwr.util
				.addOptions(
						'changeToState',
						[ '<openmrs:message code="general.loading" javaScriptEscape="true"/>' ]);
		jQuery('#changeStateOnDate').val('');
		DWRProgramWorkflowService
				.getPatientStates(
						patientProgramId,
						programWorkflowId,
						function(states) {
							dwr.util.removeAllRows('workflowTable');
							var count = 0;
							var goUntil = states.length;
							dwr.util
									.addRows(
											'workflowTable',
											states,
											[
													function(state) {
														return state.stateName;
													},
													function(state) {
														++count;
														var str = '';
														if (!isEmpty(state.startDate)) str += ' <openmrs:message code="general.fromDate" javaScriptEscape="true"/> ' + formatDate(state.startDate);
														if (!isEmpty(state.endDate)) str += ' <openmrs:message code="general.toDate" javaScriptEscape="true" /> ' + formatDate(state.endDate);
														if (count == goUntil) {
															str += ' <a href="javascript:handleVoidLastState()" style="color: red">[x]</a>';
															jQuery('#lastStateStartDate').val(formatDate(state.startDate));
															jQuery('#lastStateEndDate').val(formatDate(state.endDate));
															jQuery('#lastState').val(state.stateName);
														}
														return str;
													},
													function(state) {
														var str = '';
														str += '<small>&nbsp;&nbsp;';
														str += '<openmrs:message code="general.createdBy" javaScriptEscape="true" />&nbsp;';
														str += state.creator;
														str += '&nbsp;<openmrs:message code="general.onDate" javaScriptEscape="true" />&nbsp;';
														str += formatDate(state.dateCreated);
														str += '</small>';
														return str;
													} ], {
												escapeHtml : false
											});
						});
		DWRProgramWorkflowService
				.getPossibleNextStates(
						patientProgramId,
						programWorkflowId,
						function(items) {
							dwr.util.removeAllOptions('changeToState');
							dwr.util
									.addOptions(
											'changeToState',
											{
												'' : '<openmrs:message code="State.select" javaScriptEscape="true"/>'
											});
							dwr.util.addOptions('changeToState', items, 'id',
									'name');
						});
	}

	function setEditPatientProgramPopupSelectedLocation(locationId) {
		locationSelect = document.getElementById("programLocationElement");

		for (i = 0; i <= locationSelect.length - 1; i++) {
			if (locationSelect.options[i].value == locationId) {
				locationSelect.selectedIndex = i;
				break;
			}
		}
	}

	function showEditPatientProgramPopup(patientProgramId) {
		hideLayer('editWorkflowPopup');
		hideLayer('changedByTR');
		hideLayer('editProgramOutcomeRow');
		$j('#programOutcomeConceptElement').attr('disabled', true);
		currentProgramBeingEdited = patientProgramId;
		jQuery('#programNameElement').html('<openmrs:message code="general.loading" javaScriptEscape="true"/>');
		jQuery('#enrollmentDateElement').val('');
		jQuery('#completionDateElement').val('');
		showLayer('editPatientProgramPopup');
		DWRProgramWorkflowService
				.getPatientProgram(
						patientProgramId,
						function(program) {
							jQuery('#programNameElement').html(program.name);
							jQuery('#enrollmentDateElement').val(formatDate(program.dateEnrolled));
							jQuery('#completionDateElement').val(formatDate(program.dateCompleted));
							if (!isEmpty(program.dateCompletedAsYmd))
								$j('#programOutcomeConceptElement').attr(
										'disabled', false);

							setEditPatientProgramPopupSelectedLocation(program.location.locationId);

							jQuery('#createdByElement').html(program.creator);//program.creator is just a String object, not User class
							jQuery('#dateCreatedElement').html(formatDate(program.dateCreated));
							//show changedBy and date_changed only if changedBy is not empty
							if (!isEmpty(program.changedBy)) {
								jQuery('#changedByElement').html(program.changedBy);//program.creator is just a String object, not User class
								jQuery('#dateChangedElement').html(formatDate(program.dateChanged));
								showLayer('changedByTR');
							}
							DWRProgramWorkflowService
									.getPossibleOutcomes(
											program.programId,
											function(listItems) {
												dwr.util
														.removeAllOptions('programOutcomeConceptElement');
												if (listItems.length != 0) {
													showLayer('editProgramOutcomeRow');
													dwr.util
															.addOptions(
																	'programOutcomeConceptElement',
																	{
																		'' : '<openmrs:message code="Program.outcome.choose" javaScriptEscape="true"/>'
																	});
													dwr.util
															.addOptions(
																	'programOutcomeConceptElement',
																	listItems,
																	'id',
																	'name');
													dwr.util
															.setValue(
																	'programOutcomeConceptElement',
																	program.outcomeId);
												}
											});
						});
	}

	$j(function() {
		$j('#completionDateElement').change(function() {
			if (!isEmpty($j('#completionDateElement').val())) {
				$j('#programOutcomeConceptElement').attr('disabled', false);
			} else {
				$j('#programOutcomeConceptElement').attr('disabled', true);
				$j('#programOutcomeConceptElement').val("");
			}
		})
	})
</script>

<div id="editPatientProgramPopup" style="position: absolute; background-color: #e0e0e0; z-index: 5; padding: 10px; border: 1px black dashed; display: none">
	<table>
		<tr>
			<td><openmrs:message code="Program.program"/>:</td>
			<td><b><span id="programNameElement"></span></b></td>
		</tr>
		<tr>
			<td><openmrs:message code="Program.location"/>:</td>
			<td>
				<select name="locationId" id="programLocationElement">
					<option value=""><openmrs:message code="Program.location.choose"/></option>
					<c:forEach var="location" items="${model.locations}">
						<c:if test="${!location.retired}">
							<option value="${location.locationId}">${location.displayString}</option>
						</c:if>
					</c:forEach>
					<c:forEach var="location" items="${model.locations}">
						<c:if test="${location.retired}">
							<option value="${location.locationId}">${location.displayString} (<openmrs:message code="general.retired"/>)</option>
						</c:if>
					</c:forEach>
				</select>
			</td>
		</tr>
		<tr>
			<td><openmrs:message code="Program.dateEnrolled"/>:</td>
			<td><input type="text" id="enrollmentDateElement" size="10" onfocus="showCalendar(this)" /></td>
		</tr>
		<tr>
			<td><openmrs:message code="Program.dateCompleted"/>:</td>
			<td><input type="text" id="completionDateElement" size="10" onfocus="showCalendar(this)" /></td>
		</tr>
        <tr id="editProgramOutcomeRow">
			<td><openmrs:message code="Program.outcome"/>:</td>
			<td>
				<select name="outcomeConceptId" id="programOutcomeConceptElement"/>
			</td>
		</tr>
		<tr>
			<td><openmrs:message code="general.createdBy" />:</td><td><span id="createdByElement"></span>&nbsp;<openmrs:message code="general.onDate" />&nbsp;<span id="dateCreatedElement"></span></td>
		</tr>
		<tr id="changedByTR" style="display:none;">
			<td><openmrs:message code="general.changedBy" />:</td><td><span id="changedByElement"></span>&nbsp;<openmrs:message code="general.onDate" />&nbsp;<span id="dateChangedElement"></span></td>
		</tr>
	</table>
	<table width="400">
		<tr>
			<td align="center">
				<input type="button" value="<openmrs:message code="general.save"/>" onClick="handleSaveProgram()" />
			</td>
			<td align="center">
				<input type="button" value="<openmrs:message code="general.cancel"/>" onClick="currentProgramBeingEdited = null; hideLayer('editPatientProgramPopup')" />
			</td>
			<td align="center">
				<!-- <input type="button" value="<openmrs:message code="general.delete"/>" onClick="handleDeleteProgram()" />	 -->	
				<span style="position: relative">
				    <input type="button" id="deletePatientProgramButton" value="<openmrs:message code="general.delete"/>" onClick="showDiv('deletePatientProgramDiv')" />
					<div id="deletePatientProgramDiv" style="position: absolute; padding: 1em; bottom: -5px; left: 0px; z-index: 9; width: 250px; border: 1px black solid; background-color: #E0E0F0; display: none">
					    <openmrs:message code="general.voidReasonQuestion"/>:&nbsp;&nbsp;<input type="text" id="voidReason_PatientProgram" size="15" />
						<br/><br/>
						<div align="center">
							<input type="button" value="<openmrs:message code="general.delete"/>" onclick="handleDeleteProgram()"/>
							&nbsp; &nbsp; &nbsp;
							<input type="button" value="<openmrs:message code="general.cancel" />" onClick="hideDiv('deletePatientProgramDiv')"/>
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
						
						<input type="hidden" id="lastStateStartDate" value="" />
						<input type="hidden" id="lastStateEndDate" value="" />
						<input type="hidden" id="lastState" value="" />
						Change to 
							<select id="changeToState"><option value=""><openmrs:message code="general.loading"/></option></select>
						on 
							<input type="text" id="changeStateOnDate" size="10" onfocus="showCalendar(this)" />
			
						<input type="button" value="<openmrs:message code="general.change"/>" onClick="handleChangeWorkflowState()" />
						<input type="button" value="<openmrs:message code="general.cancel"/>" onClick="currentWorkflowBeingEdited = null; hideLayer('editWorkflowPopup')" />
					</div>						
	
<c:choose>
	<c:when test="${fn:length(model.patientPrograms) == 0}">
		<openmrs:message code="Program.notEnrolledInAny"/><br/><br/>
	</c:when>
	<c:otherwise>

		<table width="100%" border="0">
			<tr bgcolor="whitesmoke">
				<td><openmrs:message code="Program.program"/></td>
				<td><openmrs:message code="Program.dateEnrolled"/></td>
				<td><openmrs:message code="Program.location"/></td>
				<td><openmrs:message code="Program.dateCompleted"/></td>
				<td><openmrs:message code="Program.state"/></td>
				<td><openmrs:message code="Program.outcome"/></td>
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
								<small><i>[<openmrs:message code="Program.completed"/>]</i></small>
							</c:if>
							<a href="javascript:showEditPatientProgramPopup(${program.patientProgramId})">
							<openmrs:format program="${program.program}" caseConversion="global"/>
							</a>
						</td>
						<td align="left" valign="top">
							<openmrs:formatDate date="${program.dateEnrolled}" type="medium" />
						</td>
						<td align="left" valign="top">
							<openmrs:format location="${program.location}"/>
						</td>
						<td align="left" valign="top">
							
							<c:choose>
								<c:when test="${not empty program.dateCompleted}">
									<openmrs:formatDate date="${program.dateCompleted}" type="medium" />
								</c:when>
								<c:otherwise>
									<i><openmrs:message code="Program.stillEnrolled"/></i>
								</c:otherwise>								
							</c:choose>
						</td>
						<td>
							<table width="100%">
								<c:forEach var="workflow" items="${program.program.workflows}">
									<c:if test="${!workflow.retired}">
										<tr>
											<td valign="top">

												<small><openmrs:format concept="${workflow.concept}" caseConversion="global"/>:</small>
												<br/>

												<c:set var="stateId" value="" />
												<c:set var="stateStart" value="" />
												<c:set var="retired" value="" />
												<c:forEach var="state" items="${program.states}">
													<c:if test="${!state.voided && state.state.programWorkflow.programWorkflowId == workflow.programWorkflowId && state.active}">
														<c:set var="stateId" value="${state.state.concept.conceptId}" />
														<c:set var="stateStart" value="${state.startDate}" />
														<c:set var="retired" value="${state.state.retired}" />
													</c:if>
												</c:forEach>
												<c:choose>
													<c:when test="${not empty stateId}">
														<b <c:if test="${retired}"> class="voided"} </c:if> ><openmrs:format conceptId="${stateId}" caseConversion="global"/></b>
														<i>(<openmrs:message code="general.since" />
														<openmrs:formatDate date="${stateStart}" type="medium" />)</i>
													</c:when>
													<c:otherwise>
														<i>(<openmrs:message code="general.none" />)</i>
													</c:otherwise>
												</c:choose>
																					  <c:if test="${program.dateCompleted == null}">
													<a href="javascript:showEditWorkflowPopup('<openmrs:concept conceptId="${workflow.concept.conceptId}" nameVar="n" var="v" numericVar="nv">${n.name}</openmrs:concept>', ${program.patientProgramId}, ${workflow.programWorkflowId})">[<openmrs:message code="general.edit"/>]</a>
												</c:if>
											</td>
										</tr>
									</c:if>
								</c:forEach>
							</table>
						</td>
                        <td>
                            <c:choose>
                                <c:when test="${not empty program.outcome}">
                                    <openmrs:format concept="${program.outcome}"/>
                                </c:when>
                                <c:otherwise>
                                    <i>(<openmrs:message code="general.none" />)</i>
                                </c:otherwise>
                            </c:choose>

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
			title: '<openmrs:message code="Program.add" javaScriptEscape="true"/>',
			width: '90%',
			zIndex: 100,
			buttons: { '<openmrs:message code="Program.enrollButton"/>': function() { handleEnrollInProgram(); },
					   '<openmrs:message code="general.cancel"/>': function() { $j(this).dialog("close"); }
			}
		});
	});

	function handleEnrollInProgram() {
		if ($j('#programSelector').val() == ""){
			alert('<openmrs:message code="Program.error.programRequired" />');
		}
		else{
			$j('#enrollForm').submit();
		}

	}
</script>

<div id="enrollInProgramDialog" style="display:none;">
	<br/>
	<div id="enrollError" class="error" style="display:none;"></div>
	<form id="enrollForm" name="enrollForm" method="post" action="${pageContext.request.contextPath}/admin/programs/patientProgram.form">
		<input type="hidden" name="method" value="enroll"/>
		<input type="hidden" name="patientId" value="<c:out value="${model.patientId}" />"/>
		<input type="hidden" name="returnPage" value="${pageContext.request.contextPath}/patientDashboard.form?patientId=<c:out value="${model.patientId}" />"/>
		<table style="margin: 0px 0px 1em 2em;">
			<tr>
				<td nowrap><openmrs:message code="Program.program" javaScriptEscape="true"/>:</td>
				<td>
					<select id="programSelector" name="programId">
						<option value=""><openmrs:message code="Program.choose"/></option>
						<c:forEach var="program" items="${model.programs}">
							<c:if test="${!program.retired}">
							  <option id="programOption${program.programId}" value="${program.programId}"><openmrs:format program="${program}"/></option>
							</c:if>
						</c:forEach>
					</select>
				</td>
			</tr>
			<tr>
				<td nowrap><openmrs:message code="Program.dateEnrolled"/>:</td>
				<td><openmrs_tag:dateField formFieldName="dateEnrolled" startValue="" /></td>
			</tr>
			<tr>
				<td nowrap><openmrs:message code="Program.dateCompleted"/>:</td>
				<td><openmrs_tag:dateField formFieldName="dateCompleted" startValue="" /></td>
			</tr>
			<tr>
				<td nowrap><openmrs:message code="Program.location"/>:</td>
				<td>
					<select name="locationId">
						<option value=""><openmrs:message code="Program.location.choose"/></option>
						<c:forEach var="location" items="${model.locations}">
							<c:if test="${!location.retired}">
							  <option value="${location.locationId}">${location.displayString}</option>
							</c:if>
						</c:forEach>
						<c:forEach var="location" items="${model.locations}">
							<c:if test="${location.retired}">						
								<option value="${location.locationId}">${location.displayString} (<openmrs:message code="general.retired"/>)</option>						
							</c:if>
						</c:forEach>
					</select>				
				</td>
			</tr>
			<tr><td colspan="2">&nbsp;</td></tr>
			<tr id="initialStateSection" style="display:none;">
				<td valign="top"><openmrs:message code="Program.initialStates"/><br/>(<openmrs:message code="general.optional"/>)</td>
				<td>
					<c:forEach items="${model.programs}" var="p">
						<table id="workflowSection${p.programId}" style="display:none;" class="workflowSection">
							<c:forEach items="${p.workflows}" var="wf">
								<c:if test="${!wf.retired}">
									<tr>
										<th align="left"><openmrs:format concept="${wf.concept}"/></th>
										<td>
											<select name="initialState.${wf.programWorkflowId}">
												<option value=""></option>
												<c:forEach items="${wf.sortedStates}" var="wfState">
													<c:if test="${wfState.initial && !wfState.retired}">
														<option value="${wfState.programWorkflowStateId}"><openmrs:format concept="${wfState.concept}"/></option>
													</c:if>
												</c:forEach>
												<c:forEach items="${wf.sortedStates}" var="wfState">
													<c:if test="${wfState.initial && wfState.retired}">
														<option class="retired" value="${wfState.programWorkflowStateId}"><openmrs:format concept="${wfState.concept}"/></option>
													</c:if>
												</c:forEach>
											</select>
										</td>
									</tr>
								</c:if>
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
		<a href="#" id="addProgramLink"><openmrs:message code="Program.add"/></a>
	</openmrs:hasPrivilege>
</c:if>
