<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Global Properties" otherwise="/login.htm" redirect="/admin/maintenance/LocalesAndThemes.form" />

<openmrs:message var="pageTitle" code="LocalesAndThemes.titlebar" scope="page"/>

<%@ include file="/WEB-INF/template/header.jsp" %>

<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="LocalesAndThemes.view.title"/></h2>	

<form method="post">
	<table>
		<tr>
			<td><openmrs:message code="LocalesAndThemes.locale"/><span class="required">*</span></td>
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
				<openmrs:message code="LocalesAndThemes.locale.help"/>
			</td>
		</tr>
		<tr>
			<td><openmrs:message code="LocalesAndThemes.theme"/></td>
			<td>
				<input type="text" value="${theme}" name="theme"/>
			</td>
			<td class="description">
				<openmrs:message code="LocalesAndThemes.theme.help"/>
			</td>
		</tr>
	</table>
	
	<input type="submit" value='<openmrs:message code="general.submit"/>' />
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>
