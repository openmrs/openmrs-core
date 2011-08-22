<%@ include file="/WEB-INF/template/include.jsp" %>

<c:choose>
	<c:when test="${not empty model.showAnswers && not empty model.showOther}">
		<openmrs_tag:conceptField formFieldName="${model.formFieldName}" initialValue="${model.obj}" showAnswers="${model.showAnswers}" showOther="${model.showOther}" otherValue="${model.otherValue}" allowed="${model.allowed }"/>
	</c:when>
	<c:when test="${not empty model.showAnswers}">
		<openmrs_tag:conceptField formFieldName="${model.formFieldName}" initialValue="${model.obj}" showAnswers="${model.showAnswers}" allowed="${model.allowed }"/>
	</c:when>
	<c:when test="${not empty model.showOther}">
		<openmrs_tag:conceptField formFieldName="${model.formFieldName}" initialValue="${model.obj}" showOther="${model.showOther}" otherValue="${model.otherValue}" allowed="${model.allowed }" />
	</c:when>
	<c:otherwise>
		<openmrs_tag:conceptField formFieldName="${model.formFieldName}" initialValue="${model.obj}" allowed="${model.allowed }"/>
	</c:otherwise>
</c:choose>


