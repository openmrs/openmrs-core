<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="startValue" required="true" type="java.util.Date" %>
<%@ attribute name="datePattern" required="true" %>
<%@ attribute name="needScript" required="false" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<input type="text" name="${formFieldName}" size="10" value="<openmrs:formatDate date="${startValue}" />" onClick="showCalendar(this)" />
(<spring:message code="general.format"/>: ${datePattern})
