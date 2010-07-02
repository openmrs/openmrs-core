<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage System Defaults" otherwise="/login.htm" redirect="/admin/maintenance/systemDefaults.form" />

<spring:message var="pageTitle" code="SystemDefaults.titlebar" scope="page"/>

<%@ include file="/WEB-INF/template/header.jsp" %>

<%@ include file="localHeader.jsp" %>

<h2><spring:message code="SystemDefaults.view.title"/></h2>	

<form method="post">
	<table>
		<tr>
			<td><spring:message code="SystemDefaults.locale"/></td>
			<td>
				<input type="text" name="locale" value="${locale}"/>
				<%--
				<select name="locale">
					<c:forEach var="localeOption" items="${allowedLocales}">
						<option value="${localeOption}" <c:if test="${localeOption == locale}">selected</c:if>>${localeOption}</option>
					</c:forEach>
				</select>
				 --%>
			</td>
			<td class="description">
				<spring:message code="SystemDefaults.locale.help"/>
			</td>
		</tr>
		<tr>
			<td><spring:message code="SystemDefaults.theme"/></td>
			<td>
				<input type="text" value="${theme}" name="theme"/>
			</td>
			<td class="description">
				<spring:message code="SystemDefaults.theme.help"/>
			</td>
		</tr>
	</table>
	
	<input type="submit" value='<spring:message code="general.submit"/>' />
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>
