<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Roles" otherwise="/login.htm" redirect="/admin/users/role.list" />
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Role.manage.title"/></h2>	

<a href="role.form"><spring:message code="Role.add"/></a>
<br/><br/>

<b class="boxHeader"><spring:message code="Role.list.title"/></b>
<form method="post" class="box">
	<table cellpadding="2" cellspacing="0">
		<tr>
			<th> </th>
			<th> <spring:message code="Role.role"/> </th>
			<th> <spring:message code="general.description"/> </th>
			<th> <spring:message code="Role.inheritedRoles"/> </th>
			<th> <spring:message code="Role.privileges"/> </th>
		</tr>
	<c:forEach var="map" items="${roleList}" varStatus="rowStatus">
		<tr class="<c:choose><c:when test="${rowStatus.index % 2 == 0}">evenRow</c:when><c:otherwise>oddRow</c:otherwise></c:choose>">
			<td style="text-align: center">
				<c:if test="${map.value == false}">
					<input type="checkbox" name="roleId" value="<c:out value="${map.key.role}"/>">
				</c:if>
				<c:if test="${map.value == true}">
					<img src="${pageContext.request.contextPath}/images/lock.gif"/>
				</c:if>
			</td>
			<td style="white-space: nowrap">
				<a href="role.form?roleName=<c:out value="${map.key.role}"/>">
					<c:out value="${map.key.role}"/>
				</a>
			</td>
			<td><c:out value="${map.key.description}"/></td>
			<c:choose>
				<c:when test="${map.key.role == superuser}">
					<td colspan="2" style="white-space: nowrap"><spring:message code="Role.superuser.hasAllRolesAndPrivileges"/></td>
				</c:when>
				<c:otherwise>
					<td style="white-space: nowrap" <c:if test="${fn:length(map.key.inheritedRoles)>2}">title="${map.key.inheritedRoles}"</c:if>>
						<c:forEach items="${map.key.inheritedRoles}" var="role" begin="0" end="1" varStatus="status">
							<c:if test="${!status.first}">,</c:if>
							${role}
							<c:if test="${status.last && fn:length(map.key.inheritedRoles) > 2}"> ... </c:if>
						</c:forEach>
					</td>
					<td style="white-space: nowrap" <c:if test="${fn:length(map.key.privileges)>2}">title="${map.key.privileges}"</c:if>>
						<c:forEach items="${map.key.privileges}" var="priv" begin="0" end="1" varStatus="status">
							<c:if test="${!status.first}">,</c:if>
							${priv}
							<c:if test="${status.last && fn:length(map.key.privileges) > 2}"> ... </c:if>
						</c:forEach>
					</td>
				</c:otherwise>
			</c:choose>
		</tr>
	</c:forEach>
	</table>
	<input type="submit" value="<spring:message code="Role.delete"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>