<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Encounters" otherwise="/login.htm" redirect="/admin/encounters/encounter.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script src="<%= request.getContextPath() %>/scripts/calendar/calendar.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/prototype.lite.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.pack.js"></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/openmrsSearch.js"></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRPatientService.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRUserService.js'></script>

<script type="text/javascript">

	var mySearch = null;
	var findObjects = null;
	var searchType = "";
	var changeButton = null;
	<request:existsParameter name="autoJump">
		autoJump = <request:parameter name="autoJump"/>;
	</request:existsParameter>
	var display = new Array();
	
	var init = function() {
		mySearch = new fx.Resize("searchForm", {duration: 100});
		mySearch.hide();
		toggle("div", "description");
		toggleVoided();
		voidedClicked(document.getElementById("voided"));
	};
	
	var findObjects = function(txt) {
		if (searchType == 'patient') {
			DWRPatientService.findPatients(fillTable, txt, 0);
		}
		else if (searchType == 'user') {
			DWRUserService.findUsers(fillTable, txt, ['Clinician'], false);
		}
		return false;
	}
	
	var onSelect = function(objs) {
		var obj = objs[0];
		if (searchType == 'patient') {
			$("patient").value = obj.patientId;
			$("patientName").innerHTML = getName(obj);
			changeButton.focus();
		}
		else if (searchType == 'user') {
			$("provider").value = obj.userId;
			$("providerName").innerHTML = getName(obj);
			changeButton.focus();
		}
		mySearch.hide();
		return false;
	}
	
	function showSearch(btn) {
		mySearch.hide();
		setPosition(btn, $("searchForm"), 465, 350);
		resetForm();
		DWRUtil.removeAllRows("searchBody");
		if (btn.id == "userButton") {
			$('searchTitle').innerHTML = '<spring:message code="Encounter.provider.find"/>';
			searchType = 'user';
		}
		else {
			$('searchTitle').innerHTML = '<spring:message code="Patient.find"/>';
			searchType = 'patient'
		}
		mySearch.toggle();
		$("searchText").value = '';
		$("searchText").select();
		changeButton = btn;
	}
	
	var getIdentifier = function(obj) {
		if (typeof obj == 'string')  return obj;
		if (searchType == 'patient') return obj.identifier;
		return '';
	}
	
	var getName = function(obj) {
		if (typeof obj == 'string') return '';
		str = '';
		if (searchType == 'patient') {
			str += obj.givenName;
			str += ' ';
			str += obj.middleName;
			str += ' ';
			str += obj.familyName;
		}
		else if (searchType == 'user') {
			str += obj.firstName;
			str += ' ';
			str += obj.lastName;
		}
		return str;
	}
	
	function closeBox() {
		mySearch.toggle();
		return false;
	}
	
	var customCellFunctions = [getNumber, getIdentifier, getName];
	
	var oldonload = window.onload;
	if (typeof window.onload != 'function') {
		window.onload = init;
	} else {
		window.onload = function() {
			oldonload();
			init();
		}
	}

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
		document.location = "obs.form?obsId=" + obsId;
		return false;
	}
	
	function voidedClicked(input) {
		var reason = document.getElementById("voidReason");
		var voidedBy = document.getElementById("voidedBy");
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
	
	
	function toggle(tagName, className) {
		if (display[tagName] == "none")
			display[tagName] = "";
		else
			display[tagName] = "none";
			
		var items = document.getElementsByTagName(tagName);
		for (var i=0; i < items.length; i++) {
			var classes = items[i].className.split(" ");
			for (x=0; x<classes.length; x++) {
				if (classes[x] == className)
					items[i].style.display = display[tagName];
			}
		}
		
		return false;
	}
	
	function toggleVoided() {
		toggle("tr", "voided");
		
		var table = document.getElementById("obs");
		
		if (table) {
			var rows = table.rows;
			var oddRow = true;
			
			for (var i=1; i<rows.length; i++) {
				if (rows[i].style.display == "") {
					var c = "";
					if (rows[i].className.substr(0, 6) == "voided")
						c = "voided ";
					if (oddRow)
						c = c + "oddRow";
					else
						c = c + "evenRow";
					oddRow = !oddRow;
					rows[i++].className = c;
					rows[i].className = c;
				}
			}
		}
		
		return false;
	}

	function gotoPatient(tagName, patId) {
		if (patId == null)
			patId = $(tagName).value;
		window.location = "${pageContext.request.contextPath}/admin/patients/patient.form?patientId=" + patId;
		return false;
	}
	
	function gotoUser(tagName, userId) {
		if (userId == null)
			userId = $(tagName).value;
		window.location = "${pageContext.request.contextPath}/admin/users/user.form?userId=" + userId;
		return false;
	}

</script>

<style>
	.searchForm {
		width: 450px;
		position: absolute;
		z-index: 10;
		margin: 5px;
	}
	.searchForm .wrapper {
		padding: 2px;
		background-color: whitesmoke;
		border: 1px solid grey;
		height: 330px;
	}
	.searchResults {
		height: 270px;
		overflow: auto;
	}
	#table th {
		text-align: left;
	}
</style>

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
					<table>
						<tr>
							<td><a id="patientName" href="#View Patient" onclick="return gotoPatient('patient')">${status.value.patientName.givenName} ${status.value.patientName.middleName} ${status.value.patientName.familyName}</a></td>
							<td>
								<input type="hidden" id="patient" value="${status.value.patientId}" name="patientId"/>
								<c:if test="${encounter.encounterId == null}">
									&nbsp; <input type="button" id="patientButton" class="smallButton" value='<spring:message code="general.change"/>' onclick="showSearch(this)" />
								</c:if>
								<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
							</td>
						</tr>
					</table>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th><spring:message code="Encounter.provider"/></th>
			<td>
				<spring:bind path="encounter.provider">
					<table>
						<tr>
							<td><a id="providerName" href="#View Provider" onclick="return gotoUser('provider')">${status.value.firstName} ${status.value.lastName}</a></td>
							<td>
								&nbsp;
								<input type="hidden" id="provider" value="${status.value.userId}" name="providerId" />
								<input type="button" id="userButton" class="smallButton" value='<spring:message code="general.change"/>' onclick="showSearch(this)" />
								<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
							</td>
						</tr>
					</table>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th><spring:message code="Encounter.location"/></th>
			<td>
				<spring:bind path="encounter.location">
					<select name="location">
						<openmrs:forEachRecord name="location">
							<option value="${record.locationId}" <c:if test="${status.value == record.locationId}">selected</c:if>>${record.name}</option>
						</openmrs:forEachRecord>
					</select>
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
				   (<spring:message code="general.format"/>: ${datePattern})
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
							${encounter.form.name}
						</c:otherwise>
					</c:choose>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<c:if test="${!(encounter.creator == null)}">
			<tr>
				<th><spring:message code="general.createdBy" /></th>
				<td>
					<a href="#View User" onclick="return gotoUser(null, '${encounter.creator.userId}')">${encounter.creator.firstName} ${encounter.creator.lastName}</a> -
					<openmrs:formatDate date="${encounter.dateCreated}" type="medium" />
				</td>
			</tr>
		</c:if>
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
		<c:if test="${!(encounter.voidedBy == null)}">
			<tr id="voidedBy">
				<th><spring:message code="general.voidedBy" /></th>
				<td>
					<a href="#View User" onclick="return gotoUser(null, '${encounter.voidedBy.userId}')">${encounter.voidedBy.firstName} ${encounter.voidedBy.lastName}</a> -
					<openmrs:formatDate date="${encounter.dateVoided}" type="medium" />
				</td>
			</tr>
		</c:if>
	</table>
	
	<input type="hidden" name="phrase" value='<request:parameter name="phrase" />'/>
	<input type="submit" value='<spring:message code="Encounter.save"/>'>
	&nbsp;
	<input type="button" value='<spring:message code="general.cancel"/>' onclick="history.go(-1); return; document.location='index.htm?autoJump=false&phrase=<request:parameter name="phrase"/>'">
	</form>
</div>

<c:if test="${encounter.encounterId != null}">
	<br/>
	<div class="boxHeader">
		<span style="float: right">
			<a href="#" id="showDescription" onClick="return toggle('div', 'description')"><spring:message code="general.toggle.description"/></a> |
			<a href="#" id="showVoided" onClick="return toggleVoided()"><spring:message code="general.toggle.voided"/></a>
		</span>
		<b><spring:message code="Encounter.observations"/></b>
	</div>
	<div class="box">
	<table cellspacing="0" cellpadding="2" width="98%" id="obs">
				<tr>
			<th></th>
			<th><spring:message code="Obs.concept"/></th>
			<th><spring:message code="Obs.value"/></th>
			<th></th>
			<th><spring:message code="Obs.creator.or.changedBy"/></th>
		</tr>
		<c:forEach items="${observations}" var="obs" varStatus="status">
			<% pageContext.setAttribute("field", ((java.util.Map)request.getAttribute("obsMap")).get(pageContext.getAttribute("obs"))); %>
			<tr class="<c:if test="${obs.voided}">voided </c:if><c:choose><c:when test="${status.index % 2 == 0}">evenRow</c:when><c:otherwise>oddRow</c:otherwise></c:choose>" onmouseover="mouseover(this)" onmouseout="mouseout(this)" onclick="click('${obs.obsId}')">
				<td>${field.fieldNumber}<c:if test="${field.fieldPart != null && field.fieldPart != ''}">.${field.fieldPart}</c:if></td>
				<td><a href="obs.form?obsId=${obs.obsId}" onclick="return click('${obs.obsId}')"><%= ((org.openmrs.Obs)pageContext.getAttribute("obs")).getConcept().getName((java.util.Locale)request.getAttribute("locale")) %></a></td>
				<td><%= ((org.openmrs.Obs)pageContext.getAttribute("obs")).getValueAsString((java.util.Locale)request.getAttribute("locale")) %></td>
				<td valign="middle" valign="right">
					<c:if test="${fn:contains(editedObs, obs.obsId)}"><img src="${pageContext.request.contextPath}/images/alert.gif" title='<spring:message code="Obs.edited"/>' /></c:if>
					<c:if test="${obs.comment != null && obs.comment != ''}"><img src="${pageContext.request.contextPath}/images/note.gif" title="${obs.comment}" /></c:if>
				</td>
				<td style="white-space: nowrap;">
					${obs.creator.firstName} ${obs.creator.lastName} -
					<openmrs:formatDate date="${obs.dateCreated}" type="medium" />
				</td>
			</tr>
			<tr class="<c:if test="${obs.voided}">voided </c:if><c:choose><c:when test="${status.index % 2 == 0}">evenRow</c:when><c:otherwise>oddRow</c:otherwise></c:choose>" onmouseover="mouseover(this, true)" onmouseout="mouseout(this, true)" onclick="click('${obs.obsId}')">
				<td colspan="5"><div class="description"><%= ((org.openmrs.Obs)pageContext.getAttribute("obs")).getConcept().getName((java.util.Locale)request.getAttribute("locale")).getDescription() %></div></td>
			</tr>
		</c:forEach>
	</table>
	</div>
</c:if>

<br />
<a href="obs.form?encounterId=${encounter.encounterId}"><spring:message code="Obs.add"/></a>
<br />

<div id="searchForm" class="searchForm">
	<div class="wrapper">
		<input type="button" onClick="return closeBox();" class="closeButton" value="X"/>
		<form method="get" onSubmit="return searchBoxChange('searchBody', searchText, null, false, 0); return false;">
			<h3 id="searchTitle"></h3>
			<input type="text" id="searchText" size="35" onkeyup="return searchBoxChange('searchBody', this, event, false, 400);">
		</form>
		<div id="searchResults" class="searchResults">
			<table cellpadding="2" cellspacing="0">
				<tbody id="searchBody">
					<tr>
						<td></td>
						<td></td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>