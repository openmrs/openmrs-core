<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Concept Datatypes" otherwise="/login.htm" redirect="/admin/concepts/conceptDatatype.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="ConceptDatatype.manage.title"/></h2>

<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptDatatypeList.afterTitle" type="html" />

<%--  <a href="conceptDatatype.form"><spring:message code="ConceptDatatype.add"/></a> --%>

<div id="conceptDatatypeListReadOnly">(<spring:message code="general.readonly"/>)</div>

<br />

<b class="boxHeader"><spring:message code="ConceptDatatype.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<%-- <th> </th> --%>
			<th> <spring:message code="general.name"/> </th>
			<th> <spring:message code="general.description"/> </th>
		</tr>
		<c:forEach var="conceptDatatype" items="${conceptDatatypeList}">
			<tr> 
				<%-- <td valign="top"><input type="checkbox" name="conceptDatatypeId" value="${conceptDatatype.conceptDatatypeId}"></td> --%>
				<td valign="top"><a href="conceptDatatype.form?conceptDatatypeId=${conceptDatatype.conceptDatatypeId}">
					   ${conceptDatatype.name}
					</a>
				</td>
				<td valign="top">${conceptDatatype.description}</td>
			</tr>
		</c:forEach>
	</table>
	<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptDatatypeList.inForm" type="html" />
	<%--  <input type="submit" value="<spring:message code="ConceptDatatype.delete"/>" name="action"> --%>
</form>

<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptDatatypeList.footer" type="html" />

<%@ include file="/WEB-INF/template/footer.jsp" %>