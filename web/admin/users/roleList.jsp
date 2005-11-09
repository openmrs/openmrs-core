<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Users" otherwise="/login.jsp" redirect="/admin/users/role.list" />
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Role.manage.title"/></h2>	

<a href="role.form"><spring:message code="Role.add"/></a>
<br/><br/>

<b class="boxHeader"><spring:message code="Role.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th> </th>
			<th> <spring:message code="Role.role"/> </th>
			<th> <spring:message code="general.description"/> </th>
			<th> <spring:message code="Role.privileges"/> </th>
		</tr>
	<c:forEach var="role" items="${roleList}">
		<tr>
			<td><input type="checkbox" name="roleId" value="<c:out value="${role.role}"/>"></td>
			<td>
				<a href="role.form?role=<c:out value="${role.role}"/>">
					<c:out value="${role.role}"/>
				</a>
			</td>
			<td><c:out value="${role.description}"/></td>
			<td><c:out value="${role.privileges}"/></td>
		</tr>
	</c:forEach>
	</table>
	<input type="submit" value="<spring:message code="Role.delete"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>