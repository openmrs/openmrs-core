<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Edit Concepts" otherwise="/login.htm" redirect="/admin/concepts/conceptDrug.list" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<openmrs:htmlInclude file="/scripts/dojoConfig.js" />
<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.addOnLoad( function() {
		toggleRowVisibilityForClass("drugTable", "retired", false);
	})
</script>

<h2><openmrs:message code="ConceptDrug.title" /></h2>

<a href="conceptDrug.form"><openmrs:message code="ConceptDrug.add" /></a>
<br/><br/>

<b class="boxHeader">
	<a style="display: block; float: right"
		href="#"
		onClick="return toggleRowVisibilityForClass('drugTable', 'retired', false);">
		<openmrs:message code="general.toggle.retired" />
	</a>
	<openmrs:message	code="ConceptDrug.manage" />
</b>

<div class="box">
<table id="drugTable" cellpadding="2" cellspacing="0">
	<tr>
		<th><openmrs:message code="general.name" /></th>
		<%-- <th> <openmrs:message code="ConceptDrug.concept"/> </th> --%>
		<th><openmrs:message code="ConceptDrug.strength" /></th>
	</tr>

	<c:forEach var="drug" items="${conceptDrugList}">
		<tr class="<c:if test="${drug.retired}">retired </c:if>">
			<td><a
				href="conceptDrug.form?drugId=${drug.drugId}"><c:out value="${drug.name}" /> </a></td>
			<%-- <td>${drug.concept}</td> --%>
			<td><c:out value="${drug.strength}" /></td>
		</tr>
	</c:forEach>
</table>
</div>
<br />
<br />

<%@ include file="/WEB-INF/template/footer.jsp"%>