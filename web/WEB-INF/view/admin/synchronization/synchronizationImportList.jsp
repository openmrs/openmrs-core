<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Synchronization Status" otherwise="/login.htm" redirect="/admin/synchronization/synchronizationImport.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Synchronization.import.title"/></h2>

<script language="JavaScript">
	<!--
	
		function doSubmit() {
			document.getElementById("submitButton").disabled = true;
			showDiv("infoText");
			return true;
		}
		
	-->
</script>

<form method="post" enctype="multipart/form-data" onSubmit="return doSubmit();">

	<spring:message code="SychronizationImport.filePrompt" />
	
	<input type="file" name="syncDataFile" value="" />
	<input type="hidden" name="upload" value="true" />
	<input type="submit" value="<spring:message code="SychronizationImport.importData" />" id="submitButton" />

	<span id="infoText" style="display:none;"><spring:message code="SychronizationImport.generatingResponse" /></span>
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>
