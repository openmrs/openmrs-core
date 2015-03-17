<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Modules" otherwise="/login.htm" redirect="/admin/modules/moduleProperties.form" />
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="Module.header" /></h2>

<br/>

<form>
	<table>
		<tr class="oddRow">
			<th style="text-align: left"><openmrs:message code="Module.storedIn"/></th>
			<td><%= org.openmrs.module.ModuleUtil.getModuleRepository().getAbsolutePath() %></td>
		</tr>
		<tr class="evenRow">
			<th style="text-align: left"><openmrs:message code="Module.allowUploads"/></th>
			<td>
				<c:choose>
					<c:when test="${allowUpload == 'true'}">
						<openmrs:message code="general.yes"/>
					</c:when>
					<c:otherwise>
						<b class="error"><openmrs:message code="general.no"/></b>
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
			<td><input type="submit" value='<openmrs:message code="general.submit"/>' /></td>
		</tr>
		-->
	</table>

</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>