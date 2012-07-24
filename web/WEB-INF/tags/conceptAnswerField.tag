<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="concept" type="org.openmrs.Concept" required="true" %>
<%@ attribute name="initialValue" required="false" %>
<%@ attribute name="optionHeader" required="false" %>
<%@ attribute name="onChange" required="false" %>

<select name="${formFieldName}" id="${formFieldName}"<c:if test="${not empty onChange}"> onChange=${onChange}</c:if>>
	<c:if test="${optionHeader != ''}">
		<c:if test="${optionHeader == '[blank]'}">
			<option value=""></option>
		</c:if>
		<c:if test="${optionHeader != '[blank]'}">
			<option value="">${optionHeader}</option>
		</c:if>
	</c:if>
	<c:forEach items="${concept.answers}" var="a" varStatus="s">
		<option value="${a.answerConcept.conceptId}" <c:if test="${a.answerConcept.conceptId == initialValue}">selected</c:if>>
			${a.answerConcept.name.name}
		</option>
	</c:forEach>
</select>
