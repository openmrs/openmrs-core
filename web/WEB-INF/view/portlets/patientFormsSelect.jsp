<%@ include file="/WEB-INF/template/include.jsp" %>

<script type="text/javascript">
	
	function download(formId) {
		if ( formId == '' ) 
			return false;
		else 
			url = '${pageContext.request.contextPath}/formDownload?target=formEntry&formId=' + formId + '&patientId=${patient.patientId}';
		window.location = url;
	}
		
</script>

<openmrs:hasPrivilege privilege="Form Entry">		
	<div id="selectForm">
		<form id="selectFormForm" method="post" action="<%= request.getContextPath() %>/formDownload">
			
			Add an encounter: 
			 
			<select id="formSelect" onChange="download(this.options[this.selectedIndex].value);">
				<option value="" selected></option>
				<c:forEach items="${forms}" var="form">
					<option value="${form.formId}">
						${form.name} (v.${form.version})
						<c:if test="${form.published == false}"><i>(<spring:message code="formentry.unpublished"/>)</i></c:if>
					</option>
				</c:forEach>
			</select>
		</form>
	</div>
</openmrs:hasPrivilege>
		