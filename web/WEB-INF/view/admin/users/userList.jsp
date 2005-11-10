<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Users" otherwise="/login.htm" redirect="/admin/users/user.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="User.manage.title"/></h2>

<a href="user.form"><spring:message code="User.add"/></a>

<br/><br/>

<b class="boxHeader"><spring:message code="User.list.title"/></b>
<form class="box">
	<table>
		<tr>
			<th> <spring:message code="User.username"/> </th>
			<th> <spring:message code="User.firstName"/></th>
			<th> <spring:message code="User.lastName"/> </th>
			<th> <spring:message code="User.roles"/>    </th>
		</tr>
		<c:forEach var="user" items="${userList}">
			<tr>
				<td>
					<a href="user.form?userId=<c:out value="${user.userId}"/>"
					   class="<c:if test="user.voided">retired</c:if>">
						${user.username}
					</a>
				</td>
				<td>${user.firstName}</td>
				<td>${user.lastName}</td>
				<td>${user.roles}</td>
			</tr>
		</c:forEach>
	</table>
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>