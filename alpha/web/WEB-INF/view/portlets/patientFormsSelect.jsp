<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:hasPrivilege privilege="Form Entry">		
	<script type="text/javascript">	
		function downloadForm(formId) {
			if ( formId == '' ) {
				return false;
			}
			else { 
				url = '${pageContext.request.contextPath}/formDownload?target=formEntry&formId=' + formId + '&patientId=${patient.patientId}';
			}
			window.location = url;
		}
	</script>
	
	<div id="selectForm">
		<form id="selectFormForm" method="get" action="<%= request.getContextPath() %>/formDownload">			
			Add an encounter: 			 
			<select id="formSelect" name="formId">
				<option value="" selected></option>
				<c:forEach items="${forms}" var="form">
					<option value="${form.formId}">
						${form.name} (v.${form.version})
						<c:if test="${form.published == false}"><i>(<spring:message code="formentry.unpublished"/>)</i></c:if>
					</option>
				</c:forEach>
			</select>
			<input type="hidden" name="target" value="formEntry"/>
			<input type="hidden" name="patientId" value="${patient.patientId}"/>
			<input type="submit" value="Add"/>
		</form>
	</div>
</openmrs:hasPrivilege>
		