<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Forms" otherwise="/login.htm" redirect="/admin/forms/auditField.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script language="javascript">
       function warningMessage(){
                var retVal = confirm('<openmrs:message code="FormField.auditConfirm"/>');
                return retVal;
       }
</script>

<form style="padding: 0px; margin: 0px; display: inline;" method="post">
		<h2><openmrs:message code="FormField.audit"/></h2>
        <input type="submit" value="<openmrs:message code="FormField.auditButton"/>" onclick="return warningMessage();" />
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>                                                     