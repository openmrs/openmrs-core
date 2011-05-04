<%@ include file="/WEB-INF/template/include.jsp" %>

<c:choose>
	<c:when test="${visit.visitId == null}">
		<openmrs:require privilege="Add Visits" otherwise="/login.htm" redirect="/admin/visits/visit.form" />
	</c:when>
	<c:otherwise>
		<openmrs:require privilege="Edit Visits" otherwise="/login.htm" redirect="/admin/visits/visit.form" />
	</c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<openmrs:htmlInclude file="/dwr/interface/DWREncounterService.js"/>

<script type="text/javascript">

//Currently encounters can only be added to an existing visit
<c:if test="${visit.visitId != null}">
var visitEncountersCount = ${fn:length(visitEncounters)};
var encountersToAddCount = ${fn:length(encountersToAdd)};
var removeConfirmationMsg = '<spring:message code="Visit.confirm.removeMessage"/>';
var addConfirmationMsg = '<spring:message code="Visit.confirm.addMessage"/>';

function addEncounterRow(encounterObj) {
	var row = document.getElementById('newEncounterRow');
	var newrow = row.cloneNode(true);
	newrow.style.display = '';	
	newrow.id = 'encounter-' + encounterObj.encounterId;
	row.parentNode.insertBefore(newrow, row);
	var columns = newrow.getElementsByTagName("td");
	$j(columns[0]).html(encounterObj.encounterDateString);
	$j(columns[1]).html(encounterObj.encounterType);
	$j(columns[2]).html(encounterObj.location);
	$j(columns[3]).html(encounterObj.providerName);
	
	//set the attributes for the remove button
	var inputs = columns[4].getElementsByTagName("input");
	for (var x = 0; x < inputs.length; x++) {
		var input = inputs[x];
		if(inputs[x] && inputs[x].type == 'button') {
			inputs[x].onclick = function(){
				confirmAction(false, encounterObj.encounterId);
			}
		}
	}
	
	encountersToAddCount--;
	visitEncountersCount++;
	
	if(encountersToAddCount < 0)
		encountersToAddCount = 0;
	//no more encounters to add
	if(encountersToAddCount == 0){
		$j("#addEncounterRow").hide();
		//hide the dropdown so that when the next encounter gets removed
		//and the add encounter row is displayed, the drop down is not visible yet
		$j(".addEncounterInputs").css("visibility", "hidden")
	}
	else if($j("#noneRow").is(":visible") == true){
		//hide 'none' message row just in case this visit had no encounters
		$j("#noneRow").hide();
	}
}

function removeEncounterRow(encounterId){
	var encounterRowToRemove = document.getElementById("encounter-"+encounterId);
	encounterRowToRemove.parentNode.removeChild(encounterRowToRemove);
		
	visitEncountersCount--;
	encountersToAddCount++;
	
	if(visitEncountersCount < 0)
		visitEncountersCount = 0;
	
	if(visitEncountersCount > 0 && $j("#addEncounterRow").is(":visible") == false)
		$j("#addEncounterRow").show();
	
	if(visitEncountersCount == 0)
		$j("#noneRow").show();
}

function showAddEncounterDetails(){
	//show the dropdown if it was previously hidden
	$j(".addEncounterInputs").show();
	$j(".addEncounterInputs").css("visibility", "visible")
}

function addEncounter(encounterId){
	DWREncounterService.addEncounterToVisit(encounterId, ${visit.visitId}, function(encounterObj) {
		if(encounterObj){
			addEncounterRow(encounterObj);
			optionObj = document.getElementById("encounterOption-"+encounterId);
			optionObj.parentNode.removeChild(optionObj);
		}else
			alert('<spring:message code="Visit.failedToAddEncounter"/>');
	});
}

function removeEncounter(encounterId){
	DWREncounterService.removeEncounterFromVisit(encounterId, function(encounterObj) {
		if(encounterObj){
			removeEncounterRow(encounterId);
			addEncounterOption(encounterObj);
		}else
			alert('<spring:message code="Visit.failedToRemoveEncounter"/>');
	});	
}

function addEncounterOption(encounterObj){
	var option =  '<option id="encounterOption-'+encounterObj.encounterId+'" value="'+encounterObj.encounterId+'">' + encounterObj.encounterType + 
	' @' + encounterObj.location + ' | ' +encounterObj.encounterDateString + ' | ' + encounterObj.providerName + '</option>';
	
	$j("select#encounterSelect").append(option);
}

function confirmAction(isAddition, encounterId){
	var dialogElement = document.getElementById(isAddition ? "add-enc-confirmation" : "remove-enc-confirmation");
	$j(dialogElement).html("<br/>"+(isAddition ? addConfirmationMsg: removeConfirmationMsg));
	$j(dialogElement).addClass("visit-dialog-content");
	
	$j(dialogElement).dialog({
		autoOpen: true,
		resizable: false,
		width:400,
		height:200,
		modal: true,
		buttons: {
				"<spring:message code="general.yes"/>": function() {
							if(isAddition)
								addEncounter(encounterId);
							else
								removeEncounter(encounterId);
							
							$j(this).dialog('close');
						},
				"<spring:message code="general.cancel"/>": function() {
						if(isAddition)
							document.getElementById("encounterSelect").selectedIndex = 0;
					
						$j(this).dialog('close');
					}
				}
	});
	$j('.ui-dialog-buttonpane').css('text-align', 'center');
}

$j(document).ready( function() {
	$j("#delete-dialog").dialog({
		autoOpen: false,
		resizable: false,
		width:450,
		height:200,
		modal: true
	});
	
	$j("#purge-dialog").dialog({
		autoOpen: false,
		resizable: false,
		width:400,
		height:200,
		modal: true
	});
	
	$j("#purge-dialog").addClass("visit-dialog-content");
});

</c:if>

</script>

<style type="text/css">
	TH.visitLabel{
		text-align: left
	}
	.addEncounterInputs{
		visibility: hidden;
	}
	TD.removeButtonColumn{
		background-color: #FFFFFF
	}
	
	div.visit-dialog-content{
		text-align: center
	}
</style>

<spring:hasBindErrors name="visit">
	<spring:message code="fix.error"/>
	<div class="error">
		<c:forEach items="${errors.allErrors}" var="error">
			<spring:message code="${error.code}" text="${error.code}"/><br/>
		</c:forEach>
	</div>
	<br />
</spring:hasBindErrors>

<h2>
	<c:choose>
		<c:when test="${visit.visitId == null}"><spring:message code="Visit.add"/></c:when>
		<c:otherwise><spring:message code="Visit.edit"/></c:otherwise>
	</c:choose>
</h2>

<openmrs:hasPrivilege privilege="Delete Visits">
<c:if test="${visit.visitId != null && visit.voided}">
<form:form action="unvoidVisit.htm" method="post" modelAttribute="visit">
	<c:if test="${param.visitId != null}">
		<input type="hidden" name="visitId" value="${param.visitId}"/>
	</c:if>
	<c:if test="${param.patientId != null}">
		<input type="hidden" name="patientId" value="${param.patientId}"/>
	</c:if>
	<div class="voidedMessage">
		<div>
			<spring:message code="Visit.voidedMessage"/>
			<c:if test="${visit.voidedBy.personName != null}"> <spring:message code="general.byPerson"/> ${visit.voidedBy.personName}</c:if>
			<c:if test="${visit.dateVoided != null}"> <spring:message code="general.onDate"/> <openmrs:formatDate date="${visit.dateVoided}" type="long" /></c:if>
			<c:if test="${visit.voidReason!=''}"> - ${visit.voidReason}</c:if> 
		 	<input type="submit" value='<spring:message code="general.restore" />' />
		</div>
	</div>
</form:form>
<br/>
</c:if>
</openmrs:hasPrivilege>
	
<form:form method="post" action="visit.form" modelAttribute="visit">
	<c:if test="${visit.patient.patientId != null}">
	<a href="<openmrs:contextPath/>/patientDashboard.form?patientId=${visit.patient.patientId}">
		<spring:message code="PatientDashboard.backToPatientDashboard"/>
	</a>
	<c:if test="${param.visitId != null}">
		<input type="hidden" name="visitId" value="${param.visitId}"/>
	</c:if>
	<c:if test="${param.patientId != null}">
		<input type="hidden" name="patientId" value="${param.patientId}"/>
	</c:if>
	<br/><br/>
	</c:if>
	<fieldset>
		<legend><spring:message code="Visit.details" /></legend>
		<table cellpadding="3" cellspacing="3">
			<tr>
				<th class="visitLabel">
					<spring:message code="general.patient"/><c:if test="${visit.visitId == null}"><span class="required"> *</span></c:if>
				</th>
				<td>
					<c:choose>
						<c:when test="${visit.visitId == null}">
						<spring:bind path="patient">
							<openmrs_tag:patientField formFieldName="${status.expression}" initialValue="${status.value}" />
							<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
						</spring:bind>
						</c:when>
						<c:otherwise>${visit.patient.personName}</c:otherwise>
					</c:choose>
				</td>
			</tr>
			<tr>
				<th class="visitLabel"><spring:message code="Visit.type"/><span class="required"> *</span></th>
				<td>
				<spring:bind path="visitType">
					<select name="${status.expression}">
					   <option value=""></option>
					<c:forEach items="${visitTypes}" var="visitType">
						<option value="${visitType.visitTypeId}" <c:if test="${visitType.visitTypeId == status.value}">selected="selected"</c:if>>
							${visitType.name}
						</option>
					</c:forEach>
					</select>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
				</td>
			</tr>
			<tr>
				<th class="visitLabel"><spring:message code="Visit.startDatetime"/><span class="required"> *</span></th>
				<td>
					<spring:bind path="startDatetime">			
					<input type="text" name="${status.expression}" size="10" value="${status.value}" onClick="showCalendar(this)" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
					</spring:bind>
				</td>
			</tr>
			<tr>
				<th class="visitLabel"><spring:message code="Visit.stopDatetime"/></th>
				<td>
					<spring:bind path="stopDatetime">			
					<input type="text" name="${status.expression}" size="10" value="${status.value}" onClick="showCalendar(this)" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
					</spring:bind>
				</td>
			</tr>
			<tr>
				<th class="visitLabel"><spring:message code="Visit.location"/></th>
				<td>
					<spring:bind path="location">
					<openmrs_tag:locationField formFieldName="${status.expression}" initialValue="${status.value}"/>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
					</spring:bind>
				</td>
			</tr>
			<tr>
				<th class="visitLabel"><spring:message code="Visit.indication"/></th>
				<td>
					<spring:bind path="indication">
					<openmrs_tag:conceptField formFieldName="${status.expression}" formFieldId="conceptId" initialValue="${status.value}" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
					</spring:bind>
				</td>
			</tr>
			<tr>
				<td colspan="2">
					<table cellpadding="0" cellspacing="20" align="center">
        				<tr>
        					<td><input type="submit" value='<spring:message code="general.save" />' /></td>
        					<td>
        						<c:set var="cancelUrl" value="${pageContext.request.contextPath}/admin" scope="page"></c:set>
        						<c:if test="${not empty param.patientId}">
        						<c:set var="cancelUrl" value="${pageContext.request.contextPath}/patientDashboard.form?patientId=${param.patientId}" />
        						</c:if>
        						<input type="button" value='<spring:message code="general.cancel" />' onclick='javascript:window.location="${cancelUrl}"' />
        					</td>
        				</tr>
     				</table>
				</td>
			</tr>
        </table>
    </fieldset>
    <br/>
    <c:if test="${visit.visitId != null}">
    <fieldset>
		<legend><spring:message code="Visit.encounters" /></legend>
		<table id="encountersTable" cellpadding="3" cellspacing="3">
			<tr class="unremovable">
				<th><spring:message code="Encounter.datetime"/></th>
				<th><spring:message code="Encounter.type"/></th>
				<th><spring:message code="Encounter.location"/></th>
				<th><spring:message code="Encounter.provider"/></th>
				<th></th>
			</tr>
			<c:forEach items="${visitEncounters}" var="enc" varStatus="encStatus">
			<tr id="encounter-${enc.encounterId}" style='background-color: whitesmoke'>
				<td><openmrs:formatDate date="${enc.encounterDatetime}" type="small" /></td>
				<td><openmrs:format encounterType="${enc.encounterType}" /></td>
				<td><openmrs:format location="${enc.location}" /></td>
				<td><openmrs:format person="${enc.provider}" /></td>
				<td class="removeButtonColumn">
					<c:if test="${visit.voided == false}">
						<input type="button" value='<spring:message code="general.remove"/>' class="smallButton" 
							onclick="confirmAction(false, ${enc.encounterId})" />
					</c:if>
				</td>
			</tr>
			</c:forEach>
			<tr id="newEncounterRow" style="display:none; background-color: whitesmoke">
				<td></td>
				<td></td>
				<td></td>
				<td></td>
				<td class="removeButtonColumn">
					<input type="button" value='<spring:message code="general.remove"/>' class="smallButton" />
				</td>
			</tr>
			<tr id="noneRow" class="evenRow" <c:if test="${fn:length(visitEncounters) > 0}">style="display:none"</c:if>>
				<td colspan="5" style="text-align: center;"><spring:message code="general.none"/></td>
			</tr>
		</table>
		<c:if test="${visit.voided == false}">
		<table  id="addEncounterRow" cellpadding="3" cellspacing="3" 
			style='padding-top:6px<c:if test="${fn:length(encountersToAdd) == 0}">; display:none</c:if>'>
			<tr>
				<td>
					<input type="button" value='<spring:message code="Visit.addEncounter"/>' class="smallButton" 
						onclick='javascript:$j(".addEncounterInputs").css("visibility", "visible")' />
				</td>
				<td>
					<select id="encounterSelect" class="addEncounterInputs" onchange="confirmAction(true, this.value)">
						<option></option>
						<c:forEach items="${encountersToAdd}" var="enc2" varStatus="enc2Status">
							<option id="encounterOption-${enc2.encounterId}" value="${enc2.encounterId}">
								<openmrs:format encounter="${enc2}" />
							</option>
						</c:forEach>
					</select>
				</td>
				<td class="addEncounterInputs">
					<input type="button" value='<spring:message code="general.done"/>' 
						onclick='javscript:$j(".addEncounterInputs").css("visibility", "hidden")' />
				</td>
			</tr>
		</table>
		</c:if>
	</fieldset>
	</c:if>
</form:form>


<c:if test="${visit.visitId != null}">
<br/>
<table cellpadding="3" cellspacing="3">
	<tr>
		<td>
			<openmrs:hasPrivilege privilege="Delete Visits">
			<c:if test="${visit.voided == false }">
			<input type="submit" value='<spring:message code="general.delete"/>' onclick="javascript:$j('#delete-dialog').dialog('open')"/>
			<div id="delete-dialog" title="<spring:message code="general.delete"/> <spring:message code="Visit"/>">
			<form:form action="voidVisit.htm" method="post" modelAttribute="visit">
			<c:if test="${param.visitId != null}">
				<input type="hidden" name="visitId" value="${param.visitId}"/>
			</c:if>
			<c:if test="${param.patientId != null}">
				<input type="hidden" name="patientId" value="${param.patientId}"/>
			</c:if>
			<br/>
			<table cellpadding="3" cellspacing="3" align="center">
				<tr>
					<th><spring:message code="general.reason"/></th>
					<td>
						<input type="text" name="voidReason" size="40" />
					</td>
				</tr>
				<tr height="20"></tr>
				<tr>
					<td colspan="2" style="text-align: center">
						<input type="submit" value="<spring:message code="general.delete"/>" /> &nbsp; <input type="button" value="<spring:message code="general.cancel"/>" 
						onclick="javascript:$j('#delete-dialog').dialog('close')" /></td>
				</tr>
			</table>
			</form:form>
			</div>
			</c:if>
			</openmrs:hasPrivilege>
		</td>
		<td>
			<openmrs:hasPrivilege privilege="Purge Visits">
			<input type="button" value='<spring:message code="general.purge"/>' onclick="javascript:$j('#purge-dialog').dialog('open')" />
			<div id="purge-dialog" title="<spring:message code="Visit.confirm.purge"/>">
				<form:form action="purgeVisit.htm" method="post" modelAttribute="visit">
				<c:if test="${param.visitId != null}">
					<input type="hidden" name="visitId" value="${param.visitId}"/>
				</c:if>
				<c:if test="${param.patientId != null}">
					<input type="hidden" name="patientId" value="${param.patientId}"/>
				</c:if>
				<br/>
				<spring:message code="Visit.confirm.purgeMessage"/>
				<br/>
				<table cellpadding="3" cellspacing="30" align="center">
					<tr>
						<td>
							<input type="submit" value='<spring:message code="general.yes"/>' /> &nbsp; <input type="button" value="<spring:message code="general.no"/>" 
							onclick="javascript:$j('#purge-dialog').dialog('close')" />
						</td>
					</tr>
				</table>
				</form:form>
			</div>
			</openmrs:hasPrivilege>
		</td>
	</tr>
</table>
</c:if>

<div id="add-enc-confirmation" title="<spring:message code="Visit.confirm.addEncounter"/>"></div>
<div id="remove-enc-confirmation" title="<spring:message code="Visit.confirm.removeEncounter"/>"></div>

<%@ include file="/WEB-INF/template/footer.jsp" %>
