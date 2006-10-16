<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ attribute name="formFieldName" required="true" type="java.lang.String" %>
<%@ attribute name="reasons" required="true" type="java.util.List" %>
<%@ attribute name="initialValue" required="false" type="java.lang.String" %>
<%@ attribute name="optionHeader" required="false" type="java.lang.String" %>
<%@ attribute name="jsVar" required="false" type="java.lang.String" %>

<c:choose>
	<c:when test="${not empty jsVar}">
		// create a map of items that can then can be put into a select with DWRUtil.addOptions	
		var ${jsVar} = new Array();
		<c:if test="${optionHeader != ''}"><c:if test="${optionHeader == '[blank]'}"> ${jsVar}.push({ val: '', display: '' }); </c:if>
			<c:if test="${optionHeader != '[blank]'}"> ${jsVar}.push({ val: '', display: '${optionHeader}' }); </c:if></c:if>
		<c:forEach items="${reasons}" var="reason"> ${jsVar}.push({ val: '${reason}', display: '<spring:message code="${reason}" />' }); </c:forEach>

		// create a function for converting reasons to their spring:message equivalent string
		function getReason(code) {
			<c:forEach items="${reasons}" var="reason">
				if ( code == '${reason}' ) return '<spring:message code="${reason}" />';
			</c:forEach>
			return code;
		}
	</c:when>
	<c:otherwise>
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
	</c:otherwise>
</c:choose>