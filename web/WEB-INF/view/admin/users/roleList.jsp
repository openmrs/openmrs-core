<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Roles" otherwise="/login.htm" redirect="/admin/users/role.list" />
	
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
	<c:forEach var="map" items="${roleList}">
		<tr>
			<td>
				<c:if test="${map.value == false}">
					<input type="checkbox" name="roleId" value="<c:out value="${map.key.role}"/>">
				</c:if>
				<c:if test="${map.value == true}">
					<img src="${pageContext.request.contextPath}/images/lock.gif"/>
				</c:if>
			</td>
			<td>
				<a href="role.form?role=<c:out value="${map.key.role}"/>">
					<c:out value="${map.key.role}"/>
				</a>
			</td>
			<td><c:out value="${map.key.description}"/></td>
			<td>
				<c:if test="${map.key.role == superuser}">
					<spring:message code="Role.superuser.hasAllPrivileges"/>
				</c:if>
				<c:if test="${map.key.role != superuser}">
					<c:out value="${map.key.privileges}"/>
				</c:if>
			</td>
		</tr>
	</c:forEach>
	</table>
	<input type="submit" value="<spring:message code="Role.delete"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>