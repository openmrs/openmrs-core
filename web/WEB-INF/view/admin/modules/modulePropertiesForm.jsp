<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Modules" otherwise="/login.htm" redirect="/admin/modules/moduleProperties.form" />
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Module.header" /></h2>

<br/>

<form>
	<table>
		<tr class="oddRow">
			<th style="text-align: left"><spring:message code="Module.storedIn"/></th>
			<td><%= org.openmrs.module.ModuleUtil.getModuleRepository().getAbsolutePath() %></td>
		</tr>
		<tr class="evenRow">
			<th style="text-align: left"><spring:message code="Module.allowUploads"/></th>
			<td>
				<c:choose>
					<c:when test="${allowUpload == 'true'}">
						<spring:message code="general.yes"/>
					</c:when>
					<c:otherwise>
						<b class="error"><spring:message code="general.no"/></b>
						${disallowUploads}
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
		<!--
		<tr class="oddRow">
			<th></th>
			<td></td>
		</tr>
		<tr class="evenRow">
			<th></th>
			<td><input type="submit" value='<spring:message code="general.submit"/>' /></td>
		</tr>
		-->
	</table>

</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>