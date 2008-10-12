<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="startValue" required="true" type="java.lang.Object" %>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<%-- Test the startValue to see if its a date object first --%>
<%-- The placement of the html start/end comment tags is intentional --%>

<!-- 
<c:catch var="err">
  <openmrs:formatDate date="${startValue}" />
</c:catch>
-->

<%-- Only try to format the date here if it didn't fail earlier in the commented section --%>
<c:choose>
	<c:when test="${err == null}">
		<input type="text" id="${formFieldName}" name="${formFieldName}" size="10" value="<openmrs:formatDate date="${startValue}" />" onFocus="showCalendar(this)" /><span class="datePatternHint"> (<openmrs:datePattern />)</span>
	</c:when>
	<c:otherwise>
		<input type="text" id="${formFieldName}" name="${formFieldName}" size="10" value="${startValue}" onFocus="showCalendar(this)" /><span class="datePatternHint"> (<openmrs:datePattern />)</span>
		<span class="error">${err}</span>
	</c:otherwise>
</c:choose>