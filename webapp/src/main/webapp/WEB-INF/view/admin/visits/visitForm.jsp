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
	new AutoComplete('visitEncounters[' + index + ']-display', new CreateCallback({maxresults:100}).encounterCallback(), {
		select: function(event, ui) {
			jquerySelectEscaped('visitEncounters[' + index + ']').val(ui.item.object.encounterId);
		},
        placeholder:'<spring:message code="Encounter.search.placeholder" javaScriptEscape="true"/>'
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
	<table class="left-aligned-th" cellpadding="3" cellspacing="3">
		<tr>
			<th>
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
			<th><spring:message code="Visit.type"/><span class="required"> *</span></th>
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
			<th><spring:message code="Visit.startDatetime"/><span class="required"> *</span></th>
			<td>
				<spring:bind path="startDatetime">
				<input type="text" name="${status.expression}" size="20" value="${status.value}" onClick="showDateTimePicker(this)" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th><spring:message code="Visit.stopDatetime"/></th>
			<td>
				<spring:bind path="stopDatetime">
				<input type="text" name="${status.expression}" size="20" value="${status.value}" onClick="showDateTimePicker(this)" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th><spring:message code="Visit.location"/></th>
			<td>
				<spring:bind path="location">
				<openmrs_tag:locationField formFieldName="${status.expression}" initialValue="${status.value}"/>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th><spring:message code="Visit.indication"/></th>
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
		<tr>
			<th valign="top"><spring:message code="Visit.encounters" /></th>
			<td valign="top">
				<table id="encountersTable" cellpadding="3" cellspacing="3">
					<tr>
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
							<input type="button" value='<spring:message code="general.remove"/>' class="smallButton" onclick="removeEncounter(this)" />
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
							<input type="button" value='<spring:message code="general.remove"/>' class="smallButton" onclick="removeEncounter(this)" />
						</td>
					</tr>
				</table>
				<input type="button" value='<spring:message code="Visit.addEncounter"/>' class="smallButton" onclick='addEncounter()' />
				<br /><br />
			</td>
		</tr>
		<c:if test="${visit.visitId != null}">
		<c:if test="${visit.creator != null}">
		<tr>
			<th><spring:message code="general.createdBy" /></th>
			<td>
				${visit.creator.personName} - <openmrs:formatDate date="${visit.dateCreated}" type="long" />
			</td>
		</tr>
		</c:if>
		<c:if test="${visit.changedBy != null}">
		<tr>
			<th><spring:message code="general.changedBy" /></th>
			<td>
				${visit.changedBy.personName} - <openmrs:formatDate date="${visit.dateChanged}" type="long" />
			</td>
		</tr>
		</c:if>
		</c:if>
		<tr>
			<td colspan="2"><br/>
				<table cellpadding="0" cellspacing="0" align="left">
       				<tr>
       					<td><input type="submit" value='<spring:message code="general.save" />' /></td>
       					<td width="15px"></td>
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
</form:form>


<c:if test="${visit.visitId != null}">
<br/>
<table cellpadding="3" cellspacing="3" style="float: right;">
	<tr>
		<td>
			<openmrs:hasPrivilege privilege="Delete Visits">
			<c:if test="${visit.voided == false }">
			<input type="submit" value='<spring:message code="general.void"/>' onclick="javascript:$j('#delete-dialog').dialog('open')"/>
			<div id="delete-dialog" title="<spring:message code="general.void"/> <spring:message code="Visit"/>">
			<form:form action="voidVisit.htm" method="post" modelAttribute="visit">
			<c:if test="${param.visitId != null}">
				<input type="hidden" name="visitId" value="${param.visitId}"/>
			</c:if>
			<c:if test="${param.patientId != null}">
				<input type="hidden" name="patientId" value="${param.patientId}"/>
			</c:if>
			<p><spring:message code="Visit.delete.info" arguments="${encounterCount}, ${observationCount}"/></p>
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
						<input type="submit" value="<spring:message code="general.void"/>" /> &nbsp; <input type="button" value="<spring:message code="general.cancel"/>" 
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
			<input type="button" value='<spring:message code="general.purge"/>' onclick="javascript:$j('#purge-dialog').dialog('open')" 
				<c:if test="${!canPurgeVisit}"> disabled="disabled" title="<spring:message code="Visit.cannotPurgeVisitWithEncounters"/>"</c:if> />
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

<%@ include file="/WEB-INF/template/footer.jsp" %>
