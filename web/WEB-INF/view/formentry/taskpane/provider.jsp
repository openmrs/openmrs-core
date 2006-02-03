<%@ include file="/WEB-INF/template/include.jsp" %>

<c:redirect url="user.htm?${pageContext.request.queryString}&role=Provider"/>




<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/formentry/taskpane/provider.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<h1><spring:message code="provider.title"/></h1>

<table border="0">
	<c:forEach items="${list}" var="provider">
		<tr>
			<td>
				<a href="#top" class="hit" onClick="javascript:setObj('//encounter.provider_id', this)" value="${provider.userId}">
					${provider.firstName} ${provider.lastName} (${provider.username})
				</a>
			</td>
		</tr>
	</c:forEach>
</table>

<c:if test="1 == 2">
    <p class="no_hit">
	  Matata!  I was unable to find any providers.  Please
	  <a href="javascript:reloadPage()">try again</a>.  If this problem persists,
	  then contact that friendly administrator immediately!
	</p>
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp" %>