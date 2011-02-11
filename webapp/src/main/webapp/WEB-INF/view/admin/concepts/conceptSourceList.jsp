<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Concept Sources" otherwise="/login.htm" redirect="/admin/concepts/conceptSource.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="ConceptSource.title"/></h2>

<a href="conceptSource.form"><spring:message code="ConceptSource.add"/></a>

<br /><br />

<c:set var="implidfound" value="false"/>

<b class="boxHeader"><spring:message code="ConceptSource.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th> </th>
			<th> </th>
			<th> <spring:message code="general.name"/> </th>
			<th> <spring:message code="ConceptSource.hl7Code"/> </th>
			<th> <spring:message code="general.description"/> </th>
		</tr>
		<c:forEach var="conceptSource" items="${conceptSourceList}">
			<tr <c:if test="${conceptSource == implIdSource}">class="sourceId"</c:if>> 
				<td valign="top"><c:if test="${conceptSource == implIdSource}"><c:set var="implidfound" value="true"/>**</c:if></td>
				<td valign="top"><input type="checkbox" name="conceptSourceId" value="${conceptSource.conceptSourceId}"></td>
				<td valign="top"><a href="conceptSource.form?conceptSourceId=${conceptSource.conceptSourceId}">
					   ${conceptSource.name}
					</a>
				</td>
				<td valign="top">${conceptSource.hl7Code}</td>
				<td valign="top">${conceptSource.description}</td>
			</tr>
		</c:forEach>
	</table>
	<c:if test="${fn:length(conceptSourceList) > 0}">
		<br/>
		<spring:message code="general.retiredReason"/>
		<input type="text" name="retireReason" /><br/>
		<input type="submit" value="<spring:message code="ConceptSource.retire"/>" name="action">
	</c:if>
</form>

<c:if test="${implidfound == 'true'}">
	<br/>
	** <spring:message code="ConceptSource.isImplementationId" />
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp" %>