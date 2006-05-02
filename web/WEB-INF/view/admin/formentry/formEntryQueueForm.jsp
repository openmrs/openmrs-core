<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="View FormEntry Queue" otherwise="/login.htm" redirect="/admin/formentry/formEntryQueue.form" />

<response:setHeader name="Content-Type">text/xml</response:setHeader>
<response:setHeader name="Content-Disposition">attachment; filename=formEntryQueue-${entry.formEntryQueueId}.xml</response:setHeader>

${entry.formData}