<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="drugs" required="true" type="java.util.List" %>
<%@ attribute name="initialValue" required="false" %>
<%@ attribute name="optionHeader" required="false" %>
<%@ attribute name="onChange" required="false" %>
<%@ attribute name="includeVoided" required="false" %>

<c:if test="${empty drugs}">
	<spring:message code="Drug.list.empty" />
</c:if>
<c:if test="${not empty drugs}">
	<select name="${formFieldName}" id="${formFieldName}"<c:if test="${not empty onChange}">onChange=${onChange}</c:if>>
		<c:if test="${optionHeader != ''}">
			<c:if test="${optionHeader == '[blank]'}">
				<option value=""></option>
			</c:if>
			<c:if test="${optionHeader != '[blank]'}">
				<option value="">${optionHeader}</option>
			</c:if>
		</c:if>
		<c:forEach var="drug" items="${drugs}">
			<c:if test="${includeVoided == 'true' || !drug.retired}">
				<option value="${drug.drugId}" 
					<c:if test="${drug.drugId == initialValue}">selected</c:if>>${drug.name}</option>
			</c:if>		
		</c:forEach>
	</select>
</c:if>
