<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Users" otherwise="/login.htm" redirect="/admin/users/privilege.list" />
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Privilege.manage.title"/></h2>	

<a href="privilege.form"><spring:message code="Privilege.add"/></a>
<br/><br/>

<b class="boxHeader"><spring:message code="Privilege.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th> </th>
			<th> <spring:message code="Privilege.privilege"/> </th>
			<th> <spring:message code="general.description"/> </th>
		</tr>
	<c:forEach var="privilege" items="${privilegeList}">
		<tr>
			<td><input type="checkbox" name="privilegeId" value="<c:out value="${privilege.privilege}"/>"></td>
			<td>
				<a href="privilege.form?privilege=<c:out value="${privilege.privilege}"/>">
					<c:out value="${privilege.privilege}"/>
				</a>
			</td>
			<td><c:out value="${privilege.description}"/></td>
		</tr>
	</c:forEach>
	</table>
	<input type="submit" value="<spring:message code="Privilege.delete"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>