<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/formentry/taskpane/tribe.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<h1><spring:message code="Tribe.title"/></h1>

<table border="0">
	<c:forEach items="${tribes}" var="tribe">
		<tr>
			<td>
				<a href="#top" class="hit" onClick="javascript:setObj('//tribe.tribe_id', this)" value="${tribe.tribeId}">${tribe.name}</a>
			</td>
		</tr>
	</c:forEach>
</table>

<%@ include file="/WEB-INF/template/footer.jsp" %>