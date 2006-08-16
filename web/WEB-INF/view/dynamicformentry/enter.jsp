<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<div id="commandBox" style="border: 1px black dashed">
	<c:forEach var="modelValue" items="${formInProgress}">
		<c:if test="${modelValue.value != null && modelValue.value != ''}">
			${modelValue.key} -> ${modelValue.value} <br/>
		</c:if>
	</c:forEach>
<pre>
<c:out value="${formInProgress}" escapeXml="true"/>
</pre>
</div>

<div id="formBox" style="border: 1px black solid; background-color: #f0f0f0">
	<form method="post" action="dynamicForm.form">
		<input type=hidden name="formId" value="${formId}"/>
		<table>
			<c:forEach var="field" items="${fields}">
				<tr>
					<td>${field.pageNumber}.</td>	
					<td>#${field.fieldNumber}</td>
					<td>${field.field.fieldType.name}</td>
					<td>${field.field.name} :</td>
					<td><input type=text name="data.${field.formFieldId}.value"/></td>
					<td><input type=text name="data.${field.formFieldId}.date" size="10" onClick="showCalendar(this)"/></td>
				</tr>
			</c:forEach>
		</table>
		<input type=submit value="<spring:message code="general.submit"/>" />
	</form>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>