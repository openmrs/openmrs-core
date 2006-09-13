<%@ include file="/WEB-INF/template/include.jsp" %>
<%--
Parameters
	model.showUnpublishedForms == 'true' means allow users to enter forms that haven't been published yet
	model.goBackOnEntry == 'true' means have the browser go back to the find patient page after starting to enter a form
--%>

<openmrs:hasPrivilege privilege="Form Entry">

	<script type="text/javascript">
		var timeOut = null;
	
		function startDownloading() {
			<c:if test="${model.goBackOnEntry == 'true'}">
				timeOut = setTimeout("goBack()", 30000);
			</c:if>
		}
		
		function goBack() {
			document.location='findPatient.htm';
		}
		
		function switchPatient() {
			document.location='findPatient.htm?phrase=${param.phrase}&autoJump=false';
		}
		
		function cancelTimeout() {
			if (timeOut != null)
				clearTimeout(timeOut);
		}
	</script>
	
	<div id="selectFormHeader" class="boxHeader">Forms</div>
	<div id="selectForm" class="box">
		<form id="selectFormForm" method="post" action="<%= request.getContextPath() %>/formDownload">
			<c:forEach items="${forms}" var="form">
				<c:if test="${form.formId != 1}">
					<c:if test="${form.published == true || model.showUnpublishedForms == 'true'}">
						<a href="${pageContext.request.contextPath}/formDownload?target=formEntry&formId=${form.formId}&patientId=${patient.patientId}" onclick="startDownloading()" class="formLink">${form.name}
						(v.${form.version})<c:if test="${form.published == false}"><i>(<spring:message code="formentry.unpublished"/>)</i></c:if></a>
						<br />
					</c:if>
				</c:if>
			</c:forEach>
		</form>
	</div>
</openmrs:hasPrivilege>