<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Forms" otherwise="/login.htm" redirect="/admin/forms/formEdit.form" />

<% pageContext.setAttribute("newline", "\n"); %>
${fn:replace(fn:escapeXml(form.xslt), newline, "<br/>")}