<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="startValue" required="true" type="java.util.Date" %>
<%@ attribute name="datePattern" required="true" %>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<input type="text" id="${formFieldName}" name="${formFieldName}" size="10" value="<openmrs:formatDate date="${startValue}" />" onFocus="showCalendar(this)" /><span class="datePatternHint"> (${datePattern})</span>