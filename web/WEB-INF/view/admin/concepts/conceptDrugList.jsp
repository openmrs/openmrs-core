<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Concepts" otherwise="/login.htm" redirect="/admin/concepts/conceptDrug.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="ConceptDrug.title"/></h2>

<a href="conceptDrug.form"><spring:message code="ConceptDrug.add"/></a>
<br/><br/>

<b class="boxHeader"><spring:message code="ConceptDrug.manage"/></b>
	<table>
		<tr>
			<th> <spring:message code="general.name"/> </th>
			<%-- <th> <spring:message code="ConceptDrug.concept"/> </th> --%>
			<th> <spring:message code="ConceptDrug.doseStrength"/> </th>
			<th> <spring:message code="ConceptDrug.units"/> </th>
		</tr>

		<c:forEach var="drug" items="${conceptDrugList}">
			<c:if test="${!drug.voided}">
				<tr>
					<td>&nbsp;&nbsp;<a href="conceptDrug.form?drugId=${drug.drugId}">${drug.name}</a></td>
					<%-- <td>${drug.concept}</td> --%>
					<td>${drug.doseStrength}</td>
					<td>${drug.units}</td>
				</tr>
	  		</c:if>
		</c:forEach>
	</table>
	<br/>
	<br/>

<%@ include file="/WEB-INF/template/footer.jsp" %>