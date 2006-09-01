<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:hasPrivilege privilege="Form Entry">

	<script type="text/javascript">
		var timeOut = null;
	
		function startDownloading() {
			timeOut = setTimeout("goBack()", 30000);	
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
					<a href="${pageContext.request.contextPath}/formDownload?target=formEntry&formId=${form.formId}&patientId=${patient.patientId}" onclick="startDownloading()" class="formLink">${form.name}
					(v.${form.version})
					<c:if test="${form.published == false}"><i>(<spring:message code="formentry.unpublished"/>)</i></c:if>
					</a>			
					<br />
				</c:if>
			</c:forEach>
		</form>
	</div>
</openmrs:hasPrivilege>