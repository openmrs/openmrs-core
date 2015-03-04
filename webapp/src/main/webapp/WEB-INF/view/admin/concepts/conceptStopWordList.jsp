<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Concept Stop Words" otherwise="/login.htm" redirect="/admin/concepts/conceptStopWord.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="ConceptStopWord.title"/></h2>

<a href="conceptStopWord.form"><openmrs:message code="ConceptStopWord.add"/></a>

<br /><br />

<b class="boxHeader"><openmrs:message code="ConceptStopWord.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th> </th>
			<th> <openmrs:message code="general.name"/> </th>
			<th> <openmrs:message code="general.locale"/> </th>
		</tr>
		<c:forEach var="conceptStopWord" items="${conceptStopWordList}">
			<tr> 
 				<td valign="top"><input type="checkbox" name="conceptStopWord" value="${conceptStopWord.conceptStopWordId}"></td>
				<td valign="top"> ${conceptStopWord.value}</td>
				<td valign="top"> ${conceptStopWord.locale.displayName}</td>
			</tr>
		</c:forEach>
	</table>
	
	<input type="submit" value="<openmrs:message code="ConceptStopWord.delete"/>" name="action">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>
