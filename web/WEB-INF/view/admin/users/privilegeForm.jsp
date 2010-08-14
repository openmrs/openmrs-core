<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Privileges" otherwise="/login.htm" redirect="/admin/users/privilege.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript">
	/* 
	* escape ":" or ";" occur in passed text
	* Note: this method is used by localizedDescription portlet, @see localizedDescription.jsp
	*/
	function escapeDelimiter(text) {
		var reg = new RegExp(":", "g");
		text = text.replace(reg, "\\:");
		reg = new RegExp(";", "g");
		text = text.replace(reg, "\\;");
		return text;
	}
</script>

<h2><spring:message code="Privilege.manage.title"/></h2>	

<spring:hasBindErrors name="privilege">
	<spring:message code="fix.error"/>
	<br />
</spring:hasBindErrors>

<form method="post">
<table>
	<tr>
		<td><spring:message code="Privilege.privilege"/></td>
		<td>
			<spring:bind path="privilege.privilege">
				<c:if test="${status.value == null || status.value == \"\"}"><input type="text" name="${status.expression}" id="priv" value="${status.value}"></c:if>
				<c:if test="${!(status.value == null)}">${status.value}</c:if>				
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<spring:nestedPath path="privilege">
		<openmrs:portlet url="localizedDescription" id="localizedDescriptionLayout" /> 
	</spring:nestedPath>
</table>

<input type="submit" value="<spring:message code="Privilege.save"/>">
</form>

<script type="text/javascript">
 document.forms[0].elements[0].focus();
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>