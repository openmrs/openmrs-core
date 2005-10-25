<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.jsp" />

<%@ include file="/WEB-INF/template/forms/header.jsp" %>

<h1>Select a Provider</h1>

<table border="0">
	<openmrs:users role="provider" var="provider">
		<tr>
			<td>
				<a href="#top" class="hit" onClick="javascript:setObj('//encounter.provider_id', this)" value="${provider.userId}">
					${provider.firstName} ${provider.lastName} (${provider.username})
				</a>
			</td>
		</tr>
	</openmrs:users>
</table>

<c:if test="1 == 2">
    <p class="no_hit">
	  Matata!  I was unable to find any providers.  Please
	  <a href="javascript:reloadPage()">try again</a>.  If this problem persists,
	  then contact that friendly administrator immediately!
	</p>
</c:if>

<%@ include file="/WEB-INF/template/forms/footer.jsp" %>