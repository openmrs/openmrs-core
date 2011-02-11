<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="orderTypes" required="true" type="java.util.List" %>
<%@ attribute name="initialValue" required="false" %>
<%@ attribute name="optionHeader" required="false" %>

<c:if test="${empty orderTypes}">
	<spring:message code="OrderType.list.empty" />
</c:if>
<c:if test="${not empty orderTypes}">
	<select name="${formFieldName}">
		<c:if test="${optionHeader != ''}">
			<c:if test="${optionHeader == '[blank]'}">
				<option value=""></option>
			</c:if>
			<c:if test="${optionHeader != '[blank]'}">
				<option value="">${optionHeader}</option>
			</c:if>
		</c:if>
		<c:forEach var="orderType" items="${orderTypes}">
			<option value="${orderType.orderTypeId}" 
					<c:if test="${orderType.orderTypeId == initialValue}">selected</c:if>>${orderType.name}</option>		
		</c:forEach>
	</select>
</c:if>
