<%@ include file="/WEB-INF/template/include.jsp" %>


<!-- TESTING showAnswers is now ${model.showAnswers} -->
<c:choose>
	<c:when test="${not empty model.showAnswers}">
		<openmrs_tag:conceptField formFieldName="${model.formFieldName}" initialValue="${model.obj}" showAnswers="${model.showAnswers}" />
	</c:when>
	<c:otherwise>
		<openmrs_tag:conceptField formFieldName="${model.formFieldName}" initialValue="${model.obj}" />
	</c:otherwise>
</c:choose>


