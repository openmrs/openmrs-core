<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Groups" otherwise="/login.htm" redirect="/admin/users/group.list" />
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Group.manage.title"/></h2>	

<a href="group.form"><spring:message code="Group.add"/></a>
<br/><br/>

<b class="boxHeader"><spring:message code="Group.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th> </th>
			<th> <spring:message code="Group.group"/> </th>
			<th> <spring:message code="general.description"/> </th>
			<th> <spring:message code="Group.roles"/> </th>
		</tr>
	<c:forEach var="group" items="${groupList}">
		<tr>
			<td><input type="checkbox" name="groupId" value="<c:out value="${group.group}"/>"></td>
			<td>
				<a href="group.form?group=<c:out value="${group.group}"/>">
					<c:out value="${group.group}"/>
				</a>
			</td>
			<td><c:out value="${group.description}"/></td>
			<td><c:out value="${group.roles}"/></td>
		</tr>
	</c:forEach>
	</table>
	<input type="submit" value="<spring:message code="Group.delete"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>