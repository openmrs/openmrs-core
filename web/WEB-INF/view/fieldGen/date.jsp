<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" request="${request}" />

<input type="text" name="${model.formFieldName}" size="10" value="<openmrs:formatDate date="${model.obj}" />" onFocus="showCalendar(this)" />
(<spring:message code="general.format"/>: ${model.datePattern})