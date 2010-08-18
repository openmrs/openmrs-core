<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Encounters" otherwise="/login.htm" redirect="/admin/encounters/encounter.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<openmrs:htmlInclude file="/scripts/dojoConfig.js" />
<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.addOnLoad( function() {
		toggleVisibility(document, "div", "description");
		toggleRowVisibilityForClass("obs", "voided", true);
		voidedClicked(document.getElementById("voided"));
	})

</script>


<script type="text/javascript">

	function mouseover(row, isDescription) {
		if (row.className.indexOf("searchHighlight") == -1) {
			row.className = "searchHighlight " + row.className;
			var other = getOtherRow(row, isDescription);
			other.className = "searchHighlight " + other.className;
		}
	}
	function mouseout(row, isDescription) {
		var c = row.className;
		row.className = c.substring(c.indexOf(" ") + 1, c.length);
		var other = getOtherRow(row, isDescription);
		c = other.className;
		other.className = c.substring(c.indexOf(" ") + 1, c.length);
	}
	function getOtherRow(row, isDescription) {
		if (isDescription == null) {
			var other = row.nextSibling;
			if (other.tagName == null)
				other = other.nextSibling;
		}
		else {
			var other = row.previousSibling;
			if (other.tagName == null)
				other = other.previousSibling;
		}
		return other;
	}
	function click(obsId) {
		document.location = "${pageContext.request.contextPath}/admin/observations/obs.form?obsId=" + obsId;
		return false;
	}
	
	function voidedClicked(input) {
		var reason = document.getElementById("voidReason");
		var voidedBy = document.getElementById("voidedBy");
		if (input) {
		if (input.checked) {
			reason.style.display = "";
			if (voidedBy)
				voidedBy.style.display = "";
		}
		else {
			reason.style.display = "none";
			if (voidedBy)
				voidedBy.style.display = "none";
		}
		}
	}
	
	function enableSaveButton(relType, id) {
		document.getElementById("saveEncounterButton").disabled = false;
	}

</script>

<style>
	#table th { text-align: left; }
	td.fieldNumber { 
		width: 5px;
		white-space: nowrap;
	}
</style>

<a href="../../patientDashboard.form?patientId=${encounter.patient.patientId}"><spring:message code="patientDashboard.viewDashboard"/></a>

<h2><spring:message code="Encounter.manage.title"/></h2>

<spring:hasBindErrors name="encounter">
	<spring:message code="fix.error"/>
	<br />
</spring:hasBindErrors>

<b class="boxHeader"><spring:message code="Encounter.summary"/></b>
<div class="box">
	<form method="post">
	<table cellpadding="3" cellspacing="0">
		<tr>
			<th><spring:message code="Encounter.patient"/></th>
			<td>
				<spring:bind path="encounter.patient">
					<openmrs_tag:patientField formFieldName="patientId" searchLabelCode="Patient.find" initialValue="${status.value.patientId}" linkUrl="${pageContext.request.contextPath}/admin/patients/patient.form" callback="enableSaveButton" allowSearch="${encounter.encounterId == null}"/>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th><spring:message code="Encounter.provider"/></th>
			<td>
				<spring:bind path="encounter.provider">
					<openmrs_tag:personField formFieldName="providerId" initialValue="${status.value.personId}" roles="Provider" callback="enableSaveButton"/>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th><spring:message code="Encounter.location"/></th>
			<td>
				<spring:bind path="encounter.location">
					<openmrs_tag:locationField formFieldName="location" initialValue="${status.value}"/>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th><spring:message code="Encounter.datetime"/></th>
			<td>
				<spring:bind path="encounter.encounterDatetime">			
					<input type="text" name="${status.expression}" size="10" 
						   value="${status.value}" onClick="showCalendar(this)" />
				   (<spring:message code="general.format"/>: <openmrs:datePattern />)
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th><spring:message code="Encounter.type"/></th>
			<td>
				<spring:bind path="encounter.encounterType">
					<c:choose>
						<c:when test="${encounter.encounterId == null}">
							<select name="encounterType">
								<c:forEach items="${encounterTypes}" var="type">
									<option value="${type.encounterTypeId}" <c:if test="${type.encounterTypeId == status.value}">selected</c:if>>${type.name}</option>
								</c:forEach>
							</select>
						</c:when>
						<c:otherwise>
							${encounter.encounterType.name}
						</c:otherwise>
					</c:choose>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th><spring:message code="Encounter.form"/></th>
			<td>
				<spring:bind path="encounter.form">
					<c:choose>
						<c:when test="${encounter.encounterId == null}">
							<select name="form">
								<option value=""></option>
								<c:forEach items="${forms}" var="form">
									<option value="${form.formId}" <c:if test="${form.formId == status.value}">selected</c:if>>${form.name}</option>
								</c:forEach>
							</select>
						</c:when>
						<c:otherwise>
							${encounter.form.name} v${encounter.form.version}
						</c:otherwise>
					</c:choose>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<c:if test="${encounter.encounterId != null}">
			<tr>
				<th><spring:message code="general.createdBy" /></th>
				<td>
					<a href="#View User" onclick="return gotoUser(null, '${encounter.creator.userId}')">${encounter.creator.personName}</a> -
					<openmrs:formatDate date="${encounter.dateCreated}" type="medium" />
				</td>
			</tr>
			<tr>
				<th><spring:message code="general.voided" /></th>
				<td>
					<spring:bind path="encounter.voided">
						<input type="hidden" name="_${status.expression}" />
						<input type="checkbox" name="${status.expression}" id="voided" onClick="voidedClicked(this)" <c:if test="${encounter.voided}">checked</c:if> />					
						<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
					</spring:bind>
				</td>
			</tr>
			<tr id="voidReason">
				<th><spring:message code="general.voidReason" /></th>
				<td>
					<spring:bind path="encounter.voidReason">
						<input type="text" value="${status.value}" name="${status.expression}" size="40" />
						<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
					</spring:bind>
				</td>
			</tr>
			<c:if test="${encounter.voidedBy != null}">
				<tr id="voidedBy">
					<th><spring:message code="general.voidedBy" /></th>
					<td>
						<a href="#View User" onclick="return gotoUser(null, '${encounter.voidedBy.userId}')">${encounter.voidedBy.personName}</a> -
						<openmrs:formatDate date="${encounter.dateVoided}" type="medium" />
					</td>
				</tr>
			</c:if>
		</c:if>
	</table>
	
	<input type="hidden" name="phrase" value='<request:parameter name="phrase" />'/>
	<input type="submit" id="saveEncounterButton" value='<spring:message code="Encounter.save"/>' disabled>
	&nbsp;
	<input type="button" value='<spring:message code="general.cancel"/>' onclick="history.go(-1); return; document.location='index.htm?autoJump=false&phrase=<request:parameter name="phrase"/>'">
	</form>
</div>

<c:if test="${encounter.encounterId != null}">
	<br/>
	<div class="boxHeader">
		<span style="float: right">
			<a href="#" id="showDescription" onClick="return toggleVisibility(document, 'div', 'description')"><spring:message code="general.toggle.description"/></a> |
			<a href="#" id="showVoided" onClick="return toggleRowVisibilityForClass('obs', 'voided', true);"><spring:message code="general.toggle.voided"/></a>
		</span>
		<b><spring:message code="Encounter.observations"/></b>
	</div>
	<div class="box">
	<table cellspacing="0" cellpadding="2" width="98%" id="obs">
		<tr id="obsListingHeaderRow">
			<th class="fieldNumber"></th>
			<th class="obsConceptName"><spring:message code="Obs.concept"/></th>
			<th class="obsValue"><spring:message code="Obs.value"/></th>
			<th class="obsAlerts"></th>
			<th class="obsCreator"><spring:message code="Obs.creator.or.changedBy"/></th>
		</tr>
		<c:forEach items="${obsMap}" var="obsEntry" varStatus="status">
			<c:set var="obsList" value="${obsEntry.value}" scope="request"/>
		    <c:set var="field" value="${obsEntry.key}" scope="request"/>
		    <c:set var="level" value="0" scope="request"/>
			<c:import url="obsDisplay.jsp" />
		</c:forEach>
	</table>
	</div>
	
	<br />
	<div id="encounterFormAddObsMenu">
		<openmrs:hasPrivilege privilege="Add Observations">
			<div>
				<a href="${pageContext.request.contextPath}/admin/observations/obs.form?encounterId=${encounter.encounterId}">
					<spring:message code="Obs.add"/>
				</a>
			</div>
		</openmrs:hasPrivilege>
		<openmrs:extensionPoint pointId="org.openmrs.admin.encounters.encounterFormAddObsMenu" type="html" requiredClass="org.openmrs.module.web.extension.LinkProviderExtension">
			<c:forEach items="${extension.links}" var="link">
				<openmrs:hasPrivilege privilege="${link.requiredPrivilege}">
					<div>
						<a href="<c:url value="${link.url}" />"><spring:message code="${link.label}"/></a>
					</div>
				</openmrs:hasPrivilege>
			</c:forEach>
		</openmrs:extensionPoint>
	</div>
	<br />
	
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp" %>