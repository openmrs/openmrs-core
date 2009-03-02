<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Forms" otherwise="/login.htm" redirect="/admin/forms/formEdit.form" />

<% pageContext.setAttribute("newline", "\n"); %>
<!-- ${fn:replace(fn:escapeXml(form.xslt), newline, "<br/>")} -->
<center>
  <a href="javascript:window.close()"><spring:message code="FormViewXSLT.closeWindow"/></a>
</center>
<div style="background-color:#F0F0F0; border:thin solid gray; margin:0.1em; padding:0.1em; overflow:auto;">
<pre>
${fn:escapeXml(form.xslt)}
</pre>
</div>
<center>
  <a href="javascript:window.close()"><spring:message code="FormViewXSLT.closeWindow"/></a>
</center>