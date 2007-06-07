<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ attribute name="formFieldName" required="true" type="java.lang.String" %>
<%@ attribute name="reasons" required="true" type="java.util.Map" %>
<%@ attribute name="initialValue" required="false" type="java.lang.String" %>
<%@ attribute name="optionHeader" required="false" type="java.lang.String" %>
<%@ attribute name="jsVar" required="false" type="java.lang.String" %>
<%@ attribute name="onChange" required="false" type="java.lang.String" %>

<c:choose>
	<c:when test="${not empty jsVar}">
		// create a map of items that can then can be put into a select with DWRUtil.addOptions	
		var ${jsVar} = new Array();
		<c:if test="${optionHeader != ''}"><c:if test="${optionHeader == '[blank]'}"> ${jsVar}.push({ val: '', display: '' }); </c:if>
			<c:if test="${optionHeader != '[blank]'}"> ${jsVar}.push({ val: '', display: '${optionHeader}' }); </c:if></c:if>
		<c:forEach items="${reasons}" var="reason"> ${jsVar}.push({ val: '${reason.key}', display: '${reason.value}' }); </c:forEach>

		// create a function for converting reasons to their spring:message equivalent string
		function getReason(code) {
			<c:forEach items="${reasons}" var="reason">
				if ( code == '${reason.key}' ) return '${reason.value}';
			</c:forEach>
			return code;
		}

	</c:when>
	<c:otherwise>
		<c:if test="${not empty onChange}">
			<select name="${formFieldName}" id="${formFieldName}" onChange="${onChange}">
		</c:if>
		<c:if test="${empty onChange}">
			<select name="${formFieldName}" id="${formFieldName}">
		</c:if>
			<c:if test="${optionHeader != ''}">
				<c:if test="${optionHeader == '[blank]'}">
					<option value=""></option>
				</c:if>
				<c:if test="${optionHeader != '[blank]'}">
					<option value="">${optionHeader}</option>
				</c:if>
			</c:if>
			<c:forEach items="${reasons}" var="reason">
				<option value="${reason.key}" <c:if test="${reason.key == initialValue}">selected</c:if>>${reason.value}</option>
			</c:forEach>
		</select>
	</c:otherwise>
</c:choose>