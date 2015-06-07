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
		
		return confirm('<openmrs:message code="Role.leaveForm" />');
	}
		
		$j(document).ready(function() {
			$j("#toggleSelectionCheckbox").click(function(e) {
				var state = $j(e.target).attr('checked') === undefined ? false : true;
				$j("input[type='checkbox'][name='privileges']").each(function() {
					$j(this).attr("checked", state);
				}) ;
			});
		});
	
</script>

<h2><openmrs:message code="Role.manage.title"/></h2>	

<spring:hasBindErrors name="role">
    <openmrs_tag:errorNotify errors="${errors}" />
</spring:hasBindErrors>

<form method="post">
<table>
	<tr>
		<th><openmrs:message code="Role.role"/><span class="required">*</span></th>
		<td>
			<spring:bind path="role.role">
				<c:if test="${param.roleName == null}"><input type="text" id="role" name="${status.expression}" value="${status.value}" onChange="updateRoleName()"></c:if>
				<c:if test="${!(param.roleName == null)}">${status.value}</c:if>				
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th valign="top"><openmrs:message code="general.description"/></th>
		<td valign="top">
			<spring:bind path="role.description">
				<textarea name="description" rows="3" cols="50" onKeyUp="formChanged = true;" type="_moz">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<c:if test="${fn:length(inheritingRoles) > 0}">
		<tr>
			<th colspan="2"><openmrs:message htmlEscape="false" code="Role.inheritingRoles.description"/></th>
		</tr>
		<tr>
			<th></th>
			<td>
				<table cellpadding="3">
					<tr>
						<c:forEach items="${inheritingRoles}" var="role" varStatus="varStatus"> 
							<c:if test="${varStatus.index % 2 == 0}"></tr><tr></c:if>
							<td><a href="role.form?roleName=${role.role}" onclick="return leaveForm()" title="${role.description}">${role.role}</a></td>
						</c:forEach>
					</tr>
				</table>
			</td>
		</tr>
	</c:if>
	<tr>
		<th colspan="2"><openmrs:message code="Role.inheritedRoles"/></th>
	</tr>
	<tr>
		<th></th>
		<td>
			<i><openmrs:message htmlEscape="false" code="Role.inheritedRoles.description"/></i>
			<br/>
			<c:if test="${role.role == superuser}"><openmrs:message code="Role.superuser.hasAllRolesAndPrivileges"/></c:if>
			<c:if test="${role.role != superuser}">
				<openmrs:listPicker name="inheritedRoles" allItems="${allRoles}" currentItems="${role.inheritedRoles}" descendantItems="${role.allChildRoles}" />
			</c:if>
		</td>
	</tr>
	<tr>
		<th colspan="2"><openmrs:message code="Role.privileges"/></th>
	</tr>
	<tr>
		<th></th>
		<td>
			<i><openmrs:message code="Role.inheritedPrivileges.description"/></i>
			<br/>
			<c:if test="${role.role == superuser}"><openmrs:message code="Role.superuser.hasAllRolesAndPrivileges"/></c:if>
			<c:if test="${role.role != superuser}">
				<div class="listItem listItemSelectAll"><input type="checkbox" id="toggleSelectionCheckbox"><spring:message code="general.selectOrUnselectAll"/></div>
				<openmrs:listPicker name="privileges" allItems="${privileges}" currentItems="${role.privileges}" inheritedItems="${inheritedPrivileges}" />
			</c:if>
		</td>
	</tr>
	<tr>
     <c:if test="${role.role != null}">
       <th><font color="#D0D0D0"><sub><openmrs:message code="general.uuid"/></sub></font></th>
       <td colspan="${fn:length(locales)}"><font color="#D0D0D0"><sub>${role.uuid}</sub></font></td>
     </c:if>
   </tr>
</table>

<input type="submit" value="<openmrs:message code="Role.save"/>">
</form>

<script type="text/javascript">
 document.forms[0].elements[0].focus();
 <c:if test="${role.role != null}">
   updateRoleName('${role.role}');
 </c:if>
</script>


<%@ include file="/WEB-INF/template/footer.jsp" %>