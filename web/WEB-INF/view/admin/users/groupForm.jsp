<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Users" otherwise="/login.htm" redirect="/admin/users/group.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Group.manage.title"/></h2>	

<spring:hasBindErrors name="group">
	<spring:message code="fix.error"/>
	<br />
</spring:hasBindErrors>

<form method="post">
<table>
	<tr>
		<td><spring:message code="Group.group"/></td>
		<td>
			<spring:bind path="group.group">
				<c:if test="${group.group == null}"><input type="text" name="${status.expression}" value="${status.value}"></c:if>
				<c:if test="${!(group.group == null)}">${status.value}</c:if>				
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="general.description"/></td>
		<td>
			<spring:bind path="group.description">
				<input type="text" name="description" value="${status.value}"/>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="Group.roles"/></td>
		<td>
			<select name="roles" multiple size="5">
				<c:forEach var="role" items="${roles}">
					<option value="<c:out value="${role.role}"/>"
							<c:forEach var="p" items="${group.roles}"><c:if test="${p == role}">selected</c:if></c:forEach>>
						<c:out value="${role}"/>
					</option>
				</c:forEach>
			</select>
		</td>
	</tr>
</table>

<input type="submit" value="<spring:message code="Group.save"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>