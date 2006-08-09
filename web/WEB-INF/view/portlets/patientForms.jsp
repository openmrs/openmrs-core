<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:hasPrivilege privilege="Form Entry">		
	<div id="selectFormHeader" class="boxHeader">Forms</div>
	<div id="selectForm" class="box">
		<form id="selectFormForm" method="post" action="<%= request.getContextPath() %>/formDownload">
			<c:forEach items="${forms}" var="form">
				<a href="${pageContext.request.contextPath}/formDownload?target=formEntry&formId=${form.formId}&patientId=${patient.patientId}" class="formLink">${form.name}
				(v.${form.version})
				<c:if test="${form.published == false}"><i>(<spring:message code="formentry.unpublished"/>)</i></c:if>
				</a>
				
				<br />
			</c:forEach>
		</form>
	</div>
</openmrs:hasPrivilege>
		
