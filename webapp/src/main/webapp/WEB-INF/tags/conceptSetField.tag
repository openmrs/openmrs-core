<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="conceptId" required="true" %>
<%@ attribute name="initialValue" required="false" %>
<%@ attribute name="optionHeader" required="false" %>
<%@ attribute name="onChange" required="false" %>

<openmrs:concept conceptId="${conceptId}" var="c" nameVar="n" numericVar="num" setMemberVar="setMembers">
	<c:if test="${c.set}">
		<select name="${formFieldName}" id="${formFieldName}"<c:if test="${not empty onChange}"> onChange=${onChange}</c:if>>
			<c:if test="${optionHeader != ''}">
				<c:if test="${optionHeader == '[blank]'}">
					<option value=""></option>
				</c:if>
				<c:if test="${optionHeader != '[blank]'}">
					<option value="">${optionHeader}</option>
				</c:if>
			</c:if>
			<c:forEach items="${setMembers}" var="m" varStatus="s">
				<option value="${m.conceptId}" <c:if test="${m.conceptId == initialValue}">selected</c:if>>
					${m.name.name}
				</option>
			</c:forEach>
		</select>
	</c:if>
</openmrs:concept>