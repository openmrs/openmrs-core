<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Concept Sources" otherwise="/login.htm" redirect="/admin/concepts/conceptSource.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="ConceptSource.manage"/></h2>

<a href="conceptSource.form"><spring:message code="ConceptSource.add"/></a>

<br /><br />

<c:set var="implidfound" value="false"/>

<b class="boxHeader"><spring:message code="ConceptSource.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th> </th>
			<th> <spring:message code="general.name"/> </th>
			<th> <spring:message code="ConceptSource.hl7Code"/> </th>
			<th> <spring:message code="general.description"/> </th>
		</tr>
		<c:forEach var="conceptSource" items="${conceptSourceList}">
			<tr <c:if test="${conceptSource == implIdSource}">class="sourceId"</c:if>> 
				<td valign="top"><c:if test="${conceptSource == implIdSource}"><c:set var="implidfound" value="true"/>**</c:if></td>
				<td valign="top"><a href="conceptSource.form?conceptSourceId=${conceptSource.conceptSourceId}">
					  <c:choose>
					  <c:when test="${conceptSource.retired == true}">
					 	 <del> ${conceptSource.name}</del>
					  </c:when>
					  <c:otherwise>
					 	 ${conceptSource.name}
					  </c:otherwise>
					  </c:choose>
					</a>
				</td>
				<td valign="top">${conceptSource.hl7Code}</td>
				<td valign="top">${conceptSource.description}</td>
			</tr>
		</c:forEach>
	</table>
</form>

<c:if test="${implidfound == 'true'}">
	<br/>
	** <spring:message code="ConceptSource.isImplementationId" />
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp" %>