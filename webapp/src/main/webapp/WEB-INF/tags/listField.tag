<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="list" required="true" type="java.util.List" %>
<%@ attribute name="initialValue" required="false" %>
<%@ attribute name="optionHeader" required="false" %>
<%@ attribute name="onChange" required="false" %>
<%@ attribute name="includeVoided" required="false" %>

<c:if test="${empty list}">
	<spring:message code="list.empty" />
</c:if>
<c:if test="${not empty list}">
	<select name="${formFieldName}" id="${formFieldName}"<c:if test="${not empty onChange}">onChange=${onChange}</c:if>>
		<c:if test="${optionHeader != ''}">
			<c:if test="${optionHeader == '[blank]'}">
				<option value=""></option>
			</c:if>
			<c:if test="${optionHeader != '[blank]'}">
				<option value="">${optionHeader}</option>
			</c:if>
		</c:if>
		<c:forEach var="item" items="${list}">
			<c:if test="${includeVoided == 'true' || !item.retired}">
				<option value="${item.value}" 
					<c:if test="${item.value == initialValue}">selected</c:if>>${item.label}</option>
			</c:if>		
		</c:forEach>
	</select>
</c:if>
