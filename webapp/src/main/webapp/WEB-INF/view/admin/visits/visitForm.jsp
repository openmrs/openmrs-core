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

<openmrs:htmlInclude file="/scripts/timepicker/timepicker.js" />
<openmrs:htmlInclude file="/dwr/interface/DWREncounterService.js"/>


<c:set var="canDelete" value="${ false }"/>
<c:set var="canPurge" value="${ false }"/>

<script type="text/javascript">

var numberOfClonedElements = 0;
var originalEncountersCount = ${fn:length(visitEncounters)};

function addEncounter() {
	var index = originalEncountersCount+numberOfClonedElements;
	var row = document.getElementById('newEncounterRow');
	var newrow = row.cloneNode(true);
	$j(newrow).show();
	newrow.id = '';
	var inputs = newrow.getElementsByTagName("input");
	for (var i = 0; i < inputs.length; i++) {
		var input = inputs[i];
		if(input && input.type == 'text' || input.type == 'hidden') {
			input.id = input.id.replace('[x]', '[' + index + ']');
			if(input.type == 'hidden')
				input.name = 'encounterIds';
		}
	}
	row.parentNode.insertBefore(newrow, row);
	numberOfClonedElements++;
	// set up the autocomplete for selecting encounters to add
	new AutoComplete('visitEncounters[' + index + ']-display', new CreateCallback({maxresults:100, patientId:<c:out value="${param.patientId}" />}).encounterCallback(), {
		select: function(event, ui) {
			jquerySelectEscaped('visitEncounters[' + index + ']').val(ui.item.object.encounterId);
		},
        placeholder:'<openmrs:message code="Visit.encounter.search.placeholder" javaScriptEscape="true"/>'
	});
}

function removeEncounter(obj){
	obj.parentNode.parentNode.parentNode.removeChild(obj.parentNode.parentNode);
}

$j(document).ready( function() {
	$j("#delete-dialog").dialog({
		autoOpen: false,
		resizable: false,
		width:'auto',
		height:'auto',
		modal: true
	});

	$j('#close-delete-dialog').click(function() {
		$j('#delete-dialog').dialog('close')
	});
	
	$j("#endvisit-dialogue").dialog({
		autoOpen: false,
		resizable: false,
		width:'auto',
		height:'auto',
		modal: true
	});
	$j('#close-endvisit-dialog').click(function() {
		$j('#endvisit-dialogue').dialog('close')
	});
	
	$j("#purge-dialog").dialog({
		autoOpen: false,
		resizable: false,
		width:'auto',
		height:'auto',
		modal: true
	});

	$j("#purge-dialog").addClass("visit-dialog-content");
});

</script>

<style type="text/css">
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
    <openmrs_tag:errorNotify errors="${errors}" />
</spring:hasBindErrors>

<h2>
	<c:choose>
		<c:when test="${visit.visitId == null}"><openmrs:message code="Visit.add"/></c:when>
		<c:otherwise><openmrs:message code="Visit.edit"/></c:otherwise>
	</c:choose>
</h2>

<c:if test="${visit.visitId != null && visit.voided}">
	<form:form action="unvoidVisit.htm" method="post" modelAttribute="visit">
		<input type="hidden" name="visitId" value="${visit.visitId}"/>
		<input type="hidden" name="patientId" value="<c:out value="${visit.patient.patientId}" />"/>
		<div class="voidedMessage">
			<div>
				<openmrs:message code="Visit.voidedMessage"/>
				<c:if test="${visit.voidedBy.personName != null}"> <openmrs:message code="general.byPerson"/> <openmrs:format user="${visit.voidedBy}"/></c:if>
				<c:if test="${visit.dateVoided != null}"> <openmrs:message code="general.onDate"/> <openmrs:formatDate date="${visit.dateVoided}" type="long" /></c:if>
				<c:if test="${visit.voidReason!=''}"> - ${visit.voidReason}</c:if>
			 	<input type="submit" value='<openmrs:message code="general.restore" />' />
			</div>
		</div>
	</form:form>
	<br/>
</c:if>

<form:form method="post" action="visit.form" modelAttribute="visit">
	<c:if test="${visit.patient.patientId != null}">
	<a href="<openmrs:contextPath/>/patientDashboard.form?patientId=<c:out value="${visit.patient.patientId}" />">
		<openmrs:message code="PatientDashboard.backToPatientDashboard"/>
	</a>
	<c:if test="${param.visitId != null}">
		<input type="hidden" name="visitId" value="${param.visitId}"/>
	</c:if>
	<c:if test="${param.patientId != null}">
		<input type="hidden" name="patientId" value="<c:out value="${param.patientId}" />"/>
	</c:if>
	<br/><br/>
	</c:if>
	
<b class="boxHeader"><openmrs:message code="Visit.details"/></b>
<div class="box">

	<c:if test="${visit.visitId != null}">
		<div style="float: right">
			<c:if test="${visit.stopDatetime == null}">
		        <openmrs:hasPrivilege privilege="Edit Visits">
					<input type="button" value="<openmrs:message code="Visit.end"/>" onclick="javascript:$j('#endvisit-dialogue').dialog('open');	" /> 
				</openmrs:hasPrivilege>
			</c:if>
		
			<openmrs:hasPrivilege privilege="Delete Visits">
				<c:if test="${visit.voided == false}">
					<c:set var="canDelete" value="${ true }"/>
					<input type="button" value='<openmrs:message code="general.void"/>' onclick="javascript:$j('#delete-dialog').dialog('open')"/>
				</c:if>
			</openmrs:hasPrivilege>
		
			<openmrs:hasPrivilege privilege="Purge Visits">
				<c:set var="canPurge" value="${ true }"/>
				<input type="button" value='<openmrs:message code="general.purge"/>' onclick="javascript:$j('#purge-dialog').dialog('open')" 
				<c:if test="${!canPurgeVisit}"> disabled="disabled" title="<openmrs:message code="Visit.cannotPurgeVisitWithEncounters"/>"</c:if> />
			</openmrs:hasPrivilege>
		</div>
	</c:if>

	<table class="left-aligned-th" cellpadding="3" cellspacing="3">
		<tr>
			<th>
				<openmrs:message code="general.patient"/><c:if test="${visit.visitId == null}"><span class="required"> *</span></c:if>
			</th>
			<td>
				<c:choose>
					<c:when test="${visit.visitId == null}">
					<spring:bind path="patient">
						<openmrs_tag:patientField formFieldName="${status.expression}" initialValue="${status.value}" />
						<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
					</spring:bind>
					</c:when>
					<c:otherwise><c:out value="${visit.patient.personName}" /></c:otherwise>
				</c:choose>
			</td>
		</tr>
		<tr>
			<th><openmrs:message code="Visit.visitType"/><span class="required"> *</span></th>
			<td>
			<spring:bind path="visitType">
			<c:set var="groupOpen" value="false" />
				<select name="${status.expression}">
				   <option value=""></option>
				<c:forEach items="${visitTypes}" var="visitType">
				<c:if test="${visitType.retired && !groupOpen}">
					<optgroup label="<openmrs:message code="Visit.type.retired"/>">
					<c:set var="groupOpen" value="true" />
				</c:if>
					<option value="${visitType.visitTypeId}" <c:if test="${visitType.visitTypeId == status.value}">selected="selected"</c:if>>
						${visitType.name}
					</option>
				</c:forEach>
				<c:if test="${groupOpen}">
					</optgroup>
					</c:if>
				</select>
			<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
			</td>
		</tr>
		<tr>
			<th><openmrs:message code="Visit.startDatetime"/><span class="required"> *</span></th>
			<td>
				<spring:bind path="startDatetime">
				<input type="text" name="${status.expression}" size="20" value="${status.value}" onClick="showDateTimePicker(this)" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th><openmrs:message code="Visit.stopDatetime"/></th>
			<td>
				<spring:bind path="stopDatetime">
				<input type="text" name="${status.expression}" size="20" value="${status.value}" onClick="showDateTimePicker(this)" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th><openmrs:message code="Visit.location"/></th>
			<td>
				<spring:bind path="location">
				<openmrs_tag:locationField formFieldName="${status.expression}" initialValue="${status.value}"/>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th><openmrs:message code="Visit.indication"/></th>
			<td>
				<spring:bind path="indication">
				<openmrs_tag:conceptField formFieldName="${status.expression}" formFieldId="conceptId" initialValue="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<spring:bind path="activeAttributes">
			<c:if test="${status.error}">
				<tr>
					<th></th>
					<td>
						<span class="error">
							<c:forEach var="err" items="${status.errorMessages}">
								${ err }<br/>
							</c:forEach>
						</span>
					</td>
				</tr>
			</c:if>
		</spring:bind>
		<c:forEach var="attrType" items="${ attributeTypes }">
			<openmrs_tag:attributesForType attributeType="${ attrType }" customizable="${ visit }" formFieldNamePrefix="attribute.${ attrType.id }"/>
		</c:forEach>
		<c:if test="${visit.visitId != null}">
		<c:if test="${visit.creator != null}">
		<tr>
			<th><openmrs:message code="general.createdBy" /></th>
			<td>
				<c:out value="${visit.creator.personName}" /> - <openmrs:formatDate date="${visit.dateCreated}" type="long" />
			</td>
		</tr>
		</c:if>
		<c:if test="${visit.changedBy != null}">
		<tr>
			<th><openmrs:message code="general.changedBy" /></th>
			<td>
				<c:out value="${visit.changedBy.personName}" /> - <openmrs:formatDate date="${visit.dateChanged}" type="long" />
			</td>
		</tr>
		</c:if>
		</c:if>
</table>	
</div>

<br/>

<b class="boxHeader"><openmrs:message code="Visit.encounters"/></b>
<div class="box">
	<table id="encountersTable" cellpadding="3" cellspacing="3">
		<tr>
			<th><openmrs:message code="Encounter.datetime"/></th>
			<th><openmrs:message code="Encounter.type"/></th>
			<th><openmrs:message code="Encounter.location"/></th>
			<th><openmrs:message code="Encounter.provider"/></th>
			<th></th>
		</tr>
		<c:forEach items="${visitEncounters}" var="enc" varStatus="encStatus">
		<tr id="encounter-${enc.encounterId}" style='background-color: whitesmoke'>
			<td><openmrs:formatDate date="${enc.encounterDatetime}" type="small" /></td>
			<td><openmrs:format encounterType="${enc.encounterType}" /></td>
			<td><openmrs:format location="${enc.location}" /></td>
			<td><openmrs:format person="${enc.provider}" /></td>
			<td class="removeButtonColumn">
				<input type="button" value='<openmrs:message code="general.remove"/>' class="smallButton" onclick="removeEncounter(this)" />
				<input type="hidden" name="encounterIds" value="${enc.encounterId}" />
			</td>
		</tr>
		</c:forEach>
		<tr id="newEncounterRow" style="display:none;">
			<td colspan="4">
				<%-- make sure the text field is wide enough to show the placeholder message --%>
				<input type="text" id="visitEncounters[x]-display" size="62" />
				<input type="hidden" id="visitEncounters[x]" name="encounterIds" />
			</td>
			<td class="removeButtonColumn">
				<input type="button" value='<openmrs:message code="general.remove"/>' class="smallButton" onclick="removeEncounter(this)" />
			</td>
		</tr>
	</table>
	<input type="button" value='<openmrs:message code="Visit.addEncounter"/>' class="smallButton" onclick='addEncounter()' />
</div>

<br/>

<input type="submit" value='<openmrs:message code="general.save" />' /></td>
<c:set var="cancelUrl" value="${pageContext.request.contextPath}/admin" scope="page"></c:set>
<c:if test="${not empty param.patientId}">
	<c:set var="cancelUrl" value="${pageContext.request.contextPath}/patientDashboard.form?patientId=${param.patientId}" />
</c:if>
<input type="button" style="margin-left: 15px" value='<openmrs:message code="general.cancel" />' onclick='javascript:window.location="${cancelUrl}"' />

</form:form>

<c:if test="${ canDelete }">
	<div id="delete-dialog" title="<openmrs:message code="general.void"/> <openmrs:message code="Visit"/>">
		<form action="voidVisit.htm" method="post">
			<input type="hidden" name="visitId" value="${visit.visitId}"/>
			<input type="hidden" name="patientId" value="<c:out value="${visit.patient.patientId}" />"/>
			<p><openmrs:message code="Visit.delete.info" arguments="${encounterCount}, ${observationCount}"/></p>
			<table cellpadding="3" cellspacing="3" align="center">
				<tr>
					<th><openmrs:message code="Visit.optionalReason"/></th>
					<td>
						<input type="text" name="voidReason" size="40" />
					</td>
				</tr>
				<tr height="20"></tr>
				<tr>
					<td colspan="2" style="text-align: center">
						<input type="submit" value="<openmrs:message code="general.void"/>" />
						&nbsp;
						<input id="close-delete-dialog" type="button" value="<openmrs:message code="general.cancel"/>" /> 
					</td>
				</tr>
			</table>
		</form>
	</div>
</c:if>

<c:if test="${ canPurge }">
	<div id="purge-dialog" title="<openmrs:message code="Visit.confirm.purge"/>">
		<form:form action="purgeVisit.htm" method="post" modelAttribute="visit">
			<c:if test="${param.visitId != null}">
				<input type="hidden" name="visitId" value="${param.visitId}"/>
			</c:if>
			<c:if test="${param.patientId != null}">
				<input type="hidden" name="patientId" value="<c:out value="${param.patientId}" />"/>
			</c:if>
			<br/>
			<openmrs:message code="Visit.confirm.purgeMessage"/>
			<br/>
			<table cellpadding="3" cellspacing="30" align="center">
				<tr>
					<td>
						<input type="submit" value='<openmrs:message code="general.yes"/>' /> &nbsp; <input type="button" value="<openmrs:message code="general.no"/>"
						onclick="javascript:$j('#purge-dialog').dialog('close')" />
					</td>
				</tr>
			</table>
		</form:form>
	</div>
</c:if>
<div id="endvisit-dialogue" title="<openmrs:message code="Visit.end"/>">
    <form:form action="endVisit.htm" method="post" modelAttribute="visit">
       <table cellpadding="3" cellspacing="3" align="center">
			<tr>
				<td>
					<input type="hidden" name="visitId" value="${visit.visitId}" />
					<openmrs:message code="Visit.enterEndDate"/>
					<jsp:useBean id="now" class="java.util.Date" scope="page" />
					<input type="text" id="enddate_visit" size="20" name="stopDate" value="<openmrs:formatDate date="${now}" format="dd/MM/yyyy HH:mm"/>" onClick="showDateTimePicker(this)" readonly="readonly"/></br>&nbsp;&nbsp;
				</td>
			</tr>
			<tr height="20"></tr>
			<tr>
				<td colspan="2" style="text-align: center">
					<input type="submit" value="<openmrs:message code="Visit.end"/>" />
					&nbsp;
					<input id="close-endvisit-dialog" type="button" value="<openmrs:message code="general.cancel"/>" /> 
				</td>
			</tr>
		</table>
	</form:form>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>
