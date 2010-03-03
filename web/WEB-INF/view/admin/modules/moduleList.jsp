<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Modules" otherwise="/login.htm" redirect="/admin/modules/module.list" />
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>
<openmrs:htmlInclude file="/scripts/jquery/jquery-1.3.2.min.js" />
<script type="text/javascript">
	var $j = jQuery.noConflict(); 
</script>

<h2><spring:message code="Module.header" /></h2>	

<p><spring:message code="Module.notice" /></p>

<div style="width: 49.5%; float: left; margin-left: 4px;">
	<b class="boxHeader"><spring:message code="Module.add"/></b>
	<div class="box">
		<form id="moduleAddForm" action="module.list" method="post" enctype="multipart/form-data">
			<input type="file" name="moduleFile" size="40" <c:if test="${allowAdmin!='true'}">disabled="disabled"</c:if> />
			<input type="hidden" name="action" value="upload"/>
			
			<c:choose>
				<c:when test="${allowAdmin == 'true'}">
					<input type="submit" value='<spring:message code="Module.upload"/>'/>
				</c:when>
				<c:otherwise>
					${disallowUploads}
				</c:otherwise>
			</c:choose>
		</form>
	</div>
</div>
<c:if test="${allowAdmin=='true'}">
<div style="width: 49.5%; float: right; margin-right: 4px">
	<b class="boxHeader"><spring:message code="Module.upgrade"/></b>
	<div class="box">
		<form method="post" id="uploadUpdateForm" enctype="multipart/form-data">
			<input type="file" name="moduleFile" size="40" />
			<input type="hidden" name="action" value="upload"/>
			<input type="hidden" name="update" value="true"/>
			<input type="submit" value='<spring:message code="Module.upload"/>'/>
		</form>
	</div>
</div>
</c:if>

<br style="clear:both"/>
<br/>

<c:forEach var="module" items="${moduleList}" varStatus="varStatus">
	<c:if test="${varStatus.first}">
		<b class="boxHeader"><spring:message code="Module.manage" /></b>
		<div class="box" id="moduleListing">
			<table cellpadding="5" cellspacing="0">
				<thead>
					<tr>
						<c:if test="${allowAdmin=='true'}">
							<th colspan="2"><spring:message code="general.action"/></th>
						</c:if>
						<th><spring:message code="general.name"/></th>
						<th><spring:message code="general.version"/></th>
						<th><spring:message code="general.author"/></th>
						<th><spring:message code="general.description"/></th>
						<th></th>
						<th></th>
					</tr>
				</thead>
				<tbody>
	</c:if>
			
				<form method="post">
					<input type="hidden" name="moduleId" value="${module.moduleId}" />
					<tr class="<c:choose><c:when test="${varStatus.index % 2 == 0}">oddRow</c:when><c:otherwise>evenRow</c:otherwise></c:choose>" id="${module.moduleId}">
						<c:choose>
							<c:when test="${allowAdmin=='true' && module.mandatory == false && module.coreModule == false}">
								<td valign="top">
									<c:choose>
										<c:when test="${not module.started}">
											<input type="image" src="${pageContext.request.contextPath}/images/play.gif" name="start" onclick="document.getElementById('hiddenAction').value = this.value" title="<spring:message code="Module.start.help"/>" alt="<spring:message code="Module.start"/>" />
										</c:when>
										<c:otherwise>
											<input type="image" src="${pageContext.request.contextPath}/images/stop.gif" name="stop" onclick="document.getElementById('hiddenAction').value = this.value" title="<spring:message code="Module.stop.help"/>" alt="<spring:message code="Module.stop"/>" />
										</c:otherwise>
									</c:choose>
								</td>
								<td valign="top"><input type="image" src="${pageContext.request.contextPath}/images/trash.gif" name="unload" onclick="return confirm('<spring:message code="Module.unloadWarning"/>');" title="<spring:message code="Module.unload.help"/>" title="<spring:message code="Module.unload"/>" alt="<spring:message code="Module.unload"/>" /></td>
							</c:when>
							<c:otherwise>
								<td valign="top">
									<img src="${pageContext.request.contextPath}/images/lock.gif" title="<spring:message code="Module.locked.help"/>" alt="<spring:message code="Module.locked"/>" />
								</td>
								<td></td>
							</c:otherwise>
						</c:choose>
						<td valign="top">${module.name} <c:if test="${not module.started}"><b id="moduleNotStarted" style="white-space: nowrap">[<spring:message code="Module.notStarted"/>]</b></c:if></td>
						<td valign="top">${module.version}</td>
						<td valign="top">${module.author}</td>
						<td valign="top">${fn:substring(fn:escapeXml(module.description),0, 200)}...</td>
						<td valign="top"<c:if test="${module.startupErrorMessage != null}">class="error"</c:if> >
							<pre style="margin: 0px;">${module.startupErrorMessage}</pre>
						</td>
						<td>
							<c:if test="${module.downloadURL != null}">
								${module.updateVersion}
								<spring:message code="Module.updateAvailable" /> 
								<c:if test="${allowAdmin=='true'}">
									<input type="submit" name="action" value='<spring:message code="Module.installUpdate"/>'>
									<spring:message code="general.or"/>
								</c:if>
								<a href="${module.downloadURL}"><spring:message code="Module.downloadUpdate"/></a>
							</c:if>
						</td>
					</tr>
				</form>
				
	<c:if test="${varStatus.last}">
			</tbody>
			<tfoot>
				<tr>
					<td colspan="7"><form method="post"><input type="submit" value='<spring:message code="Module.checkForUpdates"/>'/></form></td>
				</tr>
			</tfoot>
	
			</table>
		</div>

	</c:if>
	
</c:forEach>

<c:if test="${fn:length(moduleList) == 0}">
	<i> &nbsp; <spring:message code="Module.noLoadedModules"/></i><br/>
</c:if>

<br/>

<b class="boxHeader"><spring:message code="Module.help" /></b>
<div class="box">
	<ul>
		<li><i><spring:message code="Module.help.load"/></i>
		<c:if test="${fn:length(moduleList) > 0}">
			<li><i><spring:message code="Module.help.unload"/></i>
			<li><i><spring:message code="Module.help.startStop"/></i>
			<li><i><spring:message code="Module.help.update"/></i>
		</c:if>
		<li><i><spring:message code="Module.help.findMore"/></i>
	</ul>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>