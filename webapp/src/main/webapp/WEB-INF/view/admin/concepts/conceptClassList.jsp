<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Concept Classes" otherwise="/login.htm" redirect="/admin/concepts/conceptClass.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="ConceptClass.manage.title"/></h2>

<a href="conceptClass.form"><openmrs:message code="ConceptClass.add"/></a>

<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptClassList.afterAdd" type="html" />

<br /><br />

<b class="boxHeader"><openmrs:message code="ConceptClass.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th> </th>
			<th> <openmrs:message code="general.name"/> </th>
			<th> <openmrs:message code="general.description"/> </th>
		</tr>
		<c:forEach var="conceptClass" items="${conceptClassList}">
			<tr> 
				<td valign="top"><input type="checkbox" name="conceptClassId" value="${conceptClass.conceptClassId}"></td>
				<td valign="top"><a href="conceptClass.form?conceptClassId=${conceptClass.conceptClassId}">
					   ${conceptClass.name}
					</a>
				</td>
				<td valign="top">${conceptClass.description}</td>
			</tr>
		</c:forEach>
	</table>
	
	<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptClassList.inForm" type="html" />
	
	<input type="submit" value="<openmrs:message code="ConceptClass.delete"/>" name="action">
</form>

<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptClassList.footer" type="html" />

<%@ include file="/WEB-INF/template/footer.jsp" %>