<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Encounters" otherwise="/login.htm" redirect="/admin/observations/encounterSummary.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<style>
	#table th {
		text-align: left;
	}
</style>
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
		document.location = "obs.form?obsId=" + obsId;
		return false;
	}
</script>

<h2><spring:message code="Encounter.title"/></h2>

<b class="boxHeader"><spring:message code="Encounter.summary"/></b>
<div class="box">
	<a href="${pageContext.request.contextPath}/admin/encounters/encounter.form?encounterId=${encounter.encounterId}" style='float:right; clear:right'>
		<spring:message code="Encounter.edit"/>
	</a>
	<table id="encounter">
		<tr>
			<th><spring:message code="Encounter.type"/></th>
			<td>${encounter.encounterType.name}</td>
		</tr>
		<tr>
			<th><spring:message code="Encounter.patient"/></th>
			<td>
				${encounter.patient.patientName.givenName} ${encounter.patient.patientName.middleName} ${encounter.patient.patientName.familyName}
			</td>
		</tr>
		<tr>
			<th><spring:message code="Encounter.provider"/></th>
			<td>
				${encounter.provider.firstName} ${encounter.provider.lastName}
			</td>
		</tr>
		<tr>
			<th><spring:message code="Encounter.location"/></th>
			<td>${encounter.location.name}</td>
		</tr>
		<tr>
			<th><spring:message code="Encounter.form"/></th>
			<td>${encounter.form.name}</td>
		</tr>
		<tr>
			<th><spring:message code="Encounter.datetime"/></th>
			<td><openmrs:formatDate date="${encounter.encounterDatetime}" type="long" /></td>
		</tr>
		<c:if test="${!(encounter.creator == null)}">
			<tr>
				<th><spring:message code="general.createdBy" /></th>
				<td>
					${encounter.creator.firstName} ${encounter.creator.lastName} -
					<openmrs:formatDate date="${encounter.dateCreated}" type="long" />
				</td>
			</tr>
		</c:if>
	</table>
</div>
<br />

<b class="boxHeader"><spring:message code="Encounter.observations"/></b>
<div class="box">
<table cellspacing="0" cellpadding="2" width="98%">
	<tr>
		<th><spring:message code="Obs.concept"/></th>
		<th><spring:message code="Obs.datetime"/></th>
		<th><spring:message code="Obs.location"/></th>
		<th><spring:message code="Obs.comment"/></th>
		<th><spring:message code="general.creator"/></th>
	</tr>
	<c:forEach items="${encounter.obs}" var="obs" varStatus="status">
		<tr class="<c:choose><c:when test="${status.index % 2 == 0}">evenRow</c:when><c:otherwise>oddRow</c:otherwise></c:choose>" onmouseover="mouseover(this)" onmouseout="mouseout(this)" onclick="click('${obs.obsId}')">
			<td><a href="obs.form?obsId=${obs.obsId}" onclick="return click('${obs.obsId}')"><%= ((org.openmrs.Obs)pageContext.getAttribute("obs")).getConcept().getName(request.getLocale()) %></a></td>
			<td><openmrs:formatDate date="${obs.obsDatetime}" type="medium" /></td>
			<td>${obs.location.name}</td>
			<td>${obs.comment}</td>
			<td>
				${obs.creator.firstName} ${obs.creator.lastName} -
				<openmrs:formatDate date="${obs.dateCreated}" type="medium" />
			</td>
		</tr>
		<tr class="<c:choose><c:when test="${status.index % 2 == 0}">evenRow</c:when><c:otherwise>oddRow</c:otherwise></c:choose>" onmouseover="mouseover(this, true)" onmouseout="mouseout(this, true)" onclick="click('${obs.obsId}')">
			<td colspan="5"><div class="description"><%= ((org.openmrs.Obs)pageContext.getAttribute("obs")).getConcept().getName(request.getLocale()).getDescription() %></div></td>
		</tr>
	</c:forEach>
</table>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>