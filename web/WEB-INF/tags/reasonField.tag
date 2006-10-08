<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ attribute name="formFieldName" required="true" type="java.lang.String" %>
<%@ attribute name="reasons" required="true" type="java.util.List" %>
<%@ attribute name="initialValue" required="false" type="java.lang.String" %>
<%@ attribute name="optionHeader" required="false" type="java.lang.String" %>

<select name="${formFieldName}" id="${formFieldName}">
	<c:if test="${optionHeader != ''}">
		<c:if test="${optionHeader == '[blank]'}">
			<option value=""></option>
		</c:if>
		<c:if test="${optionHeader != '[blank]'}">
			<option value="">${optionHeader}</option>
		</c:if>
	</c:if>
	<c:forEach items="${reasons}" var="reason">
		<option value="${reason}" <c:if test="${reason == initialValue}">selected</c:if>><spring:message code="${reason}" /></option>
	</c:forEach>
</select>
