<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Roles" otherwise="/login.htm" redirect="/admin/users/role.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<style>
 th { text-align: left; }
 .thisRole { font-style: italic; }
</style>

<script type="text/javascript">
	var formChanged = false;
	
	function updateRoleName(val) {
		if (val == null)
			val = document.getElementById("role").value;
		var spans = document.getElementsByTagName("span");
		for (var i=0; i < spans.length;i++) {
			if (spans[i].className == "thisRole") {
				spans[i].innerHTML = val;
			}
		}
	}
	
	function leaveForm() {
		if (formChanged == false) return true;
		
		return confirm('<spring:message code="Role.leaveForm" />');
	}
</script>

<h2><spring:message code="Role.manage.title"/></h2>	

<spring:hasBindErrors name="role">
	<spring:message code="fix.error"/>
	<div class="error">
		<c:forEach items="${errors.allErrors}" var="error">
			<spring:message code="${error.code}" text="${error.code}"/><br/><!-- ${error} -->
		</c:forEach>
	</div>
</spring:hasBindErrors>

<form method="post">
<table>
	<tr>
		<th><spring:message code="Role.role"/></th>
		<td>
			<spring:bind path="role.role">
				<c:if test="${param.roleName == null}"><input type="text" id="role" name="${status.expression}" value="${status.value}" onChange="updateRoleName()"></c:if>
				<c:if test="${!(param.roleName == null)}">${status.value}</c:if>				
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th valign="top"><spring:message code="general.description"/></th>
		<td valign="top">
			<spring:bind path="role.description">
				<textarea name="description" rows="3" cols="50" onKeyUp="formChanged = true;" type="_moz">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<c:if test="${fn:length(inheritingRoles) > 0}">
		<tr>
			<th colspan="2"><spring:message code="Role.inheritingRoles.description"/></th>
		</tr>
		<tr>
			<th></th>
			<td>
				<table cellpadding="3">
					<tr>
						<c:forEach items="${inheritingRoles}" var="role" varStatus="varStatus"> 
							<c:if test="${varStatus.index % 2 == 0}"></tr><tr></c:if>
							<td><a href="role.form?role=${role.role}" onclick="return leaveForm()" title="${role.description}">${role.role}</a></td>
						</c:forEach>
					</tr>
				</table>
			</td>
		</tr>
	</c:if>
	<tr>
		<th colspan="2"><spring:message code="Role.inheritedRoles"/></th>
	</tr>
	<tr>
		<th></th>
		<td>
			<i><spring:message code="Role.inheritedRoles.description"/></i>
			<br/>
			<c:if test="${role.role == superuser}"><spring:message code="Role.superuser.hasAllRolesAndPrivileges"/></c:if>
			<c:if test="${role.role != superuser}">
				<openmrs:listPicker name="inheritedRoles" allItems="${allRoles}" currentItems="${role.inheritedRoles}" />
			</c:if>
		</td>
	</tr>
	<tr>
		<th colspan="2"><spring:message code="Role.privileges"/></th>
	</tr>
	<tr>
		<th></th>
		<td>
			<i><spring:message code="Role.inheritedPrivileges.description"/></i>
			<br/>
			<c:if test="${role.role == superuser}"><spring:message code="Role.superuser.hasAllRolesAndPrivileges"/></c:if>
			<c:if test="${role.role != superuser}">
				<openmrs:listPicker name="privileges" allItems="${privileges}" currentItems="${role.privileges}" inheritedItems="${inheritedPrivileges}" />
			</c:if>
		</td>
	</tr>
</table>

<input type="submit" value="<spring:message code="Role.save"/>">
</form>

<script type="text/javascript">
 document.forms[0].elements[0].focus();
 <c:if test="${role.role != null}">
   updateRoleName('${role.role}');
 </c:if>
</script>


<%@ include file="/WEB-INF/template/footer.jsp" %>