<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Privileges" otherwise="/login.htm" redirect="/admin/users/privilege.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="Privilege.manage.title"/></h2>	

<spring:hasBindErrors name="privilege">
	<openmrs:message htmlEscape="false" code="fix.error"/>
	<br />
</spring:hasBindErrors>

<form method="post">
<table>
	<tr>
		<td><openmrs:message code="Privilege.privilege"/><span class="required">*</span></td>
		<td>
			<spring:bind path="privilege.privilege">
				<c:if test="${status.value == null || status.value == \"\"}"><input type="text" name="${status.expression}" id="priv" value="${status.value}"></c:if>
				<c:if test="${!(status.value == null)}">${status.value}</c:if>				
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><openmrs:message code="general.description"/></td>
		<td valign="top">
			<spring:bind path="privilege.description">
				<textarea name="description" rows="3" cols="50">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
     <td><font color="#D0D0D0"><sub><openmrs:message code="general.uuid"/></sub></font></td>
     <td colspan="${fn:length(locales)}"><font color="#D0D0D0"><sub>
       <spring:bind path="privilege.uuid">
           <c:out value="${status.value}"></c:out>
       </spring:bind>
       </sub>
       </font>
     </td>
    </tr>
</table>

<input type="submit" value="<openmrs:message code="Privilege.save"/>">
</form>

<script type="text/javascript">
 document.forms[0].elements[0].focus();
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>