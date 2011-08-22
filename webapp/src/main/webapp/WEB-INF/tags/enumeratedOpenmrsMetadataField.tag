<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="hierarchy" required="true" %>
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
			<option value="${item.uuid}"
				<c:if test="${item.uuid == initialValue}">selected</c:if>>${item.name}</option>
		</c:forEach>
		
		
				<openmrs:forEachRecord name="${hierarchy}">
				<option value="${record.location.locationId}" <c:if test="${record.location == initialValue}">selected</c:if>>
                          <c:forEach begin="1" end="${record.depth}" >&nbsp;</c:forEach>${record.location.name}
                  </option>
			</openmrs:forEachRecord>
		
	</select>
</c:if>
