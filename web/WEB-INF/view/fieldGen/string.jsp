<%@ include file="/WEB-INF/template/include.jsp" %>

<c:choose>
	<c:when test="${not empty model.answerSet}">
		<select name="${model.formFieldName}" id="${model.formFieldName}">
			<c:if test="${not empty model.optionHeader}">
				<c:if test="${model.optionHeader == '[blank]'}">
					<option value=""></option>
				</c:if>
				<c:if test="${model.optionHeader != '[blank]'}">
					<option value="">${model.optionHeader}</option>
				</c:if>
			</c:if>
			<c:forTokens items="${model.answerSet}" delims="," var="token">
				<option value="${token}"<c:if test="${token == model.initialValue}"> selected</c:if>>${token}</option>
			</c:forTokens>
		</select>
	</c:when>
	<c:otherwise>
		<input type="text" name="${model.formFieldName}" id="${model.formFieldName}" value="${model.initialValue}" size="${model.fieldLength}">
	</c:otherwise>
</c:choose>